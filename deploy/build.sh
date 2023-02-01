#!/bin/bash

DIR=$(dirname "$(readlink -f "$0")")
. "$DIR"/env.conf
mkdir -p "$DIR"/tmp
cd "$DIR"/tmp || exit
curl -L "$URL" -H "Authorization: bearer $TOKEN" -o "$TAR_FILE"
dir_name=$(tar -tzf "$TAR_FILE" | head -1 | cut -f1 -d"/")
tar -xf master.tar.gz
ls
echo "$dir_name"
cd "$dir_name" || exit
ls
docker build -t cmy:latest .
rm -rf "$DIR"/tmp
