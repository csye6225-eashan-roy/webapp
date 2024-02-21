#!/bin/bash

# Disable SELinux
echo "Disabling SELinux..."
sudo sed -i 's/SELINUX=enforcing/SELINUX=disabled/g' /etc/selinux/config
sudo setenforce 0

# Verify SELinux status
echo "SELinux status:"
sudo getenforce