import uuid
from typing import List
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.user import User
from app.models.project import Project
from app.schemas.project import ProjectCreate, ProjectUpdate, ProjectResponse, ProjectCodeResponse, ProjectVersionResponse
from app.schemas.common import ApiResponse
from app.dependencies import get_current_user

router = APIRouter(prefix="/projects", tags=["Projects"])

@router.get("", response_model=ApiResponse[List[ProjectResponse]])
def list_my_projects(
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    projects = db.query(Project).filter(Project.owner_id == current_user.id).all()
    return ApiResponse(data=projects)

@router.post("", response_model=ApiResponse[ProjectResponse])
def create_project(
    payload: ProjectCreate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    # Check project limit for non-admin/pro users if needed
    if not current_user.is_pro and not current_user.is_admin:
        project_count = db.query(Project).filter(Project.owner_id == current_user.id).count()
        if project_count >= 1: # Limit free users to 1 project
            raise HTTPException(status_code=403, detail="Free users are limited to 1 project. Please upgrade to Pro.")

    new_project = Project(
        owner_id=current_user.id,
        project_name=payload.project_name,
        package_name=payload.package_name,
        project_key=str(uuid.uuid4()).split('-')[0].upper(), # Generate a short key
        is_active=payload.is_active
    )
    db.add(new_project)
    db.commit()
    db.refresh(new_project)
    return ApiResponse(data=new_project)

@router.get("/{project_id}", response_model=ApiResponse[ProjectResponse])
def get_project(
    project_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    project = db.query(Project).filter(Project.id == project_id, Project.owner_id == current_user.id).first()
    if not project:
        raise HTTPException(status_code=404, detail="Project not found")
    return ApiResponse(data=project)

@router.put("/{project_id}", response_model=ApiResponse[ProjectResponse])
def update_project(
    project_id: int,
    payload: ProjectUpdate,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    project = db.query(Project).filter(Project.id == project_id, Project.owner_id == current_user.id).first()
    if not project:
        raise HTTPException(status_code=404, detail="Project not found")
    
    update_data = payload.dict(exclude_unset=True)
    for key, value in update_data.items():
        setattr(project, key, value)
    
    db.commit()
    db.refresh(project)
    return ApiResponse(data=project)

@router.delete("/{project_id}", response_model=ApiResponse)
def delete_project(
    project_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    project = db.query(Project).filter(Project.id == project_id, Project.owner_id == current_user.id).first()
    if not project:
        raise HTTPException(status_code=404, detail="Project not found")
    
    db.delete(project)
    db.commit()
    return ApiResponse(message="Project deleted successfully")

# ─── Codes ───

@router.get("/{project_id}/codes", response_model=ApiResponse[List[ProjectCodeResponse]])
def list_project_codes(
    project_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    from app.models.project import ProjectActivationCode
    codes = db.query(ProjectActivationCode).filter(ProjectActivationCode.project_id == project_id).all()
    return ApiResponse(data=codes)

@router.post("/{project_id}/codes/generate", response_model=ApiResponse)
def generate_project_codes(
    project_id: int,
    payload: dict, # Simplified for stub
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    from app.models.project import ProjectActivationCode
    count = payload.get("count", 1)
    new_codes = []
    for _ in range(count):
        code = str(uuid.uuid4()).replace('-', '').upper()[:12]
        new_codes.append(ProjectActivationCode(project_id=project_id, code=code))
    
    db.add_all(new_codes)
    db.commit()
    return ApiResponse(message=f"Generated {count} codes")

# ─── Announcements ───

@router.get("/{project_id}/announcements", response_model=ApiResponse)
def list_project_announcements(
    project_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    from app.models.announcement import Announcement
    anns = db.query(Announcement).all() # Should be filtered by project
    return ApiResponse(data=anns)

@router.post("/{project_id}/announcements", response_model=ApiResponse)
def create_project_announcement(
    project_id: int,
    payload: dict,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    from app.models.announcement import Announcement
    new_ann = Announcement(**payload)
    db.add(new_ann)
    db.commit()
    return ApiResponse(data=new_ann)

# ─── Versions ───

@router.get("/{project_id}/versions", response_model=ApiResponse[List[ProjectVersionResponse]])
def list_project_versions(
    project_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    versions = db.query(ProjectVersion).filter(ProjectVersion.project_id == project_id).all()
    return ApiResponse(data=versions)

@router.post("/{project_id}/versions/publish", response_model=ApiResponse)
def publish_project_version(
    project_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    # Stub for version publishing
    return ApiResponse(message="Version published")

# ─── Configs ───

@router.get("/{project_id}/configs", response_model=ApiResponse)
def list_project_configs(
    project_id: int,
    db: Session = Depends(get_db),
    current_user: User = Depends(get_current_user)
):
    from app.models.remote_config import RemoteConfig
    configs = db.query(RemoteConfig).all()
    return ApiResponse(data=configs)
