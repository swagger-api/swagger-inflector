#!/bin/bash

CUR=$(pwd)
TMPDIR="$(dirname -- "${0}")"

SC_RELEASE_TAG="v$SC_VERSION"

#####################
### update the version to next snapshot in maven project with set version
#####################
mvn versions:set -DnewVersion="${SC_NEXT_VERSION}-SNAPSHOT"
mvn versions:commit

#####################
### update all other versions in files around to the new release, including readme ###
#####################
sc_find="<version>$SC_LAST_RELEASE"
sc_replace="<version>$SC_VERSION"
sed -i -e "s/$sc_find/$sc_replace/g" $CUR/README.md

sc_find="<version>$SC_VERSION-SNAPSHOT<\/version>"
sc_replace="<version>$SC_VERSION<\/version>"
sed -i -e "s/$sc_find/$sc_replace/g" $CUR/scripts/pom.xml

