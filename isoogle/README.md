# Isoogle

This project demonstrates a simple Spring Boot app with a frontend that calls a Google Custom Search (CSE) API.

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

4. Open the frontend at http://localhost:8080 and use the Google CSE search form.


