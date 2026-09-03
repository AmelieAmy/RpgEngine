package fr.doryamy.rpgengine.bridge;

import fr.doryamy.rpgengine.bridge.mapping.DialogueEditorBridgeMapper;
import fr.doryamy.rpgengine.dialogue.editor.CreateDialogueScenarioRequest;
import fr.doryamy.rpgengine.dialogue.editor.SwitchDialogueEditorStateRequest;
import fr.doryamy.rpgengine.dialogue.editor.SetDialogueTransitionTerminalRequest;
import fr.doryamy.rpgengine.dialogue.editor.InsertDialogueNpcReplyRequest;
import fr.doryamy.rpgengine.dialogue.editor.CreateDialogueBranchRequest;
import fr.doryamy.rpgengine.dialogue.DialogueInsertionKind;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueEditorScenarioSummaryView;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueEditorView;
import fr.doryamy.rpgengine.model.TriggerType;
import fr.doryamy.rpgengine.quest.QuestState;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Bridge spécialisé dans l'administration
 * et l'édition des dialogues.
 *
 * <p>Cette classe assure les échanges entre
 * l'interface d'administration NeoForge
 * et les services métier du plugin.
 *
 * <pre>
 * Plugin → Mod → Client
 *   showDialogueManager(...)
 *   showDialogueEditor(...)
 *
 * Client → Mod → Plugin
 *   demande d'ouverture d'un scénario
 *   demande de suppression d'un scénario
 * </pre>
 *
 * <p>Elle ne contient aucune logique métier.
 * Les décisions restent prises par les services
 * du plugin RPGEngine.
 */
public final class DialogueEditorBridge {

    private final DialogueEditorBridgeMapper mapper =
            new DialogueEditorBridgeMapper();

    /*
     * Serveur → client.
     */
    private Method showDialogueEditorMethod;
    private Method showDialogueManagerMethod;
    private Method showDialogueQuestSelectorMethod;
    private Method showDialogueActionEditorMethod;
    private Method showDialogueConditionEditorMethod;

    /*
     * Client → serveur : ouverture.
     */
    private Method registerDialogueEditorRequestHandlerMethod;
    private Method clearDialogueEditorRequestHandlerMethod;

    /*
     * Client → serveur : démarrage de sélection du trigger.
     */
    private Method registerDialogueTriggerSelectionStartHandlerMethod;
    private Method clearDialogueTriggerSelectionStartHandlerMethod;

    private Method registerDialogueQuestListRequestHandlerMethod;
    private Method clearDialogueQuestListRequestHandlerMethod;

    private Method registerDialogueQuestSelectionHandlerMethod;
    private Method clearDialogueQuestSelectionHandlerMethod;

    private Method registerDialogueNodeTextUpdateHandlerMethod;
    private Method clearDialogueNodeTextUpdateHandlerMethod;

    private Method registerDialogueChoiceLabelUpdateHandlerMethod;
    private Method clearDialogueChoiceLabelUpdateHandlerMethod;

    private Method registerDialoguePlayerReplyAddHandlerMethod;
    private Method clearDialoguePlayerReplyAddHandlerMethod;

    private Method registerDialogueTransitionTerminalHandlerMethod;
    private Method clearDialogueTransitionTerminalHandlerMethod;

    private Method registerDialogueNpcReplyInsertHandlerMethod;
    private Method clearDialogueNpcReplyInsertHandlerMethod;

    private Method registerDialogueBranchCreateHandlerMethod;
    private Method clearDialogueBranchCreateHandlerMethod;

    private Method registerDialogueElementDeleteHandlerMethod;
    private Method clearDialogueElementDeleteHandlerMethod;

    private Method registerDialogueActionEditorRequestHandlerMethod;
    private Method clearDialogueActionEditorRequestHandlerMethod;

    private Method registerDialogueActionUpdateHandlerMethod;
    private Method clearDialogueActionUpdateHandlerMethod;

    private Method registerDialogueConditionEditorRequestHandlerMethod;
    private Method clearDialogueConditionEditorRequestHandlerMethod;

    private Method registerDialogueConditionUpdateHandlerMethod;
    private Method clearDialogueConditionUpdateHandlerMethod;

    /*
     * Client → serveur : suppression.
     */
    private Method registerDialogueScenarioDeleteHandlerMethod;
    private Method clearDialogueScenarioDeleteHandlerMethod;

    /*
     * Client → serveur : creation.
     */
    private Method registerDialogueScenarioCreateHandlerMethod;
    private Method clearDialogueScenarioCreateHandlerMethod;

    /*
     * Client → serveur : changement d'état.
     */
    private Method registerDialogueEditorStateSwitchHandlerMethod;
    private Method clearDialogueEditorStateSwitchHandlerMethod;

    /*
     * Handlers métier configurés par le plugin.
     */
    private BiConsumer<UUID, String>
            dialogueEditorRequestHandler;

    private BiConsumer<UUID, String>
            dialogueTriggerSelectionStartHandler;

    private BiConsumer<UUID, String>
            dialogueQuestListRequestHandler;

    private BiConsumer<UUID, Map<String, String>>
            dialogueQuestSelectionHandler;

    private BiConsumer<UUID, Map<String, String>>
            dialogueNodeTextUpdateHandler;

    private BiConsumer<UUID, Map<String, String>>
            dialogueChoiceLabelUpdateHandler;

    private BiConsumer<UUID, Map<String, String>>
            dialoguePlayerReplyAddHandler;

    private BiConsumer<
            UUID,
            SetDialogueTransitionTerminalRequest
            > dialogueTransitionTerminalHandler;

    private BiConsumer<
            UUID,
            InsertDialogueNpcReplyRequest
            > dialogueNpcReplyInsertHandler;

    private BiConsumer<
            UUID,
            CreateDialogueBranchRequest
            > dialogueBranchCreateHandler;

    private BiConsumer<UUID, Map<String, String>>
            dialogueElementDeleteHandler;

    private BiConsumer<UUID, Map<String, String>>
            dialogueActionEditorRequestHandler;

    private BiConsumer<UUID, Map<String, String>>
            dialogueActionUpdateHandler;

    private BiConsumer<UUID, Map<String, String>>
            dialogueConditionEditorRequestHandler;

    private BiConsumer<UUID, Map<String, String>>
            dialogueConditionUpdateHandler;

    private BiConsumer<UUID, String>
            dialogueScenarioDeleteHandler;

    /*
     * Références fortes vers les callbacks réellement
     * enregistrés dans le mod.
     */
    private BiConsumer<UUID, String>
            registeredDialogueEditorRequestCallback;

    private BiConsumer<UUID, String>
            registeredDialogueTriggerSelectionStartCallback;

    private BiConsumer<UUID, String>
            registeredDialogueQuestListRequestCallback;

    private BiConsumer<UUID, Map<String, String>>
            registeredDialogueQuestSelectionCallback;

    private BiConsumer<UUID, Map<String, String>>
            registeredDialogueNodeTextUpdateCallback;

    private BiConsumer<UUID, Map<String, String>>
            registeredDialogueChoiceLabelUpdateCallback;

    private BiConsumer<UUID, Map<String, String>>
            registeredDialoguePlayerReplyAddCallback;

    private BiConsumer<UUID, Map<String, String>>
            registeredDialogueTransitionTerminalCallback;

    private BiConsumer<UUID, Map<String, String>>
            registeredDialogueNpcReplyInsertCallback;

    private BiConsumer<UUID, Map<String, String>>
            registeredDialogueBranchCreateCallback;

    private BiConsumer<UUID, Map<String, String>>
            registeredDialogueElementDeleteCallback;

    private BiConsumer<UUID, Map<String, String>>
            registeredDialogueActionEditorRequestCallback;

    private BiConsumer<UUID, Map<String, String>>
            registeredDialogueActionUpdateCallback;

    private BiConsumer<UUID, Map<String, String>>
            registeredDialogueConditionEditorRequestCallback;

    private BiConsumer<UUID, Map<String, String>>
            registeredDialogueConditionUpdateCallback;

    private BiConsumer<UUID, String>
            registeredDialogueScenarioDeleteCallback;

    private BiConsumer<
            UUID,
            CreateDialogueScenarioRequest
            > dialogueScenarioCreateHandler;

    private BiConsumer<
            UUID,
            Map<String, String>
            > registeredDialogueScenarioCreateCallback;

    private BiConsumer<
            UUID,
            SwitchDialogueEditorStateRequest
            > dialogueEditorStateSwitchHandler;

    private BiConsumer<
            UUID,
            Map<String, String>
            > registeredDialogueEditorStateSwitchCallback;

    /**
     * Initialise le bridge d'administration.
     *
     * @param bridgeClass classe RpgEngineBridgeApi du mod
     *
     * @throws ReflectiveOperationException si l'API
     *         attendue n'est pas disponible
     */
    public void initialize(
            Class<?> bridgeClass
    ) throws ReflectiveOperationException {

        /*
         * ------------------------------------------------
         * Serveur → client
         * ------------------------------------------------
         */
        showDialogueEditorMethod =
                bridgeClass.getMethod(
                        "showDialogueEditor",
                        UUID.class,
                        Map.class
                );

        showDialogueManagerMethod =
                bridgeClass.getMethod(
                        "showDialogueManager",
                        UUID.class,
                        List.class
                );

        showDialogueQuestSelectorMethod =
                bridgeClass.getMethod(
                        "showDialogueQuestSelector",
                        UUID.class,
                        String.class,
                        List.class
                );

        showDialogueActionEditorMethod =
                bridgeClass.getMethod(
                        "showDialogueActionEditor",
                        UUID.class,
                        String.class,
                        String.class,
                        int.class,
                        String.class,
                        String.class,
                        List.class
                );

        showDialogueConditionEditorMethod =
                bridgeClass.getMethod(
                        "showDialogueConditionEditor",
                        UUID.class,
                        String.class,
                        String.class,
                        int.class,
                        String.class,
                        String.class,
                        List.class
                );

        /*
         * ------------------------------------------------
         * Client → serveur : ouverture
         * ------------------------------------------------
         */
        registerDialogueEditorRequestHandlerMethod =
                bridgeClass.getMethod(
                        "registerDialogueEditorRequestHandler",
                        BiConsumer.class
                );

        clearDialogueEditorRequestHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialogueEditorRequestHandler"
                );

        registerDialogueTriggerSelectionStartHandlerMethod =
                bridgeClass.getMethod(
                        "registerDialogueTriggerSelectionStartHandler",
                        BiConsumer.class
                );

        clearDialogueTriggerSelectionStartHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialogueTriggerSelectionStartHandler"
                );

        registerDialogueQuestListRequestHandlerMethod =
                bridgeClass.getMethod(
                        "registerDialogueQuestListRequestHandler",
                        BiConsumer.class
                );

        clearDialogueQuestListRequestHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialogueQuestListRequestHandler"
                );

        registerDialogueQuestSelectionHandlerMethod =
                bridgeClass.getMethod(
                        "registerDialogueQuestSelectionHandler",
                        BiConsumer.class
                );

        clearDialogueQuestSelectionHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialogueQuestSelectionHandler"
                );

        registerDialogueNodeTextUpdateHandlerMethod =
                bridgeClass.getMethod(
                        "registerDialogueNodeTextUpdateHandler",
                        BiConsumer.class
                );

        clearDialogueNodeTextUpdateHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialogueNodeTextUpdateHandler"
                );

        registerDialogueChoiceLabelUpdateHandlerMethod =
                bridgeClass.getMethod(
                        "registerDialogueChoiceLabelUpdateHandler",
                        BiConsumer.class
                );

        clearDialogueChoiceLabelUpdateHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialogueChoiceLabelUpdateHandler"
                );

        registerDialoguePlayerReplyAddHandlerMethod =
                bridgeClass.getMethod(
                        "registerDialoguePlayerReplyAddHandler",
                        BiConsumer.class
                );

        clearDialoguePlayerReplyAddHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialoguePlayerReplyAddHandler"
                );

        registerDialogueTransitionTerminalHandlerMethod =
                bridgeClass.getMethod(
                        "registerDialogueTransitionTerminalHandler",
                        BiConsumer.class
                );

        clearDialogueTransitionTerminalHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialogueTransitionTerminalHandler"
                );


        registerDialogueNpcReplyInsertHandlerMethod =
                bridgeClass.getMethod(
                        "registerDialogueNpcReplyInsertHandler",
                        BiConsumer.class
                );

        clearDialogueNpcReplyInsertHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialogueNpcReplyInsertHandler"
                );

        registerDialogueBranchCreateHandlerMethod =
                bridgeClass.getMethod(
                        "registerDialogueBranchCreateHandler",
                        BiConsumer.class
                );

        clearDialogueBranchCreateHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialogueBranchCreateHandler"
                );

        registerDialogueElementDeleteHandlerMethod =
                bridgeClass.getMethod(
                        "registerDialogueElementDeleteHandler",
                        BiConsumer.class
                );

        clearDialogueElementDeleteHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialogueElementDeleteHandler"
                );

        registerDialogueActionEditorRequestHandlerMethod =
                bridgeClass.getMethod(
                        "registerDialogueActionEditorRequestHandler",
                        BiConsumer.class
                );

        clearDialogueActionEditorRequestHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialogueActionEditorRequestHandler"
                );

        registerDialogueActionUpdateHandlerMethod =
                bridgeClass.getMethod(
                        "registerDialogueActionUpdateHandler",
                        BiConsumer.class
                );

        clearDialogueActionUpdateHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialogueActionUpdateHandler"
                );

        registerDialogueConditionEditorRequestHandlerMethod =
                bridgeClass.getMethod(
                        "registerDialogueConditionEditorRequestHandler",
                        BiConsumer.class
                );

        clearDialogueConditionEditorRequestHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialogueConditionEditorRequestHandler"
                );

        registerDialogueConditionUpdateHandlerMethod =
                bridgeClass.getMethod(
                        "registerDialogueConditionUpdateHandler",
                        BiConsumer.class
                );

        clearDialogueConditionUpdateHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialogueConditionUpdateHandler"
                );

        /*
         * ------------------------------------------------
         * Client → serveur : suppression
         * ------------------------------------------------
         */
        registerDialogueScenarioDeleteHandlerMethod =
                bridgeClass.getMethod(
                        "registerDialogueScenarioDeleteHandler",
                        BiConsumer.class
                );

        clearDialogueScenarioDeleteHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialogueScenarioDeleteHandler"
                );

        /*
         * ------------------------------------------------
         * Client → serveur : creation
         * ------------------------------------------------
         */
        registerDialogueScenarioCreateHandlerMethod =
                bridgeClass.getMethod(
                        "registerDialogueScenarioCreateHandler",
                        BiConsumer.class
                );

        clearDialogueScenarioCreateHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialogueScenarioCreateHandler"
                );

        /*
         * ------------------------------------------------
         * Client → serveur : changement d'état
         * ------------------------------------------------
         */
        registerDialogueEditorStateSwitchHandlerMethod =
                bridgeClass.getMethod(
                        "registerDialogueEditorStateSwitchHandler",
                        BiConsumer.class
                );

        clearDialogueEditorStateSwitchHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialogueEditorStateSwitchHandler"
                );

        /*
         * ------------------------------------------------
         * Callback ouverture
         * ------------------------------------------------
         */
        registeredDialogueEditorRequestCallback =
                (playerUuid, dialogueKey) -> {

                    BiConsumer<UUID, String> handler =
                            dialogueEditorRequestHandler;

                    if (handler == null) {

                        RpgLogger.debug(
                                "OPEN_DIALOGUE_EDITOR_REQUEST reçu "
                                        + "mais aucun handler plugin n'est configuré."
                        );

                        return;
                    }

                    handler.accept(
                            playerUuid,
                            dialogueKey
                    );
                };

        registerDialogueEditorRequestHandlerMethod.invoke(
                null,
                registeredDialogueEditorRequestCallback
        );


        registeredDialogueTriggerSelectionStartCallback =
                (playerUuid, dialogueKey) -> {

                    BiConsumer<UUID, String> handler =
                            dialogueTriggerSelectionStartHandler;

                    if (handler == null) {

                        RpgLogger.debug(
                                "START_DIALOGUE_TRIGGER_SELECTION reçu "
                                        + "mais aucun handler plugin n'est configuré."
                        );

                        return;
                    }

                    handler.accept(
                            playerUuid,
                            dialogueKey
                    );
                };

        registerDialogueTriggerSelectionStartHandlerMethod.invoke(
                null,
                registeredDialogueTriggerSelectionStartCallback
        );


        registeredDialogueQuestListRequestCallback =
                (playerUuid, dialogueKey) -> {

                    BiConsumer<UUID, String> handler =
                            dialogueQuestListRequestHandler;

                    if (handler != null) {
                        handler.accept(playerUuid, dialogueKey);
                    }
                };

        registerDialogueQuestListRequestHandlerMethod.invoke(
                null,
                registeredDialogueQuestListRequestCallback
        );


        registeredDialogueQuestSelectionCallback =
                (playerUuid, data) -> {
                    BiConsumer<UUID, Map<String, String>> handler =
                            dialogueQuestSelectionHandler;

                    if (handler != null) {
                        handler.accept(playerUuid, data);
                    }
                };

        registerDialogueQuestSelectionHandlerMethod.invoke(
                null,
                registeredDialogueQuestSelectionCallback
        );

        registeredDialogueNodeTextUpdateCallback =
                (playerUuid, data) -> {
                    BiConsumer<UUID, Map<String, String>> handler =
                            dialogueNodeTextUpdateHandler;

                    if (handler != null) {
                        handler.accept(playerUuid, data);
                    }
                };

        registerDialogueNodeTextUpdateHandlerMethod.invoke(
                null,
                registeredDialogueNodeTextUpdateCallback
        );

        registeredDialogueChoiceLabelUpdateCallback =
                (playerUuid, data) -> {

                    BiConsumer<UUID, Map<String, String>> handler =
                            dialogueChoiceLabelUpdateHandler;

                    if (handler != null) {

                        handler.accept(
                                playerUuid,
                                data
                        );
                    }
                };

        registerDialogueChoiceLabelUpdateHandlerMethod.invoke(
                null,
                registeredDialogueChoiceLabelUpdateCallback
        );


        registeredDialoguePlayerReplyAddCallback =
                (playerUuid, data) -> {

                    BiConsumer<UUID, Map<String, String>> handler =
                            dialoguePlayerReplyAddHandler;

                    if (handler != null) {
                        handler.accept(
                                playerUuid,
                                data
                        );
                    }
                };

        registerDialoguePlayerReplyAddHandlerMethod.invoke(
                null,
                registeredDialoguePlayerReplyAddCallback
        );


        registeredDialogueTransitionTerminalCallback =
                (playerUuid, data) -> {

                    BiConsumer<
                            UUID,
                            SetDialogueTransitionTerminalRequest
                            > handler =
                            dialogueTransitionTerminalHandler;

                    if (handler == null) {

                        RpgLogger.debug(
                                "SET_DIALOGUE_TRANSITION_TERMINAL reçu "
                                        + "mais aucun handler plugin n'est configuré."
                        );

                        return;
                    }

                    SetDialogueTransitionTerminalRequest request =
                            parseTransitionTerminalRequest(
                                    data
                            );

                    if (request == null) {
                        return;
                    }

                    handler.accept(
                            playerUuid,
                            request
                    );
                };

        registerDialogueTransitionTerminalHandlerMethod.invoke(
                null,
                registeredDialogueTransitionTerminalCallback
        );


        registeredDialogueNpcReplyInsertCallback =
                (playerUuid, data) -> {

                    BiConsumer<
                            UUID,
                            InsertDialogueNpcReplyRequest
                            > handler =
                            dialogueNpcReplyInsertHandler;

                    if (handler == null) {
                        RpgLogger.debug(
                                "INSERT_DIALOGUE_NPC_REPLY reçu "
                                        + "mais aucun handler plugin n'est configuré."
                        );
                        return;
                    }

                    InsertDialogueNpcReplyRequest request =
                            parseNpcReplyInsertRequest(
                                    data
                            );

                    if (request != null) {
                        handler.accept(
                                playerUuid,
                                request
                        );
                    }
                };

        registerDialogueNpcReplyInsertHandlerMethod.invoke(
                null,
                registeredDialogueNpcReplyInsertCallback
        );


        registeredDialogueBranchCreateCallback =
                (playerUuid, data) -> {

                    BiConsumer<
                            UUID,
                            CreateDialogueBranchRequest
                            > handler =
                            dialogueBranchCreateHandler;

                    if (handler == null) {
                        RpgLogger.debug(
                                "CREATE_DIALOGUE_BRANCH reçu "
                                        + "mais aucun handler plugin n'est configuré."
                        );
                        return;
                    }

                    CreateDialogueBranchRequest request =
                            parseBranchCreateRequest(
                                    data
                            );

                    if (request != null) {
                        handler.accept(
                                playerUuid,
                                request
                        );
                    }
                };

        registerDialogueBranchCreateHandlerMethod.invoke(
                null,
                registeredDialogueBranchCreateCallback
        );


        registeredDialogueElementDeleteCallback =
                (playerUuid, data) -> {

                    BiConsumer<UUID, Map<String, String>> handler =
                            dialogueElementDeleteHandler;

                    if (handler != null) {
                        handler.accept(playerUuid, data);
                    }
                };

        registerDialogueElementDeleteHandlerMethod.invoke(
                null,
                registeredDialogueElementDeleteCallback
        );


        registeredDialogueActionEditorRequestCallback =
                (playerUuid, data) -> {

                    BiConsumer<UUID, Map<String, String>> handler =
                            dialogueActionEditorRequestHandler;

                    if (handler != null) {

                        handler.accept(
                                playerUuid,
                                data
                        );
                    }
                };

        registerDialogueActionEditorRequestHandlerMethod.invoke(
                null,
                registeredDialogueActionEditorRequestCallback
        );

        registeredDialogueActionUpdateCallback =
                (playerUuid, data) -> {

                    BiConsumer<UUID, Map<String, String>> handler =
                            dialogueActionUpdateHandler;

                    if (handler != null) {

                        handler.accept(
                                playerUuid,
                                data
                        );
                    }
                };

        registerDialogueActionUpdateHandlerMethod.invoke(
                null,
                registeredDialogueActionUpdateCallback
        );


        registeredDialogueConditionEditorRequestCallback =
                (playerUuid, data) -> {

                    BiConsumer<UUID, Map<String, String>> handler =
                            dialogueConditionEditorRequestHandler;

                    if (handler != null) {
                        handler.accept(
                                playerUuid,
                                data
                        );
                    }
                };

        registerDialogueConditionEditorRequestHandlerMethod.invoke(
                null,
                registeredDialogueConditionEditorRequestCallback
        );

        registeredDialogueConditionUpdateCallback =
                (playerUuid, data) -> {

                    BiConsumer<UUID, Map<String, String>> handler =
                            dialogueConditionUpdateHandler;

                    if (handler != null) {
                        handler.accept(
                                playerUuid,
                                data
                        );
                    }
                };

        registerDialogueConditionUpdateHandlerMethod.invoke(
                null,
                registeredDialogueConditionUpdateCallback
        );

        /*
         * ------------------------------------------------
         * Callback suppression
         * ------------------------------------------------
         */
        registeredDialogueScenarioDeleteCallback =
                (playerUuid, dialogueKey) -> {

                    BiConsumer<UUID, String> handler =
                            dialogueScenarioDeleteHandler;

                    if (handler == null) {

                        RpgLogger.debug(
                                "DELETE_DIALOGUE_SCENARIO_REQUEST reçu "
                                        + "mais aucun handler plugin n'est configuré."
                        );

                        return;
                    }

                    handler.accept(
                            playerUuid,
                            dialogueKey
                    );
                };

        registerDialogueScenarioDeleteHandlerMethod.invoke(
                null,
                registeredDialogueScenarioDeleteCallback
        );

        /*
         * ------------------------------------------------
         * Callback creation
         * ------------------------------------------------
         */
        registeredDialogueScenarioCreateCallback =
                (playerUuid, data) -> {

                    BiConsumer<
                            UUID,
                            CreateDialogueScenarioRequest
                            > handler =
                            dialogueScenarioCreateHandler;

                    if (handler == null) {

                        RpgLogger.debug(
                                "CREATE_DIALOGUE_SCENARIO_REQUEST reçu "
                                        + "mais aucun handler plugin n'est configuré."
                        );

                        return;
                    }

                    CreateDialogueScenarioRequest request =
                            parseCreateRequest(
                                    data
                            );

                    if (request == null) {
                        return;
                    }

                    handler.accept(
                            playerUuid,
                            request
                    );
                };

        registerDialogueScenarioCreateHandlerMethod.invoke(
                null,
                registeredDialogueScenarioCreateCallback
        );


        /*
         * ------------------------------------------------
         * Callback changement d'état
         * ------------------------------------------------
         */
        registeredDialogueEditorStateSwitchCallback =
                (playerUuid, data) -> {

                    BiConsumer<
                            UUID,
                            SwitchDialogueEditorStateRequest
                            > handler =
                            dialogueEditorStateSwitchHandler;

                    if (handler == null) {

                        RpgLogger.debug(
                                "SWITCH_DIALOGUE_EDITOR_STATE reçu "
                                        + "mais aucun handler plugin n'est configuré."
                        );

                        return;
                    }

                    SwitchDialogueEditorStateRequest request =
                            parseStateSwitchRequest(
                                    data
                            );

                    if (request == null) {
                        return;
                    }

                    handler.accept(
                            playerUuid,
                            request
                    );
                };

        registerDialogueEditorStateSwitchHandlerMethod.invoke(
                null,
                registeredDialogueEditorStateSwitchCallback
        );
    }

    /**
     * Configure le traitement d'une demande
     * de création de scénario.
     */
    /**
     * Configure le traitement d'une demande
     * de changement de variante d'état.
     */
    public void setDialogueEditorStateSwitchHandler(
            BiConsumer<
                    UUID,
                    SwitchDialogueEditorStateRequest
                    > handler
    ) {
        this.dialogueEditorStateSwitchHandler =
                handler;
    }

    private SwitchDialogueEditorStateRequest parseStateSwitchRequest(
            Map<String, String> data
    ) {
        if (data == null) {
            return null;
        }

        String dialogueKey =
                data.get(
                        "dialogueKey"
                );

        String targetStateValue =
                data.get(
                        "targetState"
                );

        if (dialogueKey == null
                || dialogueKey.isBlank()
                || targetStateValue == null
                || targetStateValue.isBlank()) {

            RpgLogger.error(
                    "SWITCH_DIALOGUE_EDITOR_STATE incomplet."
            );

            return null;
        }

        QuestState targetState;

        try {
            targetState =
                    QuestState.valueOf(
                            targetStateValue.toUpperCase()
                    );

        } catch (IllegalArgumentException e) {

            RpgLogger.error(
                    "État de quête invalide reçu depuis le mod : "
                            + targetStateValue
            );

            return null;
        }

        if (targetState == QuestState.UNAVAILABLE) {

            RpgLogger.error(
                    "L'état UNAVAILABLE ne peut pas être sélectionné dans l'éditeur."
            );

            return null;
        }

        return new SwitchDialogueEditorStateRequest(
                dialogueKey,
                targetState
        );
    }

    public void setDialogueScenarioCreateHandler(
            BiConsumer<
                    UUID,
                    CreateDialogueScenarioRequest
                    > handler
    ) {
        this.dialogueScenarioCreateHandler =
                handler;
    }

    private CreateDialogueScenarioRequest parseCreateRequest(
            Map<String, String> data
    ) {
        if (data == null) {
            return null;
        }

        String name =
                data.get(
                        "name"
                );

        String triggerTypeValue =
                data.get(
                        "triggerType"
                );

        String targetId =
                data.get(
                        "targetId"
                );

        if (name == null
                || triggerTypeValue == null
                || targetId == null) {

            RpgLogger.error(
                    "CREATE_DIALOGUE_SCENARIO_REQUEST incomplet."
            );

            return null;
        }

        TriggerType triggerType;

        try {
            triggerType =
                    TriggerType.valueOf(
                            triggerTypeValue.toUpperCase()
                    );

        } catch (IllegalArgumentException e) {

            RpgLogger.error(
                    "Type de trigger invalide reçu depuis le mod : "
                            + triggerTypeValue
            );

            return null;
        }

        String questId =
                data.get(
                        "questId"
                );

        QuestState initialState =
                null;

        String stateValue =
                data.get(
                        "initialState"
                );

        if (stateValue != null
                && !stateValue.isBlank()) {

            try {
                initialState =
                        QuestState.valueOf(
                                stateValue.toUpperCase()
                        );

            } catch (IllegalArgumentException e) {

                RpgLogger.error(
                        "État de quête invalide reçu depuis le mod : "
                                + stateValue
                );

                return null;
            }
        }

        return new CreateDialogueScenarioRequest(
                name,
                triggerType,
                targetId,
                questId,
                initialState
        );
    }

    /**
     * Configure le traitement d'une demande
     * d'ouverture de scénario.
     */
    public void setDialogueEditorRequestHandler(
            BiConsumer<UUID, String> handler
    ) {
        this.dialogueEditorRequestHandler =
                handler;
    }

    /**
     * Configure le traitement d'une demande de démarrage
     * du mode de sélection du déclencheur.
     */
    public void setDialogueTriggerSelectionStartHandler(
            BiConsumer<UUID, String> handler
    ) {
        this.dialogueTriggerSelectionStartHandler =
                handler;
    }

    public void setDialogueQuestListRequestHandler(
            BiConsumer<UUID, String> handler
    ) {
        this.dialogueQuestListRequestHandler = handler;
    }


    public void setDialogueQuestSelectionHandler(
            BiConsumer<UUID, Map<String, String>> handler
    ) {
        this.dialogueQuestSelectionHandler = handler;
    }

    public void setDialogueNodeTextUpdateHandler(
            BiConsumer<UUID, Map<String, String>> handler
    ) {
        this.dialogueNodeTextUpdateHandler = handler;
    }

    public void setDialogueChoiceLabelUpdateHandler(
            BiConsumer<UUID, Map<String, String>> handler
    ) {
        this.dialogueChoiceLabelUpdateHandler = handler;
    }


    public void setDialoguePlayerReplyAddHandler(
            BiConsumer<UUID, Map<String, String>> handler
    ) {
        this.dialoguePlayerReplyAddHandler =
                handler;
    }


    public void setDialogueTransitionTerminalHandler(
            BiConsumer<
                    UUID,
                    SetDialogueTransitionTerminalRequest
                    > handler
    ) {
        this.dialogueTransitionTerminalHandler =
                handler;
    }

    private SetDialogueTransitionTerminalRequest parseTransitionTerminalRequest(
            Map<String, String> data
    ) {
        if (data == null) {
            return null;
        }

        String dialogueKey =
                data.get(
                        "dialogueKey"
                );

        String transitionKey =
                data.get(
                        "transitionKey"
                );

        if (dialogueKey == null
                || dialogueKey.isBlank()
                || transitionKey == null
                || transitionKey.isBlank()) {

            RpgLogger.error(
                    "SET_DIALOGUE_TRANSITION_TERMINAL incomplet."
            );

            return null;
        }

        return new SetDialogueTransitionTerminalRequest(
                dialogueKey,
                transitionKey
        );
    }


    public void setDialogueNpcReplyInsertHandler(
            BiConsumer<
                    UUID,
                    InsertDialogueNpcReplyRequest
                    > handler
    ) {
        this.dialogueNpcReplyInsertHandler =
                handler;
    }

    private InsertDialogueNpcReplyRequest parseNpcReplyInsertRequest(
            Map<String, String> data
    ) {
        if (data == null) {
            return null;
        }

        String dialogueKey =
                data.get(
                        "dialogueKey"
                );

        String transitionKey =
                data.get(
                        "transitionKey"
                );

        String insertionKindValue =
                data.get(
                        "insertionKind"
                );

        String text =
                data.get(
                        "text"
                );

        if (dialogueKey == null
                || dialogueKey.isBlank()
                || transitionKey == null
                || transitionKey.isBlank()
                || insertionKindValue == null
                || insertionKindValue.isBlank()
                || text == null
                || text.isBlank()) {

            RpgLogger.error(
                    "INSERT_DIALOGUE_NPC_REPLY incomplet."
            );
            return null;
        }

        DialogueInsertionKind insertionKind;

        try {
            insertionKind =
                    DialogueInsertionKind.valueOf(
                            insertionKindValue
                    );
        } catch (IllegalArgumentException e) {
            RpgLogger.error(
                    "Point d'insertion PNJ invalide reçu depuis le mod : "
                            + insertionKindValue
            );
            return null;
        }

        int position;

        try {
            position =
                    Integer.parseInt(
                            data.getOrDefault(
                                    "position",
                                    "0"
                            )
                    );
        } catch (NumberFormatException e) {
            RpgLogger.error(
                    "Position d'insertion PNJ invalide reçue depuis le mod."
            );
            return null;
        }

        return new InsertDialogueNpcReplyRequest(
                dialogueKey,
                transitionKey,
                insertionKind,
                position,
                text
        );
    }


    public void setDialogueBranchCreateHandler(
            BiConsumer<
                    UUID,
                    CreateDialogueBranchRequest
                    > handler
    ) {
        this.dialogueBranchCreateHandler =
                handler;
    }

    private CreateDialogueBranchRequest parseBranchCreateRequest(
            Map<String, String> data
    ) {
        if (data == null) {
            return null;
        }

        String dialogueKey =
                data.get(
                        "dialogueKey"
                );

        String transitionKey =
                data.get(
                        "transitionKey"
                );

        String insertionKindValue =
                data.get(
                        "insertionKind"
                );

        int position;
        try {
            position = Integer.parseInt(
                    data.getOrDefault(
                            "position",
                            "0"
                    )
            );
        } catch (NumberFormatException e) {
            RpgLogger.error(
                    "Position d'embranchement invalide reçue depuis le mod."
            );
            return null;
        }

        DialogueInsertionKind insertionKind;
        try {
            insertionKind = DialogueInsertionKind.valueOf(
                    insertionKindValue
            );
        } catch (IllegalArgumentException | NullPointerException e) {
            RpgLogger.error(
                    "Point d'insertion d'embranchement invalide : "
                            + insertionKindValue
            );
            return null;
        }

        String existingChoiceLabel =
                data.getOrDefault(
                        "existingChoiceLabel",
                        ""
                );

        String newChoiceLabel =
                data.get(
                        "newChoiceLabel"
                );

        if (dialogueKey == null
                || dialogueKey.isBlank()
                || transitionKey == null
                || transitionKey.isBlank()
                || newChoiceLabel == null
                || newChoiceLabel.isBlank()) {

            RpgLogger.error(
                    "CREATE_DIALOGUE_BRANCH incomplet."
            );
            return null;
        }

        return new CreateDialogueBranchRequest(
                dialogueKey,
                transitionKey,
                insertionKind,
                position,
                existingChoiceLabel,
                newChoiceLabel
        );
    }


    public void setDialogueElementDeleteHandler(
            BiConsumer<UUID, Map<String, String>> handler
    ) {
        this.dialogueElementDeleteHandler = handler;
    }


    public void setDialogueActionEditorRequestHandler(
            BiConsumer<UUID, Map<String, String>> handler
    ) {
        this.dialogueActionEditorRequestHandler =
                handler;
    }

    public void setDialogueActionUpdateHandler(
            BiConsumer<UUID, Map<String, String>> handler
    ) {
        this.dialogueActionUpdateHandler =
                handler;
    }


    public void setDialogueConditionEditorRequestHandler(
            BiConsumer<UUID, Map<String, String>> handler
    ) {
        this.dialogueConditionEditorRequestHandler =
                handler;
    }

    public void setDialogueConditionUpdateHandler(
            BiConsumer<UUID, Map<String, String>> handler
    ) {
        this.dialogueConditionUpdateHandler =
                handler;
    }

    public boolean showDialogueConditionEditor(
            UUID playerUuid,
            String dialogueKey,
            String transitionKey,
            int conditionPosition,
            String provider,
            String expression,
            List<Map<String, String>> quests
    ) {
        if (showDialogueConditionEditorMethod == null) {
            return false;
        }

        try {
            Object result =
                    showDialogueConditionEditorMethod.invoke(
                            null,
                            playerUuid,
                            dialogueKey,
                            transitionKey,
                            conditionPosition,
                            provider,
                            expression,
                            quests
                    );

            return result instanceof Boolean success
                    && success;

        } catch (ReflectiveOperationException e) {

            RpgLogger.error(
                    "Impossible d'ouvrir l'éditeur de condition via NeoForge : "
                            + e.getMessage()
            );

            return false;
        }
    }

    public boolean showDialogueActionEditor(
            UUID playerUuid,
            String dialogueKey,
            String transitionKey,
            int actionPosition,
            String provider,
            String expression,
            List<Map<String, String>> quests
    ) {
        if (showDialogueActionEditorMethod == null) {
            return false;
        }

        try {
            Object result =
                    showDialogueActionEditorMethod.invoke(
                            null,
                            playerUuid,
                            dialogueKey,
                            transitionKey,
                            actionPosition,
                            provider,
                            expression,
                            quests
                    );

            return result instanceof Boolean success
                    && success;

        } catch (ReflectiveOperationException e) {

            RpgLogger.error(
                    "Impossible d'ouvrir l'éditeur d'action via NeoForge : "
                            + e.getMessage()
            );

            return false;
        }
    }

    public boolean showDialogueQuestSelector(
            UUID playerUuid,
            String dialogueKey,
            List<Map<String, String>> quests
    ) {
        if (showDialogueQuestSelectorMethod == null) {
            return false;
        }

        try {
            Object result =
                    showDialogueQuestSelectorMethod.invoke(
                            null,
                            playerUuid,
                            dialogueKey,
                            quests
                    );

            return result instanceof Boolean success && success;

        } catch (ReflectiveOperationException e) {
            RpgLogger.error(
                    "Impossible d'ouvrir le sélecteur de quête via NeoForge : "
                            + e.getMessage()
            );
            return false;
        }
    }

    /**
     * Configure le traitement d'une demande
     * de suppression de scénario.
     */
    public void setDialogueScenarioDeleteHandler(
            BiConsumer<UUID, String> handler
    ) {
        this.dialogueScenarioDeleteHandler =
                handler;
    }

    /**
     * Envoie la liste des scénarios
     * au client NeoForge.
     */
    public boolean showDialogueManager(
            UUID playerUuid,
            List<DialogueEditorScenarioSummaryView> scenarios
    ) {
        if (showDialogueManagerMethod == null) {
            return false;
        }

        List<Map<String, Object>> data =
                scenarios.stream()
                        .map(
                                mapper::toScenarioSummaryMap
                        )
                        .toList();

        try {
            Object result =
                    showDialogueManagerMethod.invoke(
                            null,
                            playerUuid,
                            data
                    );

            return result instanceof Boolean success
                    && success;

        } catch (ReflectiveOperationException e) {

            RpgLogger.error(
                    "Impossible d'ouvrir le gestionnaire "
                            + "de dialogues via NeoForge : "
                            + e.getMessage()
            );

            return false;
        }
    }

    /**
     * Envoie une vue complète de l'éditeur
     * au client NeoForge.
     */
    public boolean showDialogueEditor(
            UUID playerUuid,
            DialogueEditorView view
    ) {
        if (showDialogueEditorMethod == null) {
            return false;
        }

        try {
            Object result =
                    showDialogueEditorMethod.invoke(
                            null,
                            playerUuid,
                            mapper.toEditorMap(
                                    view
                            )
                    );

            return result instanceof Boolean success
                    && success;

        } catch (ReflectiveOperationException e) {

            RpgLogger.error(
                    "Impossible d'ouvrir l'éditeur via NeoForge : "
                            + e.getMessage()
            );

            return false;
        }
    }

    /**
     * Nettoie les callbacks et références
     * réflexives de l'éditeur.
     */
    public void shutdown() {

        clearHandler(
                clearDialogueEditorRequestHandlerMethod,
                "DIALOGUE_EDITOR_REQUEST"
        );

        clearHandler(
                clearDialogueTriggerSelectionStartHandlerMethod,
                "DIALOGUE_TRIGGER_SELECTION_START"
        );

        clearHandler(
                clearDialogueQuestListRequestHandlerMethod,
                "DIALOGUE_QUEST_LIST_REQUEST"
        );

        clearHandler(
                clearDialogueQuestSelectionHandlerMethod,
                "DIALOGUE_QUEST_SELECTION"
        );

        clearHandler(
                clearDialogueNodeTextUpdateHandlerMethod,
                "DIALOGUE_NODE_TEXT_UPDATE"
        );

        clearHandler(
                clearDialogueChoiceLabelUpdateHandlerMethod,
                "DIALOGUE_CHOICE_LABEL_UPDATE"
        );

        clearHandler(
                clearDialoguePlayerReplyAddHandlerMethod,
                "DIALOGUE_PLAYER_REPLY_ADD"
        );

        clearHandler(
                clearDialogueTransitionTerminalHandlerMethod,
                "DIALOGUE_TRANSITION_TERMINAL"
        );


        clearHandler(
                clearDialogueNpcReplyInsertHandlerMethod,
                "DIALOGUE_NPC_REPLY_INSERT"
        );

        clearHandler(
                clearDialogueBranchCreateHandlerMethod,
                "DIALOGUE_BRANCH_CREATE"
        );

        clearHandler(
                clearDialogueElementDeleteHandlerMethod,
                "DIALOGUE_ELEMENT_DELETE"
        );

        clearHandler(
                clearDialogueActionEditorRequestHandlerMethod,
                "DIALOGUE_ACTION_EDITOR_REQUEST"
        );

        clearHandler(
                clearDialogueActionUpdateHandlerMethod,
                "DIALOGUE_ACTION_UPDATE"
        );

        clearHandler(
                clearDialogueConditionEditorRequestHandlerMethod,
                "DIALOGUE_CONDITION_EDITOR_REQUEST"
        );

        clearHandler(
                clearDialogueConditionUpdateHandlerMethod,
                "DIALOGUE_CONDITION_UPDATE"
        );

        clearHandler(
                clearDialogueScenarioDeleteHandlerMethod,
                "DIALOGUE_SCENARIO_DELETE"
        );

        clearHandler(
                clearDialogueScenarioCreateHandlerMethod,
                "DIALOGUE_SCENARIO_CREATE"
        );

        clearHandler(
                clearDialogueEditorStateSwitchHandlerMethod,
                "DIALOGUE_EDITOR_STATE_SWITCH"
        );

        dialogueEditorRequestHandler =
                null;

        dialogueTriggerSelectionStartHandler =
                null;

        dialogueQuestListRequestHandler =
                null;

        dialogueQuestSelectionHandler =
                null;

        dialogueNodeTextUpdateHandler =
                null;

        dialogueChoiceLabelUpdateHandler =
                null;

        dialoguePlayerReplyAddHandler =
                null;

        dialogueTransitionTerminalHandler =
                null;


        dialogueNpcReplyInsertHandler =
                null;

        dialogueBranchCreateHandler =
                null;

        dialogueElementDeleteHandler =
                null;

        dialogueActionEditorRequestHandler =
                null;

        dialogueActionUpdateHandler =
                null;

        dialogueConditionEditorRequestHandler =
                null;

        dialogueConditionUpdateHandler =
                null;

        dialogueScenarioDeleteHandler =
                null;

        registeredDialogueEditorRequestCallback =
                null;

        registeredDialogueTriggerSelectionStartCallback =
                null;

        registeredDialogueQuestListRequestCallback =
                null;

        registeredDialogueQuestSelectionCallback =
                null;

        registeredDialogueNodeTextUpdateCallback =
                null;

        registeredDialogueChoiceLabelUpdateCallback =
                null;

        registeredDialoguePlayerReplyAddCallback =
                null;

        registeredDialogueTransitionTerminalCallback =
                null;


        registeredDialogueNpcReplyInsertCallback =
                null;

        registeredDialogueBranchCreateCallback =
                null;

        registeredDialogueElementDeleteCallback =
                null;

        registeredDialogueActionEditorRequestCallback =
                null;

        registeredDialogueActionUpdateCallback =
                null;

        registeredDialogueConditionEditorRequestCallback =
                null;

        registeredDialogueConditionUpdateCallback =
                null;

        registeredDialogueScenarioDeleteCallback =
                null;

        showDialogueEditorMethod =
                null;

        showDialogueManagerMethod =
                null;

        showDialogueQuestSelectorMethod =
                null;

        showDialogueActionEditorMethod =
                null;

        showDialogueConditionEditorMethod =
                null;

        registerDialogueEditorRequestHandlerMethod =
                null;

        clearDialogueEditorRequestHandlerMethod =
                null;

        registerDialogueTriggerSelectionStartHandlerMethod =
                null;

        clearDialogueTriggerSelectionStartHandlerMethod =
                null;

        registerDialogueQuestListRequestHandlerMethod =
                null;

        clearDialogueQuestListRequestHandlerMethod =
                null;

        registerDialogueQuestSelectionHandlerMethod =
                null;

        clearDialogueQuestSelectionHandlerMethod =
                null;

        registerDialogueNodeTextUpdateHandlerMethod =
                null;

        clearDialogueNodeTextUpdateHandlerMethod =
                null;

        registerDialogueChoiceLabelUpdateHandlerMethod =
                null;

        clearDialogueChoiceLabelUpdateHandlerMethod =
                null;

        registerDialoguePlayerReplyAddHandlerMethod =
                null;

        clearDialoguePlayerReplyAddHandlerMethod =
                null;

        registerDialogueTransitionTerminalHandlerMethod =
                null;

        clearDialogueTransitionTerminalHandlerMethod =
                null;


        registerDialogueNpcReplyInsertHandlerMethod =
                null;

        clearDialogueNpcReplyInsertHandlerMethod =
                null;

        registerDialogueBranchCreateHandlerMethod =
                null;

        clearDialogueBranchCreateHandlerMethod =
                null;

        registerDialogueElementDeleteHandlerMethod =
                null;

        clearDialogueElementDeleteHandlerMethod =
                null;

        registerDialogueActionEditorRequestHandlerMethod =
                null;

        clearDialogueActionEditorRequestHandlerMethod =
                null;

        registerDialogueActionUpdateHandlerMethod =
                null;

        clearDialogueActionUpdateHandlerMethod =
                null;

        registerDialogueConditionEditorRequestHandlerMethod =
                null;

        clearDialogueConditionEditorRequestHandlerMethod =
                null;

        registerDialogueConditionUpdateHandlerMethod =
                null;

        clearDialogueConditionUpdateHandlerMethod =
                null;

        registerDialogueScenarioDeleteHandlerMethod =
                null;

        clearDialogueScenarioDeleteHandlerMethod =
                null;

        dialogueScenarioCreateHandler =
                null;

        registeredDialogueScenarioCreateCallback =
                null;

        registerDialogueScenarioCreateHandlerMethod =
                null;

        clearDialogueScenarioCreateHandlerMethod =
                null;

        dialogueEditorStateSwitchHandler =
                null;

        registeredDialogueEditorStateSwitchCallback =
                null;

        registerDialogueEditorStateSwitchHandlerMethod =
                null;

        clearDialogueEditorStateSwitchHandlerMethod =
                null;
    }

    private void clearHandler(
            Method method,
            String name
    ) {
        if (method == null) {
            return;
        }

        try {
            method.invoke(
                    null
            );

        } catch (ReflectiveOperationException e) {

            RpgLogger.error(
                    "Impossible de supprimer le handler "
                            + name
                            + " : "
                            + e.getMessage()
            );
        }
    }
}