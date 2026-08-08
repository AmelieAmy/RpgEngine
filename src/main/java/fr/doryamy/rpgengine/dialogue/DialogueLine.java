package fr.doryamy.rpgengine.dialogue;

/**
 * Représente une ligne appartenant à un dialogue.
 *
 * Chaque ligne possède une position qui détermine
 * son ordre d'affichage.
 *
 * Cette classe est immuable et ne contient
 * aucune logique métier.
 */
public final class DialogueLine {

    private final int position;
    private final String text;

    /**
     * Construit une ligne de dialogue.
     *
     * @param position ordre d'affichage
     * @param text texte affiché au joueur
     */
    public DialogueLine(
            int position,
            String text
    ) {
        this.position = position;
        this.text = text;
    }

    public int getPosition() {
        return position;
    }

    public String getText() {
        return text;
    }
}