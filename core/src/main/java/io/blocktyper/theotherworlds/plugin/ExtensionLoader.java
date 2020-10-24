package io.blocktyper.theotherworlds.plugin;

import com.fasterxml.jackson.databind.JsonNode;
import io.blocktyper.theotherworlds.plugin.utils.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class ExtensionLoader<C> {

    ClassLoader classLoader;

    public Optional<C> loadPlugin(File jar, Class<C> parentClass, Optional<String> optionalClasspath) throws RuntimeException {
        try {
            classLoader = URLClassLoader.newInstance(
                    new URL[]{jar.toURL()},
                    getClass().getClassLoader()
            );

            return optionalClasspath
                    .map((classPath) -> getPluginInstance(parentClass, classPath))
                    .orElseGet(() ->
                            Optional.ofNullable(classLoader.getResourceAsStream("example/config.json"))
                                    .map(FileUtils::inputStringToString)
                                    .map(FileUtils::getJsonNodeFromRawString)
                                    .map(jsonNode -> jsonNode.get("classPath"))
                                    .map(JsonNode::asText)
                                    .flatMap(classPath ->
                                            getPluginInstance(parentClass, classPath)
                                    )
                                    .or(() -> {
                                        System.out.println("Plugin missing config.json in resources or 'classPath' not set in config.json: " + jar.getName());
                                        return Optional.empty();
                                    })
                    );

        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
        System.out.println("Plugin not found: " + jar.getName());
        return Optional.empty();
    }

    public List<String> getFileNamesInResourceDirectory(String directoryName) {
        try {
            List<String> filenames = new ArrayList<>();
            URL url = classLoader.getResource(directoryName);
            String dirname = directoryName + "/";
            String path = url.getPath();
            String jarPath = path.substring(5, path.indexOf("!"));
            try (JarFile jar = new JarFile(URLDecoder.decode(jarPath, StandardCharsets.UTF_8.name()))) {
                Enumeration<JarEntry> entries = jar.entries();
                while (entries.hasMoreElements()) {
                    JarEntry entry = entries.nextElement();
                    String name = entry.getName();
                    if (name.startsWith(dirname) && !dirname.equals(name)) {
                        URL resource = Thread.currentThread().getContextClassLoader().getResource(name);
                        String fullPathInJar = resource.toString();
                        filenames.add(fullPathInJar.substring(fullPathInJar.lastIndexOf("/") + 1));
                    }
                }
            }

            return filenames;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return List.of();
    }

    public InputStream getResourceAsStream(String resourceName) {
        return classLoader.getResourceAsStream(resourceName);
    }

    @NotNull
    private Optional<C> getPluginInstance(Class<C> parentClass, String classPath) {
        try {
            Class<?> clazz = Class.forName(classPath, true, classLoader);
            Class<? extends C> newClass = clazz.asSubclass(parentClass);
            Constructor<? extends C> constructor = newClass.getConstructor();
            return Optional.of(constructor.newInstance());
        } catch (ClassNotFoundException
                | NoSuchMethodException
                | InvocationTargetException
                | IllegalAccessException
                | InstantiationException ex) {
            ex.printStackTrace();
            return Optional.empty();
        }
    }
}
