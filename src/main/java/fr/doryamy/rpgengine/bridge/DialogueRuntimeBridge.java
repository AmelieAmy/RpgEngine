package fr.doryamy.rpgengine.bridge;

import fr.doryamy.rpgengine.dialogue.DialogueRunner;
import fr.doryamy.rpgengine.dialogue.presentation.DialogueChoiceView;
import fr.doryamy.rpgengine.dialogue.presentation.DialogueView;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.lang.reflect.Method;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Bridge spécialisé dans le runtime des dialogues.
 *
 * <p>Cette classe assure les échanges entre
 * DialogueRunner et l'API du mod NeoForge :
 *
 * <pre>
 * Plugin → Mod → Client
 *   showDialogue(...)
 *   dismissDialogue(...)
 *
 * Client → Mod → Plugin
 *   CONTINUE
 *   CHOICE
 *   FINISH
 * </pre>
 *
 * <p>Elle ne contient aucune logique métier
 * de dialogue. L'exécution reste sous la
 * responsabilité de DialogueRunner.
 */
public final class DialogueRuntimeBridge {

    /*
     * Serveur → client.
     */
    private Method showDialogueMethod;
    private Method dismissDialogueMethod;

    /*
     * Nettoyage des callbacks client → serveur.
     */
    private Method clearDialogueContinueHandlerMethod;
    private Method clearDialogueChoiceHandlerMethod;
    private Method clearDialogueFinishHandlerMethod;

    /*
     * Runtime métier du plugin.
     */
    private DialogueRunner dialogueRunner;

    /**
     * Initialise les méthodes réflexives et
     * enregistre les callbacks du runtime dialogue.
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
         * Client → serveur : CONTINUE
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
         * Client → serveur : CHOICE
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
         * Client → serveur : FINISH
         * ------------------------------------------------
         */
        Method registerFinishMethod =
                bridgeClass.getMethod(
                        "registerDialogueFinishHandler",
                        Consumer.class
                );

        clearDialogueFinishHandlerMethod =
                bridgeClass.getMethod(
                        "clearDialogueFinishHandler"
                );

        /*
         * ------------------------------------------------
         * Serveur → client
         * ------------------------------------------------
         */
        showDialogueMethod =
                bridgeClass.getMethod(
                        "showDialogue",
                        UUID.class,
                        String.class,
                        String.class,
                        String.class,
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

        BiConsumer<UUID, Integer> choiceCallback =
                this::handleDialogueChoice;

        Consumer<UUID> finishCallback =
                this::handleDialogueFinish;

        registerContinueMethod.invoke(
                null,
                continueCallback
        );

        registerChoiceMethod.invoke(
                null,
                choiceCallback
        );

        registerFinishMethod.invoke(
                null,
                finishCallback
        );
    }

    /**
     * Branche le runtime métier des dialogues.
     */
    public void setDialogueRunner(
            DialogueRunner dialogueRunner
    ) {
        this.dialogueRunner =
                dialogueRunner;
    }

    /**
     * Envoie l'état visible courant d'un dialogue
     * au client NeoForge.
     */
    public boolean showDialogue(
            UUID playerUuid,
            DialogueView view
    ) {
        if (showDialogueMethod == null) {
            return false;
        }

        int size =
                view.choices()
                        .size();

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
     * Ferme l'interface de dialogue du joueur.
     */
    public boolean dismissDialogue(
            UUID playerUuid
    ) {
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
     * Traite une demande CONTINUE provenant
     * du client NeoForge.
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

        boolean success =
                dialogueRunner.advance(
                        playerUuid
                );

        if (!success) {

            RpgLogger.debug(
                    "CONTINUE_DIALOGUE refusé pour le joueur "
                            + playerUuid
            );
        }
    }

    /**
     * Traite la sélection d'un choix provenant
     * du client NeoForge.
     */
    private void handleDialogueChoice(
            UUID playerUuid,
            Integer position
    ) {
        if (dialogueRunner == null) {

            RpgLogger.error(
                    "SELECT_DIALOGUE_CHOICE reçu "
                            + "mais DialogueRunner indisponible."
            );

            return;
        }

        boolean success =
                dialogueRunner.choose(
                        playerUuid,
                        position
                );

        if (!success) {

            RpgLogger.debug(
                    "SELECT_DIALOGUE_CHOICE refusé "
                            + "pour le joueur "
                            + playerUuid
                            + " | position="
                            + position
            );
        }
    }

    /**
     * Traite une confirmation de fin provenant
     * du client NeoForge.
     */
    private void handleDialogueFinish(
            UUID playerUuid
    ) {
        if (dialogueRunner == null) {

            RpgLogger.error(
                    "FINISH_DIALOGUE reçu mais DialogueRunner indisponible."
            );

            return;
        }

        boolean success =
                dialogueRunner.finish(
                        playerUuid
                );

        if (!success) {

            RpgLogger.debug(
                    "FINISH_DIALOGUE refusé pour le joueur "
                            + playerUuid
            );
        }
    }

    /**
     * Supprime les callbacks enregistrés dans
     * le mod et libère les références conservées.
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

        clearHandler(
                clearDialogueFinishHandlerMethod,
                "FINISH"
        );

        dialogueRunner =
                null;

        showDialogueMethod =
                null;

        dismissDialogueMethod =
                null;

        clearDialogueContinueHandlerMethod =
                null;

        clearDialogueChoiceHandlerMethod =
                null;

        clearDialogueFinishHandlerMethod =
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