#!/bin/bash
# ============================================================
# Packages the backend for Elastic Beanstalk deployment.
# Run from the project root: bash scripts/deploy-backend.sh
# ============================================================

set -e

echo "Building backend JAR..."
cd backend
mvn package -DskipTests -q

echo "Creating EB deployment package..."
# EB needs: Dockerfile, Dockerrun.aws.json, .ebextensions/, and the JAR
mkdir -p ../dist
zip -r ../dist/copilot-backend.zip \
  Dockerfile \
  Dockerrun.aws.json \
  .ebextensions/ \
  target/*.jar

cd ..
echo "Package ready: dist/copilot-backend.zip"
echo ""
echo "Next: eb deploy  (from the backend/ directory)"
