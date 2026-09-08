package fr.doryamy.rpgengine.dialogue;

import java.util.Objects;

/**
 * Agrégat métier représentant un dialogue.
 */
public record Dialogue(
        DialogueKey key,
        String name,
        DialogueRules rules,
        DialogueGraph graph
) {

    public Dialogue {

        Objects.requireNonNull(
                key,
                "La clé du dialogue ne peut pas être null."
        );

        Objects.requireNonNull(
                rules,
                "Les règles globales du dialogue ne peuvent pas être null."
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

    /**
     * Constructeur de compatibilité pour les appelants qui ne
     * définissent pas encore de règles globales.
     */
    public Dialogue(
            DialogueKey key,
            String name,
            DialogueGraph graph
    ) {
        this(
                key,
                name,
                DialogueRules.empty(),
                graph
        );
    }
}