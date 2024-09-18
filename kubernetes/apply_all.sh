#!/bin/bash

# Loop through all .yaml files in the current directory
for file in *.yaml; do
  # Check if the file exists (in case no .yaml files are found)
  if [[ -f "$file" ]]; then
    kubectl apply -f "$file"
  else
    echo "No .yaml files found in the current directory."
    break
  fi
done
