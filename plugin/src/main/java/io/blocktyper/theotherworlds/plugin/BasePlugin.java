package io.blocktyper.theotherworlds.plugin;

import com.fasterxml.jackson.databind.JsonNode;
import io.blocktyper.theotherworlds.plugin.utils.FileUtils;

import java.util.Optional;

public abstract class BasePlugin implements Plugin {
    protected PluginServer pluginServer;
    protected JsonNode config;

    @Override
    public void init(PluginServer pluginServer, JsonNode config) {
        this.pluginServer = pluginServer;
        this.config = FileUtils.getJsonNodeWithLocalOverride(
                getClass().getClassLoader(),
                getConfigResourcePath(),
                Optional.ofNullable(config)
        );
    }

    @Override
    public JsonNode getConfig() {
        return config;
    }
}
