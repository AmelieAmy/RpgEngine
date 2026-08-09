package fr.doryamy.rpgengine.dialogue;

import java.util.Objects;

/**
 * Représente une unité narrative atomique d'un dialogue.
 *
 * Un node contient uniquement :
 *   une clé métier ;
 *   un texte.
 *
 * Il ne connaît ni les transitions,
 * ni les conditions,
 * ni les actions.
 *
 * Cette classe est immuable.
 */
public final class DialogueNode {

    private final String key;
    private final String text;

    /**
     * Construit un node de dialogue.
     *
     * @param key clé métier du node
     * @param text contenu narratif
     */
    public DialogueNode(
            String key,
            String text
    ) {
        this.key = Objects.requireNonNull(
                key,
                "La clé du node ne peut pas être null."
        );

        this.text = Objects.requireNonNull(
                text,
                "Le texte du node ne peut pas être null."
        );
    }

    public String getKey() {
        return key;
    }

    public String getText() {
        return text;
    }
}