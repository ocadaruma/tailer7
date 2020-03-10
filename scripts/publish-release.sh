#!/bin/bash

# A script to publish new version of tailer7

set -eu

if [ $TRAVIS != "true" ]; then
    exit 1
fi

cd $(dirname $0)/..

git config --global user.email "travis@travis-ci.com"
git config --global user.name "Travis CI"

# Publish the artifacts
./gradlew -P snapshot=false clean build publish

# Bump version in gradle.properties
VERSION=$(grep version gradle.properties | awk -F '=' '{printf $2}')
grep -v "version=$VERSION" gradle.properties > gradle.properties.tmp
cat gradle.properties.tmp > gradle.properties
rm gradle.properties.tmp
echo $VERSION | awk -F '.' '{print "version="$1"."$2"."$3+1}' >> gradle.properties

git checkout master
git add gradle.properties
git commit -m "$VERSION"

git push https://${GITHUB_TOKEN}@github.com/ocadaruma/tailer7.git master
