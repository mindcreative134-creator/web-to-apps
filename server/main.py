from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.routers.admin import router as admin_router
from app.routers.auth import router as auth_router
from app.database import engine, Base, SessionLocal
from app.models.user import User
from app.utils.password_crypto import get_password_hash

# Create database tables
Base.metadata.create_all(bind=engine)

def create_default_admin():
    db = SessionLocal()
    try:
        admin = db.query(User).filter(User.email == "admin@example.com").first()
        if not admin:
            print("Creating default admin user...")
            new_admin = User(
                email="admin@example.com",
                username="admin",
                password_hash=get_password_hash("admin123"),
                is_admin=True,
                is_pro=True
            )
            db.add(new_admin)
            db.commit()
            print("Default admin created successfully.")
    except Exception as e:
        print(f"Error creating default admin: {e}")
    finally:
        db.close()

# Run seeding
create_default_admin()

app = FastAPI(title="WebToApp API", version="1.0.0")

# CORS Configuration for Admin Panel communication
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000", "http://127.0.0.1:3000", "http://localhost:3001", "http://127.0.0.1:3001", "http://localhost:3002", "http://127.0.0.1:3002", "http://localhost:3003", "http://127.0.0.1:3003", "http://localhost:3004", "http://127.0.0.1:3004"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Root endpoint
@app.get("/")
def read_root():
    return {"message": "Welcome to WebToApp API"}

# API v1 Level Success Message
@app.get("/api/v1")
def api_v1_root():
    return {
        "status": "success",
        "message": "WebToApp API v1 is live",
        "docs": "/docs"
    }

from app.routers.projects import router as projects_router
from app.routers.sdk import router as sdk_router

# Include routers
app.include_router(auth_router, prefix="/api/v1")
app.include_router(admin_router, prefix="/api/v1")
app.include_router(projects_router, prefix="/api/v1")
app.include_router(sdk_router, prefix="/api/v1")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
