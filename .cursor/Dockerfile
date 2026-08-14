FROM ubuntu:24.04

ENV DEBIAN_FRONTEND=noninteractive
ENV ANDROID_HOME=/opt/android-sdk
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
ENV PATH="${JAVA_HOME}/bin:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools:${PATH}"

RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        ca-certificates \
        curl \
        git \
        openjdk-17-jdk \
        unzip \
        wget \
        fonts-noto \
        fonts-roboto \
        fonts-roboto-unhinted \
    && rm -rf /var/lib/apt/lists/*

RUN mkdir -p "${ANDROID_HOME}/cmdline-tools" \
    && cd /tmp \
    && wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip \
    && unzip -q commandlinetools-linux-11076708_latest.zip \
    && mv cmdline-tools "${ANDROID_HOME}/cmdline-tools/latest" \
    && rm commandlinetools-linux-11076708_latest.zip \
    && yes | sdkmanager --licenses > /dev/null \
    && sdkmanager "platform-tools" "platforms;android-37.0" "build-tools;35.0.0"

RUN printf '%s\n' \
    'export ANDROID_HOME=/opt/android-sdk' \
    'export JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64' \
    'export PATH="$JAVA_HOME/bin:$ANDROID_HOME/cmdline-tools/latest/bin:$ANDROID_HOME/platform-tools:$PATH"' \
    > /etc/profile.d/askit-android.sh

USER ubuntu
