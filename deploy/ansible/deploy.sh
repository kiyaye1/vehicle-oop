#!/usr/bin/env bash
set -e

IMAGE_LATEST="$1"

echo "🚀 Starting Ansible deployment using image: $IMAGE_LATEST"

chmod +x "$0" 2>/dev/null || true

# --- Setup Python virtual environment ---
if [ ! -d ".venv" ]; then
  echo "Creating Python virtual environment..."
  python3 -m venv .venv || {
    echo "⚙️ ensurepip missing, bootstrapping pip..."
    python3 -m venv --without-pip .venv
    python3 - <<'PY'
import ssl, urllib.request
url = "https://bootstrap.pypa.io/get-pip.py"
print("Downloading get-pip.py ...")
data = urllib.request.urlopen(url, context=ssl.create_default_context()).read()
open("get-pip.py","wb").write(data)
print("Saved get-pip.py")
PY
    . .venv/bin/activate
    python get-pip.py
  }
fi

. .venv/bin/activate

echo "Installing Python dependencies..."
pip install --upgrade pip
pip install -r requirements.txt

if [ -f requirements.yml ]; then
  echo "Installing Ansible Galaxy collections..."
  ansible-galaxy collection install -r requirements.yml
fi

# --- Run Ansible with retry and longer timeout ---
echo "Running Ansible playbook with retry and extended timeout..."
ANSIBLE_PYTHON_INTERPRETER="$(pwd)/.venv/bin/python" \
ansible-playbook -i inventory.ini deploy.yml \
  -e deploy_image="$IMAGE_LATEST" \
  --extra-vars "rollout_timeout=600" || {

  echo "⚠️ First rollout attempt failed. Retrying in 60 seconds..."
  sleep 60

  ansible-playbook -i inventory.ini deploy.yml \
    -e deploy_image="$IMAGE_LATEST" \
    --extra-vars "rollout_timeout=600"
}

echo "✅ Deployment complete."
