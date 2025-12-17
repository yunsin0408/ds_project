
# Isoogle

A search engine tailored for your ISO (International Organization of Standardization) needs.

## Overview

Isoogle is a multi-stage search application that provides different search modes optimized for ISO-related queries:

- **Keyword Search**: Uses Google CSE with keyword frequency ranking
- **Iterative Search**: Extracts and expands keywords for broader results
- **Semantic Search**: Combines keyword frequency with cosine similarity ranking

The application is built as a Spring Boot web service.

## Project Structure

```
isoogle/
├── src/main/java/com/example/isoogle/
│   ├── controller/IsoogleController.java  # Main API endpoint
│   └── IsoogleApplication.java            # Spring Boot app
├── stage1-2/          # Web analysis utilities
├── stage3/            # Google CSE integration and frequency ranking
├── stage4/            # Iterative keyword extraction
└── stage5/            # Semantic ranking with cosine similarity
```

## Search Modes

### Keyword Search (CSE Mode)
- **Purpose**: Direct Google Custom Search with keyword frequency ranking
- **Features**:
  - Fetches 5 results from Google API
  - Crawls main page + subpages for content
  - Ranks by keyword count * weight (ISO biasing)
- **Ranking**: Frequency-based (`count * weight`)
  - User query keywords: weight 4
  - ISO-related terms (e.g., "international", "organization", "standardization"): weight 1

### Iterative Search
- **Purpose**: Discovers additional relevant pages through keyword extraction
- **Features**:
  - Round 1: Fetch initial results
  - Extracts top keywords using term frequency
  - Filters with co-occurrence analysis
  - Round 2: Searches with expanded keyword set
  - Returns Top 5 results
- **Ranking**: Frequency-based (`count * weight`)
  - Original user keywords: weight 10
  - Derived keywords: weight 5
  - ISO-related terms: weight 1

### Semantic Search
- **Purpose**: Hybrid ranking combining frequency and semantic similarity
- **Features**:
  - Uses iterative backend for keyword expansion
  - Calculates TF-based cosine similarity
  - ISO term boosting in ranking
- **Ranking**: Hybrid (60% keyword frequency + 40% cosine similarity)
  - Original user keywords: weight 4
  - ISO-related terms: weight 1
  - Cosine similarity: calculated between query and page content

## Technical Stack

- **Java 21 LTS**: Modern Java runtime
- **Spring Boot 3.5**: Web framework
- **Apache Lucene 9.11**: Text analysis and stopwords
- **Google Custom Search API**: Programmatic search
- **Jsoup**: HTML parsing
- **Maven**: Multi-module build system

## Configuration

Create a `.env` file in the project root:

```bash
GOOGLE_CSE_APIKEY="your_api_key"
GOOGLE_CSE_CX="your_cse_id"
```

## Running the Application

1. Ensure Java 21+ is installed
2. Set up your `.env` file with API credentials
3. Build and run:

```bash
cd isoogle
./mvnw spring-boot:run
```

4. Open http://localhost:8080 in your browser


## Development

- **Build**: `mvn clean install`
- **Test**: `mvn test`
- **Run**: `./mvnw spring-boot:run`
- **Modules**: Each stage can be run independently for CLI testing



