package io.blocktyper.theotherworlds.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ClientConfig {
    public boolean startServer = true;
    public String host = "localhost";
}
