#!/bin/bash

# Install the Google Cloud Ops Agent
curl -sSO https://dl.google.com/cloudagents/add-google-cloud-ops-agent-repo.sh
sudo bash add-google-cloud-ops-agent-repo.sh --also-install

sudo touch /var/log/webapp/webapp.log
sudo chown -R csye6225:csye6225 /var/log/webapp/webapp.log