"""
User & Device ORM models.
"""
from datetime import datetime
from app.utils.time import utcnow
from sqlalchemy import (
    BigInteger, Boolean, Column, DateTime, Enum, Index,
    Integer, String, Text, ForeignKey, func, JSON,
)
from sqlalchemy.orm import relationship
from app.database import Base


class User(Base):
    __tablename__ = "users"

    id = Column(BigInteger, primary_key=True, autoincrement=True)
    email = Column(String(255), nullable=False, unique=True, index=True)
    username = Column(String(50), nullable=False, unique=True)
    hashed_password = Column(String(255))
    password_hash = Column(String(255))
    avatar_url = Column(String(500), default=None)

    # Pro membership
    is_pro = Column(Boolean, default=False)
    pro_since = Column(DateTime, default=None)
    pro_expires_at = Column(DateTime, default=None, index=True)
    pro_plan = Column(String(50), default="free")

    # Cloud usage tracking
    cloud_projects_used = Column(Integer, default=0)
    custom_project_limit = Column(Integer, default=None)

    # Device binding
    max_devices = Column(Integer, default=3)

    # Token/Status
    token_version = Column(Integer, default=0, nullable=False)
    is_active = Column(Boolean, default=True)
    is_admin = Column(Boolean, default=False)
    last_login_at = Column(DateTime, default=None)
    login_count = Column(Integer, default=0)

    # Usage stats
    apps_created = Column(Integer, default=0)
    apks_built = Column(Integer, default=0)

    # Password visibility (encrypted)
    encrypted_password = Column(String(500), nullable=True)

    # Online time tracking
    total_online_seconds = Column(BigInteger, default=0)
    last_heartbeat_at = Column(DateTime, nullable=True)

    # Google login
    google_email = Column(String(255), nullable=True)

    created_at = Column(DateTime, default=func.now())
    updated_at = Column(DateTime, default=func.now(), onupdate=func.now())

    # Relationships
    devices = relationship("UserDevice", back_populates="user", cascade="all, delete-orphan")
    login_logs = relationship("LoginLog", back_populates="user", cascade="all, delete-orphan")
    pro_transactions = relationship("ProTransaction", back_populates="user", cascade="all, delete-orphan")


class UserDevice(Base):
    __tablename__ = "user_devices"
    id = Column(BigInteger, primary_key=True, autoincrement=True)
    user_id = Column(BigInteger, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    device_id = Column(String(100), unique=True, index=True)
    device_name = Column(String(100))
    device_os = Column(String(50))
    app_version = Column(String(50))
    is_active = Column(Boolean, default=True)
    last_active_at = Column(DateTime, default=func.now())
    
    user = relationship("User", back_populates="devices")


class LoginLog(Base):
    __tablename__ = "login_logs"
    id = Column(BigInteger, primary_key=True, autoincrement=True)
    user_id = Column(BigInteger, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    login_type = Column(String(50), default="password")
    ip_address = Column(String(45), default=None)
    country = Column(String(100), default=None)
    success = Column(Boolean, default=True)
    created_at = Column(DateTime, default=func.now())

    user = relationship("User", back_populates="login_logs")
