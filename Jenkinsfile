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

    dir('deploy/ansible') {
      sh '''
        set -e
    
        # Try to create a venv. If it fails (ensurepip missing), install python3-venv.
        if ! python3 -m venv .venv 2>/dev/null; then
          echo "python3-venv not present; attempting install..."
          # Try with sudo first; if not available, try direct (for root agents)
          (command -v sudo >/dev/null && sudo apt-get update && sudo apt-get install -y python3-venv python3-pip) \
          || (apt-get update && apt-get install -y python3-venv python3-pip) \
          || true
        fi
    
        # Retry venv creation; if it still fails, fall back to virtualenv in user space.
        if ! python3 -m venv .venv 2>/dev/null; then
          echo "Falling back to virtualenv in user space..."
          python3 -m pip install --user --upgrade pip
          python3 -m pip install --user virtualenv
          ~/.local/bin/virtualenv .venv
        fi
    
        . .venv/bin/activate
        pip install --upgrade pip
        pip install "ansible-core>=2.16,<2.18" "kubernetes>=26,<32" pyyaml requests
    
        ansible-galaxy collection install -r requirements.yml
    
        ANSIBLE_PYTHON_INTERPRETER="$(pwd)/.venv/bin/python" \
        ansible-playbook -i inventory.ini deploy.yml \
          -e deploy_image="${IMAGE_LATEST}"
      '''
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
