#!/bin/bash

# Setup Python Virtual Environment for YouTube Transcript Fetching
# ================================================================

echo "============================================================"
echo "  Python Virtual Environment Setup"
echo "============================================================"
echo ""

# Check if Python 3 is installed
if ! command -v python3 &> /dev/null; then
    echo " Error: Python 3 not found!"
    echo "Please install Python 3.8 or above."
    exit 1
fi

echo "✓ Python found: $(python3 --version)"
echo ""

# Create virtual environment if it doesn't exist
if [ ! -d ".venv" ]; then
    echo " Creating virtual environment (.venv)..."
    python3 -m venv .venv
    
    if [ $? -eq 0 ]; then
        echo "✓ Virtual environment created successfully!"
    else
        echo " Failed to create virtual environment!"
        exit 1
    fi
else
    echo "✓ Virtual environment already exists"
fi

echo ""

# Activate virtual environment and install dependencies
echo " Installing dependencies from requirements.txt..."
source .venv/bin/activate

pip install --upgrade pip > /dev/null 2>&1
pip install -r requirements.txt

if [ $? -eq 0 ]; then
    echo "✓ Dependencies installed successfully!"
else
    echo " Failed to install dependencies!"
    deactivate
    exit 1
fi

echo ""
echo "============================================================"
echo "  Setup Complete!"
echo "============================================================"
echo ""
echo "Virtual environment is ready at: .venv/"
echo ""
echo "To activate manually:"
echo "  source .venv/bin/activate"
echo ""
echo "To deactivate:"
echo "  deactivate"
echo ""

deactivate


