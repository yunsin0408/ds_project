# Isoogle

A search engine that is tailored for International Organization of Standardization (ISO) Search.

## Local development

1. Create or edit the `.env` file in the project root with your secrets. Example:

```bash
# .env (example)
export GOOGLE_CSE_APIKEY="AIzaSy...your_key_here"
export GOOGLE_CSE_CX="371566471ab8044f9"
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


