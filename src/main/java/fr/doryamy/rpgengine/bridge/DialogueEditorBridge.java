package fr.doryamy.rpgengine.bridge;

import fr.doryamy.rpgengine.dialogue.editor.transport.DialogueAdminTransportEncoder;
import fr.doryamy.rpgengine.dialogue.editor.transport.DialogueEditorTransportEncoder;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueAdminView;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueEditorView;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Bridge NeoForge dédié à l'administration
 * des dialogues.
 *
 * <p>Cette classe assure exclusivement
 * le transport entre le plugin RPGEngine
 * et le mod NeoForge.
 *
 * <p>Aucune logique métier de dialogue
 * ne doit être placée ici.
 */
public final class DialogueEditorBridge {

    private final DialogueEditorTransportEncoder editorTransportEncoder;
    private final DialogueAdminTransportEncoder adminTransportEncoder;

    private Method openDialogueEditorMethod;
    private Method openDialogueAdminMethod;
    private Method showDialogueNpcSelectionResultMethod;

    private Method clearDialogueEditorRequestHandlerMethod;
    private Method clearDialogueCreateRequestHandlerMethod;
    private Method clearDialogueNpcSelectionRequestHandlerMethod;
    private Method clearDialogueTriggerNpcSelectionRequestHandlerMethod;
    private Method clearDialogueDeleteRequestHandlerMethod;
    private Method clearDialogueQuestRuleRequestHandlerMethod;
    private Method clearDialogueAdminRequestHandlerMethod;
    private Method clearDialogueRenameRequestHandlerMethod;
    private Method clearDialogueInsertElementRequestHandlerMethod;
    private Method clearDialogueElementTextUpdateRequestHandlerMethod;
    private Method clearDialogueElementDeleteRequestHandlerMethod;
    private Method clearDialogueRuleDeleteRequestHandlerMethod;
    private Method clearDialogueAddChoiceRequestHandlerMethod;

    private BiConsumer<UUID, String>
            dialogueEditorRequestHandler;

    private BiConsumer<UUID, String>
            dialogueCreateRequestHandler;

    private Consumer<UUID>
            dialogueNpcSelectionRequestHandler;

    private BiConsumer<UUID, String>
            dialogueTriggerNpcSelectionRequestHandler;

    private BiConsumer<UUID, String>
            dialogueDeleteRequestHandler;

    private BiConsumer<UUID, Map<String, String>>
            dialogueQuestRuleRequestHandler;

    private Consumer<UUID> dialogueAdminRequestHandler;

    private BiConsumer<UUID, Map<String, String>>
            dialogueRenameRequestHandler;

    private BiConsumer<UUID, Map<String, String>>
            dialogueInsertElementRequestHandler;

    private BiConsumer<UUID, Map<String, String>>
            dialogueElementTextUpdateRequestHandler;

    private BiConsumer<UUID, Map<String, String>>
            dialogueElementDeleteRequestHandler;

    private BiConsumer<UUID, Map<String, String>>
            dialogueRuleDeleteRequestHandler;

    private BiConsumer<UUID, Map<String, String>>
            dialogueAddChoiceRequestHandler;

    public DialogueEditorBridge(
            DialogueEditorTransportEncoder editorTransportEncoder,
            DialogueAdminTransportEncoder adminTransportEncoder
    ) {

        this.editorTransportEncoder =
                Objects.requireNonNull(
                        editorTransportEncoder,
                        "editorTransportEncoder"
                );

        this.adminTransportEncoder =
                Objects.requireNonNull(
                        adminTransportEncoder,
                        "adminTransportEncoder"
                );
    }

    public void initialize(
            Class<?> bridgeClass
    ) throws ReflectiveOperationException {

        Objects.requireNonNull(
                bridgeClass,
                "bridgeClass"
        );

        Method registerEditorRequestMethod =
                bridgeClass.getMethod(
                        "registerDialogueEditorRequestHandler",
                        BiConsumer.class
                );

        clearDialogueEditorRequestHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialogueEditorRequestHandler"
                );

        Method registerCreateRequestMethod =
                bridgeClass.getMethod(
                        "registerDialogueCreateRequestHandler",
                        BiConsumer.class
                );

        clearDialogueCreateRequestHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialogueCreateRequestHandler"
                );

        Method registerNpcSelectionRequestMethod =
                bridgeClass.getMethod(
                        "registerDialogueNpcSelectionRequestHandler",
                        Consumer.class
                );

        clearDialogueNpcSelectionRequestHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialogueNpcSelectionRequestHandler"
                );

        Method registerDialogueTriggerNpcSelectionRequestMethod =
                bridgeClass.getMethod(
                        "registerDialogueTriggerNpcSelectionRequestHandler",
                        BiConsumer.class
                );

        clearDialogueTriggerNpcSelectionRequestHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialogueTriggerNpcSelectionRequestHandler"
                );

        Method registerDialogueDeleteRequestMethod =
                bridgeClass.getMethod(
                        "registerDialogueDeleteRequestHandler",
                        BiConsumer.class
                );

        clearDialogueDeleteRequestHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialogueDeleteRequestHandler"
                );

        Method registerDialogueQuestRuleRequestMethod =
                bridgeClass.getMethod(
                        "registerDialogueQuestRuleRequestHandler",
                        BiConsumer.class
                );

        clearDialogueQuestRuleRequestHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialogueQuestRuleRequestHandler"
                );

        Method registerDialogueAdminRequestMethod =
                bridgeClass.getMethod(
                        "registerDialogueAdminRequestHandler",
                        Consumer.class
                );

        clearDialogueAdminRequestHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialogueAdminRequestHandler"
                );

        Method registerDialogueRenameRequestMethod =
                bridgeClass.getMethod(
                        "registerDialogueRenameRequestHandler",
                        BiConsumer.class
                );

        clearDialogueRenameRequestHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialogueRenameRequestHandler"
                );

        Method registerDialogueInsertElementRequestMethod =
                bridgeClass.getMethod(
                        "registerDialogueInsertElementRequestHandler",
                        BiConsumer.class
                );

        clearDialogueInsertElementRequestHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialogueInsertElementRequestHandler"
                );

        Method registerDialogueElementTextUpdateRequestMethod =
                bridgeClass.getMethod(
                        "registerDialogueElementTextUpdateRequestHandler",
                        BiConsumer.class
                );

        clearDialogueElementTextUpdateRequestHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialogueElementTextUpdateRequestHandler"
                );

        Method registerDialogueElementDeleteRequestMethod =
                bridgeClass.getMethod(
                        "registerDialogueElementDeleteRequestHandler",
                        BiConsumer.class
                );

        clearDialogueElementDeleteRequestHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialogueElementDeleteRequestHandler"
                );

        Method registerDialogueAddChoiceRequestMethod =
                bridgeClass.getMethod(
                        "registerDialogueAddChoiceRequestHandler",
                        BiConsumer.class
                );

        clearDialogueAddChoiceRequestHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialogueAddChoiceRequestHandler"
                );

        Method registerDialogueRuleDeleteRequestMethod =
                bridgeClass.getMethod(
                        "registerDialogueRuleDeleteRequestHandler",
                        BiConsumer.class
                );

        clearDialogueRuleDeleteRequestHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialogueRuleDeleteRequestHandler"
                );

        openDialogueAdminMethod =
                bridgeClass.getMethod(
                        "openDialogueAdmin",
                        UUID.class,
                        String.class
                );

        openDialogueEditorMethod =
                bridgeClass.getMethod(
                        "openDialogueEditor",
                        UUID.class,
                        String.class
                );

        showDialogueNpcSelectionResultMethod =
                bridgeClass.getMethod(
                        "showDialogueNpcSelectionResult",
                        UUID.class,
                        String.class,
                        String.class
                );

        registerEditorRequestMethod.invoke(
                null,
                (BiConsumer<UUID, String>)
                        this::handleDialogueEditorRequest
        );

        registerCreateRequestMethod.invoke(
                null,
                (BiConsumer<UUID, String>)
                        this::handleDialogueCreateRequest
        );

        registerNpcSelectionRequestMethod.invoke(
                null,
                (Consumer<UUID>)
                        this::handleDialogueNpcSelectionRequest
        );

        registerDialogueTriggerNpcSelectionRequestMethod.invoke(
                null,
                (BiConsumer<UUID, String>)
                        this::handleDialogueTriggerNpcSelectionRequest
        );

        registerDialogueDeleteRequestMethod.invoke(
                null,
                (BiConsumer<UUID, String>)
                        this::handleDialogueDeleteRequest
        );

        registerDialogueQuestRuleRequestMethod.invoke(
                null,
                (BiConsumer<UUID, Map<String, String>>)
                        this::handleDialogueQuestRuleRequest
        );

        registerDialogueAdminRequestMethod.invoke(
                null,
                (Consumer<UUID>) this::handleDialogueAdminRequest
        );

        registerDialogueRenameRequestMethod.invoke(
                null,
                (BiConsumer<UUID, Map<String, String>>)
                        this::handleDialogueRenameRequest
        );

        registerDialogueInsertElementRequestMethod.invoke(
                null,
                (BiConsumer<UUID, Map<String, String>>)
                        this::handleDialogueInsertElementRequest
        );

        registerDialogueElementTextUpdateRequestMethod.invoke(
                null,
                (BiConsumer<UUID, Map<String, String>>)
                        this::handleDialogueElementTextUpdateRequest
        );

        registerDialogueElementDeleteRequestMethod.invoke(
                null,
                (BiConsumer<UUID, Map<String, String>>)
                        this::handleDialogueElementDeleteRequest
        );

        registerDialogueAddChoiceRequestMethod.invoke(
                null,
                (BiConsumer<UUID, Map<String, String>>)
                        this::handleDialogueAddChoiceRequest
        );

        registerDialogueRuleDeleteRequestMethod.invoke(
                null,
                (BiConsumer<UUID, Map<String, String>>)
                        this::handleDialogueRuleDeleteRequest
        );
    }

    public void setDialogueEditorRequestHandler(
            BiConsumer<UUID, String> handler
    ) {

        dialogueEditorRequestHandler =
                Objects.requireNonNull(
                        handler,
                        "handler"
                );
    }

    public void setDialogueCreateRequestHandler(
            BiConsumer<UUID, String> handler
    ) {

        dialogueCreateRequestHandler =
                Objects.requireNonNull(
                        handler,
                        "handler"
                );
    }

    public void setDialogueNpcSelectionRequestHandler(
            Consumer<UUID> handler
    ) {

        dialogueNpcSelectionRequestHandler =
                Objects.requireNonNull(
                        handler,
                        "handler"
                );
    }

    public void setDialogueTriggerNpcSelectionRequestHandler(
            BiConsumer<UUID, String> handler
    ) {
        dialogueTriggerNpcSelectionRequestHandler =
                Objects.requireNonNull(handler, "handler");
    }

    public void setDialogueDeleteRequestHandler(
            BiConsumer<UUID, String> handler
    ) {
        dialogueDeleteRequestHandler =
                Objects.requireNonNull(handler, "handler");
    }

    public void setDialogueQuestRuleRequestHandler(
            BiConsumer<UUID, Map<String, String>> handler
    ) {
        dialogueQuestRuleRequestHandler =
                Objects.requireNonNull(handler, "handler");
    }

    public void setDialogueAdminRequestHandler(Consumer<UUID> handler) {
        dialogueAdminRequestHandler = Objects.requireNonNull(handler, "handler");
    }

    public void setDialogueRenameRequestHandler(
            BiConsumer<UUID, Map<String, String>> handler
    ) {
        dialogueRenameRequestHandler = Objects.requireNonNull(handler, "handler");
    }

    public void setDialogueInsertElementRequestHandler(
            BiConsumer<UUID, Map<String, String>> handler
    ) {
        dialogueInsertElementRequestHandler =
                Objects.requireNonNull(handler, "handler");
    }

    public void setDialogueElementTextUpdateRequestHandler(
            BiConsumer<UUID, Map<String, String>> handler
    ) {
        dialogueElementTextUpdateRequestHandler =
                Objects.requireNonNull(handler, "handler");
    }

    public void setDialogueElementDeleteRequestHandler(
            BiConsumer<UUID, Map<String, String>> handler
    ) {
        dialogueElementDeleteRequestHandler =
                Objects.requireNonNull(handler, "handler");
    }

    public void setDialogueAddChoiceRequestHandler(
            BiConsumer<UUID, Map<String, String>> handler
    ) {
        dialogueAddChoiceRequestHandler = Objects.requireNonNull(handler, "handler");
    }

    public void setDialogueRuleDeleteRequestHandler(
            BiConsumer<UUID, Map<String, String>> handler
    ) {
        dialogueRuleDeleteRequestHandler =
                Objects.requireNonNull(handler, "handler");
    }

    public boolean openDialogueAdmin(
            UUID playerUuid,
            DialogueAdminView view
    ) {

        Objects.requireNonNull(playerUuid, "playerUuid");
        Objects.requireNonNull(view, "view");

        if (openDialogueAdminMethod == null) {
            RpgLogger.error(
                    "Le bridge d'administration des dialogues n'est pas initialisé."
            );
            return false;
        }

        return invokeBoolean(
                openDialogueAdminMethod,
                playerUuid,
                adminTransportEncoder.encode(view),
                "l'administration des dialogues"
        );
    }

    public boolean openDialogueEditor(
            UUID playerUuid,
            DialogueEditorView view
    ) {

        Objects.requireNonNull(playerUuid, "playerUuid");
        Objects.requireNonNull(view, "view");

        if (openDialogueEditorMethod == null) {
            RpgLogger.error(
                    "Le bridge d'éditeur de dialogue n'est pas initialisé."
            );
            return false;
        }

        return invokeBoolean(
                openDialogueEditorMethod,
                playerUuid,
                editorTransportEncoder.encode(view),
                "l'éditeur de dialogue"
        );
    }

    public boolean showDialogueNpcSelectionResult(
            UUID playerUuid,
            String npcId,
            String npcName
    ) {

        Objects.requireNonNull(playerUuid, "playerUuid");
        Objects.requireNonNull(npcId, "npcId");
        Objects.requireNonNull(npcName, "npcName");

        if (showDialogueNpcSelectionResultMethod == null) {
            RpgLogger.error(
                    "Le bridge de sélection NPC n'est pas initialisé."
            );
            return false;
        }

        try {

            Object result =
                    showDialogueNpcSelectionResultMethod.invoke(
                            null,
                            playerUuid,
                            npcId,
                            npcName
                    );

            return result instanceof Boolean success
                    && success;

        } catch (IllegalAccessException | InvocationTargetException e) {

            Throwable cause =
                    e instanceof InvocationTargetException invocation
                            ? invocation.getCause()
                            : e;

            RpgLogger.error(
                    "Impossible d'envoyer le résultat de sélection NPC : "
                            + (cause != null
                            ? cause.getMessage()
                            : e.getMessage())
            );

            return false;
        }
    }

    private void handleDialogueEditorRequest(
            UUID playerUuid,
            String dialogueKey
    ) {

        BiConsumer<UUID, String> handler =
                dialogueEditorRequestHandler;

        if (handler == null) {
            RpgLogger.error(
                    "REQUEST_DIALOGUE_EDITOR reçu mais aucun contrôleur d'administration n'est disponible."
            );
            return;
        }

        try {
            handler.accept(playerUuid, dialogueKey);
        } catch (RuntimeException e) {
            RpgLogger.error(
                    "REQUEST_DIALOGUE_EDITOR refusé pour le joueur "
                            + playerUuid
                            + " | dialogue="
                            + dialogueKey
                            + " : "
                            + e.getMessage()
            );
        }
    }

    private void handleDialogueCreateRequest(
            UUID playerUuid,
            String name
    ) {

        BiConsumer<UUID, String> handler =
                dialogueCreateRequestHandler;

        if (handler == null) {
            RpgLogger.error(
                    "REQUEST_CREATE_DIALOGUE reçu mais aucun contrôleur d'administration n'est disponible."
            );
            return;
        }

        try {
            handler.accept(playerUuid, name);
        } catch (RuntimeException e) {
            RpgLogger.error(
                    "REQUEST_CREATE_DIALOGUE refusé pour le joueur "
                            + playerUuid
                            + " | name="
                            + name
                            + " : "
                            + e.getMessage()
            );
        }
    }

    private void handleDialogueNpcSelectionRequest(
            UUID playerUuid
    ) {

        Consumer<UUID> handler =
                dialogueNpcSelectionRequestHandler;

        if (handler == null) {
            RpgLogger.error(
                    "REQUEST_NPC_SELECTION reçu mais aucun contrôleur d'administration n'est disponible."
            );
            return;
        }

        try {
            handler.accept(playerUuid);
        } catch (RuntimeException e) {
            RpgLogger.error(
                    "REQUEST_NPC_SELECTION refusé pour le joueur "
                            + playerUuid
                            + " : "
                            + e.getMessage()
            );
        }
    }

    private void handleDialogueTriggerNpcSelectionRequest(
            UUID playerUuid,
            String dialogueKey
    ) {
        BiConsumer<UUID, String> handler =
                dialogueTriggerNpcSelectionRequestHandler;

        if (handler == null) {
            RpgLogger.error(
                    "REQUEST_DIALOGUE_TRIGGER_NPC_SELECTION reçu mais aucun contrôleur d'administration n'est disponible."
            );
            return;
        }

        try {
            handler.accept(playerUuid, dialogueKey);
        } catch (RuntimeException e) {
            RpgLogger.error(
                    "REQUEST_DIALOGUE_TRIGGER_NPC_SELECTION refusé pour le joueur "
                            + playerUuid
                            + " | dialogue="
                            + dialogueKey
                            + " : "
                            + e.getMessage()
            );
        }
    }

    private void handleDialogueDeleteRequest(
            UUID playerUuid,
            String dialogueKey
    ) {
        BiConsumer<UUID, String> handler = dialogueDeleteRequestHandler;

        if (handler == null) {
            RpgLogger.error(
                    "REQUEST_DELETE_DIALOGUE reçu mais aucun contrôleur d'administration n'est disponible."
            );
            return;
        }

        try {
            handler.accept(playerUuid, dialogueKey);
        } catch (RuntimeException e) {
            RpgLogger.error(
                    "REQUEST_DELETE_DIALOGUE refusé pour le joueur "
                            + playerUuid
                            + " | dialogue="
                            + dialogueKey
                            + " : "
                            + e.getMessage()
            );
        }
    }

    private void handleDialogueQuestRuleRequest(
            UUID playerUuid,
            Map<String, String> request
    ) {
        BiConsumer<UUID, Map<String, String>> handler =
                dialogueQuestRuleRequestHandler;

        if (handler == null) {
            RpgLogger.error(
                    "REQUEST_SAVE_DIALOGUE_QUEST_RULE reçu mais aucun contrôleur d'administration n'est disponible."
            );
            return;
        }

        try {
            handler.accept(playerUuid, request);
        } catch (RuntimeException e) {
            RpgLogger.error(
                    "REQUEST_SAVE_DIALOGUE_QUEST_RULE refusé pour le joueur "
                            + playerUuid
                            + " : "
                            + e.getMessage()
            );
        }
    }

    private void handleDialogueAdminRequest(UUID playerUuid) {
        Consumer<UUID> handler = dialogueAdminRequestHandler;
        if (handler == null) {
            RpgLogger.error("REQUEST_DIALOGUE_ADMIN reçu mais aucun contrôleur d'administration n'est disponible.");
            return;
        }
        try {
            handler.accept(playerUuid);
        } catch (RuntimeException e) {
            RpgLogger.error("REQUEST_DIALOGUE_ADMIN refusé pour le joueur " + playerUuid + " : " + e.getMessage());
        }
    }

    private void handleDialogueRenameRequest(
            UUID playerUuid,
            Map<String, String> request
    ) {
        BiConsumer<UUID, Map<String, String>> handler = dialogueRenameRequestHandler;
        if (handler == null) {
            RpgLogger.error("REQUEST_RENAME_DIALOGUE reçu mais aucun contrôleur d'administration n'est disponible.");
            return;
        }
        try {
            handler.accept(playerUuid, request);
        } catch (RuntimeException e) {
            RpgLogger.error("REQUEST_RENAME_DIALOGUE refusé pour le joueur " + playerUuid + " : " + e.getMessage());
        }
    }

    private void handleDialogueInsertElementRequest(
            UUID playerUuid,
            Map<String, String> request
    ) {
        BiConsumer<UUID, Map<String, String>> handler =
                dialogueInsertElementRequestHandler;

        if (handler == null) {
            RpgLogger.error(
                    "REQUEST_INSERT_DIALOGUE_ELEMENT reçu mais aucun contrôleur d'administration n'est disponible."
            );
            return;
        }

        try {
            handler.accept(playerUuid, Map.copyOf(request));
        } catch (RuntimeException e) {
            RpgLogger.error(
                    "REQUEST_INSERT_DIALOGUE_ELEMENT refusé pour le joueur "
                            + playerUuid
                            + " : "
                            + e.getMessage()
            );
        }
    }

    private void handleDialogueElementTextUpdateRequest(
            UUID playerUuid,
            Map<String, String> request
    ) {
        BiConsumer<UUID, Map<String, String>> handler =
                dialogueElementTextUpdateRequestHandler;

        if (handler == null) {
            RpgLogger.error(
                    "REQUEST_UPDATE_DIALOGUE_ELEMENT_TEXT reçu mais aucun contrôleur d'administration n'est disponible."
            );
            return;
        }

        try {
            handler.accept(playerUuid, Map.copyOf(request));
        } catch (RuntimeException e) {
            RpgLogger.error(
                    "REQUEST_UPDATE_DIALOGUE_ELEMENT_TEXT refusé pour le joueur "
                            + playerUuid
                            + " : "
                            + e.getMessage()
            );
        }
    }

    private void handleDialogueElementDeleteRequest(
            UUID playerUuid,
            Map<String, String> request
    ) {
        BiConsumer<UUID, Map<String, String>> handler =
                dialogueElementDeleteRequestHandler;

        if (handler == null) {
            RpgLogger.error(
                    "REQUEST_DELETE_DIALOGUE_ELEMENT reçu mais aucun contrôleur d'administration n'est disponible."
            );
            return;
        }

        try {
            handler.accept(playerUuid, Map.copyOf(request));
        } catch (RuntimeException e) {
            RpgLogger.error(
                    "REQUEST_DELETE_DIALOGUE_ELEMENT refusé pour le joueur "
                            + playerUuid
                            + " : "
                            + e.getMessage()
            );
        }
    }

    private void handleDialogueAddChoiceRequest(
            UUID playerUuid,
            Map<String, String> request
    ) {
        BiConsumer<UUID, Map<String, String>> handler = dialogueAddChoiceRequestHandler;
        if (handler == null) {
            RpgLogger.error("Aucun handler ADD_DIALOGUE_CHOICE enregistré côté plugin.");
            return;
        }
        handler.accept(playerUuid, Map.copyOf(request));
    }

    private void handleDialogueRuleDeleteRequest(
            UUID playerUuid,
            Map<String, String> request
    ) {
        BiConsumer<UUID, Map<String, String>> handler =
                dialogueRuleDeleteRequestHandler;

        if (handler == null) {
            RpgLogger.error(
                    "REQUEST_DELETE_DIALOGUE_RULE reçu mais aucun contrôleur d'administration n'est disponible."
            );
            return;
        }

        try {
            handler.accept(
                    playerUuid,
                    Map.copyOf(request)
            );
        } catch (RuntimeException e) {
            RpgLogger.error(
                    "REQUEST_DELETE_DIALOGUE_RULE refusé pour le joueur "
                            + playerUuid
                            + " : "
                            + e.getMessage()
            );
        }
    }

    private boolean invokeBoolean(
            Method method,
            UUID playerUuid,
            String json,
            String operation
    ) {

        try {

            Object result =
                    method.invoke(
                            null,
                            playerUuid,
                            json
                    );

            if (!(result instanceof Boolean success)) {
                RpgLogger.error(
                        "Résultat inattendu du bridge NeoForge pour "
                                + operation
                                + "."
                );
                return false;
            }

            return success;

        } catch (IllegalAccessException | InvocationTargetException e) {

            Throwable cause =
                    e instanceof InvocationTargetException invocation
                            ? invocation.getCause()
                            : e;

            RpgLogger.error(
                    "Impossible d'ouvrir "
                            + operation
                            + " : "
                            + (cause != null
                            ? cause.getMessage()
                            : e.getMessage())
            );

            return false;
        }
    }

    public void shutdown() {

        clearHandler(
                clearDialogueEditorRequestHandlerMethod,
                "REQUEST_DIALOGUE_EDITOR"
        );

        clearHandler(
                clearDialogueCreateRequestHandlerMethod,
                "REQUEST_CREATE_DIALOGUE"
        );

        clearHandler(
                clearDialogueNpcSelectionRequestHandlerMethod,
                "REQUEST_NPC_SELECTION"
        );

        clearHandler(
                clearDialogueTriggerNpcSelectionRequestHandlerMethod,
                "REQUEST_DIALOGUE_TRIGGER_NPC_SELECTION"
        );

        clearHandler(
                clearDialogueDeleteRequestHandlerMethod,
                "REQUEST_DELETE_DIALOGUE"
        );

        clearHandler(
                clearDialogueQuestRuleRequestHandlerMethod,
                "REQUEST_SAVE_DIALOGUE_QUEST_RULE"
        );

        clearHandler(
                clearDialogueAdminRequestHandlerMethod,
                "REQUEST_DIALOGUE_ADMIN"
        );

        clearHandler(
                clearDialogueRenameRequestHandlerMethod,
                "REQUEST_RENAME_DIALOGUE"
        );

        clearHandler(
                clearDialogueInsertElementRequestHandlerMethod,
                "REQUEST_INSERT_DIALOGUE_ELEMENT"
        );

        clearHandler(
                clearDialogueElementTextUpdateRequestHandlerMethod,
                "REQUEST_UPDATE_DIALOGUE_ELEMENT_TEXT"
        );

        clearHandler(
                clearDialogueElementDeleteRequestHandlerMethod,
                "REQUEST_DELETE_DIALOGUE_ELEMENT"
        );

        clearHandler(
                clearDialogueRuleDeleteRequestHandlerMethod,
                "REQUEST_DELETE_DIALOGUE_RULE"
        );

        clearHandler(
                clearDialogueAddChoiceRequestHandlerMethod,
                "REQUEST_ADD_DIALOGUE_CHOICE"
        );

        dialogueEditorRequestHandler = null;
        dialogueCreateRequestHandler = null;
        dialogueNpcSelectionRequestHandler = null;
        dialogueTriggerNpcSelectionRequestHandler = null;
        dialogueDeleteRequestHandler = null;
        dialogueQuestRuleRequestHandler = null;
        dialogueAdminRequestHandler = null;
        dialogueRenameRequestHandler = null;
        dialogueInsertElementRequestHandler = null;
        dialogueElementTextUpdateRequestHandler = null;
        dialogueElementDeleteRequestHandler = null;
        dialogueRuleDeleteRequestHandler = null;
        dialogueAddChoiceRequestHandler = null;

        openDialogueEditorMethod = null;
        openDialogueAdminMethod = null;
        showDialogueNpcSelectionResultMethod = null;

        clearDialogueEditorRequestHandlerMethod = null;
        clearDialogueCreateRequestHandlerMethod = null;
        clearDialogueNpcSelectionRequestHandlerMethod = null;
        clearDialogueTriggerNpcSelectionRequestHandlerMethod = null;
        clearDialogueDeleteRequestHandlerMethod = null;
        clearDialogueQuestRuleRequestHandlerMethod = null;
        clearDialogueAdminRequestHandlerMethod = null;
        clearDialogueRenameRequestHandlerMethod = null;
        clearDialogueInsertElementRequestHandlerMethod = null;
        clearDialogueElementTextUpdateRequestHandlerMethod = null;
        clearDialogueElementDeleteRequestHandlerMethod = null;
        clearDialogueRuleDeleteRequestHandlerMethod = null;
        clearDialogueAddChoiceRequestHandlerMethod = null;
    }

    private void clearHandler(
            Method method,
            String name
    ) {

        if (method == null) {
            return;
        }

        try {
            method.invoke(null);
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
