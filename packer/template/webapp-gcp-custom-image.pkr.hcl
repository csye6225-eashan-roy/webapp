packer {
  required_plugins {
    googlecompute = {
      version = ">= 1.0.0"
      source  = "github.com/hashicorp/googlecompute"
    }
  }
}

locals {
  is_build_workflow = "${var.is_build_workflow}" == "true"
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

variable "vpc_network" {
  type = string
}

variable "database_user" {
  type = string
}

variable "database_password" {
  type = string
}

source "googlecompute" "webapp-image" {
  project_id          = var.project_id
  source_image_family = var.source_image_family
  zone                = var.zone
  machine_type        = var.vm_size
  ssh_username        = var.ssh_username
  #  ssh_timeout          = "20m"
  image_name   = "webapp-gcp-packer-image-${formatdate("YYYYMMDDHHmmss", timestamp())}"
  image_family = var.image_family
  #  use_internal_ip      = true
  #  wait_to_add_ssh_keys = "20s"
  network = var.vpc_network
  tags    = ["ssh-tag", "http-tag", "https-tag"]
}

build {
  sources = [
    "source.googlecompute.webapp-image"
  ]

  provisioner "shell" {
    name = "updates OS, installs dependencies"
    scripts = [
      #      "../scripts/os-update.sh",
      "packer/scripts/install-dependencies-and-setup.sh"
    ]
    environment_vars = [
      "DATABASE_USER=${var.database_user}",
      "DATABASE_PASSWORD=${var.database_password}"
    ]
  }

  provisioner "file" {
    only_if     = local.is_build_workflow
    name        = "copies build file generated by GitHub Actions workflow to Packer image"
    source      = "target/app-0.0.1-SNAPSHOT.jar"
    destination = "/tmp/application.jar"
  }

  provisioner "shell" {
    name = "moves application.jar from tmp to /opt/webapp; creates user and group, and makes it owner of the jar file"
    scripts = [
      "packer/scripts/move-jar-to-opt.sh",
      "packer/scripts/create-system-user-group.sh"
    ]
  }

  provisioner "file" {
    name        = "copies systemd service file to Packer image"
    source      = "packer/services/webapp.service"
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

