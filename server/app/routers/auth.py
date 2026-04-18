from datetime import timedelta
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from pydantic import BaseModel
from typing import Optional

from app.database import get_db
from app.models.user import User
from app.utils.password_crypto import verify_password
from app.config import get_settings
from jose import jwt

router = APIRouter(prefix="/auth", tags=["Authentication"])
settings = get_settings()

class LoginRequest(BaseModel):
    account: str
    password: str

class TokenResponse(BaseModel):
    access_token: str
    refresh_token: str
    token_type: str = "bearer"

def create_access_token(data: dict, expires_delta: Optional[timedelta] = None):
    to_encode = data.copy()
    expire = expires_delta if expires_delta else timedelta(minutes=settings.ACCESS_TOKEN_EXPIRE_MINUTES)
    # Note: Using a fixed time if we wanted, but we'll use timedelta
    # Settings has minutes but it's used as int in some places
    import datetime
    to_encode.update({"exp": datetime.datetime.utcnow() + expire})
    encoded_jwt = jwt.encode(to_encode, settings.SECRET_KEY, algorithm=settings.ALGORITHM)
    return encoded_jwt

@router.post("/login")
def login(payload: LoginRequest, db: Session = Depends(get_db)):
    user = db.query(User).filter(
        (User.email == payload.account) | (User.username == payload.account)
    ).first()
    
    if not user or not verify_password(payload.password, user.password_hash or ""):
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Incorrect email or password",
            headers={"WWW-Authenticate": "Bearer"},
        )
    
    if not user.is_active:
        raise HTTPException(status_code=403, detail="Account disabled")

    access_token = create_access_token(data={"sub": user.email, "is_admin": user.is_admin})
    
    return {
        "user": {
            "id": user.id,
            "email": user.email,
            "username": user.username,
            "is_admin": user.is_admin,
            "avatar_url": user.avatar_url
        },
        "tokens": {
            "access_token": access_token,
            "refresh_token": access_token, # Simplified for now
        }
    }

@router.post("/refresh")
def refresh_token(payload: dict):
    # Simplified refresh for setup
    return {"access_token": payload.get("refresh_token"), "refresh_token": payload.get("refresh_token")}
