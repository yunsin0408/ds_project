
# Isoogle

A search engine tailored for your ISO (International Organization of Standardization) needs.

## Project Structure

This project contains multiple stages demonstrating different search approaches:

```
isoogle/
├── stage1-2/          # Basic web analysis and word counting
├── stage3/            # Google API integration with keyword frequency ranking
│   └── SimpleSearch.java       - Basic search with keyword ranking
├── stage4/            # Iterative keyword extraction search
│   ├── IterativeKeywordSearch.java  - Main iterative search implementation
│   ├── KeywordExtractor.java        - TF-based keyword extraction with context
│   └── SearchService.java           - Backend search logic
└── stage5/            # Semantic search with hybrid ranking
    ├── SemanticSearch.java          - Main semantic search implementation
    └── CosineSimilarityRanker.java  - TF-based cosine similarity calculation
```

#### Stage 3: Simple Search
- **Purpose**: Basic Google Custom Search integration with keyword frequency ranking
- **Features**:
  - Fetches 10 results from Google API
  - Crawls up to 2 sublinks per page
  - Ranks by keyword frequency 
- **Run**: `cd isoogle/stage3 && mvn compile exec:java -Dexec.mainClass="com.example.stage3.SimpleSearch"`

#### Stage 4: Iterative Keyword Search
- **Purpose**: Discovers additional relevant pages through keyword extraction
- **Features**:
  - Round 1: Fetch 5 initial results
  - Extracts top 3 keywords per page using term frequency
  - Filters with co-occurrence analysis 
  - Merges original (10x weight) and derived keywords (5x weight)
  - Round 2: Searches with expanded keyword set
  - Returns top 10 final results
- **Key Technologies**: Apache Lucene stopword filtering, multilingual tokenization
- **Run**: `cd isoogle/stage4 && mvn compile exec:java -Dexec.mainClass="com.example.stage4.IterativeKeywordSearch"`

#### Stage 5: Semantic Search
- **Purpose**: Hybrid ranking combining keyword frequency and semantic similarity
- **Features**:
  - Fetches results using Stage 4 backend
  - Calculates TF-based cosine similarity between query and content
  - Hybrid score: 60% keyword frequency + 40% cosine similarity
  - Min-Max normalization for balanced ranking
- **Key Technologies**: TF vectors, cosine similarity, min-max scaling
- **Run**: `cd isoogle/stage5 && mvn compile exec:java -Dexec.mainClass="com.example.stage5.SemanticSearch"`

### Technical Features

- **Java 21 LTS**: Modern Java runtime
- **Apache Lucene 9.11.1**: English stopword filtering
- **Google Custom Search API**: Programmatic search access
- **Multilingual Support**: Unicode-aware tokenization for Latin and CJK scripts
- **Sublink Crawling**: Aggregates content from main page + 3 sublinks
- **Co-occurrence Filtering**: Context-aware keyword extraction

## Configuration 

- Create an `.env` file.

	Example `.env` (local development only):

	```bash
	# .env 
	GOOGLE_CSE_APIKEY="your_api_key"
	GOOGLE_CSE_CX="your_cse_id"
	GOOGLE_CSE_ENABLED=true
	```

## Run locally

1. Environment variables (Google Search API)

This project uses Google Custom Search API in Stage 3–5.
Set the following environment variables before running:

- `GOOGLE_CSE_APIKEY`
- `GOOGLE_CSE_CX`
  
2. Set the working directory:
```bash
cd "...\isoogle"
```
2. Start the Stage 5 web application:

```bash
.\mvnw.cmd -f stage5\pom.xml -DskipTests clean spring-boot:run
```

3. Open the UI at:

```bash
http://localhost:8080
```


