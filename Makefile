# Data DeLorean - Bi-directional Data Flow Demo
# This Makefile provides commands for managing the Data DeLorean demo project.
# Use 'make help' to see all available commands.
# For quick setup, use 'make quick-start'.

# Shell configuration
SHELL := /bin/bash
.SHELLFLAGS := -eu -o pipefail -c

# Colors and formatting
BLUE := $(shell printf '\033[34m')
GREEN := $(shell printf '\033[32m')
RED := $(shell printf '\033[31m')
YELLOW := $(shell printf '\033[33m')
BOLD := $(shell printf '\033[1m')
RESET := $(shell printf '\033[0m')

# Project settings
PROJECT_NAME := data-delorean
GRADLE := ./gradlew

.PHONY: help setup build run stop clean test demo quick-start verify

help: ## 📚 Show this help message
	@printf '${BLUE}🚀 ${PROJECT_NAME} Management Commands${RESET}\n'
	@printf '${YELLOW}Usage: make [target]${RESET}\n\n'
	@printf '${BOLD}Available Commands:${RESET}\n'
	@printf '${YELLOW}Setup & Running:${RESET}\n'
	@grep -E '^(quick-start|setup|build|run|stop|restart):.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "${BLUE}  %-28s${RESET} %s\n", $$1, $$2}'
	@printf '\n${YELLOW}Development & Testing:${RESET}\n'
	@grep -E '^(test|demo|monitor|logs):.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "${BLUE}  %-28s${RESET} %s\n", $$1, $$2}'
	@printf '\n${YELLOW}Maintenance & Verification:${RESET}\n'
	@grep -E '^(clean|check-status|verify):.*?## .*$$' $(MAKEFILE_LIST) | awk 'BEGIN {FS = ":.*?## "}; {printf "${BLUE}  %-28s${RESET} %s\n", $$1, $$2}'

setup: verify ## 🔧 Setup local development environment
	@printf '${BLUE}🔧 Setting up development environment...${RESET}\n'
	@chmod +x scripts/check-prerequisites.sh
	@./scripts/check-prerequisites.sh
	@printf '${YELLOW}Pulling required Docker images...${RESET}\n'
	@docker-compose pull
	@printf '${GREEN}✅ Environment setup complete${RESET}\n\n'
	@printf '${BOLD}Next steps:${RESET}\n'
	@printf '  ${BLUE}1. make build${RESET}    - Build the project\n'
	@printf '  ${BLUE}2. make run${RESET}      - Start all services\n'
	@printf '  ${BLUE}3. make demo${RESET}     - Run demo scenarios\n'
	@printf '\nFor more commands, run: ${BLUE}make help${RESET}\n'

build: ## 🏗️  Build the project
	@printf '${BLUE}🏗️  Building project...${RESET}\n'
	@$(GRADLE) build
	@printf '${GREEN}✅ Build complete${RESET}\n'

run: ## ▶️  Start all services and the application
	@printf '${BLUE}🚀 Starting services...${RESET}\n'
	@docker-compose up -d
	@printf '${BLUE}⏳ Waiting for services to be ready...${RESET}\n'
	@for i in {1..30}; do \
		printf "${YELLOW}⏳ Checking services (Attempt $$i/30)${RESET}\r"; \
		MINIO_READY=0; KAFKA_READY=0; \
		if curl -s -I http://localhost:9001/minio/health/ready >/dev/null 2>&1; then \
			MINIO_READY=1; \
		fi; \
		if docker-compose exec -T kafka kafka-topics --list --bootstrap-server localhost:9092 >/dev/null 2>&1; then \
			KAFKA_READY=1; \
		fi; \
		if [ $$MINIO_READY -eq 1 ] && [ $$KAFKA_READY -eq 1 ]; then \
			printf "\n${GREEN}✅ All services are ready${RESET}\n"; \
			break; \
		fi; \
		if [ $$i -eq 30 ]; then \
			printf "\n${RED}❌ Timeout waiting for services${RESET}\n"; \
			exit 1; \
		fi; \
		sleep 2; \
	done
	@printf '${BLUE}🔧 Initializing services...${RESET}\n'
	@printf '${YELLOW}Initializing Kafka...${RESET}\n'
	@printf "${YELLOW}Creating customer-events topic...${RESET}\n"
	@if docker-compose exec -T kafka kafka-topics --create --if-not-exists \
		--bootstrap-server localhost:9092 \
		--topic customer-events \
		--partitions 1 \
		--replication-factor 1 >/dev/null 2>&1; then \
		printf "${GREEN}✓${RESET} customer-events topic ready\n"; \
	else \
		printf "${RED}❌ Failed to create customer-events topic${RESET}\n" && exit 1; \
	fi
	@printf "${YELLOW}Creating analytical-insights topic...${RESET}\n"
	@if docker-compose exec -T kafka kafka-topics --create --if-not-exists \
		--bootstrap-server localhost:9092 \
		--topic analytical-insights \
		--partitions 1 \
		--replication-factor 1 >/dev/null 2>&1; then \
		printf "${GREEN}✓${RESET} analytical-insights topic ready\n"; \
	else \
		printf "${RED}❌ Failed to create analytical-insights topic${RESET}\n" && exit 1; \
	fi
	@printf "${GREEN}✅ Kafka initialized successfully${RESET}\n"
	@printf '${YELLOW}Initializing MinIO...${RESET}\n'
	@for i in {1..10}; do \
		if docker run --rm --network host minio/mc alias set local http://localhost:9000 minioadmin minioadmin >/dev/null 2>&1; then \
			printf "${GREEN}✓${RESET} MinIO client configured\n"; \
			break; \
		fi; \
		if [ $$i -eq 10 ]; then \
			printf "${RED}❌ Failed to configure MinIO client${RESET}\n"; \
			exit 1; \
		fi; \
		printf "${YELLOW}⏳ Waiting for MinIO to be ready ($$i/10)${RESET}\r"; \
		sleep 2; \
	done
	@printf "${YELLOW}Creating warehouse bucket...${RESET}\n"
	@docker run --rm --network host minio/mc mb local/warehouse >/dev/null 2>&1 || \
		printf "${YELLOW}ℹ️  Bucket 'warehouse' already exists${RESET}\n"
	@printf "${YELLOW}Setting bucket policy...${RESET}\n"
	@docker run --rm --network host minio/mc policy set public local/warehouse >/dev/null 2>&1 || \
		(printf "${RED}❌ Failed to set bucket policy${RESET}\n" && exit 1)
	@printf "${GREEN}✅ MinIO initialized successfully${RESET}\n"
	@printf '${BLUE}▶️  Starting application...${RESET}\n'
	@$(GRADLE) bootRun & \
	APP_PID=$$!; \
	trap 'kill $$APP_PID 2>/dev/null' EXIT; \
	printf "${YELLOW}⏳ Waiting for application to be ready...${RESET}\n"; \
	for i in {1..30}; do \
		if curl -s http://localhost:8080/actuator/health | grep -q "UP"; then \
			printf "${GREEN}✅ Application is ready${RESET}\n"; \
			trap - EXIT; \
			break; \
		fi; \
		if [ $$i -eq 30 ]; then \
			printf "${RED}❌ Application failed to start${RESET}\n"; \
			exit 1; \
		fi; \
		printf "${YELLOW}⏳ Waiting for application ($$i/30)${RESET}\r"; \
		sleep 2; \
	done; \
	wait $$APP_PID || \
		(printf "${RED}❌ Application terminated unexpectedly${RESET}\n" && exit 1)

stop: ## ⏹️  Stop all services
	@printf '${BLUE}🛑 Stopping services...${RESET}\n'
	@docker-compose down
	@printf '${GREEN}✅ All services stopped${RESET}\n'

clean: stop ## 🧹 Clean up all resources
	@printf '${BLUE}🧹 Cleaning up...${RESET}\n'
	@docker-compose down -v
	@$(GRADLE) clean
	@rm -rf build/
	@printf '${GREEN}✅ Cleanup complete${RESET}\n'

test: ## 🧪 Run tests
	@printf '${BLUE}🧪 Running tests...${RESET}\n'
	@$(GRADLE) test
	@printf '${GREEN}✅ Tests complete${RESET}\n'

demo: ## 🎮 Run demo scenarios
	@printf '${BLUE}🎮 Running demo scenarios...${RESET}\n'
	@printf '${YELLOW}Scenario 1: Schema Evolution${RESET}\n'
	@curl -s -X POST http://localhost:8080/demo/schema-evolution || printf "${RED}Failed to run schema evolution demo${RESET}\n"
	@printf '\n${YELLOW}Scenario 2: High Volume Processing${RESET}\n'
	@curl -s -X POST http://localhost:8080/demo/high-volume || printf "${RED}Failed to run high volume demo${RESET}\n"
	@printf '\n${YELLOW}Scenario 3: Error Handling${RESET}\n'
	@curl -s -X POST http://localhost:8080/demo/error-handling || printf "${RED}Failed to run error handling demo${RESET}\n"
	@printf '\n${GREEN}✅ Demo scenarios complete${RESET}\n'

monitor: ## 📊 Open monitoring dashboards
	@printf '${BLUE}📊 Opening monitoring dashboards...${RESET}\n'
	@printf '${YELLOW}Prometheus: http://localhost:9090${RESET}\n'
	@printf '${YELLOW}MinIO Console: http://localhost:9001${RESET}\n'
	@printf '${GREEN}✅ Use these URLs to access monitoring dashboards${RESET}\n'

logs: ## 📋 View application logs
	@printf '${BLUE}📋 Viewing logs...${RESET}\n'
	@docker-compose logs -f

verify: ## ✅ Verify system requirements and configuration
	@printf '${BLUE}🔍 Verifying system requirements...${RESET}\n'
	@printf '\n${YELLOW}1. Required Tools${RESET}\n'
	@# Check Java
	@printf "Checking Java...            "
	@if java -version 2>&1 | head -n 1 | grep -q 'version "23'; then \
		VERSION=$$(java -version 2>&1 | head -n 1 | awk -F '"' '{print $$2}'); \
		printf "${GREEN}✓${RESET} (v$$VERSION)\n"; \
	else \
		printf "${RED}❌ Java 23 is required${RESET}\n" && exit 1; \
	fi
	@# Check Docker
	@printf "Checking Docker...          "
	@if docker version --format '{{.Server.Version}}' | grep -q '^2[3-9]\.'; then \
		VERSION=$$(docker version --format '{{.Server.Version}}'); \
		printf "${GREEN}✓${RESET} (v$$VERSION)\n"; \
	else \
		printf "${RED}❌ Docker version 23+ is required${RESET}\n" && exit 1; \
	fi
	@# Check Docker Compose
	@printf "Checking Docker Compose...   "
	@if docker-compose version --short | grep -q '^2\.'; then \
		VERSION=$$(docker-compose version --short); \
		printf "${GREEN}✓${RESET} (v$$VERSION)\n"; \
	else \
		printf "${RED}❌ Docker Compose version 2+ is required${RESET}\n" && exit 1; \
	fi
	@# Check netcat
	@printf "Checking netcat...          "
	@if which nc >/dev/null 2>&1; then \
		if nc -h 2>&1 | grep -q "GNU netcat"; then \
			VERSION="GNU netcat"; \
		else \
			VERSION="BSD netcat"; \
		fi; \
		printf "${GREEN}✓${RESET} ($$VERSION)\n"; \
	else \
		printf "${RED}❌ netcat is required for port checking${RESET}\n" && exit 1; \
	fi

	@printf '\n${YELLOW}2. Port Availability${RESET}\n'
	@printf '${BLUE}Required ports:${RESET}\n'
	@printf '  - 9092: Kafka broker\n'
	@printf '  - 9000: MinIO S3 API\n'
	@printf '  - 9001: MinIO Console\n'
	@printf '  - 8080: Spring Boot application\n\n'
	@printf "Checking port 9092 (Kafka)...     "
	@nc -z localhost 9092 2>/dev/null && printf "${RED}❌ In use${RESET}\n" || printf "${GREEN}✓${RESET}\n"
	@printf "Checking port 9000 (MinIO)...     "
	@nc -z localhost 9000 2>/dev/null && printf "${RED}❌ In use${RESET}\n" || printf "${GREEN}✓${RESET}\n"
	@printf "Checking port 9001 (MinIO UI)...  "
	@nc -z localhost 9001 2>/dev/null && printf "${RED}❌ In use${RESET}\n" || printf "${GREEN}✓${RESET}\n"
	@printf "Checking port 8080 (App)...       "
	@nc -z localhost 8080 2>/dev/null && printf "${RED}❌ In use${RESET}\n" || printf "${GREEN}✓${RESET}\n"

	@printf '\n${YELLOW}3. Configuration Files${RESET}\n'
	@printf "Checking docker-compose.yml...   "
	@if test -f docker-compose.yml; then \
		if test -r docker-compose.yml; then \
			printf "${GREEN}✓${RESET} (readable)\n"; \
		else \
			printf "${RED}❌ Not readable${RESET}\n" && exit 1; \
		fi \
	else \
		printf "${RED}❌ Not found${RESET}\n" && exit 1; \
	fi
	@printf "Checking build.gradle.kts...    "
	@if test -f build.gradle.kts; then \
		if test -r build.gradle.kts; then \
			printf "${GREEN}✓${RESET} (readable)\n"; \
		else \
			printf "${RED}❌ Not readable${RESET}\n" && exit 1; \
		fi \
	else \
		printf "${RED}❌ Not found${RESET}\n" && exit 1; \
	fi

	@printf '\n${GREEN}✅ All system requirements verified${RESET}\n'

check-status: ## 🔍 Check status of all services
	@printf '${BLUE}📊 Checking service status...${RESET}\n'
	@printf '${YELLOW}Docker Containers:${RESET}\n'
	@docker-compose ps
	@printf '\n${YELLOW}Application Health:${RESET}\n'
	@curl -s http://localhost:8080/actuator/health && printf "${GREEN}✅ Application is healthy${RESET}\n" || printf "${RED}❌ Application is not running${RESET}\n"
	@printf '\n${YELLOW}Kafka Topics:${RESET}\n'
	@docker-compose exec kafka kafka-topics --list --bootstrap-server localhost:9092 2>/dev/null && printf "${GREEN}✅ Kafka is running${RESET}\n" || printf "${RED}❌ Kafka is not running${RESET}\n"
	@printf '\n${YELLOW}MinIO Status:${RESET}\n'
	@curl -s -I http://localhost:9001 >/dev/null && printf "${GREEN}✅ MinIO is running${RESET}\n" || printf "${RED}❌ MinIO is not running${RESET}\n"
	@printf '\n${GREEN}Status check complete${RESET}\n'

restart: stop run ## 🔄 Restart all services and the application

quick-start: ## 🚀 Quick start for first-time users (recommended)
	@printf '${BLUE}🚀 Starting Data DeLorean quick setup...${RESET}\n'
	@printf '${YELLOW}This will set up and start all services.${RESET}\n'
	@printf '${YELLOW}Estimated time: 3-5 minutes${RESET}\n\n'
	@$(MAKE) setup
	@printf '\n${YELLOW}Building project...${RESET}\n'
	@$(MAKE) build
	@printf '\n${YELLOW}Starting services...${RESET}\n'
	@$(MAKE) run
	@printf '\n${GREEN}✨ Setup complete! Here are some useful commands:${RESET}\n'
	@printf '${BLUE}make demo${RESET}     - Run demo scenarios\n'
	@printf '${BLUE}make monitor${RESET}   - View monitoring dashboards\n'
	@printf '${BLUE}make logs${RESET}      - View application logs\n'
	@printf '${BLUE}make help${RESET}      - Show all available commands\n'

.DEFAULT_GOAL := help
