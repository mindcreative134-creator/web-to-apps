from sqlalchemy import Column, Integer, String, Text, DateTime, Boolean, func
from app.database import Base

class Announcement(Base):
    __tablename__ = "announcements"
    id = Column(Integer, primary_key=True, autoincrement=True)
    title = Column(String(200), nullable=False)
    content = Column(Text, nullable=False)
    content_en = Column(Text, nullable=True)
    is_active = Column(Boolean, default=True)
    display_type = Column(String(50), default="popup") # popup, banner, fullscreen
    target_audience = Column(String(50), default="all") # all, pro, free, ultra
    start_at = Column(DateTime, default=func.now())
    end_at = Column(DateTime, nullable=True)
    action_url = Column(String(500), nullable=True)
    dismissible = Column(Boolean, default=True)
    
    # Keeping old field for compat if needed
    target_group = Column(String(50), default="all")
    
    created_at = Column(DateTime, default=func.now())
