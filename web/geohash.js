// Geohash Utilities
// simplified logic to encode geohashes and calculate neighbors/bounds
// Derived from standard geohash algorithms.

const BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz";

/**
 * Encodes a (lat, lng) pair into a geohash string.
 * @param {number} lat
 * @param {number} lng
 * @param {number} precision
 * @returns {string}
 */
export function encodeGeohash(lat, lng, precision = 10) {
    let idx = 0;
    let bit = 0;
    let evenBit = true;
    let geohash = "";

    let latMin = -90, latMax = 90;
    let lonMin = -180, lonMax = 180;

    while (geohash.length < precision) {
        if (evenBit) {
            // Bisect E-W longitude
            const lonMid = (lonMin + lonMax) / 2;
            if (lng >= lonMid) {
                idx = idx * 2 + 1;
                lonMin = lonMid;
            } else {
                idx = idx * 2;
                lonMax = lonMid;
            }
        } else {
            // Bisect N-S latitude
            const latMid = (latMin + latMax) / 2;
            if (lat >= latMid) {
                idx = idx * 2 + 1;
                latMin = latMid;
            } else {
                idx = idx * 2;
                latMax = latMid;
            }
        }
        evenBit = !evenBit;

        if (++bit === 5) {
            geohash += BASE32.charAt(idx);
            bit = 0;
            idx = 0;
        }
    }

    return geohash;
}

/**
 * Calculates the bounding box of a geohash.
 * @param {string} geohash
 * @returns {{sw: {lat: number, lng: number}, ne: {lat: number, lng: number}}}
 */
function bounds(geohash) {
    let evenBit = true;
    let latMin = -90, latMax = 90;
    let lonMin = -180, lonMax = 180;

    for (let i = 0; i < geohash.length; i++) {
        const chr = geohash.charAt(i);
        const idx = BASE32.indexOf(chr);
        if (idx === -1) throw new Error("Invalid geohash");

        for (let n = 4; n >= 0; n--) {
            const bitN = (idx >> n) & 1;
            if (evenBit) {
                // Longitude
                const lonMid = (lonMin + lonMax) / 2;
                if (bitN === 1) {
                    lonMin = lonMid;
                } else {
                    lonMax = lonMid;
                }
            } else {
                // Latitude
                const latMid = (latMin + latMax) / 2;
                if (bitN === 1) {
                    latMin = latMid;
                } else {
                    latMax = latMid;
                }
            }
            evenBit = !evenBit;
        }
    }

    return {
        sw: { lat: latMin, lng: lonMin },
        ne: { lat: latMax, lng: lonMax }
    };
}

/**
 * Calculates the adjacent geohash in a given direction.
 * @param {string} geohash
 * @param {string} direction 'n', 's', 'e', 'w'
 * @returns {string}
 */
function adjacent(geohash, direction) {
    // Neighbor map based on char parity (even/odd)
    // This is a standard table for Geohash neighbors
    const NEIGHBORS = {
        n: { even: "p0r21436x8zb9dcf5h7kjnmqesgutwvy", odd: "bc01fg45238967deuvhjyznpkmstqrwx" },
        s: { even: "14365h7k9dcfesgujnmqp0r2twvyx8zb", odd: "238967debc01fg45kmstqrwxuvhjyznp" },
        e: { even: "bc01fg45238967deuvhjyznpkmstqrwx", odd: "p0r21436x8zb9dcf5h7kjnmqesgutwvy" },
        w: { even: "238967debc01fg45kmstqrwxuvhjyznp", odd: "14365h7k9dcfesgujnmqp0r2twvyx8zb" },
    };
    const BORDERS = {
        n: { even: "prxz", odd: "bcfguvyz" },
        s: { even: "028b", odd: "0145hjnp" },
        e: { even: "bcfguvyz", odd: "prxz" },
        w: { even: "0145hjnp", odd: "028b" },
    };

    const lastChr = geohash.charAt(geohash.length - 1);
    const parent = geohash.substring(0, geohash.length - 1);
    const type = (geohash.length % 2 === 0) ? 'even' : 'odd';

    // Check for edge-cases where we cross a major boundary (recursion)
    if (BORDERS[direction][type].indexOf(lastChr) !== -1 && parent !== "") {
        return adjacent(parent, direction) + BASE32.charAt(NEIGHBORS[direction][type].indexOf(lastChr));
    } else {
        // Simple neighbor
        if (parent === "" && BORDERS[direction][type].indexOf(lastChr) !== -1) {
             // We are at the edge of the world, no wrapping in this simple version
             // Just return self or empty to indicate edge?
             // Usually geohashes don't wrap E-W easily in these algos without more logic
             return geohash;
        }
        return parent + BASE32.charAt(NEIGHBORS[direction][type].indexOf(lastChr));
    }
}

/**
 * Returns the 9 geohash neighbors (including center) for a given geohash.
 * @param {string} geohash
 * @returns {string[]}
 */
export function getGeohashNeighbors(geohash) {
    const n = adjacent(geohash, 'n');
    const s = adjacent(geohash, 's');

    const e = adjacent(geohash, 'e');
    const w = adjacent(geohash, 'w');

    const ne = adjacent(n, 'e');
    const nw = adjacent(n, 'w');
    const se = adjacent(s, 'e');
    const sw = adjacent(s, 'w');

    return [geohash, n, s, e, w, ne, nw, se, sw];
}

/**
 * Calculate distance between two points in meters (Haversine).
 * @param {number} lat1
 * @param {number} lon1
 * @param {number} lat2
 * @param {number} lon2
 * @returns {number} distance in meters
 */
export function distanceInMeters(lat1, lon1, lat2, lon2) {
    const R = 6371e3; // metres
    const φ1 = lat1 * Math.PI / 180;
    const φ2 = lat2 * Math.PI / 180;
    const Δφ = (lat2 - lat1) * Math.PI / 180;
    const Δλ = (lon2 - lon1) * Math.PI / 180;

    const a = Math.sin(Δφ / 2) * Math.sin(Δφ / 2) +
              Math.cos(φ1) * Math.cos(φ2) *
              Math.sin(Δλ / 2) * Math.sin(Δλ / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return R * c;
}
