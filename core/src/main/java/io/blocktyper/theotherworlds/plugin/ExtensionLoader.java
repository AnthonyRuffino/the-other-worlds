package io.blocktyper.theotherworlds.plugin;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

public class ExtensionLoader<C> {

    public C LoadClass(File jar, String classpath, Class<C> parentClass) throws RuntimeException {
        try {
            ClassLoader loader = URLClassLoader.newInstance(
                    new URL[]{jar.toURL()},
                    getClass().getClassLoader()
            );

            Class<?> clazz = Class.forName(classpath, true, loader);
            Class<? extends C> newClass = clazz.asSubclass(parentClass);
            Constructor<? extends C> constructor = newClass.getConstructor();
            return constructor.newInstance();

        } catch (ClassNotFoundException
                | MalformedURLException
                | NoSuchMethodException
                | InvocationTargetException
                | IllegalAccessException
                | InstantiationException ex) {
            ex.printStackTrace();
        }
        System.out.println("Plugin not found: " + classpath);
        return null;
    }
}
