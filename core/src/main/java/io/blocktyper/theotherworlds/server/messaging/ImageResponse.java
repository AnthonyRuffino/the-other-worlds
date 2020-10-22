package io.blocktyper.theotherworlds.server.messaging;

public class ImageResponse {
    public String name;
    public byte[] bytes;

    public ImageResponse setName(String name) {
        this.name = name;
        return this;
    }

    public ImageResponse setBytes(byte[] bytes) {
        this.bytes = bytes;
        return this;
    }
}
