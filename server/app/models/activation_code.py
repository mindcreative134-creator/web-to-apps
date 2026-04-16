from sqlalchemy import Column, BigInteger, Integer, String, Text, DateTime, ForeignKey, Enum, func
from sqlalchemy.orm import relationship
from app.database import Base

class ProTransaction(Base):
    __tablename__ = "pro_transactions"
    id = Column(BigInteger, primary_key=True, autoincrement=True)
    user_id = Column(BigInteger, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    type = Column(String(50))
    plan_type = Column(String(50))
    activation_code = Column(String(100))
    pro_start = Column(DateTime)
    note = Column(Text)
    created_at = Column(DateTime, default=func.now())
    user = relationship("User", back_populates="pro_transactions")

class ActivationCode(Base):
    __tablename__ = "activation_codes"
    id = Column(BigInteger, primary_key=True, autoincrement=True)
    code = Column(String(100), unique=True, index=True)
    status = Column(Enum("unused", "used", "disabled", name="code_status_enum"), default="unused")
    # Add other fields as needed based on logic
