## Stage 1 - Complete Version 

This program automatically fetches multiple web pages **and YouTube video transcripts**, calculates text relevance scores based on predefined keywords and weights, and outputs the ranked results.

### Features
-  Fetch and analyze regular web pages
-  Fetch and analyze YouTube video transcripts (subtitles)
-  Keyword-based relevance scoring with custom weights
-  Automatic ranking based on calculated scores
-  Python virtual environment for dependency management
-  Graceful error handling for YouTube IP blocks

### Important Notes

**YouTube Transcript Fetching:**
- The program uses the `youtube-transcript-api` Python library
- If you see "IP blocked" errors, it means YouTube has temporarily blocked too many requests from your IP
- Wait a few minutes and try again
- For production use, consider implementing rate limiting or proxy rotation
- The system will still rank other web pages even if YouTube fetching fails

##

 The list of URLs and keyword weights are written in `Main.java`, and can be modified in the code if needed.

 ## Keywords, weight and websites

* ISO *4
* Standard *3
* Sustain *2
* Certificate *1

- https://www.iso.org/home.html
- https://en.wikipedia.org/wiki/International_Organization_for_Standardization
- https://www.cyberark.com/what-is/iso/
- https://youtu.be/a4cyMAIyWIQ?si=qxS0dFr3dVrgt_Xh
- https://youtu.be/Nhlv_3-dQSk?si=qm_Nop7a2pJCkdeP

## How to Run

### Prerequisites

1. **Java**: JDK 8 or above
2. **Python**: Python 3.8 or above (for YouTube transcript fetching)

### Setup

1. **Set up Python virtual environment** (first time only):

    | Environment | Command(bash) | Notes |
    | :--- | :--- | :--- |
    | **macOS/Linux** | `chmod +x setup_python.sh` <br> `./setup_python.sh` |  |
    | **Windows (Git Bash)** | `chmod +x setup_python.sh`<br>`cat setup_python.sh \| sed 's/python3/python/g' \| bash`<br>**excexute this line everytime to activate venv:**<br>`source .venv/Scripts/activate` | to fix `python3`(if your python name is `python`) and different filepath of env |

This will:
- Create a `.venv` virtual environment
- Install required Python dependencies from `requirements.txt`

2. **Compile all Java files**:

```bash
javac *.java
```

Or use the convenience script:

```bash
./compile_and_run.sh
```

### Running the Program

Execute the main program:

```bash
java Main
```

The program will:
- Download regular web pages (HTML)
- Fetch YouTube video transcripts (subtitles) using Python helper
- Calculate keyword-based relevance scores
- Rank and display results in the console

### Test YouTube Transcript Fetcher Only

To test the YouTube transcript fetcher independently:

```bash
javac YouTubeTranscriptFetcher.java
java YouTubeTranscriptFetcher
```

Or test the Python script directly(Recommended for Windows):

```bash
python fetch_youtube_transcript.py "https://youtu.be/a4cyMAIyWIQ"
deactivate
```

## How It Works

### For Regular Websites:
1. `HTMLFetcher` downloads the HTML content
2. `TextPreprocessor` removes HTML tags and extracts plain text
3. `WordCounter` counts keyword occurrences
4. `WebAnalyzer` coordinates the process

### For YouTube Videos:
1. `YouTubeTranscriptFetcher` extracts video ID from URL
2. Fetches the video page to locate caption tracks
3. Downloads transcript XML from YouTube's internal API
4. Parses XML to extract plain text subtitles
5. `WordCounter` counts keyword occurrences
6. `WebAnalyzer` coordinates the process

## Technical Details

- **Language:** Java (JDK 8+) + Python 3.8+
- **Java Dependencies:** None (uses only standard Java libraries)
- **Python Dependencies:** 
  - `youtube-transcript-api` - for fetching YouTube transcripts
  - Managed via virtual environment (`.venv/`)
- **Supported YouTube URL formats:**
  - `https://www.youtube.com/watch?v=VIDEO_ID`
  - `https://youtu.be/VIDEO_ID`
  - `https://www.youtube.com/embed/VIDEO_ID`
  - `https://www.youtube.com/v/VIDEO_ID`

## Project Structure

```
Stage_2/
├── Main.java                        # Main entry point
├── WebAnalyzer.java                 # Analyzes web pages and YouTube videos
├── HTMLFetcher.java                 # Fetches HTML content
├── YouTubeTranscriptFetcher.java    # Fetches YouTube transcripts (calls Python)
├── TextPreprocessor.java            # Cleans HTML text
├── WordCounter.java                 # Counts keyword occurrences
├── WebPageResult.java               # Result data structure
├── fetch_youtube_transcript.py      # Python helper for YouTube API
├── requirements.txt                 # Python dependencies
├── setup_python.sh                  # Setup script for Python venv
├── compile_and_run.sh               # Convenience script to compile and run
├── test_youtube.sh                  # Test YouTube fetcher only
├── .venv/                           # Python virtual environment (created by setup)
├── QUICK_START.md                   # Quick start guide (繁體中文)
└── README.md                        # This file (detailed documentation)
```

**Note:** `.venv/` directory and `*.class` files are excluded by `.gitignore` at project root.

## Example Output

```
=== Fetching: https://www.iso.org/home.html ===
[INFO] Detected regular webpage - fetching HTML...

=== Fetching: https://youtu.be/a4cyMAIyWIQ ===
[INFO] Detected YouTube URL - fetching transcript...
[INFO] Extracted Video ID: a4cyMAIyWIQ
[INFO] Using Python from virtual environment: .venv/bin/python3
[SUCCESS] Transcript fetched successfully! Length: 2228 characters

====================================================
                Ranking Result
====================================================
# 1 | https://en.wikipedia.org/wiki/... | SCORE = 1510 | Counts = {ISO=236, Standard=186, ...}
# 2 | https://www.iso.org/home.html | SCORE = 286 | Counts = {ISO=41, Standard=38, ...}
# 3 | https://youtu.be/a4cyMAIyWIQ | SCORE = 115 | Counts = {ISO=16, Standard=17, ...}
...
```

## Troubleshooting

### Python virtual environment issues


**macOS/Linux:**
```bash
# Remove existing venv and recreate
rm -rf .venv
./setup_python.sh
```

**Windows (Git Bash)** 
```bash
rm -rf .venv
cat setup_python.sh \| sed 's/python3/python/g' \| bash

```
### Java compilation issues
This line works on Mac/Linux. On Windows, ensure Java is in PATH.
```bash
# Make sure JAVA_HOME is set
export JAVA_HOME=$(/usr/libexec/java_home)
export PATH=$JAVA_HOME/bin:$PATH
javac *.java
```

### YouTube "IP blocked" errors
- Wait 5-10 minutes before retrying
- Reduce the number of YouTube URLs in your test
- Consider using a VPN or proxy in production environments

## Development

To modify the keywords and weights, edit `Main.java`:

```java
// Define keywords
keywords.add("ISO");        // Weight: 4
keywords.add("Standard");   // Weight: 3
keywords.add("Sustain");    // Weight: 2
keywords.add("Certificate"); // Weight: 1

// Calculate weighted score
int score = (isoCount * 4) + (standardCount * 3) + 
            (sustainCount * 2) + (certificateCount * 1);
```

## License

This project is for educational purposes.