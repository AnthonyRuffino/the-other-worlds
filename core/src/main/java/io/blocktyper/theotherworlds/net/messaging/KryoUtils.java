package io.blocktyper.theotherworlds.net.messaging;

import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.esotericsoftware.kryo.Kryo;
import io.blocktyper.theotherworlds.visible.*;
import io.blocktyper.theotherworlds.visible.impl.EntityImpl;
import io.blocktyper.theotherworlds.visible.impl.EntityUpdateImpl;
import io.blocktyper.theotherworlds.visible.impl.HudElementImpl;
import io.blocktyper.theotherworlds.visible.impl.HudElementUpdateImpl;
import io.blocktyper.theotherworlds.visible.spec.Entity;
import io.blocktyper.theotherworlds.visible.spec.EntityUpdate;
import io.blocktyper.theotherworlds.visible.spec.HudElement;
import io.blocktyper.theotherworlds.visible.spec.HudElementUpdate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class KryoUtils {
    public static void registerClasses(Kryo kryo) {
        kryo.register(Matrix4.class);
        kryo.register(Vector3.class);

        kryo.register(List.class);
        kryo.register(ArrayList.class);
        kryo.register(Optional.class);

        kryo.register(PerformActionRequest.class);

        kryo.register(LoginRequest.class);
        kryo.register(ConnectResponse.class);

        kryo.register(EntityUpdates.class);
        kryo.register(EntityRemovals.class);
        kryo.register(HudUpdates.class);
        kryo.register(HudRemovals.class);


        kryo.register(Entity.class);
        kryo.register(EntityUpdate.class);
        kryo.register(HudElement.class);
        kryo.register(HudElementUpdate.class);
        kryo.register(RelativeState.class);

        kryo.register(EntityImpl.class);
        kryo.register(EntityUpdateImpl.class);
        kryo.register(HudElementImpl.class);
        kryo.register(HudElementUpdateImpl.class);

        kryo.register(byte[].class);
    }

}
