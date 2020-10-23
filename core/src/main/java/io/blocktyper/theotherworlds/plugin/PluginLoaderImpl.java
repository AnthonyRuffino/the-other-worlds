package io.blocktyper.theotherworlds.plugin;

import com.badlogic.gdx.physics.box2d.World;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.blocktyper.theotherworlds.plugin.utils.FileUtils;

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
                Optional<String> classPath = Optional.empty();
                final JsonNode config;
                boolean enabled = true;

                if (configRaw == null) {
                    config = null;
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
                            .map(JsonNode::textValue);
                }

                File pluginJar = new File(pluginRootPath + ".jar");
                if (!pluginJar.exists()) {
                    System.out.println("Jar not found for plugin: " + pluginName);
                    continue;
                }

                final boolean enabledStatusBeforeLoading = enabled;
                loader.loadPlugin(pluginJar, Plugin.class, classPath)
                        .ifPresentOrElse(plugin -> this.loadPlugin(pluginName, plugin, enabledStatusBeforeLoading, config, pluginRootPath),
                                () -> System.out.println("Plugin could not be loaded: " + pluginName));

            }
        }
    }

    private void loadPlugin(String pluginName, Plugin plugin, boolean enabledStatusBeforeLoading, JsonNode config, String pluginRootPath) {
        if (plugin == null) {
            System.out.println("Plugin could not be loaded: " + pluginName);

        }
        plugin.init(pluginServer, config);
        JsonNode mergedConfig = plugin.getConfig();

        if (mergedConfig == null) {
            mergedConfig = FileUtils.getJsonNodeFromRawString("{}");
        }

        try {
            String json = FileUtils.OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(mergedConfig);
            FileUtils.writeFile(pluginRootPath + "_config.json", json.getBytes());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        boolean enabled = enabledStatusBeforeLoading;
        if (enabledStatusBeforeLoading) {
            enabled = Optional.ofNullable(mergedConfig.get("enabled"))
                    .map(JsonNode::booleanValue).orElse(true);
        }

        if (!enabled) {
            System.out.println("Plugin disabled: " + pluginName);
            return;
        }

        plugins.put(pluginName, plugin);
        System.out.println("Plugin loaded: " + pluginName);
    }
}
