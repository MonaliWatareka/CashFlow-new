# CashFlow - Personal Finance Manager

A simple, offline **personal finance tracking Android application** built with **Kotlin** and modern Android practices.

Track your daily income & expenses, categorize spending, set monthly budgets, get visual insights with a pie chart, receive budget alerts, and backup/restore your data.

## ✨ Features

- ➕ Add, edit and delete income & expense transactions
- 📊 Pie chart visualization of expenses by category
- 💰 Set and track monthly budget with progress bar
- 🔔 Notifications when budget usage reaches 90% or is exceeded
- 📅 Date picker for accurate transaction dates
- 🔄 Backup & restore data (JSON export/import)
- 🌐 Currency selection (USD, EUR, GBP, INR, etc.)
- 💾 Completely offline – uses SharedPreferences + Gson
- Clean bottom navigation with 4 main sections:
  - Transactions
  - Categories (pie chart)
  - Budget
  - Settings


## 🛠️ Tech Stack

- **Language**: Kotlin
- **Architecture**: Simple MVVM-like pattern (no full ViewModel/LiveData – can be improved)
- **UI**: XML layouts + Material Components
- **Navigation**: BottomNavigationView + Fragment transactions
- **Storage**: SharedPreferences + Gson serialization
- **Charts**: MPAndroidChart (PieChart)
- **Date Picker**: MaterialDatePicker
- **Notifications**: NotificationCompat + Channel (Android O+)
- **Dependency Injection**: Manual (no Hilt/Dagger yet)
- **Minimum SDK**: 21 (Android 5.0 Lollipop)

## 🚀 Getting Started

### Prerequisites

- Android Studio Koala | 2024.1.1 or newer
- Gradle 8.0+

### Installation

1. Clone the repository

```bash
https://github.com/MonaliWatareka/CashFlow-new.git
