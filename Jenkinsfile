pipeline {
    agent any

    environment {
        DOCKER_IMAGE = "harsheysss/sample-java-app:${BUILD_NUMBER}"
        AWS_REGION = "us-east-1" // Region for AWS Learner Lab / EKS cluster
    }

    stages {
        stage('Checkout Code') {
            steps {
                git branch: 'main', url: 'https://github.com/HarshGarg1290/pbl3.git'
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
                withDockerRegistry([url: 'https://index.docker.io/v1/', credentialsId: 'docker-hub-creds']) {
                    sh "docker push ${env.DOCKER_IMAGE}"
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                withCredentials([aws(credentialsId: 'aws-creds', region: env.AWS_REGION)]) {
                    sh '''
                      set -e
                      aws eks --region "$AWS_REGION" update-kubeconfig --name my-eks-cluster
                      # Bootstrap manifests on first run if not present
                      if ! kubectl get deploy sample-app-deployment >/dev/null 2>&1; then
                        kubectl apply -f kubernetes/deployment.yaml
                        kubectl apply -f kubernetes/service.yaml
                      fi
                      kubectl set image deployment/sample-app-deployment sample-container=$DOCKER_IMAGE
                    '''
                }
            }
        }

        stage('Smoke Test') {
            steps {
                withCredentials([aws(credentialsId: 'aws-creds', region: env.AWS_REGION)]) {
                    sh '''
                      set -e
                      aws eks --region "$AWS_REGION" update-kubeconfig --name my-eks-cluster
                      echo "Waiting for service external address..."
                      for i in $(seq 1 30); do
                        LB=$(kubectl get svc sample-app-service -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || true)
                        if [ -n "$LB" ]; then echo "LB=$LB"; break; fi
                        sleep 10
                      done
                      test -n "$LB" || { echo "Service did not get an external address in time"; exit 1; }
                      echo "Hitting http://$LB/ ..."
                      curl -fsS --max-time 20 "http://$LB/" | tee /tmp/app_resp.txt
                      grep -q "Hello, World!" /tmp/app_resp.txt
                    '''
                }
            }
        }
    }
}
