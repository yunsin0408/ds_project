
# Isoogle

A search engine tailored for your ISO (International Organization of Standardization) needs.

## Configuration 

- Create an `.env` file.

	Example `.env` (local development only):

	```bash
	# .env 
	GOOGLE_CSE_APIKEY="your_api_key"
	GOOGLE_CSE_CX="your_cse_id"
	GOOGLE_CSE_ENABLED=true
	```

## Run locally (zsh)

1. Load the `.env` values into your current shell (the process must be
	 started from the same shell):

	 ```bash
	 cd isoogle
	 set -o allexport
	 source .env
	 set +o allexport
	 ```

2. Start the app:

	 ```bash
	 ./mvnw spring-boot:run
	 ```

	 Or build and run the jar:

	 ```bash
	 ./mvnw -DskipTests package
	 java -jar target/isoogle-0.0.1-SNAPSHOT.jar
	 ```

3. Open the UI at:

	 http://localhost:8080



