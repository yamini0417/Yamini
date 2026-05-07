import json
import os
from typing import List, Optional

from fastapi import FastAPI, HTTPException, Query
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from fastapi.responses import FileResponse

from models import (
    Product,
    SearchRequest,
    AIRecommendationRequest,
    AIRecommendationResponse,
    ChatRequest,
)
from ai_agent import load_products, get_ai_recommendations, chat_with_agent

app = FastAPI(title="GlowHub Beauty Aggregator API", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

PRODUCTS: List[dict] = []


@app.on_event("startup")
async def startup_event():
    global PRODUCTS
    PRODUCTS = load_products()
    print(f"Loaded {len(PRODUCTS)} products")


# --- Product Endpoints ---

@app.get("/api/products", response_model=List[Product])
def get_products(
    category: Optional[str] = Query(None),
    gender: Optional[str] = Query(None),
    concern: Optional[str] = Query(None),
    subcategory: Optional[str] = Query(None),
    min_rating: Optional[float] = Query(None),
    limit: int = Query(20, le=50),
    offset: int = Query(0),
):
    results = PRODUCTS

    if category:
        results = [p for p in results if p["category"].lower() == category.lower()]
    if gender and gender.lower() != "all":
        results = [
            p for p in results
            if p["gender"].lower() == gender.lower() or p["gender"].lower() == "unisex"
        ]
    if concern:
        concern_lower = concern.lower()
        results = [
            p for p in results
            if any(concern_lower in c.lower() for c in p["concerns"])
        ]
    if subcategory:
        results = [p for p in results if p["subcategory"].lower() == subcategory.lower()]
    if min_rating:
        results = [p for p in results if p["rating"] >= min_rating]

    results = sorted(results, key=lambda x: (x["rating"], x["reviews"]), reverse=True)
    return [Product(**p) for p in results[offset : offset + limit]]


@app.get("/api/products/{product_id}", response_model=Product)
def get_product(product_id: str):
    for p in PRODUCTS:
        if p["id"] == product_id:
            return Product(**p)
    raise HTTPException(status_code=404, detail="Product not found")


@app.get("/api/products/search/query", response_model=List[Product])
def search_products(q: str = Query(..., min_length=1)):
    q_lower = q.lower()
    results = []
    for p in PRODUCTS:
        score = 0
        if q_lower in p["name"].lower():
            score += 3
        if q_lower in p["brand"].lower():
            score += 2
        if q_lower in p["description"].lower():
            score += 1
        if any(q_lower in c.lower() for c in p["concerns"]):
            score += 2
        if any(q_lower in b.lower() for b in p["key_benefits"]):
            score += 1
        if q_lower in p["category"].lower() or q_lower in p["subcategory"].lower():
            score += 2
        if score > 0:
            results.append((score, p))

    results.sort(key=lambda x: x[0], reverse=True)
    return [Product(**p) for _, p in results]


@app.get("/api/concerns", response_model=List[str])
def get_all_concerns():
    concerns = set()
    for p in PRODUCTS:
        for c in p["concerns"]:
            concerns.add(c)
    return sorted(list(concerns))


@app.get("/api/sources", response_model=List[str])
def get_all_sources():
    sources = {p["source"] for p in PRODUCTS}
    return sorted(list(sources))


@app.get("/api/stats")
def get_stats():
    return {
        "total_products": len(PRODUCTS),
        "skincare_count": sum(1 for p in PRODUCTS if p["category"] == "skincare"),
        "haircare_count": sum(1 for p in PRODUCTS if p["category"] == "haircare"),
        "sources": len({p["source"] for p in PRODUCTS}),
        "brands": len({p["brand"] for p in PRODUCTS}),
    }


# --- AI Endpoints ---

@app.post("/api/ai/recommend", response_model=AIRecommendationResponse)
def ai_recommend(request: AIRecommendationRequest):
    if not os.environ.get("ANTHROPIC_API_KEY"):
        raise HTTPException(status_code=503, detail="AI service not configured. Set ANTHROPIC_API_KEY.")
    try:
        return get_ai_recommendations(
            problem=request.problem,
            products=PRODUCTS,
            gender=request.gender,
            age_range=request.age_range,
            skin_type=request.skin_type,
            hair_type=request.hair_type,
        )
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@app.post("/api/ai/chat")
def ai_chat(request: ChatRequest):
    if not os.environ.get("ANTHROPIC_API_KEY"):
        raise HTTPException(status_code=503, detail="AI service not configured. Set ANTHROPIC_API_KEY.")
    try:
        reply = chat_with_agent(
            message=request.message,
            history=[m.dict() for m in (request.history or [])],
        )
        return {"reply": reply}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


# --- Serve Frontend ---

frontend_dir = os.path.join(os.path.dirname(__file__), "..", "frontend")
if os.path.exists(frontend_dir):
    app.mount("/static", StaticFiles(directory=frontend_dir), name="static")

    @app.get("/")
    def serve_frontend():
        return FileResponse(os.path.join(frontend_dir, "index.html"))


if __name__ == "__main__":
    import uvicorn
    uvicorn.run("main:app", host="0.0.0.0", port=8000, reload=True)
