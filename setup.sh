PROJECT=$project

if [ "$PROJECT" == "" ]
then
  echo "set a project name like so:"
  echo ""
  echo "project=my-project"
  echo ""
  echo "and try again"
  exit
fi

# setup dirs
mkdir -p editor
mkdir -p src/main/swagger
mkdir -p src/main/webapp/WEB-INF

if [ ! -f editor/swagger-editor.war ]; then
  echo "...fetching editor webapp"
  curl "https://github.com/swagger-api/swagger-inflector/raw/master/scripts/bin/swagger-editor.war" -o editor/swagger-editor.war
fi

echo "...fetching editor scripts"
curl "https://github.com/swagger-api/swagger-inflector/raw/master/scripts/editor.xml" -o editor/editor.xml
curl "https://github.com/swagger-api/swagger-inflector/raw/master/scripts/editor.sh" -o ./editor.sh

echo "...fetching sample swagger description"

curl "https://raw.githubusercontent.com/swagger-api/swagger-inflector/master/scripts/openapi.yaml" -o src/main/swagger/openapi.yaml


echo "...fetching inflector configuration"
curl "https://raw.githubusercontent.com/swagger-api/swagger-inflector/master/scripts/inflector.yaml" -o ./inflector.yaml

echo "...fetching project pom"
curl "https://raw.githubusercontent.com/swagger-api/swagger-inflector/master/scripts/pom.xml" -o ./pom.xml

echo "...fetching web.xml"
curl "https://raw.githubusercontent.com/swagger-api/swagger-inflector/master/scripts/web.xml" -o src/main/webapp/WEB-INF/web.xml
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
