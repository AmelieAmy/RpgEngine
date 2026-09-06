package fr.doryamy.rpgengine.npc;

import java.util.Optional;

/**
 * Port de lecture des PNJ externes.
 */
public interface NpcService {

    Optional<NpcSummary> find(
            String id
    );
}
