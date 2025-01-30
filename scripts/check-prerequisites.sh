#!/bin/bash

# ANSI color codes
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}🔍 Checking prerequisites...${NC}"

# Check Java version
if ! command -v java &> /dev/null; then
    echo -e "${RED}❌ Java is not installed${NC}"
    exit 1
else
    # Get Java version and ensure it's a number
    java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d'.' -f1)
    if [[ ! "$java_version" =~ ^[0-9]+$ ]]; then
        echo -e "${RED}❌ Could not determine Java version${NC}"
        exit 1
    fi

    if [ "$java_version" -lt "21" ]; then
        echo -e "${RED}❌ Java 21 or later is required (found version $java_version)${NC}"
        exit 1
    else
        echo -e "${GREEN}✅ Java $java_version is installed${NC}"
    fi
fi

# Check Docker
if ! command -v docker &> /dev/null; then
    echo -e "${RED}❌ Docker is not installed${NC}"
    exit 1
else
    echo -e "${GREEN}✅ Docker is installed${NC}"
fi

# Check Docker Compose
if ! command -v docker-compose &> /dev/null; then
    echo -e "${RED}❌ Docker Compose is not installed${NC}"
    exit 1
else
    echo -e "${GREEN}✅ Docker Compose is installed${NC}"
fi

# Check Make
if ! command -v make &> /dev/null; then
    echo -e "${RED}❌ Make is not installed${NC}"
    exit 1
else
    echo -e "${GREEN}✅ Make is installed${NC}"
fi

# Check if Gradle wrapper exists
if [ ! -f "./gradlew" ]; then
    echo -e "${RED}❌ Gradle wrapper not found${NC}"
    exit 1
else
    echo -e "${GREEN}✅ Gradle wrapper is present${NC}"
fi

# Check if Docker daemon is running
if ! docker info &> /dev/null; then
    echo -e "${RED}❌ Docker daemon is not running${NC}"
    exit 1
else
    echo -e "${GREEN}✅ Docker daemon is running${NC}"
fi

# Check AWS CLI
if ! command -v aws &> /dev/null; then
    echo -e "${RED}❌ AWS CLI is not installed${NC}"
    echo -e "${YELLOW}Please install AWS CLI:${NC}"
    echo -e "${YELLOW}- macOS: brew install awscli${NC}"
    echo -e "${YELLOW}- Linux: sudo apt-get install awscli${NC}"
    echo -e "${YELLOW}- Windows: https://aws.amazon.com/cli/${NC}"
    exit 1
else
    echo -e "${GREEN}✅ AWS CLI is installed${NC}"
fi

echo -e "${GREEN}✅ All prerequisites are satisfied!${NC}"
