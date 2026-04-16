from typing import Generic, List, Optional, TypeVar, Any
from pydantic import BaseModel

DataT = TypeVar("DataT")

class ApiResponse(BaseModel, Generic[DataT]):
    success: bool = True
    message: Optional[str] = None
    data: Optional[DataT] = None

class PaginatedResponse(ApiResponse, Generic[DataT]):
    total: int
    page: int
    page_size: int
    total_pages: int
