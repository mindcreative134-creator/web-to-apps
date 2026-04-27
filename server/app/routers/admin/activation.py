import secrets
import string
from fastapi import APIRouter, Depends, HTTPException, status, Query
from sqlalchemy.orm import Session
from sqlalchemy import func
from typing import List, Optional
from pydantic import BaseModel
from datetime import datetime

from app.database import get_db
from app.models.activation_code import ActivationCode
from app.models.user import User
from app.dependencies import get_current_admin
from app.schemas.common import ApiResponse

router = APIRouter(prefix="/activation", tags=["Admin Activation"])

class CodeGenerateRequest(BaseModel):
    count: int = 10
    plan_type: str = "pro"
    duration_days: int = 30
    batch_id: Optional[str] = None

class CodeOut(BaseModel):
    id: int
    code: str
    status: str
    plan_type: str
    duration_days: int
    batch_id: Optional[str]
    created_at: datetime
    used_at: Optional[datetime]
    used_by: Optional[int]

    class Config:
        from_attributes = True

class ActivationStats(BaseModel):
    total: int
    unused: int
    used: int
    disabled: int

def generate_code(length=12):
    chars = string.ascii_uppercase + string.digits
    return "-".join(["".join(secrets.choice(chars) for _ in range(4)) for _ in range(3)])

@router.get("/list", response_model=ApiResponse[List[CodeOut]])
def list_codes(
    page: int = Query(1, ge=1),
    page_size: int = Query(20, ge=1, le=100),
    batch_id: Optional[str] = None,
    status: Optional[str] = None,
    db: Session = Depends(get_db),
    admin: User = Depends(get_current_admin)
):
    query = db.query(ActivationCode)
    if batch_id:
        query = query.filter(ActivationCode.batch_id == batch_id)
    if status:
        query = query.filter(ActivationCode.status == status)
    
    total = query.count()
    codes = query.order_by(ActivationCode.created_at.desc())\
                 .offset((page - 1) * page_size)\
                 .limit(page_size).all()
                 
    return ApiResponse(data=codes, meta={
        "total": total,
        "page": page,
        "page_size": page_size,
        "total_pages": (total + page_size - 1) // page_size
    })

@router.get("/stats", response_model=ApiResponse)
def get_stats(db: Session = Depends(get_db), admin: User = Depends(get_current_admin)):
    total = db.query(ActivationCode).count()
    unused = db.query(ActivationCode).filter(ActivationCode.status == "unused").count()
    used = db.query(ActivationCode).filter(ActivationCode.status == "used").count()
    disabled = db.query(ActivationCode).filter(ActivationCode.status == "disabled").count()
    
    return ApiResponse(data={
        "total": total,
        "unused": unused,
        "used": used,
        "disabled": disabled,
        "usage_rate": round(used / total * 100, 1) if total > 0 else 0
    })

@router.post("/generate", response_model=ApiResponse)
def generate_codes(
    payload: CodeGenerateRequest,
    db: Session = Depends(get_db),
    admin: User = Depends(get_current_admin)
):
    batch_id = payload.batch_id or f"batch_{datetime.now().strftime('%Y%m%d_%H%M%S')}"
    new_codes = []
    
    for _ in range(payload.count):
        code_str = generate_code()
        # Ensure uniqueness
        while db.query(ActivationCode).filter(ActivationCode.code == code_str).first():
            code_str = generate_code()
            
        db_code = ActivationCode(
            code=code_str,
            plan_type=payload.plan_type,
            duration_days=payload.duration_days,
            batch_id=batch_id,
            status="unused"
        )
        db.add(db_code)
        new_codes.append(db_code)
    
    db.commit()
    for c in new_codes:
        db.refresh(c)
    
    return ApiResponse(data={
        "count": len(new_codes),
        "batch_id": batch_id,
        "codes": [c.code for c in new_codes]
    })

@router.put("/{code_id}/disable", response_model=ApiResponse)
def disable_code(
    code_id: int,
    db: Session = Depends(get_db),
    admin: User = Depends(get_current_admin)
):
    db_code = db.query(ActivationCode).filter(ActivationCode.id == code_id).first()
    if not db_code:
        raise HTTPException(status_code=404, detail="Code not found")
    
    if db_code.status == "used":
        raise HTTPException(status_code=400, detail="Cannot disable used code")
        
    db_code.status = "disabled"
    db.commit()
    return ApiResponse(message="Code disabled successfully")

@router.delete("/{code_id}", response_model=ApiResponse)
def delete_code(
    code_id: int,
    db: Session = Depends(get_db),
    admin: User = Depends(get_current_admin)
):
    db_code = db.query(ActivationCode).filter(ActivationCode.id == code_id).first()
    if not db_code:
        raise HTTPException(status_code=404, detail="Code not found")
    
    db.delete(db_code)
    db.commit()
    return ApiResponse(message="Code deleted successfully")
