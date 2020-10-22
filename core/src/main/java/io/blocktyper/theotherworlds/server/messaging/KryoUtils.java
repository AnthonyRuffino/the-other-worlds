package io.blocktyper.theotherworlds.server.messaging;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.kryo.Kryo;
import io.blocktyper.theotherworlds.server.world.WorldEntityUpdate;
import io.blocktyper.theotherworlds.visible.RelativeState;

import java.util.*;

public class KryoUtils {
    public static void registerClasses(Kryo kryo) {
        //collections/containers
        kryo.register(List.class);
        kryo.register(ArrayList.class);
        kryo.register(Set.class);
        kryo.register(HashSet.class);
        kryo.register(Optional.class);

        //Connection
        kryo.register(LoginRequest.class);
        kryo.register(ConnectResponse.class);

        //Entity management
        kryo.register(WorldEntityUpdates.class);
        kryo.register(WorldEntityUpdate.class);
        kryo.register(MissingWorldEntities.class);
        kryo.register(WorldEntityRemovals.class);
        kryo.register(ImageRequest.class);
        kryo.register(ImageResponse.class);

        //Drawing
        kryo.register(Drawable.class);
        kryo.register(Line.class);
        kryo.register(Color.class);

        //User interaction
        kryo.register(PerformActionRequest.class);

        //LibGDX
        kryo.register(Matrix4.class);
        kryo.register(Vector2.class);
        kryo.register(Vector3.class);

        //other
        kryo.register(RelativeState.class);
        kryo.register(byte[].class);
    }

}
