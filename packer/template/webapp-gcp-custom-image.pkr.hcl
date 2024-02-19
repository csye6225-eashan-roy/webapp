packer {
  required_plugins {
    googlecompute = {
      version = ">= 1.0.0"
      source  = "github.com/hashicorp/googlecompute"
    }
  }
}

variable "project_id" {
  type = string
}

variable "ssh_username" {
  type = string
}

variable "image_family" {
  type = string
}

variable "source_image_family" {
  type = string
}

variable "zone" {
  type = string
}

variable "vm_size" {
  type = string
}

#variable "gcp_service_account_file" {
#  type    = string
#}

source "googlecompute" "webapp-image" {
  project_id           = var.project_id
  source_image_family  = var.source_image_family
  zone                 = var.zone
  machine_type         = var.vm_size
  ssh_username         = var.ssh_username
#  ssh_timeout          = "20m"
  image_name           = "packer-image-${formatdate("YYYYMMDDHHmmss", timestamp())}"
  image_family         = var.image_family
#  use_internal_ip      = true
#  wait_to_add_ssh_keys = "20s"
  network              = "default"
  tags                 = ["webapp"]
}

build {
  sources = [
    "source.googlecompute.webapp-image"
  ]

  provisioner "shell" {
    name = "updates OS, installs dependencies"
    scripts = [
#      "../scripts/os-update.sh",
      "../scripts/install-dependencies-and-setup.sh"
    ]
  }

  provisioner "file" {
    name        = "copies build file to Packer image"
    source      = "../../target/CloudNativeApplication-0.0.1-SNAPSHOT.jar"
    destination = "/tmp/application.jar"
  }

  provisioner "shell" {
    name = "moves application.jar from tmp to /opt/webapp; creates user and group, and makes it owner of the jar file"
    scripts = [
      "../scripts/move-jar-to-opt.sh",
      "../scripts/create-system-user-group.sh"
    ]
  }

  provisioner "file" {
    name        = "copies systemd service file to Packer image"
    source      = "../services/webapp.service"
    destination = "/tmp/webapp.service"
#    destination = "/etc/systemd/system/webapp.service"   --permission denied issue
  }

  provisioner "shell" {
    inline = [
      "sudo mv /tmp/webapp.service /etc/systemd/system/webapp.service",
      "sudo systemctl daemon-reload",
      "sudo systemctl enable webapp.service"
    ]
  }

}
