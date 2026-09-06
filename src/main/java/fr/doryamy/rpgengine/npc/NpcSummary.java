package fr.doryamy.rpgengine.npc;

import java.util.Objects;

/**
 * Projection minimale d'un PNJ externe utilisable par RPGEngine.
 */
public record NpcSummary(
        String id,
        String name
) {

    public NpcSummary {

        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");

        if (id.isBlank()) {
            throw new IllegalArgumentException(
                    "L'identifiant du PNJ ne peut pas être vide."
            );
        }

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "Le nom du PNJ ne peut pas être vide."
            );
        }

        id = id.trim();
        name = name.trim();
    }
}
