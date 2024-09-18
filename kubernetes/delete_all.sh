#!/bin/bash

kubectl patch pvc local-pvc -p '{"metadata":{"finalizers":null}}'
kubectl delete -f persistent-volume-claim.yaml --force --grace-period=0

kubectl patch pv persistent-volume-jars -p '{"metadata":{"finalizers":null}}'
kubectl delete -f persistent-volume.yaml --force --grace-period=0

# Loop through all .yaml files in the current directory
for file in *.yaml; do

  # Check if the filename contains 'persistent-volume' and skip
  if [[ "$file" == *persistent-volume* ]]; then
    continue
  fi

  # Check if the file exists (in case no .yaml files are found)
  if [[ -f "$file" ]]; then
    kubectl delete -f "$file"
  else
    echo "No .yaml files found in the current directory."
    break
  fi
done