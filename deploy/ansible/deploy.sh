#!/usr/bin/env bash
set -euo pipefail

IMAGE_LATEST="${1:-docker.io/kiyaye1/vehicle-oop:latest}"
echo "Starting Ansible deployment using image: $IMAGE_LATEST"

PY_BIN="${PY_BIN:-python3}"
echo "Using $( $PY_BIN -V )"

if [ ! -d ".venv" ]; then
  echo "Creating virtual environment..."
  if ! $PY_BIN -m venv .venv 2>/dev/null; then
    echo "venv module complained; falling back to --without-pip + bootstrap"
    $PY_BIN -m venv --without-pip .venv
    curl -fsSL https://bootstrap.pypa.io/get-pip.py -o get-pip.py
    . .venv/bin/activate
    $PY_BIN get-pip.py
  fi
fi

. .venv/bin/activate

if ! command -v pip >/dev/null 2>&1; then
  echo "Bootstrapping pip in venv..."
  curl -fsSL https://bootstrap.pypa.io/get-pip.py -o get-pip.py
  $PY_BIN get-pip.py
fi

echo "Installing Python deps into venv..."
pip install --upgrade pip
pip install -r requirements.txt

if [ -f requirements.yml ]; then
  echo "Installing Ansible Galaxy collections..."
  ansible-galaxy collection install -r requirements.yml
fi

echo "Running Ansible playbook..."
ANSIBLE_PYTHON_INTERPRETER="$(pwd)/.venv/bin/python" \
ansible-playbook -i inventory.ini deploy.yml -e deploy_image="$IMAGE_LATEST"

echo "Deployment complete."
