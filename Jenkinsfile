pipeline {
  agent any

  environment {
    DOCKER_IMAGE = "kiyaye1/vehicle-oop"
    K8S_NAMESPACE = "vehicle"
    KUBECONFIG = "${env.WORKSPACE}/.kube/config"

    // Testcontainers + Docker inside Jenkins container
    DOCKER_HOST = "unix:///var/run/docker.sock"
    TESTCONTAINERS_DOCKER_SOCKET_OVERRIDE = "/var/run/docker.sock"
    TESTCONTAINERS_RYUK_DISABLED = "true"
    TESTCONTAINERS_CHECKS_DISABLE = "true"
    TESTCONTAINERS_DOCKER_CLIENT_STRATEGY = "org.testcontainers.dockerclient.UnixSocketClientProviderStrategy"
    DOCKER_TLS_VERIFY = ""
    DOCKER_CERT_PATH = ""
  }

  triggers { pollSCM('H/1 * * * *') }
  options { timestamps() }

  stages {

    stage('Prep kubeconfig') {
      steps {
        sh '''
          set -eux
          mkdir -p .kube
          cp -f /var/jenkins_home/.kube/config .kube/config

          echo "Before patch:"
          kubectl --kubeconfig .kube/config config view --minify -o jsonpath='{.clusters[0].cluster.server}'; echo

          # Patch the server so kubectl inside Jenkins can talk to kind on the host
          sed -i 's/127\\.0\\.0\\.1/host.docker.internal/g' .kube/config || true

          echo "After patch:"
          kubectl --kubeconfig .kube/config config view --minify -o jsonpath='{.clusters[0].cluster.server}'; echo
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

          docker info --format "ServerVersion: {{.ServerVersion}}"
          docker run --rm busybox:latest echo docker-ok || true

          chmod +x mvnw
          ./mvnw -V -B -DskipTests=false clean package
        '''
      }
    }

    stage('Docker Build & Push') {
      steps {
        withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
                                          usernameVariable: 'DOCKER_USER',
                                          passwordVariable: 'DOCKER_PASS')]) {
          sh '''
            set -eux
            echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin

            # Clean flaky BuildKit/builder caches
            docker buildx prune -af || true
            docker builder prune -af || true
            docker system prune -af --volumes || true

            # Build fresh without reusing cache and push
            docker build --pull --no-cache -t "$DOCKER_IMAGE:latest" .
            docker push "$DOCKER_IMAGE:latest"
          '''
        }
      }
    }

    stage('Deploy to kind (K8s)') {
      steps {
        sh '''
          set -eux
          K="--kubeconfig .kube/config --validate=false --insecure-skip-tls-verify=true -n ${K8S_NAMESPACE}"

          kubectl $K apply -f deploy/k8s/namespace.yaml
          kubectl $K apply -f deploy/k8s/secret.yaml
          kubectl $K apply -f deploy/k8s/deployment.yaml
          kubectl $K apply -f deploy/k8s/service.yaml

          kubectl --kubeconfig .kube/config --insecure-skip-tls-verify=true -n ${K8S_NAMESPACE} \
            set image deploy/vehicle-app vehicle-app=${DOCKER_IMAGE}:latest --record

          kubectl --kubeconfig .kube/config --insecure-skip-tls-verify=true -n ${K8S_NAMESPACE} \
            rollout status deploy/vehicle-app --timeout=180s
        '''
      }
    }
  }

  post {
    success { echo '✅ Deployed: http://localhost:8282' }
    failure { echo '❌ Build failed — check the stage logs.' }
  }
}


