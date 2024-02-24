#!/bin/bash

csv_file="../target/pmd.csv"

# Check if the file exists
if [ -e "$csv_file" ]; then
    # Print the contents of the CSV file
python3 -c "
import csv
import sys

with open('$csv_file', 'r') as csvfile:
    reader = csv.reader(csvfile)
    header = next(reader)  # Skip the header if needed

    exit_flag=0

    # Define ANSI escape codes for text formatting
    RED='\033[0;31m'      # Red text
    YELLOW='\033[1;33m'   # Yellow text (bright)
    RESET='\033[0m'       # Reset text formatting

    for row in reader:
        column1 = row[0].replace('\"', '')  # Remove double quotes
        column2 = row[1].replace('\"', '')  # Remove double quotes

        print(f'{column1}. \nPackage: {RED}{column2}{RESET}\nFile: \"{row[2]}\"\nPriority: {row[3]}\nLine: {row[4]}\nDescription: {row[5]}\nType: {YELLOW}{row[6]} - {row[7]}{RESET}\n\n'+ '*'*20)

        # Check if high priority violation
        if row[3] == '1' or row[3] == '2':
            exit_flag=1
    if exit_flag:
        sys.exit(1)
    "
else
    echo "Error: CSV file not found at $csv_file"
fi