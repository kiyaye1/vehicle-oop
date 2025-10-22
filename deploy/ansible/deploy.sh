#!/usr/bin/env bash
set -euo pipefail

IMAGE_LATEST="${1:-docker.io/kiyaye1/vehicle-oop:latest}"
echo "Deploying image: $IMAGE_LATEST"

# Simple local install (no venv) to keep minimal
pip3 install --user --upgrade ansible-core kubernetes pyyaml requests >/dev/null

export PATH="$HOME/.local/bin:$PATH"

ansible-playbook -i inventory.ini deploy.yml -e deploy_image="$IMAGE_LATEST"
echo "Done."
