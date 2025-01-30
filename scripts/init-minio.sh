#!/bin/bash

echo "Initializing MinIO..."

# Wait for MinIO to be ready
until curl -sf http://localhost:9000/minio/health/ready; do
    echo "Waiting for MinIO to be ready..."
    sleep 1
done

# Create warehouse bucket using AWS CLI with MinIO configuration
aws --endpoint-url http://localhost:9000 \
    --profile minio \
    s3 mb s3://warehouse \
    --region us-east-1

# Configure AWS CLI profile for MinIO
aws configure set aws_access_key_id minioadmin --profile minio
aws configure set aws_secret_access_key minioadmin --profile minio
aws configure set region us-east-1 --profile minio

echo "MinIO initialization completed"