package fr.doryamy.rpgengine.dialogue;

import fr.doryamy.rpgengine.model.Action;
import fr.doryamy.rpgengine.model.Condition;

import java.util.List;
import java.util.Objects;

/**
 * Représente une transition entre deux nodes
 * d'un dialogue.
 *
 * Une transition possède une clé métier unique
 * dans son dialogue.
 *
 * Elle peut :
 * - être automatique ;
 * - représenter un choix joueur ;
 * - terminer explicitement le dialogue.
 *
 * Elle peut également porter des conditions
 * et des actions interprétées par le Rule Engine.
 *
 * Cette classe est immuable.
 */
public final class DialogueTransition {

    private final String key;

    private final String sourceNodeKey;
    private final String targetNodeKey;

    private final DialogueTransitionType type;
    private final String label;

    private final int position;

    private final List<Condition> conditions;
    private final List<Action> actions;

    public DialogueTransition(
            String key,
            String sourceNodeKey,
            String targetNodeKey,
            DialogueTransitionType type,
            String label,
            int position,
            List<Condition> conditions,
            List<Action> actions
    ) {
        this.key =
                Objects.requireNonNull(
                        key,
                        "La clé de la transition ne peut pas être null."
                );

        this.sourceNodeKey =
                Objects.requireNonNull(
                        sourceNodeKey,
                        "Le node source ne peut pas être null."
                );

        this.targetNodeKey =
                targetNodeKey;

        this.type =
                Objects.requireNonNull(
                        type,
                        "Le type de transition ne peut pas être null."
                );

        this.label =
                label;

        this.position =
                position;

        this.conditions =
                List.copyOf(
                        Objects.requireNonNull(
                                conditions,
                                "Les conditions ne peuvent pas être null."
                        )
                );

        this.actions =
                List.copyOf(
                        Objects.requireNonNull(
                                actions,
                                "Les actions ne peuvent pas être null."
                        )
                );
    }

    public String getKey() {
        return key;
    }

    public String getSourceNodeKey() {
        return sourceNodeKey;
    }

    public String getTargetNodeKey() {
        return targetNodeKey;
    }

    public DialogueTransitionType getType() {
        return type;
    }

    public String getLabel() {
        return label;
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