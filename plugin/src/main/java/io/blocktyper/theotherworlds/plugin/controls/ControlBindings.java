package io.blocktyper.theotherworlds.plugin.controls;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ControlBindings {
    public String pluginName;
    public Map<String, Map<String, ButtonBinding>> gameModeButtonBindings;
    public Map<String, Map<String, KeyBinding>> gameModeKeyBindings;

    public ControlBindings setPluginName(String pluginName) {
        this.pluginName = pluginName;
        return this;
    }
}
