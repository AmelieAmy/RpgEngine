package fr.doryamy.rpgengine.model;

import java.util.List;
import java.util.Objects;

/**
 * Représente un déclencheur RPG chargé depuis la base de données.
 *
 * Un trigger contient son identité, sa cible, ses conditions et
 * les actions à exécuter lorsque les conditions sont validées.
 *
 * Cette classe est immuable et ne contient aucune logique métier.
 */
public final class Trigger {

    private final int id;
    private final String name;
    private final String targetId;
    private final TriggerType type;
    private final List<Action> actions;
    private final List<Condition> conditions;

    /**
     * Construit un trigger complet.
     *
     * @param id identifiant interne du trigger
     * @param name nom lisible du trigger
     * @param targetId identifiant métier de la cible
     * @param type type de déclencheur
     * @param conditions conditions associées
     * @param actions actions associées
     */
    public Trigger(
            int id,
            String name,
            String targetId,
            TriggerType type,
            List<Condition> conditions,
            List<Action> actions
    ) {
        this.id = id;

        this.name = Objects.requireNonNull(
                name,
                "Le nom du trigger ne peut pas être null."
        );

        if (targetId == null || targetId.isBlank()) {
            throw new IllegalArgumentException(
                    "L'identifiant de la cible ne peut pas être vide."
            );
        }

        this.targetId = targetId;

        this.type = Objects.requireNonNull(
                type,
                "Le type du trigger ne peut pas être null."
        );

        this.conditions = List.copyOf(
                Objects.requireNonNull(
                        conditions,
                        "La liste des conditions ne peut pas être null."
                )
        );

        this.actions = List.copyOf(
                Objects.requireNonNull(
                        actions,
                        "La liste des actions ne peut pas être null."
                )
        );
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getTargetId() {
        return targetId;
    }

    public TriggerType getType() {
        return type;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public List<Action> getActions() {
        return actions;
    }
}