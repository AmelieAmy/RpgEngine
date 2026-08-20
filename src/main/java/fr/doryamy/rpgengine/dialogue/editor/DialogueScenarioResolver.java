package fr.doryamy.rpgengine.dialogue.editor;

import fr.doryamy.rpgengine.model.Action;
import fr.doryamy.rpgengine.model.Trigger;
import fr.doryamy.rpgengine.quest.QuestState;
import fr.doryamy.rpgengine.repository.TriggerRepository;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Résout les relations éditoriales entre
 * triggers, dialogues et états de quête.
 *
 * <p>Un scénario n'est pas une entité persistée.
 * Il est reconstruit à partir des règles existantes :
 *
 * <pre>
 * même type de trigger
 * + même targetId
 * + même questId
 * </pre>
 *
 * <p>Cette classe ne modifie aucune donnée.
 */
final class DialogueScenarioResolver {

    static final String DIALOG_PROVIDER =
            "DIALOG";

    static final String QUEST_PROVIDER =
            "QUEST";

    private final TriggerRepository triggerRepository;

    DialogueScenarioResolver(
            TriggerRepository triggerRepository
    ) {
        this.triggerRepository =
                triggerRepository;
    }

    /**
     * Recherche le trigger possédant une action DIALOG
     * vers le dialogue demandé.
     */
    Optional<Trigger> findTriggerForDialogue(
            String dialogueKey
    ) {
        return triggerRepository.findAll()
                .stream()
                .filter(trigger ->
                        findDialogAction(
                                trigger
                        )
                                .map(
                                        Action::getExpression
                                )
                                .filter(
                                        dialogueKey::equals
                                )
                                .isPresent()
                )
                .findFirst();
    }

    /**
     * Recherche l'action DIALOG d'un trigger.
     */
    Optional<Action> findDialogAction(
            Trigger trigger
    ) {
        return trigger.getActions()
                .stream()
                .filter(action ->
                        DIALOG_PROVIDER.equalsIgnoreCase(
                                action.getProvider()
                        )
                )
                .findFirst();
    }

    /**
     * Recherche le binding QUEST d'un trigger.
     */
    Optional<QuestBinding> findQuestBinding(
            Trigger trigger
    ) {
        return trigger.getConditions()
                .stream()
                .filter(condition ->
                        QUEST_PROVIDER.equalsIgnoreCase(
                                condition.getProvider()
                        )
                )
                .map(condition ->
                        parseQuestCondition(
                                condition.getExpression()
                        )
                )
                .flatMap(
                        Optional::stream
                )
                .findFirst();
    }

    /**
     * Analyse une condition QUEST
     * sous la forme :
     *
     * <pre>
     * questId==STATE
     * </pre>
     */
    Optional<QuestBinding> parseQuestCondition(
            String expression
    ) {
        if (expression == null
                || expression.isBlank()) {

            return Optional.empty();
        }

        int operatorIndex =
                expression.indexOf(
                        "=="
                );

        if (operatorIndex <= 0) {
            return Optional.empty();
        }

        String questId =
                expression.substring(
                                0,
                                operatorIndex
                        )
                        .trim();

        String stateValue =
                expression.substring(
                                operatorIndex + 2
                        )
                        .trim();

        if (questId.isEmpty()
                || stateValue.isEmpty()) {

            return Optional.empty();
        }

        try {
            QuestState state =
                    QuestState.valueOf(
                            stateValue.toUpperCase()
                    );

            if (state == QuestState.UNAVAILABLE) {
                return Optional.empty();
            }

            return Optional.of(
                    new QuestBinding(
                            questId,
                            state
                    )
            );

        } catch (IllegalArgumentException e) {

            return Optional.empty();
        }
    }

    /**
     * Recherche tous les triggers appartenant
     * au même scénario.
     */
    List<Trigger> findScenarioTriggers(
            Trigger referenceTrigger,
            QuestBinding referenceQuest
    ) {
        return triggerRepository.findAll()
                .stream()
                .filter(trigger ->
                        trigger.getType()
                                == referenceTrigger.getType()
                )
                .filter(trigger ->
                        trigger.getTargetId()
                                .equals(
                                        referenceTrigger.getTargetId()
                                )
                )
                .filter(trigger ->
                        findQuestBinding(
                                trigger
                        )
                                .map(
                                        QuestBinding::questId
                                )
                                .filter(
                                        referenceQuest.questId()::equals
                                )
                                .isPresent()
                )
                .toList();
    }

    /**
     * Recherche le dialogue servant
     * de point d'entrée du scénario.
     */
    Optional<String> findDefaultDialogueKey(
            List<Trigger> scenarioTriggers
    ) {
        for (QuestState state : List.of(
                QuestState.NOT_STARTED,
                QuestState.ACTIVE,
                QuestState.COMPLETED
        )) {

            Optional<String> dialogueKey =
                    scenarioTriggers.stream()
                            .filter(trigger ->
                                    findQuestBinding(
                                            trigger
                                    )
                                            .map(
                                                    QuestBinding::state
                                            )
                                            .filter(
                                                    state::equals
                                            )
                                            .isPresent()
                            )
                            .map(
                                    this::findDialogAction
                            )
                            .flatMap(
                                    Optional::stream
                            )
                            .map(
                                    Action::getExpression
                            )
                            .findFirst();

            if (dialogueKey.isPresent()) {
                return dialogueKey;
            }
        }

        return Optional.empty();
    }

    /**
     * Retourne les états configurés
     * pour un scénario.
     */
    List<QuestState> findAvailableStates(
            List<Trigger> scenarioTriggers
    ) {
        return scenarioTriggers.stream()
                .map(
                        this::findQuestBinding
                )
                .flatMap(
                        Optional::stream
                )
                .map(
                        QuestBinding::state
                )
                .distinct()
                .sorted(
                        Comparator.comparingInt(
                                this::stateOrder
                        )
                )
                .toList();
    }

    /**
     * Construit l'identité éditoriale d'un scénario.
     *
     * <p>Pour un scénario lié à une quête, les variantes
     * partageant le même déclencheur, la même cible
     * et la même quête sont regroupées.
     *
     * <p>Sans quête, un trigger représente à lui seul
     * un scénario afin de ne jamais fusionner
     * deux dialogues indépendants.
     */
    String buildScenarioIdentity(
            Trigger trigger,
            Optional<QuestBinding> questBinding
    ) {
        if (questBinding.isPresent()) {

            return trigger.getType().name()
                    + ":"
                    + trigger.getTargetId()
                    + ":QUEST:"
                    + questBinding.get().questId();
        }

        return trigger.getType().name()
                + ":"
                + trigger.getTargetId()
                + ":TRIGGER:"
                + trigger.getId();
    }

    private int stateOrder(
            QuestState state
    ) {
        return switch (state) {

            case NOT_STARTED ->
                    0;

            case ACTIVE ->
                    1;

            case COMPLETED ->
                    2;

            case UNAVAILABLE ->
                    3;
        };
    }

    /**
     * Association entre une quête
     * et l'état attendu par un trigger.
     */
    record QuestBinding(
            String questId,
            QuestState state
    ) {
    }

}