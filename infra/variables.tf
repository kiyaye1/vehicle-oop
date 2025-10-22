variable "region" {
  type    = string
  default = "us-east-2"
}

variable "cluster_name" {
  type    = string
  default = "vehicle-eks"
}

variable "vpc_cidr" {
  type    = string
  default = "10.0.0.0/16"
}
