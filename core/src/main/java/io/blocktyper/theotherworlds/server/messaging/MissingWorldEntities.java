package io.blocktyper.theotherworlds.server.messaging;

import java.util.Set;

public class MissingWorldEntities {

    private Set<String> missingEntities;

    private MissingWorldEntities() {

    }

    public MissingWorldEntities(Set<String> missingEntities) {
        this.missingEntities = missingEntities;
    }

    public Set<String> getMissingEntities() {
        return missingEntities;
    }

    public void setMissingEntities(Set<String> missingEntities) {
        this.missingEntities = missingEntities;
    }

}
