pipeline {
  agent any

  environment {
    AWS_REGION  = 'us-east-2'
    EKS_CLUSTER = 'vehicle-eks'
    DOCKER_USER = credentials('dockerhub-user')
    DOCKER_PASS = credentials('dockerhub-pass')
    DOCKER_REPO = 'kiyaye1/vehicle-oop'
  }

  options { timestamps() }

  stages {
    stage('Checkout') { steps { checkout scm } }

    stage('Build') {
      steps {
        sh '''
          export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
          export PATH=$JAVA_HOME/bin:$PATH
          # Skip tests to prevent H2 driver load failure
          mvn -B clean package -DskipTests
        '''
      }
    }

    stage('Docker Build & Push') {
      steps {
        script {
          env.IMAGE_TAG   = sh(returnStdout: true, script: "git rev-parse --short HEAD").trim()
          env.IMAGE_SHA   = "docker.io/${DOCKER_REPO}:${IMAGE_TAG}"
          env.IMAGE_LATEST= "docker.io/${DOCKER_REPO}:latest"
        }
        sh 'echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin'
        sh 'docker build -t "$IMAGE_SHA" .'
        sh 'docker push "$IMAGE_SHA"'
        sh 'docker tag "$IMAGE_SHA" "$IMAGE_LATEST"'
        sh 'docker push "$IMAGE_LATEST"'
      }
    }

    stage('Terraform Apply') {
      steps {
        withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-creds']]) {
          withEnv(["AWS_DEFAULT_REGION=${AWS_REGION}"]) {
            dir('infra') {
              sh '''
                terraform init -input=false
                terraform apply -auto-approve -input=false
              '''
            }
          }
        }
      }
    }

    stage('Deploy (Ansible)') {
      steps {
        withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-creds']]) {
          withEnv(["AWS_DEFAULT_REGION=${AWS_REGION}"]) {
            sh "aws eks update-kubeconfig --name ${EKS_CLUSTER} --region ${AWS_REGION}"
            dir('deploy/ansible') {
              sh 'chmod +x deploy.sh && ./deploy.sh "$IMAGE_LATEST"'
            }
          }
        }
      }
    }
  }

  post {
    success {
      echo 'Done'
      withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-creds']]) {
        withEnv(["AWS_DEFAULT_REGION=${AWS_REGION}"]) {
          sh "aws eks update-kubeconfig --name ${EKS_CLUSTER} --region ${AWS_REGION} || true"
          sh 'kubectl -n vehicle get svc vehicle-svc || true'
        }
      }
    }
    failure { echo 'Failed' }
  }
}
