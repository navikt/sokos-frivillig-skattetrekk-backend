#!/bin/bash

# Ensure user is authenicated, and run login if not.
gcloud auth print-identity-token &> /dev/null
if [ $? -gt 0 ]; then
    gcloud auth login
fi

# Suppress kubectl config output
kubectl config use-context dev-fss
kubectl config set-context --current --namespace=okonomi

# Get pod name
POD_NAME=$(kubectl get pods --no-headers | grep sokos-frivillig-skattetrekk-backend-q2 | head -n1 | awk '{print $1}')

if [ -z "$POD_NAME" ]; then
    echo "Error: No sokos-frivillig-skattetrekk-backend-q2 pod found" >&2
    exit 1
fi

echo "Fetching environment variables from pod: $POD_NAME"

# Get system variables
envValue=$(kubectl exec "$POD_NAME" -c sokos-frivillig-skattetrekk-backend-q2 -- env | egrep "^AZURE|^TREKK|^TOKEN_X" | sort)

# Set local environment variables
rm -f defaults.properties
echo "$envValue" > defaults.properties
echo "Environment variables saved to defaults.properties"

rm -f privateKey
echo "$PRIVATE_KEY" > privateKey

