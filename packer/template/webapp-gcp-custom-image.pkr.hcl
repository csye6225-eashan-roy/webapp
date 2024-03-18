packer {
  required_plugins {
    googlecompute = {
      version = ">= 1.0.0"
      source  = "github.com/hashicorp/googlecompute"
    }
  }
}

variable "jar_file" {
  type    = string
  default = "packer/template/dummy.jar" # This is the name of the placeholder file
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
  type    = string
  default = "dbuser"
}

variable "database_password" {
  type    = string
  default = "access4db"
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
    #    environment_vars = [
    #      "DATABASE_USER=${var.database_user}",
    #      "DATABASE_PASSWORD=${var.database_password}"
    #    ]
  }

  provisioner "file" {
    name = "copies build file generated by GitHub Actions workflow to Packer image"
    #    source      = "target/app-0.0.1-SNAPSHOT.jar"
    source      = "${var.jar_file}"
    destination = "/tmp/application.jar"
  }

  provisioner "shell" {
    name = "moves application.jar from tmp to /opt/webapp; creates user and group, and makes it owner of the jar file"
    scripts = [
      "packer/scripts/move-jar-to-opt.sh",
      "packer/scripts/create-system-user-group.sh"
    ]
  }
  provisioner "shell" {
    name = "Create log directory and set ownership so that application can write logs to it"
    inline = [
      "sudo mkdir -p /var/log/webapp",
      "sudo chown -R csye6225:csye6225 /var/log/webapp",
      "sudo chmod -R 755 /var/log/webapp"
    ]
  }

  provisioner "shell" {
    name   = "disables selinux"
    script = "packer/scripts/disable_selinux.sh"
  }

  provisioner "shell" {
    name = "creates empty application.properties file"
    inline = [
      "sudo touch /opt/webapp/application.properties",
      "sudo chown -R csye6225:csye6225 /opt/webapp/application.properties"
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
      #      "sudo systemctl enable webapp.service",
      "echo \"Check logs\"",
      "journalctl -u webapp.service"
    ]
  }
  provisioner "shell" {
    name   = "Install Ops Agent for collecting VM logs and sending them to GCP"
    script = "packer/scripts/install-ops-agent.sh"
  }

  provisioner "file" {
    name        = "Copy Ops Agent Config File to Packer image"
    source      = "packer/configs/ops-agent-config.yml"
    destination = "/tmp/ops-agent-config.yml"
  }

  provisioner "shell" {
    inline = [
      "sudo mv /tmp/ops-agent-config.yml /etc/google-cloud-ops-agent/config.yml",
      "sudo systemctl enable google-cloud-ops-agent.service",
      "sudo systemctl start google-cloud-ops-agent.service"
    ]
  }


}

