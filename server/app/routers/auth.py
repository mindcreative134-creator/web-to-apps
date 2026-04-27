from datetime import timedelta
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from pydantic import BaseModel
from typing import Optional, List
import random
import string

from app.database import get_db
from app.models.user import User
from app.utils.password_crypto import verify_password
from app.config import get_settings
from jose import jwt
from app.services.email_service import email_service
from app.utils.password_crypto import get_password_hash

router = APIRouter(prefix="/auth", tags=["Authentication"])
settings = get_settings()

class LoginRequest(BaseModel):
    account: str
    password: str

class TokenResponse(BaseModel):
    access_token: str
    refresh_token: str
    token_type: str = "bearer"

from app.utils.security import create_access_token
from app.schemas.common import ApiResponse

@router.post("/login", response_model=ApiResponse)
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
    
    return ApiResponse(data={
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
    })

@router.post("/refresh")
def refresh_token(payload: dict):
    # Simplified refresh for setup
    return {"access_token": payload.get("refresh_token"), "refresh_token": payload.get("refresh_token")}

class RegisterRequest(BaseModel):
    email: str
    username: str
    password: str

@router.post("/register", response_model=ApiResponse)
def register(payload: RegisterRequest, db: Session = Depends(get_db)):
    # Check if user exists
    existing = db.query(User).filter((User.email == payload.email) | (User.username == payload.username)).first()
    if existing:
        raise HTTPException(status_code=400, detail="User already exists")
    
    # Generate verification code
    code = ''.join(random.choices(string.digits, k=6))
    
    new_user = User(
        email=payload.email,
        username=payload.username,
        password_hash=get_password_hash(payload.password),
        is_active=False, # Wait for verification
        verification_code=code
    )
    
    db.add(new_user)
    db.commit()
    
    # Send email
    email_service.send_verification_email(payload.email, code)
    
    return ApiResponse(message="Verification code sent to your email")

@router.post("/verify-email", response_model=ApiResponse)
def verify_email(email: str, code: str, db: Session = Depends(get_db)):
    user = db.query(User).filter(User.email == email).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    
    if user.verification_code == code:
        user.is_active = True
        user.verification_code = None
        db.commit()
        return ApiResponse(message="Account verified successfully")
    else:
        raise HTTPException(status_code=400, detail="Invalid verification code")
