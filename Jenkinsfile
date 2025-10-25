pipeline {
  agent any
  environment {
  	DOCKER_IMAGE = "kiyaye1/vehicle-oop"
        K8S_NAMESPACE = "vehicle"
        KUBECONFIG = "${env.WORKSPACE}/.kube/config"

        DOCKER_HOST = "unix:///var/run/docker.sock"
  	TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE = "/var/run/docker.sock"

  	TESTCONTAINERS_RYUK_DISABLED = "true"          // already added earlier
  	TESTCONTAINERS_CHECKS_DISABLE = "true"         // already added earlier

  	// ✅ Force unix-socket client strategy (prevents Desktop/NullPointer path)
  	TESTCONTAINERS_DOCKER_CLIENT_STRATEGY = "org.testcontainers.dockerclient.UnixSocketClientProviderStrategy"

  	// ✅ Ensure no TLS desktop vars confuse detection
  	DOCKER_TLS_VERIFY = ""
  	DOCKER_CERT_PATH = ""
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
          	set -euxo pipefail
      	  	id && groups
          	ls -l /var/run/docker.sock || true
      		echo "DOCKER_HOST=${DOCKER_HOST}"
      		echo "TESTCONTAINERS_*:"
      		env | grep -E "TESTCONTAINERS|DOCKER_" || true

      		# prove docker works for this shell (same env as Maven)
      		docker info --format "ServerVersion: {{.ServerVersion}}"
      		docker run --rm busybox:latest echo docker-ok || true

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

