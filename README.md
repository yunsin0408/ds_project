# Isoogle

A search engine that is tailored for International Organization of Standardization (ISO) Search.

## Local development

1. Create or edit the `.env` file in the project root with your secrets. Example:

```bash
# .env (example)
export GOOGLE_CSE_APIKEY="your_api_key"
export GOOGLE_CSE_CX="your_cse_id"
```

2. Load the variables into your shell (zsh):

```bash
source .env
```

3. Run the application with Maven:

```bash
./mvnw spring-boot:run
```

4. Open at http://localhost:8080 


