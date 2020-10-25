package io.blocktyper.theotherworlds.plugin;

import com.fasterxml.jackson.databind.JsonNode;
import io.blocktyper.theotherworlds.plugin.utils.FileUtils;

import java.util.Optional;

public abstract class BasePlugin implements Plugin {
    protected String pluginName;
    protected PluginServer pluginServer;
    protected JsonNode config;

    @Override
    public void init(String pluginName, PluginServer pluginServer, JsonNode config) {
        this.pluginName = pluginName;
        this.pluginServer = pluginServer;
        this.config = FileUtils.getJsonNodeWithLocalOverride(
                getClass().getClassLoader(),
                pluginName + "/" + Plugin.CONFIG_NAME,
                Optional.ofNullable(config)
        );
    }

    @Override
    public JsonNode getConfig() {
        return config;
    }
}
