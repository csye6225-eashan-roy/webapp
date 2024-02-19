#!/bin/bash
set -e

# Create a new system group 'csye6225'
sudo groupadd -r csye6225

# Create a new system user 'csye6225' with the primary group 'csye6225' and no login shell
sudo useradd -r -g csye6225 -s /usr/sbin/nologin csye6225

# Recursively changes the ownership of all files and directories within /opt/webapp
sudo chown -R csye6225:csye6225 /opt/webapp