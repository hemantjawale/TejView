# TejView ✨ 

**TejView** is your ultimate holistic health and wellness companion, wrapped in a breathtaking, fluid **Aurora** design system. It goes beyond basic fitness tracking by giving equal importance to physical health, medical records, digital wellbeing, and mental serenity.

---

## 🌌 The Aurora Experience
The interface has been meticulously crafted to be visually stunning, engaging, and dynamic. It features:
* **Glassmorphism Design:** Beautiful frosted glass cards that adapt to the environment.
* **Dual Theme System:** 
  * 🌙 **Dark Aurora:** A near-black `#07070F` canvas accentuated by deep, atmospheric violet and glowing emerald accents. 
  * ☀️ **Light Aurora:** A premium `#F5F0FF` frosted surface boasting refined high-contrast text and vibrant glassmorphic layers.
* **Dynamic Animations:** Real-time pulse interactions, premium scrolling, and a 360° animated theme toggle.

---

## 🚀 Key Features

### 1. Dashboard (The "Serenity" Overview)
* A high-level visual rundown of your day.
* **Serenity Score:** Calculates your overall balance and wellbeing as an elegant glowing ring.
* Quick glances at BPM (heart pulse), SpO2, Daily Steps, Calories Burned, and Sleep duration. 
* Quick-add shortcuts to easily log water intake, protein, and more.

### 2. Activity & Fitness
* Complete workout selection menu (Running, Cycling, Yoga, Gym, Swimming, Walking).
* Active workout timer showcasing live calorie and distance generation. 
* Step tracking integrated seamlessly with the native Android sensor payload.

### 3. Holistic Health Hub
* **Vitals Tracking:** Logging for resting heart rate and blood pressure.
* **Nutrition Goals:** Easily hit your daily markers by logging hydration (water glasses) and tracking protein intake. 
* **Medical Context:** Instantly report emerging health issues and log medications on the fly. 

### 4. Mental & Digital Wellbeing
* **Mood Tracker:** Interactive multi-emoji selector mapping dynamically to a modeled "Stress Level".
* **Screen Time Audits:** Automatically chunks device usage into "Social", "Productive", and "Other". 
* **Meditation Timer & Journal:** Complete brief mindfulness routines, track your streaks, and keep a personal diary.

### 5. Advanced Sleep Analytics
* Complete visualizations mapping your transitions across **Deep, Light, REM, and Awake** sleep cycles. 
* Detailed schedule monitoring showing bedtime vs. wake-up. 
* Weekly pattern aggregations ensuring you stay within your rest goals. 

---

## 🛠 Tech Stack & Architecture
* **Language:** Kotlin
* **Architecture:** MVVM Design Pattern (Model-View-ViewModel) + Material UI implementation
* **Database:** Room Persistence Library (Offline-first data security)
* **Background Tasks:** Android Services, BroadcastReceivers, and precise `SharedPreferences`.

### ⚙️ App Settings & Reset Features
At the top right of your dashboard, accessing the context gear icon (⚙️) enters the **App Settings**. From here you can perform a complete nuclear database reset via Room's efficient `clearAllTables()` logic on native background threads—giving you a truly clean slate whenever you need. 

---

## 📥 Getting Started 

1. Ensure you have **Android Studio** installed (Flamingo or later recommended).
2. Clone the repository:
   ```bash
   git clone https://github.com/hemantjawale/TejView.git
   ```
3. Open the project in Android Studio.
4. Sync Gradle and build the project.
5. Hit **Run** (`Shift + F10`) to deploy to your emulator or physical Android device!

---
*Your Health, Visualized.*
