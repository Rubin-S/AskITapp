#!/usr/bin/env bash
set -euo pipefail

export ANDROID_HOME="${ANDROID_HOME:-/opt/android-sdk}"
export JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-17-openjdk-amd64}"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"

if [ -f /etc/profile.d/askit-android.sh ]; then
  # shellcheck disable=SC1091
  source /etc/profile.d/askit-android.sh
fi

if [ -f /workspace/gradlew ]; then
  chmod +x /workspace/gradlew
fi
