from fastapi import APIRouter, Depends, Query
from app.dependencies import get_current_admin
from app.schemas.common import ApiResponse
from app.services.intelligence.threat_scorer import threat_scorer
from app.services.intelligence.circuit_breaker import circuit_manager
from app.services.intelligence.abuse_detector import abuse_detector
from app.services.intelligence.advanced import adv_intel

router = APIRouter(prefix="/intelligence", tags=["Admin Intelligence"])

@router.get("/overview", response_model=ApiResponse)
def intelligence_overview_full(admin = Depends(get_current_admin)):
    """Combined intelligence dashboard overview."""
    top_threats = threat_scorer.get_top_threats(10)
    circuits = circuit_manager.get_all_status()
    abuse_data = abuse_detector.get_suspicious_modules(10)

    # Simplified threat level calculation
    threat_level = "low"
    if any(t.get("risk_level") == "CRITICAL" for t in top_threats): threat_level = "critical"
    elif any(t.get("risk_level") == "HIGH" for t in top_threats): threat_level = "high"

    return ApiResponse(data={
        "threat_level": threat_level,
        "active_threats": len([t for t in top_threats if t.get("status") == "active"]),
        "blocked_requests_24h": 0,
        "circuit_breakers_open": sum(1 for c in circuits if c.get("state") == "open"),
        "threats": {
            "top_actors": top_threats,
            "critical_count": sum(1 for t in top_threats if t.get("risk_level") == "CRITICAL"),
            "high_count": sum(1 for t in top_threats if t.get("risk_level") == "HIGH"),
        },
        "circuits": circuits,
        "abuse": {
            "suspicious_modules": abuse_data,
        },
        "advanced": adv_intel.get_full_status(),
    })

@router.get("/threats", response_model=ApiResponse)
def intelligence_threats(n: int = Query(50, ge=1, le=200), admin = Depends(get_current_admin)):
    return ApiResponse(data=threat_scorer.get_top_threats(n))

@router.get("/circuits", response_model=ApiResponse)
def intelligence_circuits(admin = Depends(get_current_admin)):
    return ApiResponse(data=circuit_manager.get_all_status())

@router.get("/abuse", response_model=ApiResponse)
def intelligence_abuse(n: int = Query(50, ge=1, le=200), admin = Depends(get_current_admin)):
    return ApiResponse(data=abuse_detector.get_suspicious_modules(n))

@router.get("/advanced", response_model=ApiResponse)
def intelligence_advanced(admin = Depends(get_current_admin)):
    return ApiResponse(data=adv_intel.get_full_status())
