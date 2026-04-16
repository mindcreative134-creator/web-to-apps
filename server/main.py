from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from app.routers.admin import router as admin_router
from app.database import engine, Base

# Create database tables
Base.metadata.create_all(bind=engine)

app = FastAPI(title="WebToApp API", version="1.0.0")

# CORS Configuration for Admin Panel communication
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],  # In production, specify the actual admin domain
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Root endpoint
@app.get("/")
def read_root():
    return {"message": "Welcome to WebToApp API"}

# Include routers
app.include_router(admin_router, prefix="/api/v1")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
