package fr.doryamy.rpgengine.bridge;

import fr.doryamy.rpgengine.dialogue.DialogueRunner;
import fr.doryamy.rpgengine.dialogue.editor.CreateDialogueScenarioRequest;
import fr.doryamy.rpgengine.dialogue.editor.SwitchDialogueEditorStateRequest;
import fr.doryamy.rpgengine.dialogue.editor.SetDialogueTransitionTerminalRequest;
import fr.doryamy.rpgengine.dialogue.editor.CreateDialogueBranchRequest;
import fr.doryamy.rpgengine.dialogue.editor.InsertDialogueNpcReplyRequest;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueEditorScenarioSummaryView;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueEditorView;
import fr.doryamy.rpgengine.dialogue.presentation.DialogueView;
import fr.doryamy.rpgengine.quest.QuestSummary;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Façade entre RPGEngine Plugin et le mod RPGEngine NeoForge.
 */
public final class NeoForgeBridge {

    private static final String BRIDGE_CLASS =
            "fr.doryamy.rpgengine.neoforge.bridge.RpgEngineBridgeApi";

    private final DialogueRuntimeBridge dialogueRuntimeBridge =
            new DialogueRuntimeBridge();

    private final DialogueEditorBridge dialogueEditorBridge =
            new DialogueEditorBridge();

    private final QuestBridge questBridge =
            new QuestBridge();

    public boolean initialize() {
        try {
            Class<?> bridgeClass =
                    Class.forName(
                            BRIDGE_CLASS
                    );

            dialogueRuntimeBridge.initialize(
                    bridgeClass
            );

            dialogueEditorBridge.initialize(
                    bridgeClass
            );

            questBridge.initialize(
                    bridgeClass
            );

            RpgLogger.info(
                    "Bridge NeoForge initialisé."
            );

            return true;

        } catch (ReflectiveOperationException e) {

            RpgLogger.error(
                    "Impossible d'initialiser le bridge NeoForge : "
                            + e.getMessage()
            );

            shutdownAfterInitializationFailure();
            return false;

        } catch (RuntimeException e) {

            RpgLogger.error(
                    "Erreur inattendue pendant l'initialisation "
                            + "du bridge NeoForge : "
                            + e.getClass().getSimpleName()
                            + " | "
                            + e.getMessage()
            );

            shutdownAfterInitializationFailure();
            return false;
        }
    }

    public void setDialogueRunner(
            DialogueRunner dialogueRunner
    ) {
        dialogueRuntimeBridge.setDialogueRunner(
                dialogueRunner
        );
    }

    public void setDialogueEditorRequestHandler(
            BiConsumer<UUID, String> handler
    ) {
        dialogueEditorBridge.setDialogueEditorRequestHandler(
                handler
        );
    }

    public void setDialogueTriggerSelectionStartHandler(
            BiConsumer<UUID, String> handler
    ) {
        dialogueEditorBridge.setDialogueTriggerSelectionStartHandler(
                handler
        );
    }

    public void setDialogueQuestListRequestHandler(
            BiConsumer<UUID, String> handler
    ) {
        dialogueEditorBridge.setDialogueQuestListRequestHandler(
                handler
        );
    }

    public void setDialogueQuestSelectionHandler(
            BiConsumer<UUID, Map<String, String>> handler
    ) {
        dialogueEditorBridge.setDialogueQuestSelectionHandler(
                handler
        );
    }

    public void setDialogueNodeTextUpdateHandler(
            BiConsumer<UUID, Map<String, String>> handler
    ) {
        dialogueEditorBridge.setDialogueNodeTextUpdateHandler(
                handler
        );
    }

    public void setDialogueChoiceLabelUpdateHandler(
            BiConsumer<UUID, Map<String, String>> handler
    ) {
        dialogueEditorBridge.setDialogueChoiceLabelUpdateHandler(
                handler
        );
    }

    public void setDialoguePlayerReplyAddHandler(
            BiConsumer<UUID, Map<String, String>> handler
    ) {
        dialogueEditorBridge.setDialoguePlayerReplyAddHandler(
                handler
        );
    }

    public void setDialogueTransitionTerminalHandler(
            BiConsumer<
                    UUID,
                    SetDialogueTransitionTerminalRequest
                    > handler
    ) {
        dialogueEditorBridge.setDialogueTransitionTerminalHandler(
                handler
        );
    }

    public void setDialogueNpcReplyInsertHandler(
            BiConsumer<
                    UUID,
                    InsertDialogueNpcReplyRequest
                    > handler
    ) {
        dialogueEditorBridge.setDialogueNpcReplyInsertHandler(
                handler
        );
    }

    public void setDialogueBranchCreateHandler(
            BiConsumer<
                    UUID,
                    CreateDialogueBranchRequest
                    > handler
    ) {
        dialogueEditorBridge.setDialogueBranchCreateHandler(
                handler
        );
    }

    public void setDialogueElementDeleteHandler(
            BiConsumer<UUID, Map<String, String>> handler
    ) {
        dialogueEditorBridge.setDialogueElementDeleteHandler(
                handler
        );
    }


    public void setDialogueActionEditorRequestHandler(
            BiConsumer<UUID, Map<String, String>> handler
    ) {
        dialogueEditorBridge.setDialogueActionEditorRequestHandler(
                handler
        );
    }

    public void setDialogueActionUpdateHandler(
            BiConsumer<UUID, Map<String, String>> handler
    ) {
        dialogueEditorBridge.setDialogueActionUpdateHandler(
                handler
        );
    }

    public void setDialogueConditionEditorRequestHandler(
            BiConsumer<UUID, Map<String, String>> handler
    ) {
        dialogueEditorBridge.setDialogueConditionEditorRequestHandler(
                handler
        );
    }

    public void setDialogueConditionUpdateHandler(
            BiConsumer<UUID, Map<String, String>> handler
    ) {
        dialogueEditorBridge.setDialogueConditionUpdateHandler(
                handler
        );
    }

    public boolean showDialogueQuestSelector(
            UUID playerUuid,
            String dialogueKey,
            List<QuestSummary> quests
    ) {
        List<Map<String, String>> data =
                quests.stream()
                        .map(quest -> Map.of(
                                "id", quest.id(),
                                "name", quest.name(),
                                "description", quest.description()
                        ))
                        .toList();

        return dialogueEditorBridge.showDialogueQuestSelector(
                playerUuid,
                dialogueKey,
                data
        );
    }

    public boolean showDialogueConditionEditor(
            UUID playerUuid,
            String dialogueKey,
            String transitionKey,
            int conditionPosition,
            String provider,
            String expression,
            List<QuestSummary> quests
    ) {
        List<Map<String, String>> data =
                quests.stream()
                        .map(quest -> Map.of(
                                "id", quest.id(),
                                "name", quest.name(),
                                "description", quest.description()
                        ))
                        .toList();

        return dialogueEditorBridge.showDialogueConditionEditor(
                playerUuid,
                dialogueKey,
                transitionKey,
                conditionPosition,
                provider,
                expression,
                data
        );
    }

    public boolean showDialogueActionEditor(
            UUID playerUuid,
            String dialogueKey,
            String transitionKey,
            int actionPosition,
            String provider,
            String expression,
            List<QuestSummary> quests
    ) {
        List<Map<String, String>> data =
                quests.stream()
                        .map(quest -> Map.of(
                                "id", quest.id(),
                                "name", quest.name(),
                                "description", quest.description()
                        ))
                        .toList();

        return dialogueEditorBridge.showDialogueActionEditor(
                playerUuid,
                dialogueKey,
                transitionKey,
                actionPosition,
                provider,
                expression,
                data
        );
    }

    public void setDialogueEditorStateSwitchHandler(
            BiConsumer<UUID, SwitchDialogueEditorStateRequest> handler
    ) {
        dialogueEditorBridge.setDialogueEditorStateSwitchHandler(
                handler
        );
    }

    public void setDialogueScenarioDeleteHandler(
            BiConsumer<UUID, String> handler
    ) {
        dialogueEditorBridge.setDialogueScenarioDeleteHandler(
                handler
        );
    }

    public void setDialogueScenarioCreateHandler(
            BiConsumer<UUID, CreateDialogueScenarioRequest> handler
    ) {
        dialogueEditorBridge.setDialogueScenarioCreateHandler(
                handler
        );
    }

    public boolean showDialogue(
            UUID playerUuid,
            DialogueView view
    ) {
        return dialogueRuntimeBridge.showDialogue(
                playerUuid,
                view
        );
    }

    public boolean dismissDialogue(
            UUID playerUuid
    ) {
        return dialogueRuntimeBridge.dismissDialogue(
                playerUuid
        );
    }

    public boolean showDialogueManager(
            UUID playerUuid,
            List<DialogueEditorScenarioSummaryView> scenarios
    ) {
        return dialogueEditorBridge.showDialogueManager(
                playerUuid,
                scenarios
        );
    }

    public boolean showDialogueEditor(
            UUID playerUuid,
            DialogueEditorView view
    ) {
        return dialogueEditorBridge.showDialogueEditor(
                playerUuid,
                view
        );
    }

    public List<QuestSummary> getAvailableQuests() {
        return questBridge.getAvailableQuests();
    }

    public String getQuestState(
            UUID playerUuid,
            String questId
    ) {
        return questBridge.getQuestState(
                playerUuid,
                questId
        );
    }

    public boolean startQuest(
            UUID playerUuid,
            String questId
    ) {
        return questBridge.startQuest(
                playerUuid,
                questId
        );
    }

    public String getQuestDisplayName(
            String questId
    ) {
        return questBridge.getQuestDisplayName(
                questId
        );
    }

    public void shutdown() {
        dialogueRuntimeBridge.shutdown();
        dialogueEditorBridge.shutdown();
        questBridge.shutdown();

        RpgLogger.info(
                "Bridge NeoForge arrêté."
        );
    }

    private void shutdownAfterInitializationFailure() {
        dialogueRuntimeBridge.shutdown();
        dialogueEditorBridge.shutdown();
        questBridge.shutdown();
    }
}
