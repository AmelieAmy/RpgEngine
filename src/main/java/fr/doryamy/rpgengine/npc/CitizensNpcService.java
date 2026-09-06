package fr.doryamy.rpgengine.npc.citizens;

import fr.doryamy.rpgengine.npc.NpcService;
import fr.doryamy.rpgengine.npc.NpcSummary;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;

import java.util.Objects;
import java.util.Optional;

/**
 * Adaptateur Citizens du port NpcService.
 */
public final class CitizensNpcService
        implements NpcService {

    @Override
    public Optional<NpcSummary> find(
            String id
    ) {

        Objects.requireNonNull(
                id,
                "id"
        );

        final int npcId;

        try {
            npcId =
                    Integer.parseInt(
                            id.trim()
                    );
        } catch (NumberFormatException e) {
            return Optional.empty();
        }

        NPC npc =
                CitizensAPI.getNPCRegistry()
                        .getById(
                                npcId
                        );

        if (npc == null) {
            return Optional.empty();
        }

        String name =
                npc.getName();

        if (name == null
                || name.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(
                new NpcSummary(
                        String.valueOf(npcId),
                        name
                )
        );
    }
}
