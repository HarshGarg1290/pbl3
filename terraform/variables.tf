variable "aws_region" {
  description = "The AWS region to deploy to."
  default     = "us-west-2"
}

variable "instance_type" {
  description = "The EC2 instance type for Jenkins."
  default     = "t2.medium"
}

variable "cluster_name" {
  description = "The name of the EKS cluster."
  default     = "my-eks-cluster"
}
