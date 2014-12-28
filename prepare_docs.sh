#!/bin/sh

rm -rf docs/*

./gradlew javadoc
cp -R build/docs/javadoc docs/javadoc

asciidoctor README.adoc -o docs/index.html