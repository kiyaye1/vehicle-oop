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
        withCredentials([usernamePassword(credentialsId: 'aws-creds',
                           usernameVariable: 'AWS_ACCESS_KEY_ID',
                           passwordVariable: 'AWS_SECRET_ACCESS_KEY')]) {
          withEnv(["AWS_DEFAULT_REGION=${AWS_REGION}"]) {
            sh 'aws --version || true'  
            dir('infra') {
              sh 'terraform init -input=false'
              sh 'terraform apply -auto-approve -input=false'
            }
          }
        }
      }
    }

    stage('Deploy to EKS (Ansible, image update)') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'aws-creds',
                           usernameVariable: 'AWS_ACCESS_KEY_ID',
                           passwordVariable: 'AWS_SECRET_ACCESS_KEY')]) {
          withEnv(["AWS_DEFAULT_REGION=${AWS_REGION}"]) {
            sh "aws eks update-kubeconfig --name ${EKS_CLUSTER} --region ${AWS_REGION}"
            dir('deploy/ansible') {
              sh 'ansible-galaxy collection install -r requirements.yml'
              sh """
                ansible-playbook -i inventory.ini deploy.yml \
                  -e deploy_image=${IMAGE_LATEST}
              """
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
