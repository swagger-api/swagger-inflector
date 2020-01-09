FROM maven

RUN curl -L https://raw.githubusercontent.com/swagger-api/swagger-inflector/v1/setup.sh | project=my-project bash

EXPOSE 8000
CMD ["bash","./editor.sh"]
