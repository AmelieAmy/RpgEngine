package fr.doryamy.rpgengine.bridge;

import fr.doryamy.rpgengine.dialogue.character.transport.CharacterAdminTransportEncoder;
import fr.doryamy.rpgengine.dialogue.character.view.CharacterAdminView;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** Transport dédié à l'administration globale des personnages. */
public final class CharacterAdminBridge {

    private final CharacterAdminTransportEncoder transportEncoder;

    private Method openCharacterAdminMethod;
    private Method clearCharacterAdminRequestHandlerMethod;
    private Method clearCharacterCreateRequestHandlerMethod;
    private Method clearCharacterUpdateRequestHandlerMethod;
    private Method clearCharacterDeleteRequestHandlerMethod;

    private Consumer<UUID> adminRequestHandler;
    private BiConsumer<UUID, Map<String, String>> createRequestHandler;
    private BiConsumer<UUID, Map<String, String>> updateRequestHandler;
    private BiConsumer<UUID, String> deleteRequestHandler;

    public CharacterAdminBridge(CharacterAdminTransportEncoder transportEncoder) {
        this.transportEncoder = Objects.requireNonNull(transportEncoder, "transportEncoder");
    }

    public void initialize(Class<?> bridgeClass) throws ReflectiveOperationException {
        Objects.requireNonNull(bridgeClass, "bridgeClass");

        Method registerAdmin = bridgeClass.getMethod("registerCharacterAdminRequestHandler", Consumer.class);
        Method registerCreate = bridgeClass.getMethod("registerCharacterCreateRequestHandler", BiConsumer.class);
        Method registerUpdate = bridgeClass.getMethod("registerCharacterUpdateRequestHandler", BiConsumer.class);
        Method registerDelete = bridgeClass.getMethod("registerCharacterDeleteRequestHandler", BiConsumer.class);

        clearCharacterAdminRequestHandlerMethod = bridgeClass.getMethod("clearCharacterAdminRequestHandler");
        clearCharacterCreateRequestHandlerMethod = bridgeClass.getMethod("clearCharacterCreateRequestHandler");
        clearCharacterUpdateRequestHandlerMethod = bridgeClass.getMethod("clearCharacterUpdateRequestHandler");
        clearCharacterDeleteRequestHandlerMethod = bridgeClass.getMethod("clearCharacterDeleteRequestHandler");
        openCharacterAdminMethod = bridgeClass.getMethod("openCharacterAdmin", UUID.class, String.class);

        registerAdmin.invoke(null, (Consumer<UUID>) this::handleAdminRequest);
        registerCreate.invoke(null, (BiConsumer<UUID, Map<String, String>>) this::handleCreateRequest);
        registerUpdate.invoke(null, (BiConsumer<UUID, Map<String, String>>) this::handleUpdateRequest);
        registerDelete.invoke(null, (BiConsumer<UUID, String>) this::handleDeleteRequest);
    }

    public void setAdminRequestHandler(Consumer<UUID> handler) {
        adminRequestHandler = Objects.requireNonNull(handler, "handler");
    }

    public void setCreateRequestHandler(BiConsumer<UUID, Map<String, String>> handler) {
        createRequestHandler = Objects.requireNonNull(handler, "handler");
    }

    public void setUpdateRequestHandler(BiConsumer<UUID, Map<String, String>> handler) {
        updateRequestHandler = Objects.requireNonNull(handler, "handler");
    }

    public void setDeleteRequestHandler(BiConsumer<UUID, String> handler) {
        deleteRequestHandler = Objects.requireNonNull(handler, "handler");
    }

    public boolean openCharacterAdmin(UUID playerUuid, CharacterAdminView view) {
        Objects.requireNonNull(playerUuid, "playerUuid");
        Objects.requireNonNull(view, "view");

        if (openCharacterAdminMethod == null) {
            RpgLogger.error("Le bridge d'administration des personnages n'est pas initialisé.");
            return false;
        }

        return invokeBoolean(
                openCharacterAdminMethod,
                playerUuid,
                transportEncoder.encode(view),
                "la bibliothèque de personnages"
        );
    }

    private void handleAdminRequest(UUID playerUuid) {
        Consumer<UUID> handler = adminRequestHandler;
        if (handler == null) {
            RpgLogger.error("Aucun handler CHARACTER_ADMIN enregistré côté plugin.");
            return;
        }
        handler.accept(playerUuid);
    }

    private void handleCreateRequest(UUID playerUuid, Map<String, String> request) {
        BiConsumer<UUID, Map<String, String>> handler = createRequestHandler;
        if (handler == null) {
            RpgLogger.error("Aucun handler CREATE_CHARACTER enregistré côté plugin.");
            return;
        }
        handler.accept(playerUuid, Map.copyOf(request));
    }

    private void handleUpdateRequest(UUID playerUuid, Map<String, String> request) {
        BiConsumer<UUID, Map<String, String>> handler = updateRequestHandler;
        if (handler == null) {
            RpgLogger.error("Aucun handler UPDATE_CHARACTER enregistré côté plugin.");
            return;
        }
        handler.accept(playerUuid, Map.copyOf(request));
    }

    private void handleDeleteRequest(UUID playerUuid, String key) {
        BiConsumer<UUID, String> handler = deleteRequestHandler;
        if (handler == null) {
            RpgLogger.error("Aucun handler DELETE_CHARACTER enregistré côté plugin.");
            return;
        }
        handler.accept(playerUuid, key);
    }

    public void shutdown() {
        clearHandler(clearCharacterAdminRequestHandlerMethod, "CHARACTER_ADMIN");
        clearHandler(clearCharacterCreateRequestHandlerMethod, "CREATE_CHARACTER");
        clearHandler(clearCharacterUpdateRequestHandlerMethod, "UPDATE_CHARACTER");
        clearHandler(clearCharacterDeleteRequestHandlerMethod, "DELETE_CHARACTER");

        adminRequestHandler = null;
        createRequestHandler = null;
        updateRequestHandler = null;
        deleteRequestHandler = null;
        openCharacterAdminMethod = null;
    }

    private void clearHandler(Method method, String name) {
        if (method == null) {
            return;
        }
        try {
            method.invoke(null);
        } catch (IllegalAccessException | InvocationTargetException e) {
            RpgLogger.error("Impossible de supprimer le handler " + name + " : " + e.getMessage());
        }
    }

    private boolean invokeBoolean(Method method, UUID playerUuid, String json, String operation) {
        try {
            Object result = method.invoke(null, playerUuid, json);
            return result instanceof Boolean value && value;
        } catch (IllegalAccessException | InvocationTargetException e) {
            Throwable cause = e instanceof InvocationTargetException invocation
                    ? invocation.getCause()
                    : e;
            RpgLogger.error("Impossible d'ouvrir " + operation + " : "
                    + (cause != null ? cause.getMessage() : e.getMessage()));
            return false;
        }
    }
}
