#!/usr/bin/env bash

if ! [ -f "$1" ]; then
  echo "File $1 does not exist"
  exit 1
fi

cp $1 ./input

mvn package spring-boot:repackage
docker build . -t alibenzarrouk-trader-bot:1.0
rm -f input
docker run alibenzarrouk-trader-bot:1.0
