#!/usr/bin/env bash
# Fabric-Build. Standard: schneller Build aus committeten generierten Assets.
# Mit --datagen: zuerst EIN runDatagen-Lauf (assets/ + data/ in einem Durchgang).
set -euo pipefail
cd "$(dirname "$0")/.."
if [[ "${1:-}" == "--datagen" ]]; then
  ./gradlew runDatagen --build-cache
fi
./gradlew build --build-cache
echo "Jar: build/libs/akw-$(grep '^mod_version' gradle.properties | cut -d= -f2).jar"
