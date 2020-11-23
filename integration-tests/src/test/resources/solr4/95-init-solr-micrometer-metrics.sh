#!/bin/bash

echo "Calling metrics url after 10 seconds: sleep 10 && curl -s \"http://localhost:8080/${SOLR_ENDPOINT}/alfresco/metrics\" -o /tmp/out"
sh -c "sleep 10 && curl -s \"http://localhost:8080/${SOLR_ENDPOINT}/alfresco/metrics\" -o /tmp/out" &