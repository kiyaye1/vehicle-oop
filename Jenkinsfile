pipeline {
  agent any
  environment {
    DOCKER_IMAGE = "kiyaye1/vehicle-oop"
    K8S_NAMESPACE = "vehicle"
    KUBECONFIG = "${env.WORKSPACE}/.kube/config"
  }
  triggers { pollSCM('H/1 * * * *') }
  options { timestamps() }

  stages {
    stage('Prep kubeconfig') {
      steps {
        sh '''
          mkdir -p .kube
          cp -f /var/jenkins_home/.kube/config .kube/config
          kubectl config current-context || true
        '''
      }
    }

    stage('Checkout') {
      steps { checkout scm }
    }

    stage('Build & Test') {
      steps {
        sh '''
          chmod +x mvnw
          ./mvnw -V -B -DskipTests=false clean package
        '''
      }
    }

    stage('Docker Build & Push') {
      steps {
        withCredentials([usernamePassword(credentialsId: "dockerhub-creds",
                                          usernameVariable: "DOCKER_USER",
                                          passwordVariable: "DOCKER_PASS")]) {
          sh '''
            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
            docker build -t $DOCKER_IMAGE:latest .
            docker push $DOCKER_IMAGE:latest
          '''
        }
      }
    }

    stage('Deploy to kind (K8s)') {
      steps {
        sh '''
          kubectl apply -f deploy/k8s/namespace.yaml
          kubectl apply -f deploy/k8s/secret.yaml
          kubectl apply -f deploy/k8s/deployment.yaml
          kubectl apply -f deploy/k8s/service.yaml

          kubectl -n ${K8S_NAMESPACE} set image deploy/vehicle-app vehicle-app=$DOCKER_IMAGE:latest --record
          kubectl -n ${K8S_NAMESPACE} rollout status deploy/vehicle-app --timeout=180s
        '''
      }
    }
  }

  post {
    success { echo "✅ Deployed: http://localhost:8282" }
    failure { echo "❌ Build failed — check the stage logs." }
  }
}

