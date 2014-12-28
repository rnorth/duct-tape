#!/bin/sh

set -x
set -e

./gradlew javadoc
rm -rf docs/javadoc
cp -R build/docs/javadoc docs/javadoc

pushd docs
asciidoctor *.adoc
popd