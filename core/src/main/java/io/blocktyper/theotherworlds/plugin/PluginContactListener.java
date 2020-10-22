package io.blocktyper.theotherworlds.plugin;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;

import java.util.List;
import java.util.stream.Collectors;

public interface PluginContactListener extends PluginLoader {
    default ContactListener getContactListener() {

        List<ContactListener> listeners = getPlugins().values().stream().flatMap(p -> p.getContactListeners().stream()).collect(Collectors.toList());

        return new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                listeners.forEach(c -> c.beginContact(contact));
            }

            @Override
            public void endContact(Contact contact) {
                listeners.forEach(c -> c.endContact(contact));
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
                listeners.forEach(c -> c.preSolve(contact, oldManifold));
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
                listeners.forEach(c -> c.postSolve(contact, impulse));
            }
        };
    }
}
