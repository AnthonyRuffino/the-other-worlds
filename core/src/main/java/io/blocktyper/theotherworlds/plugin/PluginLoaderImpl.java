package io.blocktyper.theotherworlds.plugin;

import com.badlogic.gdx.physics.box2d.World;
import com.fasterxml.jackson.databind.JsonNode;
import io.blocktyper.theotherworlds.config.FileUtils;

import java.io.File;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
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
                if (configRaw == null) {
                    System.out.println("No config for plugin: " + pluginName);
                    continue;
                }
                JsonNode config = FileUtils.getJsonNodeFromRawString(configRaw);
                if (config == null) {
                    System.out.println("Null config for plugin: " + pluginName);
                    continue;
                }
                if (!Optional.ofNullable(config.get("enabled")).map(JsonNode::booleanValue).orElse(false)) {
                    System.out.println("Plugin disabled: " + pluginName);
                    continue;
                }
                String classPath = Optional.ofNullable(config.get("classPath")).map(c -> c.textValue()).orElse(null);
                if (classPath == null || classPath.trim().isEmpty()) {
                    System.out.println("classPath not configured plugin: " + pluginName);
                    continue;
                }
                File pluginJar = new File(pluginRootPath + ".jar");
                if (!pluginJar.exists()) {
                    System.out.println("Jar not found for plugin: " + pluginName);
                    continue;
                }

                Plugin plugin = loader.LoadClass(pluginJar, classPath, Plugin.class);

                if (plugin == null) {
                    System.out.println("Plugin could not be loaded: " + pluginName);
                    continue;
                }
                plugin.init(pluginServer, config);
                plugins.put(pluginName, plugin);
                System.out.println("Plugin loaded: " + pluginName);
            }
        }
    }
}
