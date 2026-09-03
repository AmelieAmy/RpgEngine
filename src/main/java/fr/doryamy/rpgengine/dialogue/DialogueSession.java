package fr.doryamy.rpgengine.dialogue;

import fr.doryamy.rpgengine.trigger.TriggerContext;

import java.util.Objects;
import java.util.UUID;

/**
 * Représente l'état temporaire d'un joueur pendant l'exécution d'un dialogue.
 */
public final class DialogueSession {

    private final UUID playerUuid;
    private final Dialogue dialogue;
    private final TriggerContext context;

    private String currentNodeKey;

    private DialogueSessionPhase phase =
            DialogueSessionPhase.NODE;

    private String pendingTransitionKey;
    private int pendingPlayerReplyPosition;

    public DialogueSession(
            UUID playerUuid,
            Dialogue dialogue,
            String currentNodeKey,
            TriggerContext context
    ) {
        this.playerUuid = Objects.requireNonNull(
                playerUuid,
                "L'UUID du joueur ne peut pas être null."
        );

        this.dialogue = Objects.requireNonNull(
                dialogue,
                "Le dialogue ne peut pas être null."
        );

        this.currentNodeKey = Objects.requireNonNull(
                currentNodeKey,
                "Le node courant ne peut pas être null."
        );

        this.context = Objects.requireNonNull(
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

    public DialogueSessionPhase getPhase() {
        return phase;
    }

    public boolean isAutoPlayerReplyPhase() {
        return phase == DialogueSessionPhase.AUTO_PLAYER_REPLY;
    }

    public String getPendingTransitionKey() {
        return pendingTransitionKey;
    }

    public int getPendingPlayerReplyPosition() {
        return pendingPlayerReplyPosition;
    }

    public void beginAutoPlayerReply(
            String transitionKey,
            int playerReplyPosition
    ) {
        this.phase = DialogueSessionPhase.AUTO_PLAYER_REPLY;

        this.pendingTransitionKey = Objects.requireNonNull(
                transitionKey,
                "La transition en attente ne peut pas être null."
        );

        this.pendingPlayerReplyPosition = playerReplyPosition;
    }

    public void clearPresentationPhase() {
        this.phase = DialogueSessionPhase.NODE;
        this.pendingTransitionKey = null;
        this.pendingPlayerReplyPosition = 0;
    }

    public TriggerContext getContext() {
        return context;
    }

    public void setCurrentNodeKey(
            String currentNodeKey
    ) {
        this.currentNodeKey = Objects.requireNonNull(
                currentNodeKey,
                "Le node courant ne peut pas être null."
        );

        clearPresentationPhase();
    }
}
