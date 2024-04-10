#!/bin/bash

# Fetch database credentials from instance metadata
DB_HOST=$(curl http://metadata.google.internal/computeMetadata/v1/instance/attributes/db_host -H "Metadata-Flavor: Google")
DB_NAME=$(curl "http://metadata.google.internal/computeMetadata/v1/instance/attributes/db_name" -H "Metadata-Flavor: Google")
DB_USER=$(curl "http://metadata.google.internal/computeMetadata/v1/instance/attributes/db_user" -H "Metadata-Flavor: Google")
DB_PASSWORD=$(curl "http://metadata.google.internal/computeMetadata/v1/instance/attributes/db_password" -H "Metadata-Flavor: Google")


# Write full configuration to the application.properties file
cat <<EOF > /opt/webapp/application.properties
server.port=8081
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.url=jdbc:postgresql://${DB_HOST}/${DB_NAME}
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.hikari.connection-timeout=5000
spring.jpa.hibernate.ddl-auto=update
spring.jpa.open-in-view=false
spring.web.resources.add-mappings=false
spring.jpa.show-sql=true
EOF

# Reload and start the webapp service
sudo systemctl daemon-reload
sudo systemctl enable webapp.service
sudo systemctl start webapp.service

# Restart the Google Cloud Ops Agent
sudo systemctl restart google-cloud-ops-agent