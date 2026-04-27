"""
Admin sub-package — modular admin endpoints.
"""
from fastapi import APIRouter

from app.routers.admin.dashboard import router as dashboard_router
from app.routers.admin.users import router as users_router
from app.routers.admin.moderation import router as moderation_router
from app.routers.admin.notifications import router as notifications_router
from app.routers.admin.maintenance import router as maintenance_router
from app.routers.admin.store_management import router as store_management_router
from app.routers.admin.analytics import router as analytics_router
from app.routers.admin.activation import router as activation_router
from app.routers.admin.credentials import router as credentials_router
from app.routers.admin.remote_config import router as remote_config_router
from app.routers.admin.intelligence import router as intelligence_router
from app.routers.admin.announcements import router as announcements_router
from app.routers.admin.app_versions import router as app_versions_router

router = APIRouter(prefix="/admin", tags=["Admin"])

router.include_router(dashboard_router)
router.include_router(users_router)
router.include_router(moderation_router)
router.include_router(notifications_router)
router.include_router(maintenance_router)
router.include_router(store_management_router)
router.include_router(analytics_router)
router.include_router(activation_router)
router.include_router(credentials_router)
router.include_router(remote_config_router)
router.include_router(intelligence_router)
router.include_router(announcements_router)
router.include_router(app_versions_router)
