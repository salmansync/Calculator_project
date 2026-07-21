# 🧮 Android Calculator - Jetpack Compose

![Kotlin](https://img.shields.io/badge/Kotlin-2.x-7F52FF?style=for-the-badge&logo=kotlin)
![Android](https://img.shields.io/badge/Android-API%2024+-3DDC84?style=for-the-badge&logo=android)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge)
![Architecture](https://img.shields.io/badge/Architecture-MVVM-orange?style=for-the-badge)
![License](https://img.shields.io/badge/License-Apache%202.0-green?style=for-the-badge)

A modern, professional-grade Android Calculator built using **Kotlin**, **Jetpack Compose**, and **MVVM Architecture**. This app mimics real scientific calculator behavior with advanced symbol support, smart bracket logic, and real-time evaluation.

---

# 📱 Overview

This project showcases modern Android development practices, emphasizing a reactive UI and robust mathematical logic. 

By integrating the **Mozilla Rhino JavaScript Engine**, the calculator handles complex expressions with ease. Custom preprocessing ensures that user-friendly symbols like **Xʸ**, **¹/ₓ**, and **√** are evaluated with high precision, while smart logic handles common user typing patterns automatically.

## Highlights

- ⚡ **Real-Time Evaluation:** See results instantly as you type.
- 📐 **Scientific Symbols:** Professional UI using `Xʸ` (Power) and `¹/ₓ` (Reciprocal).
- 🧠 **Smart Dynamic Brackets:** A single `()` button that intelligently opens or closes brackets.
- 🌙 **Adaptive Themes:** Seamless switching between Dark and Light modes.
- 📜 **Session History:** Access a full log of your current session's calculations.
- 🔋 **Power Toggle:** Dedicated ON/OFF functionality to preserve state or clear memory.
- ⌨️ **Keyboard Optimization:** Full support for physical external keyboards.

---

# 📸 Screenshots

| Light Mode | Dark Mode | History |
|:---:|:---:|:---:|
| ![](docs/screenshots/light.png) | ![](docs/screenshots/dark.png) | ![](docs/screenshots/history.png) |

---

# ✨ Features

## 🧮 Advanced Mathematical Operations

Beyond standard arithmetic, the app supports:

- **Power (Xʸ):** Raise numbers to any power effortlessly.
- **Reciprocal (¹/ₓ):** Instantly calculate the inverse of your current expression.
- **Square Root (√):** Clean visual representation of root calculations.
- **Modulo (MOD):** Dedicated remainder operator for advanced math.
- **Smart Percentages (%):** Handles context-aware percentage math (e.g., `200 + 10% = 220`).

---

## 🧠 Intelligence & UX

### Smart Dynamic Brackets `()`
No need for two separate buttons. Our intelligent logic:
- **Opens** a bracket if the expression is empty or ends with an operator.
- **Closes** the nearest bracket if there's an unclosed pair and the last input was a number or `)`.
- **Auto-completes** missing closing brackets upon evaluation to prevent errors.

### Implicit Multiplication
The app understands mathematical shorthand:
- `5(2)` becomes `5 × (2)`
- `9ANS` becomes `9 × ANS`
- `(2)(3)` becomes `(2) × (3)`

---

## ⌨️ Physical Keyboard Mapping

For power users with external keyboards:

| Key | Action |
|------|--------|
| `0–9` | Number Input |
| `+` `-` `*` `/` | Standard Arithmetic |
| `^` | Power (`Xʸ`) |
| `Enter` / `=` | Final Calculation |
| `Backspace` / `Del` | Delete last character |
| `Esc` / `C` | Clear All |
| `S` | Square Root (`√`) |
| `M` | Modulo (`MOD`) |
| `%` | Percentage |

---

# 🧩 Architecture & Stack

- **Pattern:** MVVM (Model-View-ViewModel) for clean separation of concerns.
- **UI:** Jetpack Compose (Material 3) for a modern, declarative interface.
- **Engine:** Mozilla Rhino (JS Engine) for reliable mathematical evaluation.
- **State:** LiveData & Compose State for reactive UI updates.

---

# 🚀 Getting Started

## 📦 Download APK
Get the latest stable build directly:
[**Download Calculator v1.3 APK**](release/Calculator-v1.3-debug.apk)

## Development Setup
1. **Clone:** `git clone https://github.com/salmansync/Calculator_project.git`
2. **Open:** Android Studio Ladybug (2024.2.1) or newer.
3. **Build:** Sync Gradle and click the **Run** icon.

---

# 👨‍💻 Author

**Salman Farsi**
- GitHub: [@salmansync](https://github.com/salmansync)

---

# 📄 License

This project is licensed under the **Apache License 2.0**.
See the [LICENSE](LICENSE) file for details.
