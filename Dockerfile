FROM maven:3.9.16-eclipse-temurin-8-noble

RUN curl -L https://raw.githubusercontent.com/swagger-api/swagger-inflector/v1/setup.sh | project=my-project bash

EXPOSE 8000
CMD ["bash","./editor.sh"]
