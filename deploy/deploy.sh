#!/bin/bash
DIR=$(dirname "$(readlink -f "$0")")
sh "$DIR"/build.sh && sh "$DIR"/load.sh && docker logs -f cmy
