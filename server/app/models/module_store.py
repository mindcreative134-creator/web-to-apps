from sqlalchemy import Column, Integer, String, Text, DateTime, ForeignKey, Boolean, Float, func
from sqlalchemy.orm import relationship
from app.database import Base

class StoreModule(Base):
    __tablename__ = "store_modules"
    id = Column(Integer, primary_key=True, autoincrement=True)
    module_type = Column(String(50), default="app") # app, extension, browser_extension
    name = Column(String(100), nullable=False)
    identifier = Column(String(100), unique=True, index=True)
    description = Column(Text)
    icon = Column(String(500), nullable=True)
    category = Column(String(50), nullable=True)
    tags = Column(String(200), nullable=True)
    
    # Versioning
    version_name = Column(String(50), default="1.0.0")
    version_code = Column(Integer, default=1)
    package_name = Column(String(255), nullable=True)
    
    # Author & Status
    author_id = Column(Integer, ForeignKey("users.id"))
    is_active = Column(Boolean, default=True)
    is_approved = Column(Boolean, default=False)
    is_featured = Column(Boolean, default=False)
    
    # Metrics
    downloads = Column(Integer, default=0)
    view_count = Column(Integer, default=0)
    like_count = Column(Integer, default=0)
    rating = Column(Float, default=0.0)
    rating_count = Column(Integer, default=0)
    comment_count = Column(Integer, default=0)
    
    # URLs & Media
    screenshots = Column(Text, nullable=True) # JSON string or comma separated
    video_url = Column(String(500), nullable=True)
    apk_url_github = Column(String(500), nullable=True)
    apk_url_gitee = Column(String(500), nullable=True)
    
    # Support
    contact_email = Column(String(255), nullable=True)
    website_url = Column(String(500), nullable=True)
    privacy_policy_url = Column(String(500), nullable=True)
    
    # Physical
    file_size = Column(Integer, default=0)
    storage_url_github = Column(String(500), nullable=True)
    storage_url_gitee = Column(String(500), nullable=True)

    created_at = Column(DateTime, default=func.now())
    updated_at = Column(DateTime, default=func.now(), onupdate=func.now())
    
    author = relationship("User")
    comments = relationship("ModuleComment", back_populates="module", cascade="all, delete-orphan")

class ModuleComment(Base):
    __tablename__ = "module_comments"
    id = Column(Integer, primary_key=True, autoincrement=True)
    module_id = Column(Integer, ForeignKey("store_modules.id", ondelete="CASCADE"))
    user_id = Column(Integer, ForeignKey("users.id"))
    content = Column(Text, nullable=False)
    created_at = Column(DateTime, default=func.now())
    
    module = relationship("StoreModule", back_populates="comments")
