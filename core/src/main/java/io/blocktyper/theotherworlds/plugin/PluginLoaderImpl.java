package io.blocktyper.theotherworlds.plugin;

import com.badlogic.gdx.physics.box2d.World;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import io.blocktyper.theotherworlds.plugin.actions.ActionListener;
import io.blocktyper.theotherworlds.plugin.actions.PlayerAction;
import io.blocktyper.theotherworlds.plugin.actions.PlayerConnectionListener;
import io.blocktyper.theotherworlds.plugin.utils.FileUtils;
import io.blocktyper.theotherworlds.server.messaging.PerformActionRequest;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class PluginLoaderImpl implements PluginLoader, PluginContactListener, PluginStaticWorldEntities, PluginEntityCreator, PluginControlBinding {
    PluginServer pluginServer;
    Map<String, Plugin> plugins = new HashMap<>();
    Map<String, EntityCreator> entityCreators;
    String pluginsPath;
    Map<String, List<ActionListener>> actionListeners;
    Map<String, PlayerConnectionListener> playerConnectionListeners;

    @Override
    public Map<String, EntityCreator> getEntityCreators() {
        return entityCreators;
    }

    @Override
    public void handlePlayerConnection(String player, boolean isDisconnect) {
        playerConnectionListeners.values().forEach(playerConnectionListener -> {
            playerConnectionListener.handlePlayerConnection(player, isDisconnect);
        });
    }

    @Override
    public void handlePlayerActions(String player, PerformActionRequest performActionRequest) {
        Optional.ofNullable(actionListeners.get(performActionRequest.action))
                .ifPresent(listeners ->
                        listeners.forEach(listener ->
                                listener.handlePlayerActions(List.of(new PlayerAction(player, performActionRequest.action, performActionRequest.target, performActionRequest.cancel)))
                        )
                );
    }

    @Override
    public Map<String, Plugin> getPlugins() {
        return plugins;
    }

    @Override
    public World getWorld() {
        return pluginServer.getWorld();
    }

    public PluginLoaderImpl(String pluginsPath, PluginServer pluginServer) {
        this.pluginsPath = pluginsPath;
        this.pluginServer = pluginServer;

        System.out.println(loadAllInstalledPlugins() + " Plugins loaded...");

        entityCreators = getPlugins().entrySet().stream()
                .flatMap(plugin -> plugin.getValue().getEntityCreator()
                        .map(ec -> new AbstractMap.SimpleEntry<>(plugin.getKey(), ec)).stream())
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue, (a, b) -> a));

        actionListeners = getPlugins().values().stream()
                .flatMap(plugin -> plugin.getActionListener().stream())
                .flatMap(actionListener ->
                        actionListener.getInterests().stream().map(interest ->
                                new AbstractMap.SimpleEntry<>(interest, actionListener)
                        )
                )
                .collect(Collectors.groupingBy(
                        Map.Entry::getKey,
                        Collectors.mapping(Map.Entry::getValue, Collectors.toList()))
                );

        playerConnectionListeners = getPlugins().entrySet().stream()
                .flatMap(pluginNameEntry ->
                        pluginNameEntry.getValue().getPlayerConnectionListener().stream()
                                .map(cl -> new AbstractMap.SimpleEntry<>(pluginNameEntry.getKey(), cl))
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a,b) -> a));

    }

    private int loadAllInstalledPlugins() {
        System.out.println("Loading plugins...");
        File pluginsDir = new File(pluginsPath);
        if (pluginsDir == null || !pluginsDir.exists()) {
            System.out.println("No plugins directory...");
            return 0;
        }
        for (File dir : pluginsDir.listFiles()) {

            if (dir.isDirectory()) {
                String pluginName = dir.getName().toLowerCase();
                System.out.println("Plugin located: " + pluginName);
                String pluginRootPath = dir.getAbsolutePath() + "/";
                String configRaw = FileUtils.getLocalFileString(pluginRootPath + pluginName + "_config.json");
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

                File pluginJar = new File(pluginRootPath + pluginName + ".jar");
                if (!pluginJar.exists()) {
                    System.out.println("Jar not found for plugin: " + pluginName);
                    continue;
                }

                final ExtensionLoader<Plugin> loader = new ExtensionLoader<>();
                final boolean enabledStatusBeforeLoading = enabled;


                final PluginData pluginData = new PluginData(loader, pluginName, enabledStatusBeforeLoading, config, pluginRootPath);

                loader.loadPlugin(pluginJar, pluginName, Plugin.class, classPath)
                        .ifPresentOrElse(plugin -> this.loadPlugin(plugin, pluginData),
                                () -> System.out.println("Plugin could not be loaded: " + pluginName));

            }
        }
        return plugins.size();
    }

    private void loadPlugin(Plugin plugin, PluginData pluginData) {

        plugin.init(pluginData.pluginName, pluginServer, pluginData.config);
        JsonNode mergedConfig = plugin.getConfig();

        if (mergedConfig == null) {
            mergedConfig = FileUtils.getJsonNodeFromRawString("{}");
        }

        try {
            String json = FileUtils.getPrettyString(mergedConfig);
            FileUtils.writeFile(pluginData.pluginRootPath + pluginData.pluginName + "_config.json", json.getBytes());
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        boolean enabled = pluginData.enabledStatusBeforeLoading;
        if (pluginData.enabledStatusBeforeLoading) {
            enabled = Optional.ofNullable(mergedConfig.get("enabled"))
                    .map(JsonNode::booleanValue).orElse(true);
        }

        if (!enabled) {
            System.out.println("Plugin disabled: " + pluginData.pluginName);
            return;
        }

        plugins.put(pluginData.pluginName, plugin);
        System.out.println("Plugin loaded: " + pluginData.pluginName);


        String imageDirectory = pluginData.pluginName + "/img";
        pluginData.loader.getFileNamesInResourceDirectory(imageDirectory)
                .forEach(fileName -> {
                    String imagePath = pluginData.pluginRootPath + "img/" + fileName;
                    if (!Files.exists(Paths.get(imagePath))) {
                        FileUtils.writeFile(imagePath, pluginData.loader.getResourceAsStream(imageDirectory + "/" + fileName));
                    }
                });

    }

    private static class PluginData {
        ExtensionLoader<Plugin> loader;
        String pluginName;
        boolean enabledStatusBeforeLoading;
        JsonNode config;
        String pluginRootPath;

        public PluginData(ExtensionLoader<Plugin> loader, String pluginName, boolean enabledStatusBeforeLoading, JsonNode config, String pluginRootPath) {
            this.loader = loader;
            this.pluginName = pluginName;
            this.enabledStatusBeforeLoading = enabledStatusBeforeLoading;
            this.config = config;
            this.pluginRootPath = pluginRootPath;
        }
    }
}
