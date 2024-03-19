# Define the line numbers to remove
lines_to_remove="54,59d;73,74d"

# Input file name
input_file="pom.xml"

# Use sed to delete the specified lines in-place
sed -i.bak -e "$lines_to_remove" "$input_file"

# Remove the backup file created by sed (optional)
rm "${input_file}.bak"

echo "Lines removed successfully."
