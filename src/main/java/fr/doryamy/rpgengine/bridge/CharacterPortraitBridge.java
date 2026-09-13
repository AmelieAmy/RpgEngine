package fr.doryamy.rpgengine.bridge;

import fr.doryamy.rpgengine.util.RpgLogger;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;

/** Transport NeoForge dédié à l'upload de portraits Character. */
public final class CharacterPortraitBridge {

    private Method clearUploadRequestHandlerMethod;
    private Method showUploadResultMethod;
    private BiConsumer<UUID, byte[]> uploadRequestHandler;

    public void initialize(Class<?> bridgeClass) throws ReflectiveOperationException {
        Objects.requireNonNull(bridgeClass, "bridgeClass");

        Method registerUpload = bridgeClass.getMethod(
                "registerCharacterPortraitUploadRequestHandler",
                BiConsumer.class
        );

        clearUploadRequestHandlerMethod = bridgeClass.getMethod(
                "clearCharacterPortraitUploadRequestHandler"
        );

        showUploadResultMethod = bridgeClass.getMethod(
                "showCharacterPortraitUploadResult",
                UUID.class,
                boolean.class,
                String.class,
                String.class
        );

        registerUpload.invoke(
                null,
                (BiConsumer<UUID, byte[]>) this::handleUploadRequest
        );
    }

    public void setUploadRequestHandler(BiConsumer<UUID, byte[]> handler) {
        uploadRequestHandler = Objects.requireNonNull(handler, "handler");
    }

    public boolean showUploadResult(
            UUID playerUuid,
            boolean success,
            String portraitResource,
            String message
    ) {
        Objects.requireNonNull(playerUuid, "playerUuid");

        if (showUploadResultMethod == null) {
            RpgLogger.error("Le bridge d'upload des portraits n'est pas initialisé.");
            return false;
        }

        try {
            Object result = showUploadResultMethod.invoke(
                    null,
                    playerUuid,
                    success,
                    portraitResource == null ? "" : portraitResource,
                    message == null ? "" : message
            );
            return result instanceof Boolean value && value;
        } catch (IllegalAccessException | InvocationTargetException e) {
            Throwable cause = e instanceof InvocationTargetException invocation
                    ? invocation.getCause()
                    : e;
            RpgLogger.error(
                    "Impossible d'envoyer le résultat d'upload du portrait : "
                            + (cause != null ? cause.getMessage() : e.getMessage())
            );
            return false;
        }
    }

    private void handleUploadRequest(UUID playerUuid, byte[] imageBytes) {
        BiConsumer<UUID, byte[]> handler = uploadRequestHandler;
        if (handler == null) {
            RpgLogger.error("Aucun handler CHARACTER_PORTRAIT_UPLOAD enregistré côté plugin.");
            return;
        }
        handler.accept(playerUuid, imageBytes.clone());
    }

    public void shutdown() {
        if (clearUploadRequestHandlerMethod != null) {
            try {
                clearUploadRequestHandlerMethod.invoke(null);
            } catch (ReflectiveOperationException e) {
                RpgLogger.error(
                        "Impossible de supprimer le handler CHARACTER_PORTRAIT_UPLOAD : "
                                + e.getMessage()
                );
            }
        }

        uploadRequestHandler = null;
        clearUploadRequestHandlerMethod = null;
        showUploadResultMethod = null;
    }
}
