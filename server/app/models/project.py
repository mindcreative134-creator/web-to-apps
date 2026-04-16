from sqlalchemy import Column, BigInteger, String, Text, DateTime, ForeignKey, Boolean, func
from sqlalchemy.orm import relationship
from app.database import Base

class Project(Base):
    __tablename__ = "projects"
    id = Column(BigInteger, primary_key=True, autoincrement=True)
    owner_id = Column(BigInteger, ForeignKey("users.id", ondelete="CASCADE"), nullable=False, index=True)
    project_name = Column(String(100), nullable=False)
    project_key = Column(String(100), unique=True, index=True)
    package_name = Column(String(255))
    is_active = Column(Boolean, default=True)
    created_at = Column(DateTime, default=func.now())
    updated_at = Column(DateTime, default=func.now(), onupdate=func.now())

class ProjectActivationCode(Base):
    __tablename__ = "project_activation_codes"
    id = Column(BigInteger, primary_key=True, autoincrement=True)
    project_id = Column(BigInteger, ForeignKey("projects.id", ondelete="CASCADE"), nullable=False)
    code = Column(String(100))

class ProjectVersion(Base):
    __tablename__ = "project_versions"
    id = Column(BigInteger, primary_key=True, autoincrement=True)
    project_id = Column(BigInteger, ForeignKey("projects.id", ondelete="CASCADE"), nullable=False)
    version_name = Column(String(50))
    created_at = Column(DateTime, default=func.now())
