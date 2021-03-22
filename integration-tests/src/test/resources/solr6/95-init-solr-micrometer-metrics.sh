#!/bin/bash

  if [ "${ALFRED_TELEMETRY_EXPORT_GRAPHITE_ENABLED}" = true ]
  then
   if [ none = "${ALFRESCO_SSL}" ]
   then
    echo "Calling metrics url after 20 seconds: sleep 20 && curl -s \"http://localhost:8080/${SOLR_ENDPOINT}/alfresco/metrics\" -o /dev/null"
    sh -c "sleep 20 && curl -s \"http://localhost:8080/${SOLR_ENDPOINT}/alfresco/metrics\" -o /dev/null" &
   else
    echo "Calling metrics url after 20 seconds: sleep 20 && curl -f -k -L -s -E ${SOLR_DIR_ROOT}/keystore/browser.pem \"https://localhost:8443/${SOLR_ENDPOINT}/alfresco/metrics\" -o /dev/null"
    sh -c "sleep 20 && curl -f -k -L -s -E \"${SOLR_INSTALL_HOME}/keystore/browser.pem\" \"https://localhost:8443/${SOLR_ENDPOINT}/alfresco/metrics\" -o /dev/null" &
   fi
  fi
