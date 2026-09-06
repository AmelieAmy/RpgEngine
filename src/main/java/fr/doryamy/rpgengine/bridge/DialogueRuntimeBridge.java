package fr.doryamy.rpgengine.bridge;

import fr.doryamy.rpgengine.dialogue.DialogueElementKey;
import fr.doryamy.rpgengine.dialogue.DialogueRunner;
import fr.doryamy.rpgengine.dialogue.runtime.view.DialogueChoiceView;
import fr.doryamy.rpgengine.dialogue.runtime.view.DialogueView;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.lang.reflect.Method;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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

    /*
     * ====================================================
     * Serveur -> client
     * ====================================================
     */

    private Method showDialogueMethod;
    private Method dismissDialogueMethod;

    /*
     * ====================================================
     * Nettoyage des callbacks client -> serveur
     * ====================================================
     */

    private Method clearDialogueContinueHandlerMethod;
    private Method clearDialogueChoiceHandlerMethod;

    /*
     * ====================================================
     * Runtime métier
     * ====================================================
     */

    private DialogueRunner dialogueRunner;

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

        BiConsumer<UUID, String> choiceCallback =
                this::handleDialogueChoice;

        registerContinueMethod.invoke(
                null,
                continueCallback
        );

        registerChoiceMethod.invoke(
                null,
                choiceCallback
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
                            view.speaker(),
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
     * Supprime les callbacks enregistrés
     * dans le mod et libère les références.
     */
    public void shutdown() {

        clearHandler(
                clearDialogueContinueHandlerMethod,
                "CONTINUE"
        );

        clearHandler(
                clearDialogueChoiceHandlerMethod,
                "CHOICE"
        );

        dialogueRunner = null;

        showDialogueMethod = null;
        dismissDialogueMethod = null;

        clearDialogueContinueHandlerMethod = null;
        clearDialogueChoiceHandlerMethod = null;
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