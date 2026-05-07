import os
import json
from typing import List, Optional
import anthropic

from models import Product, AIRecommendationResponse

client = anthropic.Anthropic(api_key=os.environ.get("ANTHROPIC_API_KEY"))


def load_products() -> List[dict]:
    data_path = os.path.join(os.path.dirname(__file__), "data", "products.json")
    with open(data_path, "r") as f:
        return json.load(f)


def get_ai_recommendations(
    problem: str,
    products: List[dict],
    gender: Optional[str] = None,
    age_range: Optional[str] = None,
    skin_type: Optional[str] = None,
    hair_type: Optional[str] = None,
) -> AIRecommendationResponse:
    product_catalog = json.dumps(
        [
            {
                "id": p["id"],
                "name": p["name"],
                "brand": p["brand"],
                "category": p["category"],
                "subcategory": p["subcategory"],
                "gender": p["gender"],
                "concerns": p["concerns"],
                "description": p["description"],
                "price": p["price"],
                "rating": p["rating"],
                "key_benefits": p["key_benefits"],
            }
            for p in products
        ],
        indent=2,
    )

    user_context = f"Problem/concern: {problem}"
    if gender:
        user_context += f"\nGender: {gender}"
    if age_range:
        user_context += f"\nAge range: {age_range}"
    if skin_type:
        user_context += f"\nSkin type: {skin_type}"
    if hair_type:
        user_context += f"\nHair type: {hair_type}"

    system_prompt = """You are an expert beauty advisor specializing in skincare and haircare for all genders.
    You have deep knowledge of beauty products and their ingredients.

    When given a user's beauty concern and a product catalog, you must:
    1. Provide empathetic, expert advice addressing their specific problem
    2. Select the most relevant product IDs from the catalog (2-5 products max)
    3. Create a simple daily/weekly routine with steps

    Always respond in valid JSON with this exact structure:
    {
        "advice": "Expert advice text addressing the specific problem (2-3 paragraphs)",
        "recommended_product_ids": ["id1", "id2", "id3"],
        "routine_steps": ["Step 1: ...", "Step 2: ...", "Step 3: ..."]
    }

    Be specific, helpful, and consider the user's gender and other context if provided.
    Only recommend products from the provided catalog."""

    response = client.messages.create(
        model="claude-sonnet-4-6",
        max_tokens=1500,
        system=system_prompt,
        messages=[
            {
                "role": "user",
                "content": f"Here is the available product catalog:\n{product_catalog}\n\nUser context:\n{user_context}\n\nProvide recommendations in the required JSON format.",
            }
        ],
    )

    raw = response.content[0].text.strip()
    if raw.startswith("```"):
        raw = raw.split("```")[1]
        if raw.startswith("json"):
            raw = raw[4:]
    result = json.loads(raw)

    recommended_ids = result.get("recommended_product_ids", [])
    product_map = {p["id"]: p for p in products}
    recommended_products = [
        Product(**product_map[pid]) for pid in recommended_ids if pid in product_map
    ]

    return AIRecommendationResponse(
        advice=result["advice"],
        recommended_products=recommended_products,
        routine_steps=result.get("routine_steps", []),
    )


def chat_with_agent(message: str, history: list) -> str:
    products = load_products()
    product_summary = json.dumps(
        [
            {
                "id": p["id"],
                "name": p["name"],
                "brand": p["brand"],
                "category": p["category"],
                "concerns": p["concerns"],
                "price": p["price"],
                "gender": p["gender"],
            }
            for p in products
        ],
        indent=2,
    )

    system_prompt = f"""You are BeautyAI, a friendly and knowledgeable beauty consultant for GlowHub - a beauty aggregator platform.

    You help users with:
    - Skincare concerns (acne, dryness, oily skin, aging, dark spots, sensitivity, etc.)
    - Haircare concerns (dandruff, hair loss, dryness, frizz, damage, etc.)
    - Product recommendations from our curated catalog
    - Beauty routines and tips for men and women

    Available products catalog:
    {product_summary}

    When recommending products, mention the product name and ID so users can find them.
    Be warm, professional, and give personalized advice based on what the user shares.
    Keep responses concise but helpful (2-4 sentences for simple questions, more for complex ones)."""

    messages = []
    for msg in history:
        messages.append({"role": msg.get("role", "user"), "content": msg.get("content", "")})
    messages.append({"role": "user", "content": message})

    response = client.messages.create(
        model="claude-sonnet-4-6",
        max_tokens=800,
        system=system_prompt,
        messages=messages,
    )

    return response.content[0].text
