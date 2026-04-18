from sqlalchemy import Column, Integer, String, DateTime, func
from app.database import Base

class Credential(Base):
    __tablename__ = "credentials"

    id = Column(Integer, primary_key=True, index=True)
    username = Column(String(255), nullable=False)
    password = Column(String(255), nullable=False)
    label = Column(String(255), nullable=True)
    created_at = Column(DateTime, default=func.now())
