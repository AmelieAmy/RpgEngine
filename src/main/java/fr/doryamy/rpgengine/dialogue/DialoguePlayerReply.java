package fr.doryamy.rpgengine.dialogue;

import fr.doryamy.rpgengine.model.Action;
import fr.doryamy.rpgengine.model.Condition;

import java.util.List;
import java.util.Objects;

/**
 * Représente une réplique Joueur intégrée à une transition AUTO.
 *
 * Une réplique possède :
 * - un texte ;
 * - une position dans la séquence ;
 * - zéro ou plusieurs conditions ;
 * - zéro ou plusieurs actions.
 *
 * Les conditions déterminent si la réplique est affichée.
 * Les actions sont exécutées lorsque le joueur valide la réplique.
 *
 * Cette classe est immuable.
 */
public final class DialoguePlayerReply {

    private final String text;
    private final int position;
    private final List<Condition> conditions;
    private final List<Action> actions;

    public DialoguePlayerReply(
            String text,
            int position,
            List<Condition> conditions,
            List<Action> actions
    ) {
        this.text = Objects.requireNonNull(
                text,
                "Le texte de la réplique Joueur ne peut pas être null."
        );

        this.position = position;

        this.conditions = List.copyOf(
                Objects.requireNonNull(
                        conditions,
                        "Les conditions de la réplique Joueur ne peuvent pas être null."
                )
        );

        this.actions = List.copyOf(
                Objects.requireNonNull(
                        actions,
                        "Les actions de la réplique Joueur ne peuvent pas être null."
                )
        );
    }

    public String getText() {
        return text;
    }

    public int getPosition() {
        return position;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public List<Action> getActions() {
        return actions;
    }
}
