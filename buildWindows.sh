#wget -O ~/Downloads/OpenJDK11U-jre_x64_windows_openj9_11.0.8_10_openj9-0.21.0.zip https://github.com/AdoptOpenJDK/openjdk11-binaries/releases/download/jdk-11.0.8%2B10_openj9-0.21.0/OpenJDK11U-jre_x64_windows_openj9_11.0.8_10_openj9-0.21.0.zip
java -jar packr-all-2.7.0.jar  \
	--platform windows64   \
     	--jdk ~/Downloads/OpenJDK11U-jre_x64_windows_openj9_11.0.8_10_openj9-0.21.0.zip  \
    	--useZgcIfSupportedOs  \
    	--executable the-other-worlds     \
       	--classpath desktop/build/libs/desktop-1.0.jar  \
    	--mainclass io.blocktyper.theotherworlds.desktop.DesktopLauncher  \
    	--vmargs Xmx1G  \
    	--resources desktop/client_config.json  \
     	--minimizejre soft  \
    	--output windows
