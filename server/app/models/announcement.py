from sqlalchemy import Column, Integer, String, Text, DateTime, Boolean, func
from app.database import Base

class Announcement(Base):
    __tablename__ = "announcements"
    id = Column(Integer, primary_key=True, autoincrement=True)
    title = Column(String(200), nullable=False)
    content = Column(Text, nullable=False)
    is_active = Column(Boolean, default=True)
    target_group = Column(String(50), default="all") # all, pro, free
    created_at = Column(DateTime, default=func.now())
