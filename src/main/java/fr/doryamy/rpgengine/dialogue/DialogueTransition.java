package fr.doryamy.rpgengine.dialogue;

import fr.doryamy.rpgengine.model.Action;
import fr.doryamy.rpgengine.model.Condition;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public final class DialogueTransition {

    private final String key;
    private final String sourceNodeKey;
    private final String targetNodeKey;
    private final DialogueTransitionType type;
    private final String label;
    private final int position;
    private final boolean terminal;
    private final List<Condition> conditions;
    private final List<Action> actions;
    private final List<DialoguePlayerReply> playerReplies;

    public DialogueTransition(
            String key,
            String sourceNodeKey,
            String targetNodeKey,
            DialogueTransitionType type,
            String label,
            int position,
            boolean terminal,
            List<Condition> conditions,
            List<Action> actions,
            List<DialoguePlayerReply> playerReplies
    ) {
        this.key = Objects.requireNonNull(key, "La clé de la transition ne peut pas être null.");
        this.sourceNodeKey = Objects.requireNonNull(sourceNodeKey, "Le node source ne peut pas être null.");
        this.targetNodeKey = targetNodeKey;
        this.type = Objects.requireNonNull(type, "Le type de transition ne peut pas être null.");
        this.label = label;
        this.position = position;
        this.terminal = terminal;
        this.conditions = List.copyOf(Objects.requireNonNull(conditions, "Les conditions ne peuvent pas être null."));
        this.actions = List.copyOf(Objects.requireNonNull(actions, "Les actions ne peuvent pas être null."));
        this.playerReplies = Objects.requireNonNull(playerReplies, "Les répliques Joueur ne peuvent pas être null.")
                .stream()
                .sorted(Comparator.comparingInt(DialoguePlayerReply::getPosition))
                .toList();
    }

    public DialogueTransition(
            String key, String sourceNodeKey, String targetNodeKey,
            DialogueTransitionType type, String label, int position,
            boolean terminal, List<Condition> conditions, List<Action> actions
    ) {
        this(key, sourceNodeKey, targetNodeKey, type, label, position, terminal, conditions, actions, List.of());
    }

    public DialogueTransition(
            String key, String sourceNodeKey, String targetNodeKey,
            DialogueTransitionType type, String label, int position,
            List<Condition> conditions, List<Action> actions,
            List<DialoguePlayerReply> playerReplies
    ) {
        this(key, sourceNodeKey, targetNodeKey, type, label, position,
                type == DialogueTransitionType.END, conditions, actions, playerReplies);
    }

    public DialogueTransition(
            String key, String sourceNodeKey, String targetNodeKey,
            DialogueTransitionType type, String label, int position,
            List<Condition> conditions, List<Action> actions
    ) {
        this(key, sourceNodeKey, targetNodeKey, type, label, position,
                type == DialogueTransitionType.END, conditions, actions, List.of());
    }

    public String getKey() { return key; }
    public String getSourceNodeKey() { return sourceNodeKey; }
    public String getTargetNodeKey() { return targetNodeKey; }
    public DialogueTransitionType getType() { return type; }
    public String getLabel() { return label; }
    public int getPosition() { return position; }
    public boolean isTerminal() { return terminal; }
    public List<Condition> getConditions() { return conditions; }
    public List<Action> getActions() { return actions; }
    public List<DialoguePlayerReply> getPlayerReplies() { return playerReplies; }
}
