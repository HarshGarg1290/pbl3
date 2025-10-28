output "jenkins_ip" {
  value = aws_instance.jenkins.public_ip
}

output "eks_cluster_endpoint" {
  value = aws_eks_cluster.main.endpoint
}

output "eks_cluster_ca" {
  value = base64decode(aws_eks_cluster.main.certificate_authority[0].data)
}
