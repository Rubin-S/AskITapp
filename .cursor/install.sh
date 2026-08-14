#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

export ANDROID_HOME="${ANDROID_HOME:-/opt/android-sdk}"
export JAVA_HOME="${JAVA_HOME:-/usr/lib/jvm/java-17-openjdk-amd64}"
export PATH="$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"

chmod +x ./gradlew

# Warm Gradle and download Android dependencies without running tests.
./gradlew --version
./gradlew compileDebugKotlin compileDebugUnitTestKotlin --quiet
