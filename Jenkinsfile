pipeline {
  agent any

  environment {
    JAVA_HOME   = '/usr/lib/jvm/java-21-openjdk-amd64'
    PATH        = "${JAVA_HOME}/bin:${PATH}"

    AWS_REGION  = 'us-east-2'
    EKS_CLUSTER = 'vehicle-eks'

    DOCKER_USER = credentials('dockerhub-user')
    DOCKER_PASS = credentials('dockerhub-pass')
    DOCKER_REPO = 'kiyaye1/vehicle-oop'
  }

  options { timestamps() }

  stages {
    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Build & Unit Test') {
      steps {
        sh '''
          export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
          export PATH=$JAVA_HOME/bin:$PATH
          java -version
          mvn  -version
          mvn -B clean verify
        '''
      }
    }

    stage('Docker Build & Push (Docker Hub)') {
      steps {
        script {
          env.IMAGE_TAG    = sh(returnStdout: true, script: "git rev-parse --short HEAD").trim()
          env.IMAGE_SHA    = "docker.io/${DOCKER_REPO}:${IMAGE_TAG}"
          env.IMAGE_LATEST = "docker.io/${DOCKER_REPO}:latest"
        }
        sh 'echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin'
        sh 'docker build -t "$IMAGE_SHA" .'
        sh 'docker push "$IMAGE_SHA"'
        sh 'docker tag "$IMAGE_SHA" "$IMAGE_LATEST"'
        sh 'docker push "$IMAGE_LATEST"'
      }
    }

    stage('Provision/Update Infra (Terraform)') {
      steps {
        withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-creds']]) {
          withEnv(["AWS_DEFAULT_REGION=${AWS_REGION}"]) {
            dir('infra') {
              sh 'terraform fmt -recursive'
              sh 'terraform init -input=false -upgrade'
              sh 'terraform validate'
              sh 'terraform apply -auto-approve -input=false'
            }
          }
        }
      }
    }

    stage('Deploy to EKS (Ansible, image update)') {
      steps {
        withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-creds']]) {
          withEnv(["AWS_DEFAULT_REGION=${AWS_REGION}"]) {

            // Make sure kubectl context is set
            sh "aws eks update-kubeconfig --name ${EKS_CLUSTER} --region ${AWS_REGION}"

            dir('deploy/ansible') {
              sh '''
                set -e
                # Create & activate a local virtualenv
                python3 -m venv .venv
                . .venv/bin/activate
                pip install --upgrade pip

                # Install Ansible + Kubernetes Python client in the venv
                pip install "ansible-core>=2.16,<2.18" "kubernetes>=26,<32" pyyaml requests

                # Install Ansible collections you use (e.g. kubernetes.core)
                ansible-galaxy collection install -r requirements.yml

                # Use the venv's interpreter so k8s libs are available to the k8s modules
                ANSIBLE_PYTHON_INTERPRETER="$(pwd)/.venv/bin/python" \
                ansible-playbook -i inventory.ini deploy.yml \
                  -e deploy_image="${IMAGE_LATEST}"
              '''
            }
          }
        }
      }
    }
  } 

  post {
    success {
      echo "Deployed image: ${IMAGE_LATEST}"
      sh "aws eks update-kubeconfig --name ${EKS_CLUSTER} --region ${AWS_REGION} || true"
      sh 'kubectl -n vehicle get svc vehicle-svc || true'
    }
    failure {
      echo "Pipeline failed"
    }
  }
}
