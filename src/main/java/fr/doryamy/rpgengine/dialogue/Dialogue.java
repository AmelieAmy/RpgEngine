package fr.doryamy.rpgengine.dialogue;

import java.util.List;
import java.util.Objects;

/**
 * Représente un dialogue chargé depuis
 * la base de données.
 *
 * Un dialogue possède :
 * - une clé métier ;
 * - un nom lisible ;
 * - une liste ordonnée de lignes.
 *
 * Cette classe est immuable et ne contient
 * aucune logique métier.
 */
public final class Dialogue {

    private final String key;
    private final String name;
    private final List<DialogueLine> lines;

    /**
     * Construit un dialogue.
     *
     * @param key clé métier du dialogue
     * @param name nom lisible
     * @param lines lignes du dialogue
     */
    public Dialogue(
            String key,
            String name,
            List<DialogueLine> lines
    ) {
        this.key = Objects.requireNonNull(
                key,
                "La clé du dialogue ne peut pas être null."
        );

        this.name = Objects.requireNonNull(
                name,
                "Le nom du dialogue ne peut pas être null."
        );

        this.lines = List.copyOf(
                Objects.requireNonNull(
                        lines,
                        "Les lignes du dialogue ne peuvent pas être null."
                )
        );
    }

    public String getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    public List<DialogueLine> getLines() {
        return lines;
    }
}