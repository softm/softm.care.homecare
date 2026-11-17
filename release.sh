#!/bin/bash
set -euo pipefail
cd "$(dirname "$0")"
if [[ -z "${JAVA_HOME:-}" ]]; then
  export JAVA_HOME="$(/usr/libexec/java_home -v 17)"
fi
./gradlew :app:lintRelease :app:assembleRelease :app:bundleRelease "$@"
