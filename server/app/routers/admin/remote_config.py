from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from typing import List, Optional
from pydantic import BaseModel
from datetime import datetime

from app.database import get_db
from app.models.remote_config import RemoteConfig
from app.dependencies import get_current_admin
from app.schemas.common import ApiResponse

router = APIRouter(prefix="/remote-config", tags=["Admin Config"])

class ConfigBase(BaseModel):
    key: str
    value: str
    description: Optional[str] = None

class ConfigCreate(ConfigBase):
    pass

class ConfigUpdate(BaseModel):
    value: Optional[str] = None
    description: Optional[str] = None

class ConfigOut(ConfigBase):
    id: int
    created_at: datetime
    class Config:
        from_attributes = True

@router.get("", response_model=ApiResponse[List[ConfigOut]])
def list_configs(db: Session = Depends(get_db), admin = Depends(get_current_admin)):
    configs = db.query(RemoteConfig).all()
    return ApiResponse(data=configs)

@router.post("", response_model=ApiResponse[ConfigOut])
def create_config(payload: ConfigCreate, db: Session = Depends(get_db), admin = Depends(get_current_admin)):
    existing = db.query(RemoteConfig).filter(RemoteConfig.key == payload.key).first()
    if existing:
        raise HTTPException(status_code=400, detail="Config key already exists")
    
    db_config = RemoteConfig(**payload.dict())
    db.add(db_config)
    db.commit()
    db.refresh(db_config)
    return ApiResponse(data=db_config)

@router.put("/{config_id}", response_model=ApiResponse[ConfigOut])
def update_config(config_id: int, payload: ConfigUpdate, db: Session = Depends(get_db), admin = Depends(get_current_admin)):
    db_config = db.query(RemoteConfig).filter(RemoteConfig.id == config_id).first()
    if not db_config:
        raise HTTPException(status_code=404, detail="Config not found")
    
    if payload.value is not None:
        db_config.value = payload.value
    if payload.description is not None:
        db_config.description = payload.description
    
    db.commit()
    db.refresh(db_config)
    return ApiResponse(data=db_config)

@router.delete("/{config_id}", response_model=ApiResponse)
def delete_config(config_id: int, db: Session = Depends(get_db), admin = Depends(get_current_admin)):
    db_config = db.query(RemoteConfig).filter(RemoteConfig.id == config_id).first()
    if not db_config:
        raise HTTPException(status_code=404, detail="Config not found")
    
    db.delete(db_config)
    db.commit()
    return ApiResponse(message="Config deleted successfully")
