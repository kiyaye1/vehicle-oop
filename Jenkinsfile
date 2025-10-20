pipeline {
  agent any

  environment {
    JAVA_HOME = '/usr/lib/jvm/java-21-openjdk-amd64'
    PATH = "${JAVA_HOME}/bin:${PATH}"
    
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
      echo "=== JAVA VERSION ==="
      java -version
      echo "=== MAVEN VERSION ==="
      mvn -v
    '''
    sh 'mvn -B -DskipITs=true clean verify'
  }
}
 

    stage('Docker Build & Push (Docker Hub)') {
      steps {
        script {
          env.IMAGE_TAG = sh(returnStdout: true, script: "git rev-parse --short HEAD").trim()
          env.IMAGE_SHA = "docker.io/${DOCKER_REPO}:${IMAGE_TAG}"
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
        withAWS(credentials: 'aws-creds', region: "${AWS_REGION}") {
          dir('infra') {
            sh 'terraform init -input=false'
            sh 'terraform apply -auto-approve -input=false'
          }
        }
      }
    }

    stage('Deploy to EKS (Ansible, image update w/o sed)') {
      environment {
        ORACLE_URL      = credentials('ORACLE_URL')
        ORACLE_USERNAME = credentials('ORACLE_USERNAME')
        ORACLE_PASSWORD = credentials('ORACLE_PASSWORD')
      }
      steps {
        withAWS(credentials: 'aws-creds', region: "${AWS_REGION}") {
          dir('deploy/ansible') {
            sh 'ansible-galaxy collection install -r requirements.yml'
            sh """
              ansible-playbook -i inventory.ini deploy.yml \
                -e oracle_url='${ORACLE_URL}' \
                -e oracle_user='${ORACLE_USERNAME}' \
                -e oracle_pass='${ORACLE_PASSWORD}' \
                -e deploy_image=${IMAGE_LATEST}
            """
          }
        }
      }
    }
  }

  post {
    success {
      echo "Deployed image: ${IMAGE_LATEST}"
      sh 'kubectl -n vehicle get svc vehicle-svc || true'
    }
    failure { echo "Pipeline failed" }
  }
}
