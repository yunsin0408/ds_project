#!/bin/bash

# Compile and Run Script for Web Analyzer with YouTube Support
# ============================================================

echo "============================================================"
echo "  Web Analyzer with YouTube Transcript Support"
echo "============================================================"
echo ""

# Check if Python virtual environment is set up
if [ ! -d ".venv" ]; then
    echo "  Warning: Python virtual environment not found!"
    echo "Setting up Python environment..."
    ./setup_python.sh
    if [ $? -ne 0 ]; then
        echo " Failed to set up Python environment!"
        exit 1
    fi
    echo ""
fi

# Check if Java is installed
if ! command -v javac &> /dev/null; then
    echo " Error: Java compiler (javac) not found!"
    echo "Please install Java JDK 8 or above."
    echo "Visit: https://www.oracle.com/java/technologies/downloads/"
    exit 1
fi

echo "✓ Java found: $(java -version 2>&1 | head -n 1)"
echo "✓ Python venv found: .venv/"
echo ""

# Clean up old class files
echo " Cleaning up old compiled files..."
rm -f *.class
echo ""

# Compile all Java files
echo " Compiling Java files..."
javac *.java

if [ $? -eq 0 ]; then
    echo "✓ Compilation successful!"
    echo ""
    
    # Run the main program
    echo "============================================================"
    echo "  Running Web Analyzer..."
    echo "============================================================"
    echo ""
    java Main
    
else
    echo " Compilation failed!"
    echo "Please check the error messages above."
    exit 1
fi

echo ""
echo "============================================================"
echo "  Execution Complete"
echo "============================================================"

