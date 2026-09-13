package fr.doryamy.rpgengine.bridge;

import fr.doryamy.rpgengine.util.RpgLogger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

/** Bridge NeoForge dédié à la sélection en jeu d'un trigger NPC depuis l'administration des personnages. */
public final class CharacterTriggerSelectionBridge {

    private Method showSelectionResultMethod;
    private Method showSelectionCandidatesMethod;
    private Method clearSelectionRequestHandlerMethod;
    private Consumer<UUID> selectionRequestHandler;

    public void initialize(Class<?> bridgeClass) throws ReflectiveOperationException {
        Objects.requireNonNull(bridgeClass, "bridgeClass");

        Method registerSelectionRequestHandlerMethod = bridgeClass.getMethod(
                "registerCharacterTriggerNpcSelectionRequestHandler", Consumer.class);
        clearSelectionRequestHandlerMethod = bridgeClass.getMethod(
                "clearCharacterTriggerNpcSelectionRequestHandler");
        showSelectionResultMethod = bridgeClass.getMethod(
                "showCharacterTriggerSelectionResult", UUID.class, int.class, String.class);
        showSelectionCandidatesMethod = bridgeClass.getMethod(
                "showCharacterTriggerSelectionCandidates", UUID.class, String.class, int[].class, String[].class);

        registerSelectionRequestHandlerMethod.invoke(null, (Consumer<UUID>) this::handleSelectionRequest);
    }

    public void setSelectionRequestHandler(Consumer<UUID> handler) {
        selectionRequestHandler = Objects.requireNonNull(handler, "handler");
    }

    public boolean showSelectionResult(UUID playerUuid, int triggerId, String triggerName) {
        return invokeBoolean(showSelectionResultMethod,
                "résultat de sélection du trigger de personnage",
                playerUuid, triggerId, triggerName);
    }

    public boolean showSelectionCandidates(UUID playerUuid, String npcName, int[] triggerIds, String[] triggerNames) {
        Objects.requireNonNull(playerUuid, "playerUuid");
        Objects.requireNonNull(npcName, "npcName");
        Objects.requireNonNull(triggerIds, "triggerIds");
        Objects.requireNonNull(triggerNames, "triggerNames");
        if (triggerIds.length != triggerNames.length) {
            throw new IllegalArgumentException("Les listes de triggers candidates sont incohérentes.");
        }
        return invokeBoolean(showSelectionCandidatesMethod,
                "liste des triggers de personnage",
                playerUuid, npcName, triggerIds, triggerNames);
    }

    private boolean invokeBoolean(Method method, String operation, Object... arguments) {
        if (method == null) {
            RpgLogger.error("Le bridge de sélection de trigger de personnage n'est pas initialisé.");
            return false;
        }
        try {
            Object result = method.invoke(null, arguments);
            return result instanceof Boolean success && success;
        } catch (IllegalAccessException | InvocationTargetException e) {
            Throwable cause = e instanceof InvocationTargetException invocation ? invocation.getCause() : e;
            RpgLogger.error("Impossible d'envoyer " + operation + " : "
                    + (cause != null ? cause.getMessage() : e.getMessage()));
            return false;
        }
    }

    private void handleSelectionRequest(UUID playerUuid) {
        Consumer<UUID> handler = selectionRequestHandler;
        if (handler == null) {
            RpgLogger.error("REQUEST_CHARACTER_TRIGGER_NPC_SELECTION reçu mais aucun contrôleur n'est disponible.");
            return;
        }
        try {
            handler.accept(playerUuid);
        } catch (RuntimeException e) {
            RpgLogger.error("REQUEST_CHARACTER_TRIGGER_NPC_SELECTION refusé pour le joueur "
                    + playerUuid + " : " + e.getMessage());
        }
    }

    public void shutdown() {
        if (clearSelectionRequestHandlerMethod != null) {
            try {
                clearSelectionRequestHandlerMethod.invoke(null);
            } catch (ReflectiveOperationException e) {
                RpgLogger.error("Impossible de supprimer le handler REQUEST_CHARACTER_TRIGGER_NPC_SELECTION : "
                        + e.getMessage());
            }
        }
        selectionRequestHandler = null;
        showSelectionResultMethod = null;
        showSelectionCandidatesMethod = null;
        clearSelectionRequestHandlerMethod = null;
    }
}
