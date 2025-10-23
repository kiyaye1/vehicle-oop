pipeline {
  agent any

  environment {
    AWS_REGION  = 'us-east-2'
    EKS_CLUSTER = 'vehicle-eks'
    DOCKER_REPO = 'kiyaye1/vehicle-oop'     // your Docker Hub repo
  }

  options { timestamps() }

  stages {
    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build') {
      steps {
        sh '''
          export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
          export PATH=$JAVA_HOME/bin:$PATH
          mvn -B clean package -DskipTests
        '''
      }
    }

    stage('Docker Build & Push') {
      steps {
        script {
          env.IMAGE_TAG     = sh(returnStdout: true, script: "git rev-parse --short HEAD").trim()
          env.IMAGE_SHA     = "docker.io/${DOCKER_REPO}:${IMAGE_TAG}"
          env.IMAGE_LATEST  = "docker.io/${DOCKER_REPO}:latest"
        }
        withCredentials([usernamePassword(credentialsId: 'dockerhub-user',
                                          usernameVariable: 'DH_USER',
                                          passwordVariable: 'DH_PASS')]) {
          sh 'echo "$DH_PASS" | docker login -u "$DH_USER" --password-stdin'
        }
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
        withCredentials([
          [$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-creds'],
          string(credentialsId: 'db-host',     variable: 'DB_HOST'),
          string(credentialsId: 'db-password', variable: 'DB_PASSWORD'),
          usernamePassword(credentialsId: 'dockerhub-user',
                           usernameVariable: 'DH_USER',
                           passwordVariable: 'DH_PASS')
        ]) {
          withEnv([
            "AWS_DEFAULT_REGION=${AWS_REGION}",
            "DB_HOST=${DB_HOST}",
            "DB_PASSWORD=${DB_PASSWORD}",
            "DB_USER=system",
            "DH_USER=${DH_USER}",
            "DH_PASS=${DH_PASS}"
          ]) {
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
      withCredentials([[$class: 'AmazonWebServicesCredentialsBinding', credentialsId: 'aws-creds']]) {
        withEnv(["AWS_DEFAULT_REGION=${AWS_REGION}"]) {
          sh "aws eks update-kubeconfig --name ${EKS_CLUSTER} --region ${AWS_REGION} || true"
          sh 'kubectl -n vehicle get svc vehicle-svc -o wide || true'
        }
      }
    }
    failure { echo 'Failed' }
  }
}
