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
packer validate -var-file="packer/template/dev.pkrvar.hcl" packer/template/webapp-gcp-custom-image.pkr.hcl

echo "Packer template validation completed successfully. Proceeding with the build..."