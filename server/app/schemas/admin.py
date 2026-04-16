from typing import Optional
from pydantic import BaseModel

class AdminUpdateUserRequest(BaseModel):
    is_pro: Optional[bool] = None
    pro_plan: Optional[str] = None
    pro_expires_at: Optional[str] = None
    is_active: Optional[bool] = None
    max_devices: Optional[int] = None
    custom_project_limit: Optional[int] = None
