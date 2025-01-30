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
    java_version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | awk -F '.' '{print $1}')
    if [ "$java_version" -lt "23" ]; then
        echo -e "${RED}❌ Java 23 or later is required (found version $java_version)${NC}"
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

echo -e "${GREEN}✅ All prerequisites are satisfied!${NC}"