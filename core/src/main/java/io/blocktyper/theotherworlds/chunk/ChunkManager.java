package io.blocktyper.theotherworlds.chunk;

import com.badlogic.gdx.math.Vector3;
import io.blocktyper.theotherworlds.visible.WorldEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkManager {

    private final int chunkSize;
    private final Map<Vector3, Chunk> chunks = new ConcurrentHashMap<>();

    public ChunkManager(int chunkSize) {
        this.chunkSize = chunkSize;
    }

    private Chunk getChunk(int x, int y) {
        Vector3 address = new Vector3(x / chunkSize, y / chunkSize, 0);
        Chunk chunk = chunks.get(address);
        if (chunk == null) {
            chunk = new Chunk(address);
            chunks.put(address, chunk);
        }
        return chunk;
    }

    public Chunk getChunk(WorldEntity worldEntity) {
        return getChunk(worldEntity.getVector().x, worldEntity.getVector().y);
    }

    public Chunk getChunk(Vector3 point) {
        return getChunk(point.x, point.y);
    }

    public Chunk getChunk(float x, float y) {
        return getChunk(Math.round(x), Math.round(y));
    }

    public List<Chunk> getSurroundingChunks(int x, int y) {
        return getSurroundingChunks(x, y, 1);
    }

    public List<Chunk> getSurroundingChunks(float x, float y, int radius) {
        return getSurroundingChunks(getChunk(x, y), radius);
    }

    public List<Chunk> getSurroundingChunks(int x, int y, int radius) {
        return getSurroundingChunks(getChunk(x, y), radius);
    }

    public List<Chunk> getSurroundingChunks(Vector3 vector, int radius) {
        return getSurroundingChunks(getChunk(vector.x, vector.y), radius);
    }

    public List<Chunk> getSurroundingChunks(Chunk chunk, int radius) {
        int chunkX = Math.round(chunk.getAddress().x);
        int chunkY = Math.round(chunk.getAddress().y);
        List<Chunk> surroundingChunks = new ArrayList<>();
        surroundingChunks.add(chunk);
        for (int rx = 1; rx <= radius; rx++) {
            for (int ry = 1; ry <= radius; ry++) {
                surroundingChunks.addAll(List.of(
                        getChunk(new Vector3(chunkX + rx, chunkY, 0)),
                        getChunk(new Vector3(chunkX - rx, chunkY, 0)),
                        getChunk(new Vector3(chunkX + rx, chunkY + ry, 0)),
                        getChunk(new Vector3(chunkX - rx, chunkY - ry, 0)),
                        getChunk(new Vector3(chunkX, chunkY + ry, 0)),
                        getChunk(new Vector3(chunkX, chunkY - ry, 0)),
                        getChunk(new Vector3(chunkX + rx, chunkY - ry, 0)),
                        getChunk(new Vector3(chunkX - rx, chunkY + ry, 0))
                ));
            }
        }
        return surroundingChunks;
    }

    public List<WorldEntity> getThingsInChunks(List<Chunk> chunks) {
        //return chunks.stream().flatMap(chunk -> chunk.getThings().stream()).collect(Collectors.toList());
        List<WorldEntity> returnList = new ArrayList<>();
        for (Chunk chunk : chunks) {
            returnList.addAll(chunk.getThings());
        }

        return returnList;
    }

    public int addThingToChunk(WorldEntity worldEntity) {
        removeThingFromChunk(worldEntity);
        Chunk chunk = getChunk(worldEntity);
        chunk.getThings().add(worldEntity);
        worldEntity.setChunk(chunk);
        return chunk.getThings().size();
    }


    public void updateChunkIfNeeded(WorldEntity worldEntity) {
        if(worldEntity.getChunk().isEmpty()) {
            return;
        }
        Chunk currentChunk = getChunk(worldEntity);
        if(worldEntity.getChunk().isEmpty() || worldEntity.getChunk().get() != currentChunk) {
            removeThingFromChunk(worldEntity);
            System.out.println("REMOVING!!!!!!!!!!!!!!!!");
        }
        currentChunk.getThings().add(worldEntity);
        worldEntity.setChunk(currentChunk);
    }

    public void removeThingFromChunk(WorldEntity worldEntity) {
        worldEntity.getChunk().ifPresent(oldChunk -> oldChunk.getThings().remove(worldEntity));
    }
}
