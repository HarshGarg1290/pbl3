pipeline {
    agent any

    tools {
        maven 'Maven 3.8.1' // This name must match the one configured in Jenkins Global Tool Configuration
    }

    environment {
        DOCKER_IMAGE = "yourdockerhubusername/sample-java-app:${BUILD_NUMBER}"
        AWS_REGION = "us-west-2" // Change if you used a different region
    }

    stages {
        stage('Checkout Code') {
            steps {
                git 'https://github.com/your-username/sample-java-app.git'
            }
        }

        stage('Build with Maven') {
            steps {
                dir('sample-java-app') {
                    sh 'mvn clean package'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                dir('sample-java-app') {
                    sh 'docker build -t $DOCKER_IMAGE .'
                }
            }
        }

        stage('Push to Docker Hub') {
            steps {
                withDockerRegistry([credentialsId: 'docker-hub-creds']) {
                    sh "docker push ${env.DOCKER_IMAGE}"
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                withCredentials([aws(credentialsId: 'aws-creds', region: env.AWS_REGION)]) {
                    sh 'aws eks --region $AWS_REGION update-kubeconfig --name my-eks-cluster'
                    sh 'kubectl set image deployment/sample-app-deployment sample-container=$DOCKER_IMAGE'
                }
            }
        }
    }
}
