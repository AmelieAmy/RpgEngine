package fr.doryamy.rpgengine.dialogue.editor;

import fr.doryamy.rpgengine.dialogue.Dialogue;
import fr.doryamy.rpgengine.dialogue.DialogueService;
import fr.doryamy.rpgengine.dialogue.editor.DialogueScenarioResolver.QuestBinding;
import fr.doryamy.rpgengine.dialogue.editor.formatter.DialogueEditorValueFormatter;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueEditorQuestView;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueEditorScenarioSummaryView;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueEditorTriggerView;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueEditorView;
import fr.doryamy.rpgengine.model.Action;
import fr.doryamy.rpgengine.model.Trigger;
import fr.doryamy.rpgengine.quest.QuestService;
import fr.doryamy.rpgengine.quest.QuestState;
import fr.doryamy.rpgengine.repository.TriggerRepository;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Fournit les projections en lecture utilisées
 * par l'éditeur visuel de dialogues.
 *
 * <p>Ce service ne modifie aucune donnée.
 * Il construit les vues de l'éditeur à partir
 * des dialogues, triggers et quêtes existants.
 */
public final class DialogueEditorService {

    private final DialogueService dialogueService;
    private final TriggerRepository triggerRepository;
    private final QuestService questService;
    private final DialogueScenarioResolver scenarioResolver;
    private final DialogueEditorValueFormatter valueFormatter;
    private final DialogueEditorViewMapper viewMapper;

    public DialogueEditorService(
            DialogueService dialogueService,
            TriggerRepository triggerRepository,
            QuestService questService
    ) {
        this.dialogueService =
                dialogueService;

        this.triggerRepository =
                triggerRepository;

        this.questService =
                questService;

        this.scenarioResolver =
                new DialogueScenarioResolver(
                        triggerRepository
                );

        this.valueFormatter =
                new DialogueEditorValueFormatter(
                        questService
                );

        this.viewMapper =
                new DialogueEditorViewMapper(
                        valueFormatter
                );
    }

    /**
     * Construit la vue complète
     * d'un scénario.
     *
     * <p>Un scénario peut être lié à une quête,
     * auquel cas plusieurs variantes d'état peuvent
     * exister, ou être un dialogue simple sans quête.
     */
    public Optional<DialogueEditorView> buildView(
            String dialogueKey
    ) {
        Optional<Dialogue> dialogueResult =
                dialogueService.find(
                        dialogueKey
                );

        if (dialogueResult.isEmpty()) {
            return Optional.empty();
        }

        Optional<Trigger> triggerResult =
                scenarioResolver.findTriggerForDialogue(
                        dialogueKey
                );

        if (triggerResult.isEmpty()) {
            return Optional.empty();
        }

        Dialogue dialogue =
                dialogueResult.get();

        Trigger trigger =
                triggerResult.get();

        Optional<QuestBinding> questBindingResult =
                scenarioResolver.findQuestBinding(
                        trigger
                );

        /*
         * ------------------------------------------------
         * Scénario sans quête
         * ------------------------------------------------
         */
        if (questBindingResult.isEmpty()) {

            return Optional.of(
                    new DialogueEditorView(
                            dialogue.getName(),
                            buildTriggerView(
                                    trigger
                            ),
                            null,
                            null,
                            List.of(),
                            viewMapper.toGraphView(
                                    dialogue
                            )
                    )
            );
        }

        /*
         * ------------------------------------------------
         * Scénario lié à une quête
         * ------------------------------------------------
         */
        QuestBinding questBinding =
                questBindingResult.get();

        List<Trigger> scenarioTriggers =
                scenarioResolver.findScenarioTriggers(
                        trigger,
                        questBinding
                );

        return Optional.of(
                new DialogueEditorView(
                        dialogue.getName(),
                        buildTriggerView(
                                trigger
                        ),
                        buildQuestView(
                                questBinding
                        ),
                        questBinding.state(),
                        scenarioResolver.findAvailableStates(
                                scenarioTriggers
                        ),
                        viewMapper.toGraphView(
                                dialogue
                        )
                )
        );
    }

    /**
     * Construit la liste des scénarios
     * affichés dans le gestionnaire.
     *
     * <p>Les scénarios liés à une quête regroupent
     * leurs différentes variantes narratives.
     *
     * <p>Les scénarios sans quête représentent
     * chacun un trigger indépendant.
     */
    public List<DialogueEditorScenarioSummaryView> findScenarios() {

        List<Trigger> triggers =
                triggerRepository.findAll();

        List<DialogueEditorScenarioSummaryView> result =
                new ArrayList<>();

        Set<String> processedScenarios =
                new HashSet<>();

        for (Trigger trigger : triggers) {

            Optional<Action> dialogActionResult =
                    scenarioResolver.findDialogAction(
                            trigger
                    );

            if (dialogActionResult.isEmpty()) {
                continue;
            }

            String dialogueKey =
                    dialogActionResult.get()
                            .getExpression();

            if (dialogueKey == null
                    || dialogueKey.isBlank()) {

                continue;
            }

            Optional<Dialogue> dialogueResult =
                    dialogueService.find(
                            dialogueKey
                    );

            if (dialogueResult.isEmpty()) {
                continue;
            }

            Optional<QuestBinding> questBindingResult =
                    scenarioResolver.findQuestBinding(
                            trigger
                    );

            String scenarioIdentity =
                    scenarioResolver.buildScenarioIdentity(
                            trigger,
                            questBindingResult
                    );

            if (!processedScenarios.add(
                    scenarioIdentity
            )) {
                continue;
            }

            /*
             * ------------------------------------------------
             * Scénario simple sans quête
             * ------------------------------------------------
             */
            if (questBindingResult.isEmpty()) {

                Dialogue dialogue =
                        dialogueResult.get();

                result.add(
                        new DialogueEditorScenarioSummaryView(
                                dialogue.getName(),
                                buildTriggerView(
                                        trigger
                                ),
                                null,
                                List.of(),
                                dialogueKey
                        )
                );

                continue;
            }

            /*
             * ------------------------------------------------
             * Scénario avec quête
             * ------------------------------------------------
             */
            QuestBinding questBinding =
                    questBindingResult.get();

            List<Trigger> scenarioTriggers =
                    scenarioResolver.findScenarioTriggers(
                            trigger,
                            questBinding
                    );

            Optional<String> defaultDialogueKey =
                    scenarioResolver.findDefaultDialogueKey(
                            scenarioTriggers
                    );

            if (defaultDialogueKey.isEmpty()) {
                continue;
            }

            Dialogue defaultDialogue =
                    dialogueService.find(
                                    defaultDialogueKey.get()
                            )
                            .orElse(
                                    dialogueResult.get()
                            );

            result.add(
                    new DialogueEditorScenarioSummaryView(
                            defaultDialogue.getName(),
                            buildTriggerView(
                                    trigger
                            ),
                            buildQuestView(
                                    questBinding
                            ),
                            scenarioResolver.findAvailableStates(
                                    scenarioTriggers
                            ),
                            defaultDialogueKey.get()
                    )
            );
        }

        return List.copyOf(
                result
        );
    }

    /**
     * Charge une autre variante du même scénario.
     */
    public Optional<DialogueEditorView> switchState(
            String currentDialogueKey,
            QuestState targetState
    ) {
        if (targetState == null
                || targetState == QuestState.UNAVAILABLE) {

            return Optional.empty();
        }

        Optional<Trigger> currentTriggerResult =
                scenarioResolver.findTriggerForDialogue(
                        currentDialogueKey
                );

        if (currentTriggerResult.isEmpty()) {
            return Optional.empty();
        }

        Trigger currentTrigger =
                currentTriggerResult.get();

        Optional<QuestBinding> currentQuestResult =
                scenarioResolver.findQuestBinding(
                        currentTrigger
                );

        if (currentQuestResult.isEmpty()) {
            return Optional.empty();
        }

        List<Trigger> scenarioTriggers =
                scenarioResolver.findScenarioTriggers(
                        currentTrigger,
                        currentQuestResult.get()
                );

        Optional<Trigger> targetTriggerResult =
                scenarioTriggers.stream()
                        .filter(trigger ->
                                scenarioResolver.findQuestBinding(
                                                trigger
                                        )
                                        .map(
                                                QuestBinding::state
                                        )
                                        .filter(
                                                targetState::equals
                                        )
                                        .isPresent()
                        )
                        .findFirst();

        if (targetTriggerResult.isEmpty()) {
            return Optional.empty();
        }

        Optional<Action> dialogActionResult =
                scenarioResolver.findDialogAction(
                        targetTriggerResult.get()
                );

        if (dialogActionResult.isEmpty()) {
            return Optional.empty();
        }

        return buildView(
                dialogActionResult.get()
                        .getExpression()
        );
    }

    private DialogueEditorTriggerView buildTriggerView(
            Trigger trigger
    ) {
        return new DialogueEditorTriggerView(
                trigger.getType().name(),
                trigger.getTargetId(),
                trigger.getType().name()
                        + " "
                        + trigger.getTargetId()
        );
    }

    private DialogueEditorQuestView buildQuestView(
            QuestBinding questBinding
    ) {
        String questId =
                questBinding.questId();

        return new DialogueEditorQuestView(
                questId,
                questService.getDisplayName(
                        questId
                )
        );
    }
}