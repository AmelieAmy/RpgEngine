package fr.doryamy.rpgengine.dialogue;

import java.util.Objects;

/**
 * Agrégat métier représentant un dialogue.
 */
public record Dialogue(
        DialogueKey key,
        String name,
        DialogueGraph graph
) {

    public Dialogue {

        Objects.requireNonNull(
                key,
                "La clé du dialogue ne peut pas être null."
        );

        Objects.requireNonNull(
                graph,
                "Le graphe du dialogue ne peut pas être null."
        );

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Le nom du dialogue ne peut pas être vide."
            );
        }

        name = name.trim();
    }
}