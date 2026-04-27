from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from typing import List, Optional
from pydantic import BaseModel
from datetime import datetime

from app.database import get_db
from app.models.app_version import AppVersion
from app.dependencies import get_current_admin
from app.schemas.common import ApiResponse

router = APIRouter(prefix="/app-versions", tags=["Admin App Versions"])

class VersionBase(BaseModel):
    version_name: str
    version_code: int
    download_url: str
    update_log: Optional[str] = None
    is_force_update: bool = False
    is_active: bool = True

class VersionCreate(VersionBase):
    pass

class VersionOut(VersionBase):
    id: int
    created_at: datetime
    class Config:
        from_attributes = True

@router.get("", response_model=ApiResponse[List[VersionOut]])
def list_versions(db: Session = Depends(get_db), admin = Depends(get_current_admin)):
    versions = db.query(AppVersion).order_by(AppVersion.version_code.desc()).all()
    return ApiResponse(data=versions)

@router.post("", response_model=ApiResponse[VersionOut])
def create_version(payload: VersionCreate, db: Session = Depends(get_db), admin = Depends(get_current_admin)):
    db_version = AppVersion(**payload.dict())
    db.add(db_version)
    db.commit()
    db.refresh(db_version)
    return ApiResponse(data=db_version)

@router.put("/{version_id}/publish", response_model=ApiResponse[VersionOut])
def toggle_publish(version_id: int, db: Session = Depends(get_db), admin = Depends(get_current_admin)):
    db_version = db.query(AppVersion).filter(AppVersion.id == version_id).first()
    if not db_version:
        raise HTTPException(status_code=404, detail="Version not found")
    
    db_version.is_active = not db_version.is_active
    db.commit()
    db.refresh(db_version)
    return ApiResponse(data=db_version)
