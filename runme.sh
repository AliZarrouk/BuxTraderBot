#!/usr/bin/env bash

if ! [ -f "$1" ]; then
  echo "File does not exist"
  exit 1
fi

mvn package spring-boot:repackage
docker build . -t alibenzarrouk-trader-bot:1.0
docker run -e JAVAOPTS=-Dproduct-buy-sell-file=$1 alibenzarrouk-trader-bot:1.0