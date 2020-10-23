package io.blocktyper.theotherworlds.plugin;

import com.fasterxml.jackson.databind.JsonNode;
import io.blocktyper.theotherworlds.plugin.utils.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Optional;

public class ExtensionLoader<C> {

    public Optional<C> loadPlugin(File jar, Class<C> parentClass, Optional<String> optionalClasspath) throws RuntimeException {
        try {
            ClassLoader loader = URLClassLoader.newInstance(
                    new URL[]{jar.toURL()},
                    getClass().getClassLoader()
            );

            return optionalClasspath
                    .map((classPath) -> getPluginInstance(parentClass, loader, classPath))
                    .orElseGet(() ->
                            Optional.ofNullable(loader.getResourceAsStream("config.json"))
                                    .map(FileUtils::inputStringToString)
                                    .map(FileUtils::getJsonNodeFromRawString)
                                    .map(jsonNode -> jsonNode.get("classPath"))
                                    .map(JsonNode::asText)
                                    .flatMap(classPath ->
                                            getPluginInstance(parentClass, loader, classPath)
                                    )
                    );

        } catch (MalformedURLException ex) {
            ex.printStackTrace();
        }
        System.out.println("Plugin not found: " + jar.getName());
        return Optional.empty();
    }

    @NotNull
    private Optional<C> getPluginInstance(Class<C> parentClass, ClassLoader loader, String classPath) {
        try {
            Class<?> clazz = Class.forName(classPath, true, loader);
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
