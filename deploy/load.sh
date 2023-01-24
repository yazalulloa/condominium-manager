#!/bin/bash
DIR=$(dirname "$(readlink -f "$0")")
docker-compose -f "$DIR"/docker-compose.yml up -d --no-deps --build --remove-orphans
docker image prune -f