from fastapi import Header, HTTPException, Depends
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.user import User

async def get_current_user(
    db: Session = Depends(get_db),
    authorization: str = Header(None)
):
    # Simplified authentication for setup
    if not authorization:
        raise HTTPException(status_code=401, detail="Unauthorized")
    
    # In a real app, logic to verify JWT token would go here
    token = authorization.split(" ")[-1]
    # For now, let's assume the first user is admin for testing
    user = db.query(User).first()
    if not user:
        raise HTTPException(status_code=404, detail="User not found")
    return user

async def get_current_admin(current_user: User = Depends(get_current_user)):
    if not current_user.is_admin:
        raise HTTPException(status_code=403, detail="Forbidden: Admin access required")
    return current_user
