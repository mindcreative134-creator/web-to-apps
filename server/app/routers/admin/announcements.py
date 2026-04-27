from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from typing import List, Optional
from pydantic import BaseModel
from datetime import datetime

from app.database import get_db
from app.models.announcement import Announcement as DbAnnouncement
from app.schemas.common import ApiResponse
from app.dependencies import get_current_admin

router = APIRouter(prefix="/announcements", tags=["Admin Announcements"])

class AnnouncementBase(BaseModel):
    title: str
    content: str
    content_en: Optional[str] = None
    display_type: str = "popup"
    target_audience: str = "all"
    is_active: bool = True
    start_at: Optional[datetime] = None
    end_at: Optional[datetime] = None
    action_url: Optional[str] = None
    dismissible: bool = True

class AnnouncementCreate(AnnouncementBase):
    pass

class AnnouncementUpdate(BaseModel):
    title: Optional[str] = None
    content: Optional[str] = None
    content_en: Optional[str] = None
    display_type: Optional[str] = None
    target_audience: Optional[str] = None
    is_active: Optional[bool] = None
    start_at: Optional[datetime] = None
    end_at: Optional[datetime] = None
    action_url: Optional[str] = None
    dismissible: Optional[bool] = None

class Announcement(AnnouncementBase):
    id: int
    created_at: datetime
    
    model_config = {
        "from_attributes": True
    }

@router.get("", response_model=ApiResponse[List[Announcement]])
def get_announcements(
    page: int = 1,
    page_size: int = 50,
    db: Session = Depends(get_db),
    admin = Depends(get_current_admin)
):
    query = db.query(DbAnnouncement).order_by(DbAnnouncement.created_at.desc())
    announcements = query.offset((page - 1) * page_size).limit(page_size).all()
    return ApiResponse(data=announcements)

@router.post("", response_model=ApiResponse[Announcement])
def create_announcement(announcement: AnnouncementCreate, db: Session = Depends(get_db), admin = Depends(get_current_admin)):
    db_announcement = DbAnnouncement(**announcement.dict())
    db.add(db_announcement)
    db.commit()
    db.refresh(db_announcement)
    return ApiResponse(data=db_announcement)

@router.put("/{announcement_id}", response_model=ApiResponse[Announcement])
def update_announcement(announcement_id: int, announcement: AnnouncementUpdate, db: Session = Depends(get_db), admin = Depends(get_current_admin)):
    db_announcement = db.query(DbAnnouncement).filter(DbAnnouncement.id == announcement_id).first()
    if not db_announcement:
        raise HTTPException(status_code=404, detail="Announcement not found")
    
    update_data = announcement.dict(exclude_unset=True)
    for key, value in update_data.items():
        setattr(db_announcement, key, value)
    
    db.commit()
    db.refresh(db_announcement)
    return ApiResponse(data=db_announcement)

@router.delete("/{announcement_id}", response_model=ApiResponse)
def delete_announcement(announcement_id: int, db: Session = Depends(get_db), admin = Depends(get_current_admin)):
    db_announcement = db.query(DbAnnouncement).filter(DbAnnouncement.id == announcement_id).first()
    if not db_announcement:
        raise HTTPException(status_code=404, detail="Announcement not found")
    
    db.delete(db_announcement)
    db.commit()
    return ApiResponse(message="Announcement deleted successfully")
