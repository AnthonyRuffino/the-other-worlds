package io.blocktyper.theotherworlds.chunk;

import com.badlogic.gdx.math.Vector3;
import io.blocktyper.theotherworlds.visible.WorldEntity;

import java.util.ArrayList;
import java.util.List;

public class Chunk {
    private final Vector3 address;
    private List<WorldEntity> worldEntities = new ArrayList<>();

    public Chunk(Vector3 address) {
        this.address = address;
    }

    public List<WorldEntity> getThings() {
        return worldEntities;
    }

    public Vector3 getAddress() {
        return address;
    }
}
