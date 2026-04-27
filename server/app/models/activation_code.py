from sqlalchemy import Column, BigInteger, Integer, String, Text, DateTime, ForeignKey, Enum, func
from sqlalchemy.orm import relationship
from app.database import Base

class ProTransaction(Base):
    __tablename__ = "pro_transactions"
    id = Column(Integer, primary_key=True, autoincrement=True)
    user_id = Column(Integer, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    type = Column(String(50))
    plan_type = Column(String(50))
    activation_code = Column(String(100))
    pro_start = Column(DateTime)
    note = Column(Text)
    created_at = Column(DateTime, default=func.now())
    user = relationship("User", back_populates="pro_transactions")

class ActivationCode(Base):
    __tablename__ = "activation_codes"
    id = Column(Integer, primary_key=True, autoincrement=True)
    code = Column(String(100), unique=True, index=True)
    status = Column(Enum("unused", "used", "disabled", name="code_status_enum"), default="unused")
    plan_type = Column(String(50), default="pro") # pro, premium, etc.
    duration_days = Column(Integer, default=30)
    used_by = Column(Integer, ForeignKey("users.id", ondelete="SET NULL"), nullable=True)
    batch_id = Column(String(100), index=True, nullable=True)
    created_at = Column(DateTime, default=func.now())
    used_at = Column(DateTime, nullable=True)

    user = relationship("User", foreign_keys=[used_by])
