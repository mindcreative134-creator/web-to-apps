from sqlalchemy import Column, Integer, String, Text, DateTime, ForeignKey, func
from app.database import Base

class ModuleReport(Base):
    __tablename__ = "module_reports"
    id = Column(Integer, primary_key=True, autoincrement=True)
    module_id = Column(Integer, ForeignKey("store_modules.id", ondelete="CASCADE"))
    reporter_id = Column(Integer, ForeignKey("users.id"))
    reason = Column(String(200))
    details = Column(Text)
    status = Column(String(50), default="pending") # pending, resolved, dismissed
    created_at = Column(DateTime, default=func.now())
