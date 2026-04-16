# 🚀 WebToApp Project Setup Guide

This project consists of three main components:
1.  **Android App**: A Jetpack Compose application to convert websites to apps.
2.  **API Server**: A Python FastAPI backend for management and coordination.
3.  **Admin Panel**: A Vue.js dashboard to manage users, projects, and analytics.

---

## 🐍 Backend API Setup (FastAPI)

Located in `server/`.

### 1. Environment Setup
```bash
cd server
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate
pip install fastapi uvicorn sqlalchemy pynacl passlib python-jose[cryptography] python-multipart
```

### 2. Database Initialization
By default, the server uses **SQLite** (a file named `sql_app.db` will be created).
```bash
# To initialize/migrate the database:
# (Tables are automatically created on first startup in this synthesized version)
```

### 3. Launching
```bash
python main.py
```
The API serves at `http://localhost:8000/api/v1`.

---

## 💻 Admin Panel Setup (Vue.js)

Located in `admin/`.

### 1. Installation
```bash
cd admin
npm install
```

### 2. Development
```bash
npm run dev
```
The dashboard will be available at `http://localhost:3000`.

---

## 📱 Android App Setup

### 1. IDE
- Open the root folder in **Android Studio Hedgehog (2023.1.1)** or higher.

### 2. API Configuration
If you wish to connect the Android app to your local backend:
- Locate the network configuration or API constants (if applicable).
- In the current "beta" version, the app is standalone, but you can point it to `http://10.0.2.2:8000` (Android Emulator loopback) for integrated features.

---

## 🏁 Verification Check
Once all services are running:
1.  **Server Health**: Visit `http://localhost:8000/` in your browser.
2.  **Admin Login**: Open `http://localhost:3000/login`, ensuring the server is running to handle authentication.
3.  **Connectivity**: The Admin panel is pre-configured to proxy requests to `http://localhost:8000`.
