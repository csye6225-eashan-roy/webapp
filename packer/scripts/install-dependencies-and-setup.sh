#!/bin/bash
set -e
# Install java, maven, postgres
sudo dnf install java-17-openjdk-devel -y
sudo dnf install maven -y
sudo dnf module enable postgresql:14 -y
sudo dnf install postgresql-server -y

# Initialize and start PostgreSQL
sudo postgresql-setup --initdb
sudo systemctl start postgresql
sudo systemctl enable postgresql

# Configure PostgreSQL
sudo su - postgres -c "psql -d template1 -c \"ALTER USER postgres WITH PASSWORD 'E@s123h@n456';\""
sudo su - postgres -c "psql -U postgres -c \"CREATE USER \\\"${DATABASE_USER}\\\" WITH PASSWORD '${DATABASE_PASSWORD}';\""
sudo su - postgres -c "psql -U postgres -c \"CREATE DATABASE users;\""
sudo su - postgres -c "psql -U postgres -c \"ALTER DATABASE users OWNER TO \\\"${DATABASE_USER}\\\";\""

# Configure md5 authentication
sudo sed -i "s/ident/md5/g" /var/lib/pgsql/data/pg_hba.conf
sudo systemctl reload postgresql

# Set Java 17 as the default version
sudo update-alternatives --set java /usr/lib/jvm/java-17-openjdk-17.0.6.0.9-0.3.ea.el8.x86_64/bin/java

# Set JAVA_HOME for all users
echo 'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-17.0.6.0.9-0.3.ea.el8.x86_64' | sudo tee /etc/profile.d/jdk17.sh

# Add JAVA_HOME to PATH
echo 'export PATH=$JAVA_HOME/bin:$PATH' | sudo tee -a /etc/profile.d/jdk17.sh