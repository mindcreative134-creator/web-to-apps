# Backend & Admin Setup Guide

This project now includes the backend server and administrative dashboard migrated from the previous version. 

## 🚀 Server Setup (FastAPI)

The server is located in the `server/` directory.

### Prerequisites
- Python 3.10+
- Virtual environment (recommended)

### Installation
1. Navigate to the server directory:
   ```bash
   cd server
   ```
2. Create and activate a virtual environment:
   ```bash
   python -m venv venv
   source venv/bin/activate  # On Windows: venv\Scripts\activate
   ```
3. Install dependencies:
   *Note: If `requirements.txt` is missing, common dependencies include:*
   ```bash
   pip install fastapi uvicorn sqlalchemy pydantic python-jose passlib
   ```

### Running the Server
```bash
uvicorn app.main:app --reload
```
*Note: If `main.py` is missing from the root of `server/`, you may need to create an entry point that imports the routers from `app.routers`.*

---

## 💻 Admin Panel Setup (Vue.js)

The admin dashboard is located in the `admin/` directory.

### Prerequisites
- Node.js (v16+)
- npm or yarn

### Installation & Development
1. Navigate to the admin directory:
   ```bash
   cd admin
   ```
2. Install dependencies:
   ```bash
   npm install
   ```
3. Start development server:
   ```bash
   npm run dev
   ```

### Configuration
The admin panel connects to the API via `admin/src/api/index.js`. 
- Default API URL: `https://api.shiaho.sbs/api/v1`
- To use your local server, set the environment variable `VITE_API_URL=http://localhost:8000/api/v1`.

---

## 🗄️ Database
The backend uses **SQLAlchemy** models defined in `server/app/models/`. By default, it is configured to work with the database schema provided in those models. Check `server/app/database.py` (if present) for connection string configurations.
