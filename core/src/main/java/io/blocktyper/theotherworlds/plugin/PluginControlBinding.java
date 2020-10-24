package io.blocktyper.theotherworlds.plugin;

import io.blocktyper.theotherworlds.plugin.controls.ControlBindings;
import org.jetbrains.annotations.NotNull;

import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface PluginControlBinding extends PluginLoader {
    @Override
    default Map<String, ControlBindings> getControlBindings() {
        return getPlugins().entrySet().stream()
                .flatMap(this::getControlBindingsStream)
                .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue, (a, b) -> a));
    }

    @NotNull
    private Stream<AbstractMap.SimpleEntry<String, ControlBindings>> getControlBindingsStream(Map.Entry<String, Plugin> plugin) {
        return plugin.getValue()
                .getControlBindings()
                .map(controlBindings -> new AbstractMap.SimpleEntry<>(plugin.getKey(), controlBindings))
                .stream();
    }
}
