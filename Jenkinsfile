pipeline {
  agent any
  environment {
    DOCKER_IMAGE = "kiyaye1/vehicle-oop"
    K8S_NAMESPACE = "vehicle"
    // Use the kubeconfig we mounted into the Jenkins container
    KUBECONFIG = "${env.WORKSPACE}/.kube/config"
  }
  triggers {
    pollSCM('H/1 * * * *')  // every minute; replace with GitHub webhook later
  }
  options { timestamps() }

  stages {
    stage('Prep kubeconfig') {
      steps {
        // copy the host-mounted kubeconfig from /var/jenkins_home into the workspace
        sh '''
          mkdir -p .kube
          cp -f /var/jenkins_home/.kube/config .kube/config
          kubectl config current-context || true
        '''
      }
    }

    stage('Checkout') {
      steps {
        checkout scm
      }
    }

    stage('Build & Test') {
      steps {
        sh 'mvn -q -DskipTests=false clean package'
      }
    }

    stage('Docker Build & Push') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
                                          usernameVariable: 'DOCKER_USER',
                                          passwordVariable: 'DOCKER_PASS')]) {
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
          # Apply the same manifests we used manually
          kubectl apply -f deploy/k8s/namespace.yaml
          kubectl apply -f deploy/k8s/secret.yaml
          kubectl apply -f deploy/k8s/deployment.yaml
          kubectl apply -f deploy/k8s/service.yaml

          # Force rollout to the new image
          kubectl -n ${K8S_NAMESPACE} set image deploy/vehicle-app vehicle-app=$DOCKER_IMAGE:latest --record
          kubectl -n ${K8S_NAMESPACE} rollout status deploy/vehicle-app --timeout=180s
        '''
      }
    }
  }

  post {
    success {
      echo "✅ Deployed. Open http://localhost:8282"
    }
    failure {
      echo "❌ Build failed. Check logs."
    }
  }
}

