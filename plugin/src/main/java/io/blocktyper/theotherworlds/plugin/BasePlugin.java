package io.blocktyper.theotherworlds.plugin;

import com.fasterxml.jackson.databind.JsonNode;

public abstract class BasePlugin implements Plugin {
    protected PluginServer pluginServer;
    protected JsonNode config;

    @Override
    public void init(PluginServer pluginServer, JsonNode config) {
        this.pluginServer = pluginServer;
        this.config = config;
    }
}
