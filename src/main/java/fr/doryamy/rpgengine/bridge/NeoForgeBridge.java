package fr.doryamy.rpgengine.bridge;

import fr.doryamy.rpgengine.dialogue.DialogueRunner;
import fr.doryamy.rpgengine.dialogue.character.transport.CharacterAdminTransportEncoder;
import fr.doryamy.rpgengine.dialogue.character.view.CharacterAdminView;
import fr.doryamy.rpgengine.dialogue.editor.transport.DialogueAdminTransportEncoder;
import fr.doryamy.rpgengine.dialogue.editor.transport.DialogueEditorTransportEncoder;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueAdminView;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueEditorView;
import fr.doryamy.rpgengine.dialogue.runtime.view.DialogueView;
import fr.doryamy.rpgengine.quest.QuestSummary;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Façade entre RPGEngine Plugin
 * et le mod RPGEngine NeoForge.
 *
 * <p>Cette façade ne contient aucune logique métier.
 */
public final class NeoForgeBridge {

    private static final String BRIDGE_CLASS =
            "fr.doryamy.rpgengine.neoforge.bridge.RpgEngineBridgeApi";

    private final DialogueRuntimeBridge dialogueRuntimeBridge =
            new DialogueRuntimeBridge();

    private final DialogueEditorBridge dialogueEditorBridge =
            new DialogueEditorBridge(
                    new DialogueEditorTransportEncoder(),
                    new DialogueAdminTransportEncoder()
            );

    private final CharacterAdminBridge characterAdminBridge =
            new CharacterAdminBridge(
                    new CharacterAdminTransportEncoder()
            );

    private final CharacterNpcSelectionBridge characterNpcSelectionBridge =
            new CharacterNpcSelectionBridge();

    private final CharacterPortraitBridge characterPortraitBridge =
            new CharacterPortraitBridge();

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

            characterAdminBridge.initialize(
                    bridgeClass
            );


            characterNpcSelectionBridge.initialize(
                    bridgeClass
            );

            characterPortraitBridge.initialize(
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

    public void setDialoguePortraitLoader(
            Function<String, byte[]> loader
    ) {
        dialogueRuntimeBridge.setDialoguePortraitLoader(
                loader
        );
    }

    public void setDialogueEditorRequestHandler(
            BiConsumer<UUID, String> handler
    ) {

        dialogueEditorBridge.setDialogueEditorRequestHandler(
                handler
        );
    }

    public void setDialogueCreateRequestHandler(
            BiConsumer<UUID, String> handler
    ) {

        dialogueEditorBridge.setDialogueCreateRequestHandler(
                handler
        );
    }

    public void setDialogueNpcSelectionRequestHandler(
            Consumer<UUID> handler
    ) {

        dialogueEditorBridge.setDialogueNpcSelectionRequestHandler(
                handler
        );
    }

    public void setDialogueTriggerNpcSelectionRequestHandler(
            BiConsumer<UUID, String> handler
    ) {
        dialogueEditorBridge.setDialogueTriggerNpcSelectionRequestHandler(
                handler
        );
    }

    public void setDialogueDeleteRequestHandler(
            BiConsumer<UUID, String> handler
    ) {
        dialogueEditorBridge.setDialogueDeleteRequestHandler(
                handler
        );
    }

    public void setDialogueQuestRuleRequestHandler(
            BiConsumer<UUID, Map<String, String>> handler
    ) {
        dialogueEditorBridge.setDialogueQuestRuleRequestHandler(handler);
    }

    public void setDialogueAdminRequestHandler(Consumer<UUID> handler) {
        dialogueEditorBridge.setDialogueAdminRequestHandler(handler);
    }

    public void setDialogueRenameRequestHandler(
            BiConsumer<UUID, Map<String, String>> handler
    ) {
        dialogueEditorBridge.setDialogueRenameRequestHandler(handler);
    }

    public void setDialogueInsertElementRequestHandler(
            BiConsumer<UUID, Map<String, String>> handler
    ) {
        dialogueEditorBridge.setDialogueInsertElementRequestHandler(handler);
    }

    public void setDialogueElementTextUpdateRequestHandler(
            BiConsumer<UUID, Map<String, String>> handler
    ) {
        dialogueEditorBridge.setDialogueElementTextUpdateRequestHandler(handler);
    }

    public void setDialogueNpcReplyUpdateRequestHandler(
            BiConsumer<UUID, Map<String, String>> handler
    ) {
        dialogueEditorBridge.setDialogueNpcReplyUpdateRequestHandler(handler);
    }

    public void setDialogueElementDeleteRequestHandler(
            BiConsumer<UUID, Map<String, String>> handler
    ) {
        dialogueEditorBridge.setDialogueElementDeleteRequestHandler(handler);
    }

    public void setDialogueAddChoiceRequestHandler(
            BiConsumer<UUID, Map<String, String>> handler
    ) {
        dialogueEditorBridge.setDialogueAddChoiceRequestHandler(handler);
    }

    public void setDialogueRuleDeleteRequestHandler(
            BiConsumer<UUID, Map<String, String>> handler
    ) {
        dialogueEditorBridge.setDialogueRuleDeleteRequestHandler(
                handler
        );
    }

    public boolean openDialogueAdmin(
            UUID playerUuid,
            DialogueAdminView view
    ) {

        return dialogueEditorBridge.openDialogueAdmin(
                playerUuid,
                view
        );
    }

    public boolean openDialogueEditor(
            UUID playerUuid,
            DialogueEditorView view
    ) {

        return dialogueEditorBridge.openDialogueEditor(
                playerUuid,
                view
        );
    }

    public boolean showDialogueNpcSelectionResult(
            UUID playerUuid,
            String npcId,
            String npcName
    ) {

        return dialogueEditorBridge.showDialogueNpcSelectionResult(
                playerUuid,
                npcId,
                npcName
        );
    }

    public void setCharacterAdminRequestHandler(
            Consumer<UUID> handler
    ) {
        characterAdminBridge.setAdminRequestHandler(handler);
    }

    public void setCharacterCreateRequestHandler(
            BiConsumer<UUID, Map<String, String>> handler
    ) {
        characterAdminBridge.setCreateRequestHandler(handler);
    }

    public void setCharacterUpdateRequestHandler(
            BiConsumer<UUID, Map<String, String>> handler
    ) {
        characterAdminBridge.setUpdateRequestHandler(handler);
    }

    public void setCharacterDeleteRequestHandler(
            BiConsumer<UUID, String> handler
    ) {
        characterAdminBridge.setDeleteRequestHandler(handler);
    }


    public void setCharacterNpcSelectionRequestHandler(
            Consumer<UUID> handler
    ) {
        characterNpcSelectionBridge.setSelectionRequestHandler(handler);
    }

    public boolean showCharacterNpcSelectionResult(
            UUID playerUuid, String npcId, String npcName
    ) {
        return characterNpcSelectionBridge.showSelectionResult(playerUuid, npcId, npcName);
    }

    public boolean openCharacterAdmin(
            UUID playerUuid,
            CharacterAdminView view
    ) {
        return characterAdminBridge.openCharacterAdmin(
                playerUuid,
                view
        );
    }

    public void setCharacterPortraitUploadRequestHandler(
            BiConsumer<UUID, byte[]> handler
    ) {
        characterPortraitBridge.setUploadRequestHandler(handler);
    }

    public boolean showCharacterPortraitUploadResult(
            UUID playerUuid,
            boolean success,
            String portraitResource,
            String message
    ) {
        return characterPortraitBridge.showUploadResult(
                playerUuid,
                success,
                portraitResource,
                message
        );
    }

    public List<QuestSummary> getAvailableQuests() {
        return questBridge.getAvailableQuests();
    }

    public String getQuestState(
            UUID playerUuid,
            String questId
    ) {
        return questBridge.getQuestState(playerUuid, questId);
    }

    public boolean startQuest(
            UUID playerUuid,
            String questId
    ) {
        return questBridge.startQuest(playerUuid, questId);
    }

    public boolean completeQuest(
            UUID playerUuid,
            String questId
    ) {
        return questBridge.completeQuest(playerUuid, questId);
    }

    public boolean reactivateQuest(
            UUID playerUuid,
            String questId
    ) {
        return questBridge.reactivateQuest(playerUuid, questId);
    }

    public String getQuestDisplayName(
            String questId
    ) {
        return questBridge.getQuestDisplayName(questId);
    }

    public void shutdown() {

        dialogueRuntimeBridge.shutdown();
        dialogueEditorBridge.shutdown();
        characterAdminBridge.shutdown();
        characterNpcSelectionBridge.shutdown();
        characterPortraitBridge.shutdown();
        questBridge.shutdown();

        RpgLogger.info(
                "Bridge NeoForge arrêté."
        );
    }

    private void shutdownAfterInitializationFailure() {

        dialogueRuntimeBridge.shutdown();
        dialogueEditorBridge.shutdown();
        characterAdminBridge.shutdown();
        characterNpcSelectionBridge.shutdown();
        characterPortraitBridge.shutdown();
        questBridge.shutdown();
    }
}
