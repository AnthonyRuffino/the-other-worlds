./gradlew :plugin:build
./gradlew :example:build
mkdir -p desktop/plugins/example
cp example/build/libs/example-1.0.jar desktop/plugins/example/example.jar