# geochat: The World is Your Chatroom

> **"What if your text messages could fade away as you walked away?"**

geochat is a proximity-based, real-time chat application where the physical world dictates your digital conversation. Unlike traditional chat apps that connect you with contacts across the globe, geochat connects you with the people standing right next to you.

## Why geochat?

In a world of infinite connectivity, we often ignore our immediate surroundings. geochat brings the focus back to **here and now**.

*   **Concerts & Festivals:** Shout to the crowd around you.
*   **Conferences:** Find others in the same hall without exchanging numbers.
*   **Neighborhoods:** Share local news that literally fades out as you leave the block.
*   **Privacy First:** No friend requests, no permanent history. Just transient, location-aware connection.

---

## How It Works

The core of geochat is its **Distance-Font Algorithm**. The visual presentation of a message is directly correlated to the physical distance between the sender and the receiver.

*   **Close neighbors (0-50m):** Messages appear **Large, Bold, and Opaque**. They demand attention.
*   **Distant neighbors (500m+):** Messages appear **Tiny, Faint, and Transparent**. They fade into the background noise.
*   **Time:** All messages naturally drift away and vanish as time passes.

![geochat Screenshot](screenshot.png)
*(Screenshot coming soon)*

---

## Technology Stack

geochat is built with the absolute latest modern Android standards, including Kotlin, Jetpack Compose, Hilt, and Coroutines.

**[See `GEMINI.md` for the detailed Tech Stack and Architecture decisions.](./GEMINI.md)**

---

## Setup & Installation

The backend is powered by Firebase (Firestore & Auth).

**[See `FIRESTORE.md` for detailed Backend Setup and Integration instructions.](./FIRESTORE.md)**

### Quick Start (Client)
1.  Clone the repository.
2.  Open in Android Studio (Koala or later recommended).
3.  Sync Gradle.
4.  Run on an Emulator or Device with Location enabled.

---

## Project Status

**Current State: Active Development / Prototype**

*   ✅ **UI Layer:** Fully functional with `RadarFeedScreen` and `ProximityMessageRow` implementing the distance-font scaling.
*   ✅ **Architecture:** MVVM with Clean Architecture and Hilt injection is set up.
*   🚧 **Data Layer:** Currently using **Mocked Repositories** for Location and Firestore to facilitate rapid UI iteration and testing without live backend dependencies.
*   🚧 **Backend:** Firebase integration is ready to be enabled (see `FIRESTORE.md`).

---

## License

MIT License

Copyright (c) 2024 Benjamin Hill

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
