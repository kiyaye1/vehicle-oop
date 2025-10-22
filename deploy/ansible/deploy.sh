#!/usr/bin/env bash
set -euo pipefail

IMAGE_LATEST="${1:-docker.io/kiyaye1/vehicle-oop:latest}"
echo "🚀 Starting Ansible deployment using image: $IMAGE_LATEST"

# Choose python
PY_BIN="${PY_BIN:-python3}"
echo "🐍 Using $( $PY_BIN -V )"

# Create a venv without touching system site-packages
if [ ! -d ".venv" ]; then
  echo "🧪 Creating virtual environment..."
  # Try normal venv; if it fails, fall back to --without-pip + bootstrap
  if ! $PY_BIN -m venv .venv 2>/dev/null; then
    echo "⚙️  venv module complained; falling back to --without-pip + bootstrap"
    $PY_BIN -m venv --without-pip .venv
    # Bootstrap pip into the venv only
    curl -fsSL https://bootstrap.pypa.io/get-pip.py -o get-pip.py
    . .venv/bin/activate
    $PY_BIN get-pip.py
  fi
fi

# Activate venv (all installs now go to .venv)
. .venv/bin/activate

# Make sure pip exists in the venv (covers the normal path)
if ! command -v pip >/dev/null 2>&1; then
  echo "📦 Bootstrapping pip in venv..."
  curl -fsSL https://bootstrap.pypa.io/get-pip.py -o get-pip.py
  $PY_BIN get-pip.py
fi

echo "📦 Installing Python deps into venv..."
pip install --upgrade pip
pip install -r requirements.txt

# Install Ansible Galaxy collections (k8s module)
if [ -f requirements.yml ]; then
  echo "🪄 Installing Ansible Galaxy collections..."
  ansible-galaxy collection install -r requirements.yml
fi

# Run the playbook
echo "🧩 Running Ansible playbook..."
ANSIBLE_PYTHON_INTERPRETER="$(pwd)/.venv/bin/python" \
ansible-playbook -i inventory.ini deploy.yml -e deploy_image="$IMAGE_LATEST"

echo "✅ Deployment complete."
