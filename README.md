# Message Tracker 📱

**Message Tracker** is a powerful Android application designed to track and store notifications from popular messaging apps like Messenger, Zalo, Telegram, etc. It allows you to view revoked messages or read messages without triggering a "seen" status.

## ✨ Key Features

*   **Notification Tracking:** Automatically capture and store message content as soon as a notification appears.
*   **Multi-App Support:** Customize specific apps to track (Messenger, Zalo, Viber, etc.).
*   **App Filtering:** Intuitive interface allows filtering messages by specific application.
*   **Detailed History:** View conversation history in a familiar chat bubble format.
*   **Dark Mode Support:** Modern Material 3 interface that automatically adapts to system light/dark themes.
*   **Smart Search:** Quickly find apps in the installed list to start tracking.

## 🛠 Tech Stack

*   **Language:** Kotlin
*   **Database:** Room Persistence Library (Secure local data storage).
*   **UI Components:** Material 3, RecyclerView, CardView.
*   **Service:** `NotificationListenerService` (Listens to system notifications).
*   **Architecture:** Clean Architecture with Feature-based structure (UI, Data, Service).
*   **Concurrency:** Kotlin Coroutines & Flow (Smooth data handling without UI lag).

## 📂 Project Structure

```
com.example.myapplication
├── data                # Data management (Room DB, Entities, Dao)
├── ui                  # User Interface
│   ├── main            # Chat list screen
│   ├── detail          # Message detail screen
│   └── selectapp       # Tracking app management screen
├── service             # Background service listening for notifications
└── utils               # Utility functions and constants
```

## 🚀 Installation Guide

1.  Clone the repository: `git clone https://github.com/thanhhoang1105/Message-Tracker.git`
2.  Open the project in **Android Studio**.
3.  Build and run on a physical device (Requires Android 8.0+).
4.  **Important:** Grant "Notification Access" permission for the app to function.

## 📝 Privacy Note
The app only stores data locally on your phone, ensuring absolute privacy. No data is sent to external servers.

---
*Developed by ThanhHoang*
