package fr.doryamy.rpgengine.dialogue;

import fr.doryamy.rpgengine.trigger.TriggerContext;

import java.util.Objects;
import java.util.UUID;

/**
 * Représente l'état temporaire d'un joueur
 * pendant l'exécution d'un dialogue.
 *
 * Une session conserve :
 * - le joueur concerné ;
 * - le dialogue complet en cours ;
 * - le node actuellement affiché ;
 * - le contexte d'exécution initial.
 *
 * La session ne contient aucune logique
 * d'évaluation ou d'exécution.
 */
public final class DialogueSession {

    private final UUID playerUuid;
    private final Dialogue dialogue;
    private final TriggerContext context;

    private String currentNodeKey;

    public DialogueSession(
            UUID playerUuid,
            Dialogue dialogue,
            String currentNodeKey,
            TriggerContext context
    ) {
        this.playerUuid =
                Objects.requireNonNull(
                        playerUuid,
                        "L'UUID du joueur ne peut pas être null."
                );

        this.dialogue =
                Objects.requireNonNull(
                        dialogue,
                        "Le dialogue ne peut pas être null."
                );

        this.currentNodeKey =
                Objects.requireNonNull(
                        currentNodeKey,
                        "Le node courant ne peut pas être null."
                );

        this.context =
                Objects.requireNonNull(
                        context,
                        "Le contexte ne peut pas être null."
                );
    }

    public UUID getPlayerUuid() {
        return playerUuid;
    }

    public Dialogue getDialogue() {
        return dialogue;
    }

    public String getDialogueKey() {
        return dialogue.getKey();
    }

    public String getCurrentNodeKey() {
        return currentNodeKey;
    }

    public TriggerContext getContext() {
        return context;
    }

    public void setCurrentNodeKey(
            String currentNodeKey
    ) {
        this.currentNodeKey =
                Objects.requireNonNull(
                        currentNodeKey,
                        "Le node courant ne peut pas être null."
                );
    }
}