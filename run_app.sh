#!/bin/bash
echo "Installing dependencies..."
pip install -r requirements.txt

echo "Starting ConsuCalcu Pro..."
python3 main.py
