#!/bin/bash
set -e

echo "Initialising Packer..."
packer init packer/template/webapp-gcp-custom-image.pkr.hcl

echo "Checking if Packer templates require formatting..."
if packer fmt -check -diff -recursive packer; then
  echo "All Packer templates are correctly formatted."
else
  echo "Packer templates require formatting. Please run 'packer fmt -recursive packer' locally and commit the changes."
  exit 1
fi

if [[ -n "$GCLOUD_CREDENTIALS" ]]; then
  echo "Setting up Google Cloud credentials..."
  echo "$GCLOUD_CREDENTIALS" > /tmp/gcloud_credentials.json
  export GOOGLE_APPLICATION_CREDENTIALS=/tmp/gcloud_credentials.json
fi

echo "Validating Packer templates..."
packer validate \
  -var "project_id=${PROJECT_ID}" \
  -var "ssh_username=packer" \
  -var "vm_size=e2-small" \
  -var "image_family=custom-centos-8-image-webapp" \
  -var "source_image_family=centos-stream-8" \
  -var "zone=us-central1-a" \
  -var "vpc_network=default" \
  packer/template/webapp-gcp-custom-image.pkr.hcl

echo "Packer template validation completed successfully. Proceeding with the build..."