from typing import Optional, List
from pydantic import BaseModel
from datetime import datetime

class ProjectBase(BaseModel):
    project_name: str
    package_name: Optional[str] = None
    description: Optional[str] = None
    is_active: bool = True

class ProjectCreate(ProjectBase):
    pass

class ProjectUpdate(BaseModel):
    project_name: Optional[str] = None
    package_name: Optional[str] = None
    description: Optional[str] = None
    is_active: Optional[bool] = None

class ProjectResponse(ProjectBase):
    id: int
    project_key: str
    owner_id: int
    created_at: datetime
    updated_at: datetime

    class Config:
        from_attributes = True

class ProjectCodeResponse(BaseModel):
    id: int
    project_id: int
    code: str

    class Config:
        from_attributes = True

class ProjectVersionResponse(BaseModel):
    id: int
    project_id: int
    version_name: str
    created_at: datetime

    class Config:
        from_attributes = True
