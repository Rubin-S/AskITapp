#!/usr/bin/env bash
set -euo pipefail

ANDROID_HOME="${ANDROID_HOME:-/opt/android-sdk}"
JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-17-openjdk-amd64}"

if [ -d /workspace ]; then
  cat > /workspace/local.properties <<EOF
sdk.dir=${ANDROID_HOME}
EOF
  [ -f /workspace/gradlew ] && chmod +x /workspace/gradlew
fi

PROFILE_MARKER="# askit-android-env"
BASHRC="${HOME}/.bashrc"
PROFILE_SCRIPT="/etc/profile.d/askit-android.sh"

write_env_block() {
  cat <<EOF
export ANDROID_HOME=${ANDROID_HOME}
export JAVA_HOME=${JAVA_HOME}
export PATH="\$JAVA_HOME/bin:\$ANDROID_HOME/cmdline-tools/latest/bin:\$ANDROID_HOME/platform-tools:\$PATH"
EOF
}

if ! grep -q "$PROFILE_MARKER" "$BASHRC" 2>/dev/null; then
  {
    echo ""
    echo "$PROFILE_MARKER"
    write_env_block
  } >> "$BASHRC"
fi

if [ ! -f "$PROFILE_SCRIPT" ] && command -v sudo >/dev/null 2>&1 && sudo -n true 2>/dev/null; then
  write_env_block | sudo tee "$PROFILE_SCRIPT" > /dev/null
  sudo chmod 644 "$PROFILE_SCRIPT"
fi
