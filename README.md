# Course Search Service

A Spring Boot application that provides a REST API for searching courses using Elasticsearch with advanced filtering, pagination, sorting, and autocomplete functionality.

## Features

### Assignment A (Required)
- ✅ Full-text search on course titles and descriptions
- ✅ Multiple filters (category, type, age range, price range, date)
- ✅ Pagination and sorting support
- ✅ Bulk indexing of sample course data
- ✅ RESTful API endpoints

### Assignment B (Bonus)
- ✅ Autocomplete suggestions using Elasticsearch completion suggester
- ✅ Fuzzy search for handling typos in search queries

## Technology Stack

- **Spring Boot 3.3.1** - Main framework
- **Spring Data Elasticsearch** - Elasticsearch integration
- **Elasticsearch 8.14.0** - Search engine
- **Docker & Docker Compose** - Container orchestration
- **Maven** - Build tool
- **Lombok** - Code generation
- **JUnit 5 & Testcontainers** - Testing

## Prerequisites

- Java 21
- Docker and Docker Compose
- Maven 3.3+

## Quick Start

### 1. Clone the Repository

```bash
git clone <repository-url>
cd course-search-service
```

### 2. Start Elasticsearch

```bash
docker-compose up -d
```

Verify Elasticsearch is running:
```bash
curl http://localhost:9200
```

You should see a JSON response with cluster information.

### 3. Build and Run the Application

```bash
mvn clean install
mvn spring-boot:run
```

The application will:
- Start on port 8080
- Connect to Elasticsearch on localhost:9200
- Automatically index 50 sample courses on startup
- Be ready to handle search requests


## API Endpoints

### Search Courses - `GET /api/search`

Search for courses with various filters and options.

#### Query Parameters

| Parameter | Type | Description | Example |
|-----------|------|-------------|---------|
| `q` | String | Search keyword (searches title and description) | `q=math` |
| `minAge` | Integer | Minimum age filter | `minAge=8` |
| `maxAge` | Integer | Maximum age filter | `maxAge=12` |
| `category` | String | Course category filter | `category=Science` |
| `type` | Enum | Course type filter (ONE_TIME, COURSE, CLUB) | `type=COURSE` |
| `minPrice` | Double | Minimum price filter | `minPrice=50.0` |
| `maxPrice` | Double | Maximum price filter | `maxPrice=200.0` |
| `startDate` | ISO DateTime | Show courses on or after this date | `startDate=2025-07-15T10:00:00` |
| `sort` | String | Sort order (upcoming, priceAsc, priceDesc) | `sort=priceAsc` |
| `page` | Integer | Page number (0-based) | `page=0` |
| `size` | Integer | Results per page | `size=10` |

#### Example Requests

**Basic search:**
```bash
curl "http://localhost:8080/api/search?q=math"
```

**Search with multiple filters:**
```bash
curl "http://localhost:8080/api/search?category=Science&minAge=8&maxAge=12&sort=priceAsc"
```

**Search with price range and pagination:**
```bash
curl "http://localhost:8080/api/search?minPrice=50&maxPrice=150&page=0&size=5"
```

**Search with date filter:**
```bash
curl "http://localhost:8080/api/search?startDate=2025-08-01T00:00:00&sort=upcoming"
```

**Fuzzy search (handles typos):**
```bash
curl "http://localhost:8080/api/search?q=matg"  # Will match "math" courses
```

#### Response Format

```json
{
  "total": 15,
  "courses": [
    {
      "id": "1",
      "title": "Algebra Basics",
      "description": "Master the fundamentals of algebra with hands-on exercises",
      "category": "Math",
      "type": "COURSE",
      "gradeRange": "7th-9th",
      "minAge": 12,
      "maxAge": 15,
      "price": 125.50,
      "nextSessionDate": "2025-08-15T14:00:00"
    }
  ],
  "page": 0,
  "size": 10,
  "totalPages": 2
}
```

### Autocomplete Suggestions - `GET /api/search/suggest`

Get autocomplete suggestions for course titles.

#### Query Parameters

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `q` | String | Yes | Partial title for suggestions |

#### Example Requests

```bash
curl "http://localhost:8080/api/search/suggest?q=phy"
```

```bash
curl "http://localhost:8080/api/search/suggest?q=art"
```

#### Response Format

```json
[
  "Physics Principles",
  "Physical Education",
  "Philosophy Introduction"
]
```

## Sample Data

The application automatically generates 50 sample courses with diverse:
- **Categories**: Math, Science, Art, History, English, Music, Physical Education, Technology
- **Types**: ONE_TIME, COURSE, CLUB
- **Age ranges**: 5-18 years with overlapping ranges
- **Prices**: $20-$200
- **Session dates**: Spread over the next 60 days
- **Grade ranges**: 1st-3rd, 4th-6th, 7th-8th, 9th-12th

## Testing

Run the test suite:
```bash
mvn test
```

The tests use Testcontainers to spin up an ephemeral Elasticsearch instance, ensuring tests run in isolation.

### Integration Tests Include:
- Search functionality with various filters
- Pagination and sorting
- Edge cases and error handling

## Configuration

### Application Properties

```properties
# Server Configuration
server.port=8080

# Elasticsearch Configuration
spring.elasticsearch.uris=http://localhost:9200
```

### Environment Variables

You can override configuration using environment variables:

```bash
export ELASTICSEARCH_URIS=http://your-elasticsearch:9200
export SERVER_PORT=8081
```

## Architecture

### Package Structure

```
com.undoschool.cousesearch/
├── config/           # Configuration classes
├── controller/       # REST controllers
├── document/         # Elasticsearch document entities
├── dto/              # Data transfer objects
├── repository/       # Data access layer
├── service/          # Business logic layer
└── CouseSearchApplication.java
```

### Key Components

1. **CourseDocument** - Elasticsearch document mapping
2. **CourseSearchService** - Core search logic with query building
3. **CourseSearchController** - REST API endpoints
4. **CourseSearchApplication** - Handles sample data generation and application startup
5. **ElasticsearchConfig** - Elasticsearch client configuration

## Troubleshooting

### Common Issues

1. **Elasticsearch not starting:**
   ```bash
   docker-compose down
   docker-compose up -d
   ```

2. **Connection refused:**
    - Ensure Elasticsearch is running on port 9200
    - Check firewall settings
    - Verify Docker network connectivity

3. **No search results:**
    - Check if data initialization completed
    - Verify index exists: `curl http://localhost:9200/courses`
    - Check application logs for errors

4. **Memory issues:**
    - Increase Docker memory allocation
    - Adjust ES_JAVA_OPTS in docker-compose.yml