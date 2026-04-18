import sys
import os

# Add parent directory to sys.path to allow imports from 'app'
sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))

from app.database import SessionLocal, engine, Base
from app.models.user import User
from app.utils.password_crypto import get_password_hash

def reset_admin():
    print("Resetting administrator account...")
    db = SessionLocal()
    try:
        # Create tables if not exist
        Base.metadata.create_all(bind=engine)
        
        # Remove existing admin at this email to ensure fresh hash
        email = "admin@example.com"
        db.query(User).filter(User.email == email).delete()
        
        # Create fresh admin
        new_admin = User(
            email=email,
            username="admin",
            password_hash=get_password_hash("admin123"),
            is_admin=True,
            is_pro=True,
            is_active=True
        )
        db.add(new_admin)
        db.commit()
        print(f"SUCCESS: Administrator {email} reset with password 'admin123'")
    except Exception as e:
        print(f"FAILED: {e}")
        db.rollback()
    finally:
        db.close()

if __name__ == "__main__":
    reset_admin()
