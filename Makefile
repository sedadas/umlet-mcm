TARGET:=development

ifeq ($(OS), Windows_NT)
	FRONTEND_DIR:="./mcm-frontend"
else
	FRONTEND_DIR:=$(shell pwd)/mcm-frontend
endif

ifeq ($(OS), Windows_NT)
	BACKEND_DIR:="./mcm-backend"
else
	BACKEND_DIR:=$(shell pwd)/mcm-backend
endif

ifeq ($(OS), Windows_NT)
	GRADLE_WRAPPER_COMMAND:="./gradlew"
else
	GRADLE_WRAPPER_COMMAND:=./gradlew
endif

all: frontend backend docker

export VITE_API_PORT=9081
export VITE_NODE_ENV=$(TARGET)

frontend:
	cd $(FRONTEND_DIR) && npm install
	cd $(FRONTEND_DIR) && npm run build

backend:
	cd $(BACKEND_DIR) && $(GRADLE_WRAPPER_COMMAND) :mcm-core:assemble
	cd $(BACKEND_DIR) && $(GRADLE_WRAPPER_COMMAND) :mcm-server:assemble

# COMPOSE_BAKE is broken on Windows on latest versions of docker compose.
# See https://github.com/docker/for-win/issues/14761.
ifeq ($(OS), Windows_NT)
docker:
	docker compose build
else
docker:
	COMPOSE_BAKE=true docker compose build
endif

push:
	docker compose push

run:
	docker compose up

lint:
	docker run --rm -v /var/run/docker.sock:/var/run/docker.sock:rw -v $(shell pwd):/tmp/lint:rw oxsecurity/megalinter:v8

test:
	cd $(BACKEND_DIR) && ./gradlew test
