from sqlalchemy import Column, Integer, String, Text, DateTime, ForeignKey, Boolean, Float, func
from sqlalchemy.orm import relationship
from app.database import Base

class StoreModule(Base):
    __tablename__ = "store_modules"
    id = Column(Integer, primary_key=True, autoincrement=True)
    name = Column(String(100), nullable=False)
    identifier = Column(String(100), unique=True, index=True)
    description = Column(Text)
    version = Column(String(50))
    author_id = Column(Integer, ForeignKey("users.id"))
    is_active = Column(Boolean, default=True)
    price = Column(Float, default=0.0)
    created_at = Column(DateTime, default=func.now())
    
    comments = relationship("ModuleComment", back_populates="module", cascade="all, delete-orphan")

class ModuleComment(Base):
    __tablename__ = "module_comments"
    id = Column(Integer, primary_key=True, autoincrement=True)
    module_id = Column(Integer, ForeignKey("store_modules.id", ondelete="CASCADE"))
    user_id = Column(Integer, ForeignKey("users.id"))
    content = Column(Text, nullable=False)
    created_at = Column(DateTime, default=func.now())
    
    module = relationship("StoreModule", back_populates="comments")
