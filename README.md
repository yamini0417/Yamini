# GlowHub — AI Beauty Aggregator Platform

GlowHub is an AI-powered beauty product aggregator that collects skincare and haircare products for men and women from multiple retailers into one unified website, with personalized AI recommendations.

## Features

- **Multi-source aggregation** — Products from Sephora, Amazon, Ulta, Target, Walmart, CVS, and Dermstore
- **Skincare & Haircare** — Moisturizers, serums, cleansers, sunscreens, shampoos, conditioners, hair masks, oils, and more
- **Men & Women** — Dedicated sections for both genders plus unisex products
- **Concern-based search** — Find products by problem: acne, dry skin, dandruff, hair loss, anti-aging, frizz, and more
- **AI Advisor** — Claude-powered personalized recommendations with a tailored daily routine
- **BeautyAI Chat** — Conversational AI beauty consultant for real-time advice
- **Advanced filters** — Filter by category, gender, concern, and minimum rating
- **Product detail modal** — Full ingredient list, benefits, concerns, and direct links to retailer

## Tech Stack

- **Backend**: FastAPI (Python)
- **AI Agent**: Anthropic Claude (claude-sonnet-4-6)
- **Frontend**: Vanilla HTML, CSS, JavaScript
- **Database**: JSON-based product catalog (easily extensible to PostgreSQL)

## Project Structure

```
Yamini/
├── backend/
│   ├── main.py          # FastAPI server + REST API
│   ├── ai_agent.py      # Claude AI recommendation engine
│   ├── models.py        # Pydantic data models
│   └── data/
│       └── products.json  # Aggregated product catalog
├── frontend/
│   ├── index.html       # Single-page application
│   ├── styles.css       # Responsive styling
│   └── app.js           # Frontend logic
└── requirements.txt
```

## Setup and Run

### 1. Install dependencies

```bash
pip install -r requirements.txt
```

### 2. Set your Anthropic API key

```bash
export ANTHROPIC_API_KEY=your_api_key_here
```

### 3. Start the server

```bash
cd backend
python main.py
```

The app will be available at http://localhost:8000

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products` | List products with filters |
| GET | `/api/products/{id}` | Get product by ID |
| GET | `/api/products/search/query?q=...` | Full-text search |
| GET | `/api/concerns` | List all available concerns |
| GET | `/api/stats` | Platform statistics |
| POST | `/api/ai/recommend` | AI-powered product recommendations |
| POST | `/api/ai/chat` | Conversational AI beauty advisor |

## Extending the Product Catalog

Add entries to `backend/data/products.json` with this schema:

```json
{
  "id": "unique_id",
  "name": "Product Name",
  "brand": "Brand",
  "category": "skincare or haircare",
  "subcategory": "moisturizer or serum or shampoo etc",
  "gender": "women or men or unisex",
  "concerns": ["acne", "dry skin"],
  "description": "Product description",
  "price": "$XX.XX",
  "rating": 4.5,
  "reviews": 1000,
  "image": "https://image-url.com/photo.jpg",
  "source": "Retailer Name",
  "source_url": "https://retailer.com",
  "ingredients": ["Ingredient 1"],
  "key_benefits": ["Benefit 1"]
}
```
