# 🌐 Full Deployment Guide (Render + Vercel)

This guide provides a step-by-step walkthrough for deploying your **Backend API** to Render and your **Admin Panel** to Vercel.

---

## 🏗️ Phase 1: Deploy Backend to **Render**

Since you have a Render account, we will use it to host the FastAPI server and a managed PostgreSQL database.

### 1. Database Setup (Render Postgres)
1.  Log in to [Render](https://dashboard.render.com).
2.  Click **New +** and select **PostgreSQL**.
3.  Give it a name (e.g., `webtoapp-db`).
4.  Once created, find the **Internal Database URL** or **External Database URL**. 
    - *Note: Render will automatically provide a `DATABASE_URL` environment variable if you link the database to your web service.*

### 2. Web Service Setup (FastAPI)
1.  Click **New +** and select **Web Service**.
2.  Connect your GitHub repository.
3.  Configure the following:
    - **Name**: `webtoapp-api`
    - **Environment**: `Python 3`
    - **Build Command**: `pip install -r server/requirements.txt`
    - **Start Command**: `uvicorn main:app --host 0.0.0.0 --port $PORT` (set the Root Directory to `server/`).
4.  In the **Environment** tab, add:
    - `DATABASE_URL`: (Your Render Postgres URL)
    - `PYTHON_VERSION`: `3.10.0` or higher.

---

## 🎨 Phase 2: Deploy Admin Panel to **Vercel**

Vercel is the best place to host the Vue.js dashboard.

### 1. Vercel Configuration
1.  Log in to [Vercel](https://vercel.com).
2.  Click **Add New** > **Project**.
3.  Import your GitHub repository.
4.  Configure the Project:
    - **Root Directory**: Select the `admin/` folder.
    - **Framework Preset**: Vercel should auto-detect **Vite**.
5.  **Environment Variables**:
    - Add `VITE_API_URL`: Use the URL provided by Render (e.g., `https://webtoapp-api.onrender.com/api/v1`).
6.  Click **Deploy**.

---

## 📱 Phase 3: Android App Integration

To "connect" your Android app to this new production environment:

1.  **Generate a Production Build**: In your Android project, ensure your network calls are pointing to the Render API URL.
2.  **Manifest Update**: Ensure `AndroidManifest.xml` has Internet permissions (this is already there in your beta version).
3.  **Network Security**: Since Render uses HTTPS, your app will securely connect to the backend.

---

## 🛠️ Summary Checklist

- [ ] **Render**: Create Postgres Database.
- [ ] **Render**: Create Web Service (pointing to `server/`).
- [ ] **Vercel**: Create Project (pointing to `admin/`).
- [ ] **Vercel Env**: Set `VITE_API_URL` to your Render service URL.
- [ ] **Test**: Log in to your Vercel URL and check if the dashboard displays data from your Render backend.

> [!TIP]
> **Persistent Disk**: If you choose to stay with SQLite on Render instead of using their managed Postgres, you **must** add a "Disk" to your Render Web Service and set the database path to that disk's mount point to avoid losing data every time the server restarts. Using Render Postgres is recommended for production.

> [!IMPORTANT]
> **Requirements File**: I've created a `server/requirements.txt` for you. Make sure it's committed to your repo!
