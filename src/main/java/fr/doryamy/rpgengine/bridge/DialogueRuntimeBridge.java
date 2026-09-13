package fr.doryamy.rpgengine.bridge;

import fr.doryamy.rpgengine.dialogue.DialogueElementKey;
import fr.doryamy.rpgengine.dialogue.DialogueRunner;
import fr.doryamy.rpgengine.dialogue.runtime.view.DialogueChoiceView;
import fr.doryamy.rpgengine.dialogue.runtime.view.DialogueParticipantView;
import fr.doryamy.rpgengine.dialogue.runtime.view.DialogueView;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Bridge spécialisé dans le runtime des dialogues.
 *
 * <p>Cette classe assure exclusivement les échanges
 * entre DialogueRunner et l'API du mod NeoForge.
 *
 * <pre>
 * Plugin -> Mod -> Client
 *   showDialogue(...)
 *   dismissDialogue(...)
 *
 * Client -> Mod -> Plugin
 *   CONTINUE
 *   CHOICE(choiceKey)
 * </pre>
 *
 * <p>La progression du dialogue reste entièrement
 * sous l'autorité de DialogueRunner.
 *
 * <p>La position visible d'un choix n'est jamais
 * utilisée comme identité métier. Seule sa
 * DialogueElementKey est renvoyée au serveur.
 */
public final class DialogueRuntimeBridge {

    private static final int PORTRAIT_CHUNK_BYTES = 24 * 1024;
    private static final int MAX_PORTRAIT_BYTES = 4 * 1024 * 1024;

    /*
     * ====================================================
     * Serveur -> client
     * ====================================================
     */

    private Method showDialogueMethod;
    private Method dismissDialogueMethod;
    private Method showDialoguePortraitChunkMethod;
    private Method showDialoguePortraitUnavailableMethod;

    /*
     * ====================================================
     * Nettoyage des callbacks client -> serveur
     * ====================================================
     */

    private Method clearDialogueContinueHandlerMethod;
    private Method clearDialogueCancelHandlerMethod;
    private Method clearDialogueChoiceHandlerMethod;
    private Method clearDialoguePortraitRequestHandlerMethod;

    /*
     * ====================================================
     * Runtime métier
     * ====================================================
     */

    private DialogueRunner dialogueRunner;
    private Function<String, byte[]> dialoguePortraitLoader;

    /**
     * Initialise le bridge runtime et enregistre
     * les callbacks provenant du mod NeoForge.
     *
     * @param bridgeClass classe RpgEngineBridgeApi
     *
     * @throws ReflectiveOperationException
     *         si le contrat attendu n'est pas disponible
     */
    public void initialize(
            Class<?> bridgeClass
    ) throws ReflectiveOperationException {

        Objects.requireNonNull(
                bridgeClass,
                "bridgeClass"
        );

        /*
         * ------------------------------------------------
         * Client -> serveur : CONTINUE
         * ------------------------------------------------
         */

        Method registerContinueMethod =
                bridgeClass.getMethod(
                        "registerDialogueContinueHandler",
                        Consumer.class
                );

        clearDialogueContinueHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialogueContinueHandler"
                );

        Method registerCancelMethod =
                bridgeClass.getMethod(
                        "registerDialogueCancelHandler",
                        Consumer.class
                );

        clearDialogueCancelHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialogueCancelHandler"
                );

        /*
         * ------------------------------------------------
         * Client -> serveur : CHOICE
         *
         * Le second paramètre est désormais
         * la clé stable du DialogueChoice.
         * ------------------------------------------------
         */

        Method registerChoiceMethod =
                bridgeClass.getMethod(
                        "registerDialogueChoiceHandler",
                        BiConsumer.class
                );

        clearDialogueChoiceHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialogueChoiceHandler"
                );

        Method registerPortraitRequestMethod =
                bridgeClass.getMethod(
                        "registerDialoguePortraitRequestHandler",
                        BiConsumer.class
                );

        clearDialoguePortraitRequestHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialoguePortraitRequestHandler"
                );

        showDialoguePortraitChunkMethod =
                bridgeClass.getMethod(
                        "showDialoguePortraitChunk",
                        UUID.class,
                        String.class,
                        int.class,
                        int.class,
                        byte[].class
                );

        showDialoguePortraitUnavailableMethod =
                bridgeClass.getMethod(
                        "showDialoguePortraitUnavailable",
                        UUID.class,
                        String.class,
                        String.class
                );

        /*
         * ------------------------------------------------
         * Serveur -> client
         *
         * Pour chaque choix nous transmettons :
         *
         * key       identité stable
         * position  ordre visible
         * label     texte affiché
         * ------------------------------------------------
         */

        showDialogueMethod =
                bridgeClass.getMethod(
                        "showDialogue",
                        UUID.class,
                        String[].class,
                        String[].class,
                        String[].class,
                        String[].class,
                        String.class,
                        String.class,
                        String.class,
                        String[].class,
                        int[].class,
                        String[].class
                );

        dismissDialogueMethod =
                bridgeClass.getMethod(
                        "dismissDialogue",
                        UUID.class
                );

        /*
         * ------------------------------------------------
         * Enregistrement des callbacks
         * ------------------------------------------------
         */

        Consumer<UUID> continueCallback =
                this::handleDialogueContinue;

        Consumer<UUID> cancelCallback =
                this::handleDialogueCancel;

        BiConsumer<UUID, String> choiceCallback =
                this::handleDialogueChoice;

        BiConsumer<UUID, String> portraitRequestCallback =
                this::handleDialoguePortraitRequest;

        registerContinueMethod.invoke(
                null,
                continueCallback
        );

        registerCancelMethod.invoke(
                null,
                cancelCallback
        );

        registerChoiceMethod.invoke(
                null,
                choiceCallback
        );

        registerPortraitRequestMethod.invoke(
                null,
                portraitRequestCallback
        );
    }

    /**
     * Branche le DialogueRunner autoritaire.
     */
    public void setDialogueRunner(
            DialogueRunner dialogueRunner
    ) {

        this.dialogueRunner =
                Objects.requireNonNull(
                        dialogueRunner,
                        "dialogueRunner"
                );
    }

    /**
     * Branche le chargeur d'assets portrait géré par le plugin.
     */
    public void setDialoguePortraitLoader(
            Function<String, byte[]> dialoguePortraitLoader
    ) {
        this.dialoguePortraitLoader =
                Objects.requireNonNull(
                        dialoguePortraitLoader,
                        "dialoguePortraitLoader"
                );
    }

    /**
     * Envoie au client l'état visible courant
     * du dialogue.
     */
    public boolean showDialogue(
            UUID playerUuid,
            DialogueView view
    ) {

        Objects.requireNonNull(
                playerUuid,
                "playerUuid"
        );

        Objects.requireNonNull(
                view,
                "view"
        );

        if (showDialogueMethod == null) {
            return false;
        }

        int participantCount =
                view.participants()
                        .size();

        String[] participantKeys =
                new String[participantCount];

        String[] participantTypes =
                new String[participantCount];

        String[] participantDisplayNames =
                new String[participantCount];

        String[] participantPortraitResources =
                new String[participantCount];

        for (int i = 0;
             i < participantCount;
             i++) {

            DialogueParticipantView participant =
                    view.participants()
                            .get(i);

            participantKeys[i] =
                    participant.key();

            participantTypes[i] =
                    participant.type()
                            .name();

            participantDisplayNames[i] =
                    participant.displayName();

            participantPortraitResources[i] =
                    participant.portraitResource();
        }

        int size =
                view.choices()
                        .size();

        String[] keys =
                new String[size];

        int[] positions =
                new int[size];

        String[] labels =
                new String[size];

        for (int i = 0;
             i < size;
             i++) {

            DialogueChoiceView choice =
                    view.choices()
                            .get(i);

            keys[i] =
                    choice.key();

            positions[i] =
                    choice.position();

            labels[i] =
                    choice.label();
        }

        try {

            Object result =
                    showDialogueMethod.invoke(
                            null,
                            playerUuid,
                            participantKeys,
                            participantTypes,
                            participantDisplayNames,
                            participantPortraitResources,
                            view.activeParticipantKey(),
                            view.text(),
                            view.interactionType()
                                    .name(),
                            keys,
                            positions,
                            labels
                    );

            return result instanceof Boolean success
                    && success;

        } catch (ReflectiveOperationException e) {

            RpgLogger.error(
                    "Impossible d'envoyer le dialogue via NeoForge : "
                            + e.getMessage()
            );

            return false;
        }
    }

    /**
     * Ferme l'interface de dialogue côté client.
     */
    public boolean dismissDialogue(
            UUID playerUuid
    ) {

        Objects.requireNonNull(
                playerUuid,
                "playerUuid"
        );

        if (dismissDialogueMethod == null) {
            return false;
        }

        try {

            Object result =
                    dismissDialogueMethod.invoke(
                            null,
                            playerUuid
                    );

            return result instanceof Boolean success
                    && success;

        } catch (ReflectiveOperationException e) {

            RpgLogger.error(
                    "Impossible de fermer le dialogue via NeoForge : "
                            + e.getMessage()
            );

            return false;
        }
    }

    /**
     * Traite l'abandon explicite provenant du client.
     *
     * <p>L'opération est volontairement idempotente : si le serveur a déjà
     * terminé la session avant de fermer l'écran, le CANCEL tardif est ignoré.
     */
    private void handleDialogueCancel(
            UUID playerUuid
    ) {
        if (dialogueRunner == null) {
            RpgLogger.error(
                    "CANCEL_DIALOGUE reçu mais DialogueRunner indisponible."
            );
            return;
        }

        dialogueRunner.cancel(
                playerUuid
        );
    }

    /**
     * Traite CONTINUE provenant du client.
     */
    private void handleDialogueContinue(
            UUID playerUuid
    ) {

        if (dialogueRunner == null) {

            RpgLogger.error(
                    "CONTINUE_DIALOGUE reçu mais DialogueRunner indisponible."
            );

            return;
        }

        try {

            dialogueRunner.continueDialogue(
                    playerUuid
            );

        } catch (RuntimeException e) {

            RpgLogger.error(
                    "CONTINUE_DIALOGUE refusé pour le joueur "
                            + playerUuid
                            + " : "
                            + e.getMessage()
            );
        }
    }

    /**
     * Traite la sélection d'un choix.
     *
     * <p>Le client retourne exclusivement
     * la clé stable reçue lors de l'affichage.
     */
    private void handleDialogueChoice(
            UUID playerUuid,
            String choiceKeyValue
    ) {

        if (dialogueRunner == null) {

            RpgLogger.error(
                    "SELECT_DIALOGUE_CHOICE reçu "
                            + "mais DialogueRunner indisponible."
            );

            return;
        }

        final DialogueElementKey choiceKey;

        try {

            choiceKey =
                    new DialogueElementKey(
                            choiceKeyValue
                    );

        } catch (RuntimeException e) {

            RpgLogger.error(
                    "SELECT_DIALOGUE_CHOICE contient "
                            + "une clé invalide pour le joueur "
                            + playerUuid
                            + " : "
                            + choiceKeyValue
            );

            return;
        }

        try {

            dialogueRunner.choose(
                    playerUuid,
                    choiceKey
            );

        } catch (RuntimeException e) {

            RpgLogger.error(
                    "SELECT_DIALOGUE_CHOICE refusé "
                            + "pour le joueur "
                            + playerUuid
                            + " | choice="
                            + choiceKey
                            + " : "
                            + e.getMessage()
            );
        }
    }

    /**
     * Charge puis transfère un portrait demandé par le client. Le découpage
     * appartient au bridge et ne fuite donc pas dans le domaine Character.
     */
    private void handleDialoguePortraitRequest(
            UUID playerUuid,
            String portraitResource
    ) {
        if (dialoguePortraitLoader == null) {
            sendPortraitUnavailable(
                    playerUuid,
                    portraitResource,
                    "Le chargeur de portraits RPGEngine n'est pas disponible."
            );
            return;
        }

        try {
            byte[] bytes =
                    dialoguePortraitLoader.apply(
                            portraitResource
                    );

            if (bytes == null || bytes.length == 0) {
                throw new IllegalArgumentException(
                        "Le portrait demandé est vide."
                );
            }

            if (bytes.length > MAX_PORTRAIT_BYTES) {
                throw new IllegalArgumentException(
                        "Le portrait demandé dépasse la taille runtime autorisée."
                );
            }

            int chunkCount =
                    (bytes.length + PORTRAIT_CHUNK_BYTES - 1)
                            / PORTRAIT_CHUNK_BYTES;

            for (int chunkIndex = 0;
                 chunkIndex < chunkCount;
                 chunkIndex++) {

                int offset =
                        chunkIndex * PORTRAIT_CHUNK_BYTES;

                int length =
                        Math.min(
                                PORTRAIT_CHUNK_BYTES,
                                bytes.length - offset
                        );

                byte[] chunk =
                        new byte[length];

                System.arraycopy(
                        bytes,
                        offset,
                        chunk,
                        0,
                        length
                );

                Object result =
                        showDialoguePortraitChunkMethod.invoke(
                                null,
                                playerUuid,
                                portraitResource,
                                chunkIndex,
                                chunkCount,
                                chunk
                        );

                if (!(result instanceof Boolean success)
                        || !success) {
                    RpgLogger.error(
                            "Impossible d'envoyer le chunk "
                                    + chunkIndex
                                    + "/"
                                    + chunkCount
                                    + " du portrait "
                                    + portraitResource
                                    + "."
                    );
                    return;
                }
            }

        } catch (ReflectiveOperationException e) {
            RpgLogger.error(
                    "Impossible d'envoyer le portrait via NeoForge : "
                            + e.getMessage()
            );
        } catch (RuntimeException e) {
            sendPortraitUnavailable(
                    playerUuid,
                    portraitResource,
                    e.getMessage() == null
                            ? "Portrait indisponible."
                            : e.getMessage()
            );
        }
    }

    private void sendPortraitUnavailable(
            UUID playerUuid,
            String portraitResource,
            String message
    ) {
        if (showDialoguePortraitUnavailableMethod == null) {
            return;
        }

        try {
            showDialoguePortraitUnavailableMethod.invoke(
                    null,
                    playerUuid,
                    portraitResource == null ? "" : portraitResource,
                    message == null ? "Portrait indisponible." : message
            );
        } catch (ReflectiveOperationException e) {
            RpgLogger.error(
                    "Impossible de signaler un portrait indisponible via NeoForge : "
                            + e.getMessage()
            );
        }
    }

    /**
     * Supprime les callbacks enregistrés
     * dans le mod et libère les références.
     */
    public void shutdown() {

        clearHandler(
                clearDialogueContinueHandlerMethod,
                "CONTINUE"
        );

        clearHandler(
                clearDialogueCancelHandlerMethod,
                "CANCEL"
        );

        clearHandler(
                clearDialogueChoiceHandlerMethod,
                "CHOICE"
        );

        clearHandler(
                clearDialoguePortraitRequestHandlerMethod,
                "DIALOGUE_PORTRAIT_REQUEST"
        );

        dialogueRunner = null;
        dialoguePortraitLoader = null;

        showDialogueMethod = null;
        dismissDialogueMethod = null;
        showDialoguePortraitChunkMethod = null;
        showDialoguePortraitUnavailableMethod = null;

        clearDialogueContinueHandlerMethod = null;
        clearDialogueCancelHandlerMethod = null;
        clearDialogueChoiceHandlerMethod = null;
        clearDialoguePortraitRequestHandlerMethod = null;
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