#!/usr/bin/env bash
java -Dconfig=editor/inflector.yaml -jar editor/jetty-runner.jar --port 8000 editor/swagger-editor.war