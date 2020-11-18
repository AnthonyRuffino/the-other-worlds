./gradlew :plugin:build
./gradlew :core:build
./gradlew :example:build
./gradlew :desktop:build
mkdir -p desktop/plugins/example
cp example/build/libs/example-1.0.jar desktop/plugins/example/example.jar
./gradlew :desktop:run
