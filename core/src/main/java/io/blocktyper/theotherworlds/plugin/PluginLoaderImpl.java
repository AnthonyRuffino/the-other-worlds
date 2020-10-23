package io.blocktyper.theotherworlds.plugin;

import com.badlogic.gdx.physics.box2d.World;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.blocktyper.theotherworlds.plugin.utils.FileUtils;

import java.io.*;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

public class PluginLoaderImpl implements PluginLoader, PluginContactListener, PluginStaticWorldEntities, PluginEntityCreator {
    PluginServer pluginServer;
    Map<String, Plugin> plugins = new HashMap<>();
    Map<String, EntityCreator> entityCreators;


    @Override
    public Map<String, EntityCreator> getEntityCreators() {
        return entityCreators;
    }

    @Override
    public Map<String, Plugin> getPlugins() {
        return plugins;
    }

    @Override
    public World getWorld() {
        return pluginServer.getWorld();
    }

    public PluginLoaderImpl(String pluginsPath, PluginServer pluginServer, boolean defaultOnly) {
        this.pluginServer = pluginServer;

        if (defaultOnly) {
            Plugin defaultPlugin = new PluginExample();
            defaultPlugin.init(pluginServer, null);
            plugins.put("default", defaultPlugin);
        } else {
            loadAllInstalledPlugins(pluginsPath, pluginServer);
        }

        entityCreators = getPlugins().entrySet().stream()
                .flatMap(plugin -> plugin.getValue().getEntityCreator()
                        .map(ec -> new AbstractMap.SimpleEntry<>(plugin.getKey(), ec)).stream())
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue, (a, b) -> a));

    }

    private void loadAllInstalledPlugins(String pluginsPath, PluginServer pluginServer) {
        ExtensionLoader<Plugin> loader = new ExtensionLoader<>();

        File pluginsDir = new File(pluginsPath);

        for (File dir : pluginsDir.listFiles()) {

            if (dir.isDirectory()) {
                String pluginName = dir.getName().toLowerCase();
                System.out.println("Plugin located: " + pluginName);
                String pluginRootPath = dir.getAbsolutePath() + "/" + pluginName;
                String configRaw = FileUtils.getLocalFileString(pluginRootPath + "_config.json");
                String classPath = null;
                JsonNode config = null;
                boolean enabled = true;

                if (configRaw == null) {
                    System.out.println("No config for plugin: " + pluginName);
                } else {
                    config = FileUtils.getJsonNodeFromRawString(configRaw);
                    if (config == null) {
                        System.out.println("Null config for plugin: " + pluginName);
                        continue;
                    }
                    enabled = Optional.ofNullable(config.get("enabled"))
                            .map(JsonNode::booleanValue).orElse(true);

                    classPath = Optional.ofNullable(config.get("classPath"))
                            .map(JsonNode::textValue).orElse(null);
                }

                File pluginJar = new File(pluginRootPath + ".jar");
                if (!pluginJar.exists()) {
                    System.out.println("Jar not found for plugin: " + pluginName);
                    continue;
                }

                if(classPath == null || classPath.trim().isEmpty()) {
                    try {
                        final InputStream targetStream =
                                new DataInputStream(new FileInputStream(pluginJar));

                        JarInputStream jarStream = new JarInputStream(targetStream);
                        Manifest manifest = jarStream.getManifest();
                        classPath = (String) Optional.ofNullable(manifest)
                                .map(mf -> mf.getMainAttributes().get(new Attributes.Name("Main-Class")))
                                .orElse(null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                if (classPath == null || classPath.trim().isEmpty()) {
                    System.out.println("classPath not configured plugin: " + pluginName);
                    continue;
                }

                Plugin plugin = loader.LoadClass(pluginJar, classPath, Plugin.class);

                if (plugin == null) {
                    System.out.println("Plugin could not be loaded: " + pluginName);
                    continue;
                }
                plugin.init(pluginServer, config);
                JsonNode mergedConfig = plugin.getConfig();

                if(mergedConfig == null) {
                    mergedConfig = FileUtils.getJsonNodeFromRawString("{}");
                }

                if(!mergedConfig.has("classPath")) {
                    ((ObjectNode)mergedConfig).put("classPath", classPath);
                }

                try {
                    String json = FileUtils.OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(mergedConfig);
                    FileUtils.writeFile(pluginRootPath + "_config.json", json.getBytes());
                } catch (JsonProcessingException e) {
                    e.printStackTrace();
                }

                if (enabled) {
                    enabled = Optional.ofNullable(mergedConfig.get("enabled"))
                            .map(JsonNode::booleanValue).orElse(true);
                    if(!enabled) {
                        System.out.println("Plugin disabled: " + pluginName);
                        continue;
                    }
                }

                plugins.put(pluginName, plugin);
                System.out.println("Plugin loaded: " + pluginName);
            }
        }
    }
}
