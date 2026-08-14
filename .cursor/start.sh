#!/usr/bin/env bash
set -euo pipefail

PROFILE_MARKER="# askit-android-env"
BASHRC="${HOME}/.bashrc"
PROFILE_SCRIPT="/etc/profile.d/askit-android.sh"

write_env_block() {
  cat <<'EOF'
export ANDROID_HOME=/opt/android-sdk
export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"
EOF
}

if ! grep -q "$PROFILE_MARKER" "$BASHRC" 2>/dev/null; then
  {
    echo ""
    echo "$PROFILE_MARKER"
    write_env_block
  } >> "$BASHRC"
fi

if [ ! -f "$PROFILE_SCRIPT" ]; then
  if command -v sudo >/dev/null 2>&1 && sudo -n true 2>/dev/null; then
    {
      write_env_block
    } | sudo tee "$PROFILE_SCRIPT" > /dev/null
    sudo chmod 644 "$PROFILE_SCRIPT"
  fi
fi

if [ -f /workspace/gradlew ]; then
  chmod +x /workspace/gradlew
fi

# Apply for the start process itself.
# shellcheck disable=SC1090
source <(write_env_block)
