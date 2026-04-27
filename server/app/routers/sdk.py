from typing import Optional, List
from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy.orm import Session
from app.database import get_db
from app.models.project import Project, ProjectVersion
from app.models.announcement import Announcement
from app.models.remote_config import RemoteConfig
from app.schemas.common import ApiResponse

router = APIRouter(prefix="/sdk/{project_key}", tags=["Cloud SDK"])

@router.get("/check-update", response_model=ApiResponse)
def check_update(
    project_key: str,
    current_version: int = Query(...),
    db: Session = Depends(get_db)
):
    project = db.query(Project).filter(Project.project_key == project_key, Project.is_active == True).first()
    if not project:
        raise HTTPException(status_code=404, detail="Project not found or inactive")

    latest_version = db.query(ProjectVersion).filter(ProjectVersion.project_id == project.id).order_by(ProjectVersion.id.desc()).first()
    
    if not latest_version:
        return ApiResponse(data={"has_update": False})

    # Assuming we store version code in version_name for simplicity in this stub
    # In a real app, you'd have a version_code field
    try:
        latest_vc = int(latest_version.version_name.split('+')[-1] if '+' in latest_version.version_name else 1)
    except:
        latest_vc = 1

    has_update = latest_vc > current_version
    
    return ApiResponse(data={
        "has_update": has_update,
        "latest": {
            "version_name": latest_version.version_name,
            "changelog": "New features and stability improvements.",
            "is_force_update": False,
            "download_urls": {
                "r2": f"https://cdn.shiaho.sbs/apks/{project.project_key}/latest.apk",
                "github": "",
                "gitee": ""
            }
        } if has_update else None
    })

@router.get("/announcements", response_model=ApiResponse)
def get_sdk_announcements(
    project_key: str,
    lang: str = "en",
    db: Session = Depends(get_db)
):
    project = db.query(Project).filter(Project.project_key == project_key).first()
    if not project:
        raise HTTPException(status_code=404, detail="Project not found")

    # Fetch global announcements + project specific ones if implemented
    announcements = db.query(Announcement).filter(Announcement.is_active == True).all()
    
    result = []
    for ann in announcements:
        result.append({
            "id": str(ann.id),
            "title": ann.title if lang != "en" or not ann.content_en else ann.title, # Simplified
            "content": ann.content if lang != "en" or not ann.content_en else ann.content_en,
            "type": ann.display_type
        })
    
    return ApiResponse(data={"announcements": result})

@router.get("/config", response_model=ApiResponse)
def get_sdk_config(
    project_key: str,
    db: Session = Depends(get_db)
):
    project = db.query(Project).filter(Project.project_key == project_key).first()
    if not project:
        raise HTTPException(status_code=404, detail="Project not found")

    configs = db.query(RemoteConfig).all() # Should be filtered by project if implemented
    
    result = []
    for cfg in configs:
        result.append({
            "key": cfg.key,
            "value": cfg.value
        })
    
    return ApiResponse(data={"configs": result})

@router.post("/stats", response_model=ApiResponse)
def report_stats(
    project_key: str,
    opens: int = Query(0),
    crash: int = Query(0),
    device_id: str = Query(None),
    db: Session = Depends(get_db)
):
    # Logic to record usage stats in a real app
    return ApiResponse(message="Stats recorded")

@router.get("/verify-code", response_model=ApiResponse)
def verify_activation_code(
    project_key: str,
    code: str = Query(...),
    device_id: str = Query(None),
    db: Session = Depends(get_db)
):
    # Logic to verify activation code against ProjectActivationCode
    return ApiResponse(data={"valid": True, "message": "Valid code"})
