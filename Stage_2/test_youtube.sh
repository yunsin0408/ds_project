#!/bin/bash

# Test YouTube Transcript Fetcher Only
# ============================================================

echo "============================================================"
echo "  YouTube Transcript Fetcher - Test Script"
echo "============================================================"
echo ""

# Check if Java is installed
if ! command -v javac &> /dev/null; then
    echo " Error: Java compiler (javac) not found!"
    echo "Please install Java JDK 8 or above."
    exit 1
fi

echo "✓ Java found: $(java -version 2>&1 | head -n 1)"
echo ""

# Compile YouTubeTranscriptFetcher
echo " Compiling YouTubeTranscriptFetcher.java..."
javac YouTubeTranscriptFetcher.java

if [ $? -eq 0 ]; then
    echo "✓ Compilation successful!"
    echo ""
    
    # Run the test
    echo "============================================================"
    echo "  Running YouTube Transcript Fetcher Test..."
    echo "============================================================"
    echo ""
    java YouTubeTranscriptFetcher
    
else
    echo " Compilation failed!"
    exit 1
fi

echo ""
echo "============================================================"
echo "  Test Complete"
echo "============================================================"


