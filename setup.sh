#!/usr/bin/env bash
PROJECT=$project
FORK=${FORK:-"swagger-api/swagger-inflector/master"}

if [ "$PROJECT" == "" ]
then
  echo "set a project name like so:"
  echo ""
  echo "project=my-project"
  echo ""
  echo "and try again"
  exit
fi

echo "fetching setup files from $FORK"

# setup dirs
mkdir -p editor
mkdir -p src/main/swagger
mkdir -p src/main/webapp/WEB-INF

if [ ! -f editor/swagger-editor.war ]; then
  echo "...fetching editor webapp"
  curl -sL "https://raw.githubusercontent.com/$FORK/scripts/bin/swagger-editor.war" -o editor/swagger-editor.war
  curl -sL "https://raw.githubusercontent.com/$FORK/scripts/bin/jetty-runner.jar" -o editor/jetty-runner.jar
fi

echo "...fetching editor scripts"
curl -sL "https://raw.githubusercontent.com/$FORK/scripts/editor.sh" -o ./editor.sh

echo "...fetching sample swagger description"
wget --quiet --no-check-certificate "https://raw.githubusercontent.com/$FORK/scripts/openapi.yaml" -O src/main/swagger/openapi.yaml


echo "...fetching inflector configuration"
curl -sL "https://raw.githubusercontent.com/$FORK/scripts/inflector.yaml" -o ./inflector.yaml

echo "...fetching project pom"
curl -sL "https://raw.githubusercontent.com/$FORK/scripts/pom.xml" -o ./pom.xml

echo "...fetching web.xml"
curl -sL "https://raw.githubusercontent.com/$FORK/scripts/web.xml" -o src/main/webapp/WEB-INF/web.xml
chmod a+x ./editor.sh

rp="s/SAMPLE_PROJECT/$PROJECT/g"
sed -i -- $rp pom.xml
rm pom.xml--

echo "done!  You can run swagger editor as follows:"
echo "./editor.sh"
echo "then open a browser at open http://localhost:8000"

echo ""
echo "you can run your server as follows:"
echo "mvn package jetty:run"
echo ""
echo "and your swagger listing will be at http://localhost:8080/{basePath}/openapi.json"
echo ""
