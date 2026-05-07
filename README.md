# GlowHub — AI Beauty Aggregator Platform

GlowHub is an AI-powered beauty product aggregator built with **Java Spring Boot** and the **Anthropic Claude API**. It collects skincare and haircare products for men and women from multiple retailers into one unified website, with personalized AI recommendations.

## Features

- **Multi-source aggregation** — Products from Sephora, Amazon, Ulta, Target, Walmart, CVS, and Dermstore
- **Skincare & Haircare** — Moisturizers, serums, cleansers, sunscreens, shampoos, conditioners, hair masks, oils, and more
- **Men & Women** — Dedicated sections for both genders plus unisex products
- **Concern-based search** — Find products by problem: acne, dry skin, dandruff, hair loss, anti-aging, frizz, and more
- **AI Advisor** — Claude-powered personalized recommendations with a tailored daily routine
- **BeautyAI Chat** — Conversational AI beauty consultant for real-time advice
- **Advanced filters** — Filter by category, gender, concern, and minimum rating

## Tech Stack

- **Backend**: Java 17 + Spring Boot 3.3
- **AI Agent**: Anthropic Java SDK (claude-sonnet-4-6)
- **Frontend**: Vanilla HTML, CSS, JavaScript (served from classpath)
- **Build Tool**: Maven

## Project Structure

```
Yamini/
├── pom.xml
└── src/
    └── main/
        ├── java/com/glowhub/
        │   ├── GlowHubApplication.java      # Spring Boot entry point
        │   ├── config/WebConfig.java         # CORS config
        │   ├── controller/
        │   │   ├── ProductController.java    # Product REST endpoints
        │   │   └── AIController.java         # AI REST endpoints
        │   ├── model/
        │   │   ├── Product.java
        │   │   ├── AIRecommendationRequest.java
        │   │   ├── AIRecommendationResponse.java
        │   │   ├── ChatMessage.java
        │   │   └── ChatRequest.java
        │   └── service/
        │       ├── ProductService.java       # Product loading and filtering
        │       └── AIAgentService.java       # Anthropic Claude integration
        └── resources/
            ├── application.properties
            ├── data/products.json            # 30+ product catalog
            └── static/                       # Frontend (served at /)
                ├── index.html
                ├── styles.css
                └── app.js
```

## Setup and Run

### Prerequisites

- Java 17+
- Maven 3.8+
- Anthropic API key (get one at console.anthropic.com)

### 1. Set your Anthropic API key

```bash
export ANTHROPIC_API_KEY=sk-ant-api03-xxxxxxxxxxxxxxxx
```

### 2. Build the project

```bash
mvn clean package -DskipTests
```

### 3. Run the application

```bash
mvn spring-boot:run
```

Or run the JAR directly:

```bash
java -jar target/glowhub-1.0.0.jar
```

The app will be available at **http://localhost:8080**

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products` | List products with filters (category, gender, concern, min_rating, limit, offset) |
| GET | `/api/products/{id}` | Get single product by ID |
| GET | `/api/products/search/query?q=...` | Full-text search across name, brand, concerns |
| GET | `/api/concerns` | List all available concerns |
| GET | `/api/sources` | List all source retailers |
| GET | `/api/stats` | Platform statistics (counts, brands, sources) |
| POST | `/api/ai/recommend` | AI-powered product recommendations |
| POST | `/api/ai/chat` | Conversational AI beauty advisor |

### Example: AI Recommendation Request

```json
POST /api/ai/recommend
{
  "problem": "I have oily skin with frequent breakouts",
  "gender": "women",
  "age_range": "20s",
  "skin_type": "oily"
}
```

## Extending the Product Catalog

Add entries to `src/main/resources/data/products.json`:

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
