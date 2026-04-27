import secrets
import string
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List, Optional
from pydantic import BaseModel
from datetime import datetime

from app.models.user import User
from app.database import get_db
from app.models.credential import Credential
from app.schemas.common import ApiResponse
from app.dependencies import get_current_admin

router = APIRouter(prefix="/credentials", tags=["Admin Credentials"])

class CredentialBase(BaseModel):
    username: Optional[str] = None
    label: Optional[str] = None

class CredentialCreate(CredentialBase):
    pass

class CredentialOut(CredentialBase):
    id: int
    password: str
    created_at: datetime
    class Config:
        from_attributes = True

def generate_random_password(length=16):
    alphabet = string.ascii_letters + string.digits + string.punctuation
    return ''.join(secrets.choice(alphabet) for _ in range(length))

@router.get("", response_model=ApiResponse[List[CredentialOut]])
def list_credentials(db: Session = Depends(get_db), current_admin: User = Depends(get_current_admin)):
    creds = db.query(Credential).order_by(Credential.created_at.desc()).all()
    return ApiResponse(data=creds)

@router.post("", response_model=ApiResponse[CredentialOut])
def create_credential(payload: CredentialCreate, db: Session = Depends(get_db), current_admin: User = Depends(get_current_admin)):
    username = payload.username
    if not username:
        username = f"database_user_{secrets.token_hex(4)}"
    
    password = generate_random_password()
    
    db_credential = Credential(
        username=username,
        password=password,
        label=payload.label or "Default Credential"
    )
    db.add(db_credential)
    db.commit()
    db.refresh(db_credential)
    return ApiResponse(data=db_credential)

@router.delete("/{credential_id}", response_model=ApiResponse)
def delete_credential(credential_id: int, db: Session = Depends(get_db), current_admin: User = Depends(get_current_admin)):
    db_credential = db.query(Credential).filter(Credential.id == credential_id).first()
    if not db_credential:
        raise HTTPException(status_code=404, detail="Credential not found")
    db.delete(db_credential)
    db.commit()
    return ApiResponse(message="Credential deleted successfully")
