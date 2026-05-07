from pydantic import BaseModel
from typing import List, Optional


class Product(BaseModel):
    id: str
    name: str
    brand: str
    category: str
    subcategory: str
    gender: str
    concerns: List[str]
    description: str
    price: str
    rating: float
    reviews: int
    image: str
    source: str
    source_url: str
    ingredients: List[str]
    key_benefits: List[str]


class SearchRequest(BaseModel):
    query: str
    category: Optional[str] = None
    gender: Optional[str] = None
    concern: Optional[str] = None


class AIRecommendationRequest(BaseModel):
    problem: str
    gender: Optional[str] = None
    age_range: Optional[str] = None
    skin_type: Optional[str] = None
    hair_type: Optional[str] = None


class AIRecommendationResponse(BaseModel):
    advice: str
    recommended_products: List[Product]
    routine_steps: List[str]


class ChatMessage(BaseModel):
    role: str
    content: str


class ChatRequest(BaseModel):
    message: str
    history: Optional[List[ChatMessage]] = []
