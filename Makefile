TARGET:=development
FRONTEND_DIR:=$(shell pwd)/mcm-frontend
BACKEND_DIR:=$(shell pwd)/mcm-backend
all: frontend backend docker

export VITE_API_PORT=8081
export VITE_NODE_ENV=$(TARGET)

frontend:
	cd $(FRONTEND_DIR) && npm install
	cd $(FRONTEND_DIR) && npm run build

backend:
	cd $(BACKEND_DIR) && ./gradlew :mcm-core:assemble 
	cd $(BACKEND_DIR) && ./gradlew :mcm-server:assemble

docker:
	COMPOSE_BAKE=true docker compose build

run:
	docker compose up

login:
	docker login registry.reset.inso-w.at

lint:
	docker run --rm -v /var/run/docker.sock:/var/run/docker.sock:rw -v $(shell pwd):/tmp/lint:rw oxsecurity/megalinter:v8

test:
	cd $(BACKEND_DIR) && ./gradlew test
