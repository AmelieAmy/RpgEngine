package fr.doryamy.rpgengine.quest;

/**
 * Opérations métier disponibles pour les actions QUEST.
 *
 * <p>Chaque opération décrit une transition explicite
 * entre deux états de progression.
 */
public enum QuestActionOperation {

    START(QuestState.NOT_STARTED, QuestState.ACTIVE),
    COMPLETE(QuestState.ACTIVE, QuestState.COMPLETED),
    REACTIVATE(QuestState.COMPLETED, QuestState.ACTIVE);

    private final QuestState sourceState;
    private final QuestState targetState;

    QuestActionOperation(
            QuestState sourceState,
            QuestState targetState
    ) {
        this.sourceState = sourceState;
        this.targetState = targetState;
    }

    public QuestState sourceState() {
        return sourceState;
    }

    public QuestState targetState() {
        return targetState;
    }

    public String toExpression(String questId) {
        if (questId == null || questId.isBlank()) {
            throw new IllegalArgumentException(
                    "L'identifiant d'une quête ne peut pas être vide."
            );
        }

        return name() + ":" + questId.trim();
    }
}
