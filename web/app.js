import { initializeApp } from "https://www.gstatic.com/firebasejs/11.0.2/firebase-app.js";
import { getAuth, signInAnonymously, onAuthStateChanged } from "https://www.gstatic.com/firebasejs/11.0.2/firebase-auth.js";
import {
    getFirestore,
    collection,
    addDoc,
    onSnapshot,
    query,
    where,
    orderBy,
    serverTimestamp,
    GeoPoint,
    limit
} from "https://www.gstatic.com/firebasejs/11.0.2/firebase-firestore.js";
import { firebaseConfig } from "./config.js";
import { encodeGeohash, getGeohashNeighbors, distanceInMeters } from "./geohash.js";

// --- Constants ---
const MAX_DISTANCE_METERS = 1000; // 1km visibility
const MIN_FONT_PX = 12;
const MAX_FONT_PX = 48; // Significantly larger for close messages
const GEOHASH_PRECISION = 7; // ~150m cells. Neighbors cover ~450m radius.
// Note: Precision 6 is ~1.2km cells (neighbors ~3.6km).
// If radius is 1km, Precision 6 is safer to capture everything.
// GEMINI.md says "Geohash (precision 6 or 7)". Let's use 6 to be safe for 1km visibility.
const QUERY_PRECISION = 6;

// --- State ---
let currentUser = null;
let currentLocation = null; // { lat, lng }
let currentPlusCode = null;
let currentGeohash = null;
let activeUnsubscribes = []; // List of unsubscribe functions for listeners
let messages = new Map(); // id -> messageData
let isUserScrolling = false;

// --- DOM Elements ---
const statusBarStatus = document.getElementById("connection-status");
const statusBarLocation = document.getElementById("location-display");
const messageFeed = document.getElementById("message-feed");
const messageForm = document.getElementById("message-form");
const messageInput = document.getElementById("message-input");
const sendButton = document.getElementById("send-button");

// --- Initialization ---
const app = initializeApp(firebaseConfig);
const auth = getAuth(app);
const db = getFirestore(app);

// --- Auth ---
onAuthStateChanged(auth, (user) => {
    if (user) {
        currentUser = user;
        statusBarStatus.textContent = "Connected";
        checkReadyState();
    } else {
        signInAnonymously(auth).catch((error) => {
            console.error("Auth failed:", error);
            statusBarStatus.textContent = "Auth Error";
        });
    }
});

// --- Location ---
if ("geolocation" in navigator) {
    navigator.geolocation.watchPosition(
        (position) => {
            const lat = position.coords.latitude;
            const lng = position.coords.longitude;

            // Only update if moved significantly (simple check)
            if (!currentLocation || distanceInMeters(lat, lng, currentLocation.lat, currentLocation.lng) > 10) {
                currentLocation = { lat, lng };
                updateLocationState(lat, lng);
            }
        },
        (error) => {
            console.error("Location error:", error);
            statusBarLocation.textContent = "Location Denied/Error";
        },
        { enableHighAccuracy: true, maximumAge: 10000, timeout: 5000 }
    );
} else {
    statusBarLocation.textContent = "Geolocation not supported";
}

function updateLocationState(lat, lng) {
    // Generate Plus Code
    // OpenLocationCode is global from script tag
    const codeObj = OpenLocationCode.encode(lat, lng, 10);
    currentPlusCode = codeObj; // Full code "849VCWC8+R9"

    // Generate Geohash
    currentGeohash = encodeGeohash(lat, lng, QUERY_PRECISION);

    // Update UI
    // We might want to show just the short code if we knew the reference,
    // but full code is safe.
    statusBarLocation.textContent = `📍 ${currentPlusCode}`;

    checkReadyState();
    setupFirestoreListeners(); // Re-setup listeners when we move to a new grid cell
}

function checkReadyState() {
    if (currentUser && currentLocation) {
        messageInput.disabled = false;
        sendButton.disabled = false;
        messageInput.placeholder = "Say something nearby...";
    }
}

// --- Firestore Querying ---
function setupFirestoreListeners() {
    // Clear old listeners
    activeUnsubscribes.forEach(unsub => unsub());
    activeUnsubscribes = [];

    // Clear messages if we moved far enough to reset?
    // Actually, we might want to keep them and just re-calc distance/visibilty.
    // But if we move out of range, we should probably stop updating them.
    // For simplicity, we'll keep the Map but only render valid ones.

    if (!currentGeohash) return;

    // Get 9 neighboring geohash cells
    const neighbors = getGeohashNeighbors(currentGeohash);

    // Create a listener for each neighbor
    neighbors.forEach(hash => {
        const q = query(
            collection(db, "posts"),
            where("geohash", ">=", hash),
            where("geohash", "<=", hash + "\uf8ff"),
            // We can't sort by timestamp AND filter by geohash easily without composite index
            // Firestore allows ONE inequality field.
            // If we filter by geohash range, we can't order by timestamp in the query
            // without a specific index for every possible geohash prefix?
            // No, actually, "If you include a filter with a range comparison (<, <=, >, >=),
            // your first ordering must be on the same field."
            // So we must order by geohash.
            // We will do client-side sorting/filtering for time.
            orderBy("geohash"),
            // We can limit, but ideally we want time-based.
            // Since we can't easily query "last 5 mins" AND "geohash range" together efficiently
            // without composite indexes for every combination,
            // we will fetch recent posts in these buckets.
            // In a real high-volume app, we'd use time buckets or strict composite indexes.
            // For this demo, let's just listen.
            limit(50) // Per bucket safety
        );

        const unsub = onSnapshot(q, (snapshot) => {
            snapshot.docChanges().forEach((change) => {
                if (change.type === "added" || change.type === "modified") {
                    const data = change.doc.data();
                    messages.set(change.doc.id, { id: change.doc.id, ...data });
                }
                // We don't remove "removed" docs instantly to avoid jitter,
                // or we could.
                if (change.type === "removed") {
                    messages.delete(change.doc.id);
                }
            });
            renderMessages();
        });
        activeUnsubscribes.push(unsub);
    });
}

// --- Rendering ---
function renderMessages() {
    if (!currentLocation) return;

    // Convert map to array
    const msgs = Array.from(messages.values());

    // Filter and Sort
    // 1. Filter by actual distance (radius check)
    // 2. Sort by timestamp (newest last for chat flow)
    const validMsgs = msgs.map(msg => {
        const dist = distanceInMeters(
            currentLocation.lat, currentLocation.lng,
            msg.location.latitude, msg.location.longitude
        );
        return { ...msg, distance: dist };
    })
    .filter(msg => msg.distance <= MAX_DISTANCE_METERS)
    .sort((a, b) => (a.timestamp?.seconds || 0) - (b.timestamp?.seconds || 0));

    // Clear feed (naive re-render, VDOM-like behavior would be better but this is Vanilla)
    // To preserve scroll and animations, we should try to update existing DOM nodes
    // but for "ultra simple", clearing innerHTML is risky for scroll.
    // Let's do a simple diff or append.
    // Actually, complete re-render is easiest for sorting.
    messageFeed.innerHTML = "";

    validMsgs.forEach(msg => {
        const el = createMessageElement(msg);
        messageFeed.appendChild(el);
    });

    // Auto-scroll
    if (!isUserScrolling) {
        messageFeed.scrollTop = messageFeed.scrollHeight;
    }
}

function createMessageElement(msg) {
    const div = document.createElement("div");
    div.className = "message-row";

    // Distance-Font Algorithm
    // relevance = 1 - (distance / MAX)
    // fontSize = MIN + (rel * (MAX - MIN))
    const relevance = 1 - Math.min(Math.max(msg.distance / MAX_DISTANCE_METERS, 0), 1);
    const fontSize = MIN_FONT_PX + (relevance * (MAX_FONT_PX - MIN_FONT_PX));
    const opacity = 0.3 + (relevance * 0.7);

    div.style.fontSize = `${fontSize}px`;
    div.style.opacity = opacity;

    // Content
    const textDiv = document.createElement("div");
    textDiv.className = "message-text";
    textDiv.textContent = msg.text;

    const metaDiv = document.createElement("div");
    metaDiv.className = "message-meta";
    // Format timestamp
    const date = msg.timestamp ? new Date(msg.timestamp.seconds * 1000) : new Date();
    const timeStr = date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });

    metaDiv.textContent = `${timeStr} • ${msg.plusCode || "Unknown"} • ${Math.round(msg.distance)}m away`;

    div.appendChild(textDiv);
    div.appendChild(metaDiv);

    return div;
}

// Scroll handling
messageFeed.addEventListener("scroll", () => {
    // If user scrolls up significantly, stop auto-scrolling
    if (messageFeed.scrollTop + messageFeed.clientHeight < messageFeed.scrollHeight - 50) {
        isUserScrolling = true;
    } else {
        isUserScrolling = false;
    }
});

// --- Sending ---
messageForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const text = messageInput.value.trim();
    if (!text || !currentUser || !currentLocation) return;

    messageInput.value = "";

    try {
        await addDoc(collection(db, "posts"), {
            userId: currentUser.uid,
            text: text,
            timestamp: serverTimestamp(),
            location: new GeoPoint(currentLocation.lat, currentLocation.lng),
            geohash: encodeGeohash(currentLocation.lat, currentLocation.lng, 10), // High precision for storage
            plusCode: currentPlusCode
        });
        // Scroll to bottom immediately? Listener will handle it.
        isUserScrolling = false;
    } catch (err) {
        console.error("Error sending:", err);
        alert("Failed to send message.");
    }
});
