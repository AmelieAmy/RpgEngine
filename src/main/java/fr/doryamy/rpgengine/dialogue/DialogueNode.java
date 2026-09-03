package fr.doryamy.rpgengine.dialogue;

import java.util.Objects;

/**
 * Représente un node du graphe de dialogue.
 *
 * <p>Un node NPC porte une réplique narrative. Un node STRUCTURAL
 * est invisible et sert uniquement à découpler la structure du graphe
 * des répliques PNJ.
 */
public final class DialogueNode {

    private final String key;
    private final String text;
    private final DialogueNodeKind kind;

    public DialogueNode(
            String key,
            String text,
            DialogueNodeKind kind
    ) {
        this.key = Objects.requireNonNull(key, "La clé du node ne peut pas être null.");
        this.text = Objects.requireNonNull(text, "Le texte du node ne peut pas être null.");
        this.kind = Objects.requireNonNull(kind, "Le type du node ne peut pas être null.");
    }

    /** Compatibilité : les anciens appels créent des nodes PNJ. */
    public DialogueNode(String key, String text) {
        this(key, text, DialogueNodeKind.NPC);
    }

    public static DialogueNode structural(String key) {
        return new DialogueNode(key, "", DialogueNodeKind.STRUCTURAL);
    }

    public String getKey() { return key; }
    public String getText() { return text; }
    public DialogueNodeKind getKind() { return kind; }
    public boolean isStructural() { return kind == DialogueNodeKind.STRUCTURAL; }
}
