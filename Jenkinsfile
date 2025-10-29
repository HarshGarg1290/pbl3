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
                        // Bind AKID/Secret via AWS creds and the STS token via Secret Text
                        withCredentials([
                            aws(credentialsId: 'aws-creds'),
                            string(credentialsId: 'aws-session-token', variable: 'AWS_SESSION_TOKEN')
                        ]) {
                                        sh '''
                                            set -e
                                            # Ensure region is available to AWS SDK/CLI
                                            export AWS_DEFAULT_REGION="$AWS_REGION"
                                            aws sts get-caller-identity
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
                                        withCredentials([
                                                aws(credentialsId: 'aws-creds'),
                                                string(credentialsId: 'aws-session-token', variable: 'AWS_SESSION_TOKEN')
                                        ]) {
                                        sh '''
                                            set -e
                                            export AWS_DEFAULT_REGION="$AWS_REGION"
                                            aws eks --region "$AWS_REGION" update-kubeconfig --name my-eks-cluster
                                                    # Ensure the new pod version is available before hitting the Service
                                                    echo "Waiting for deployment rollout..."
                                                    kubectl rollout status deployment/sample-app-deployment --timeout=300s

                                                    echo "Waiting for service external address..."
                                            for i in $(seq 1 30); do
                                                LB=$(kubectl get svc sample-app-service -o jsonpath='{.status.loadBalancer.ingress[0].hostname}' 2>/dev/null || true)
                                                if [ -n "$LB" ]; then echo "LB=$LB"; break; fi
                                                sleep 10
                                            done
                                            test -n "$LB" || { echo "Service did not get an external address in time"; exit 1; }
                      
                                                    # Wait for DNS to propagate so the ELB hostname resolves
                                                    echo "Waiting for DNS to resolve $LB ..."
                                                    for i in $(seq 1 30); do
                                                        if getent hosts "$LB" >/dev/null 2>&1; then echo "DNS resolved"; break; fi
                                                        sleep 5
                                                    done
                                                    getent hosts "$LB" >/dev/null 2>&1 || { echo "DNS did not resolve in time"; exit 1; }

                                                    echo "Hitting http://$LB/ ..."
                                                    # Retry HTTP until the load balancer targets are healthy
                                                    until curl -fsS --max-time 10 "http://$LB/" -o /tmp/app_resp.txt; do
                                                        echo "Waiting for service HTTP..."; sleep 5;
                                                    done
                                                    cat /tmp/app_resp.txt
                                            grep -q "Hello, World!" /tmp/app_resp.txt
                                        '''
                                }
                        }
                }
    }
}
