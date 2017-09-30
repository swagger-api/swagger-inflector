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
  wget --quiet --no-check-certificate "https://github.com/swagger-api/swagger-inflector/raw/2.0/scripts/bin/swagger-editor.war" -O editor/swagger-editor.war
fi

echo "...fetching editor scripts"
wget --quiet --no-check-certificate "https://github.com/swagger-api/swagger-inflector/raw/2.0/scripts/editor.xml" -O editor/editor.xml
wget --quiet --no-check-certificate "https://github.com/swagger-api/swagger-inflector/raw/2.0/scripts/editor.sh" -O ./editor.sh

echo "...fetching sample swagger description"
wget --quiet --no-check-certificate "https://raw.githubusercontent.com/swagger-api/swagger-inflector/2.0/scripts/swagger.yaml" -O src/main/swagger/swagger.yaml

echo "...fetching inflector configuration"
wget --quiet --no-check-certificate "https://raw.githubusercontent.com/swagger-api/swagger-inflector/2.0/scripts/inflector.yaml" -O ./inflector.yaml

echo "...fetching project pom"
wget --quiet --no-check-certificate "https://raw.githubusercontent.com/swagger-api/swagger-inflector/2.0/scripts/pom.xml" -O ./pom.xml

echo "...fetching web.xml"
wget --quiet --no-check-certificate "https://raw.githubusercontent.com/swagger-api/swagger-inflector/2.0/scripts/web.xml" -O src/main/webapp/WEB-INF/web.xml
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
echo "and your swagger listing will be at http://localhost:8080/{basePath}/swagger.json"
echo ""