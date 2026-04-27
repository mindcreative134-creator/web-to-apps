from sqlalchemy import Column, Integer, String, Text, DateTime, Boolean, func
from app.database import Base

class AppVersion(Base):
    __tablename__ = "app_versions"

    id = Column(Integer, primary_key=True, index=True)
    version_name = Column(String(50), nullable=False)
    version_code = Column(Integer, nullable=False)
    download_url = Column(String(500), nullable=False)
    update_log = Column(Text, nullable=True)
    is_force_update = Column(Boolean, default=False)
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime, default=func.now())
