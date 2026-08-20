package fr.doryamy.rpgengine.bridge;

import fr.doryamy.rpgengine.bridge.mapping.DialogueEditorBridgeMapper;
import fr.doryamy.rpgengine.dialogue.editor.CreateDialogueScenarioRequest;
import fr.doryamy.rpgengine.dialogue.editor.SwitchDialogueEditorStateRequest;
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

    /*
     * Client → serveur : ouverture.
     */
    private Method registerDialogueEditorRequestHandlerMethod;
    private Method clearDialogueEditorRequestHandlerMethod;

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
            dialogueScenarioDeleteHandler;

    /*
     * Références fortes vers les callbacks réellement
     * enregistrés dans le mod.
     */
    private BiConsumer<UUID, String>
            registeredDialogueEditorRequestCallback;

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

        dialogueScenarioDeleteHandler =
                null;

        registeredDialogueEditorRequestCallback =
                null;

        registeredDialogueScenarioDeleteCallback =
                null;

        showDialogueEditorMethod =
                null;

        showDialogueManagerMethod =
                null;

        registerDialogueEditorRequestHandlerMethod =
                null;

        clearDialogueEditorRequestHandlerMethod =
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