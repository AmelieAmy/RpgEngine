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
 * Bridge entre RPGEngine Plugin et le mod RPGEngine NeoForge.
 *
 * <p>Le plugin accède au mod uniquement par réflexion afin
 * de ne conserver aucune dépendance compile-time vers NeoForge.
 *
 * <p>Le bridge assure deux directions :
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
 * <p>La logique métier reste dans le plugin, notamment dans
 * {@link DialogueRunner}. Le bridge se limite au transport
 * des données et des interactions.
 */
public final class NeoForgeBridge {

    private static final String BRIDGE_CLASS =
            "fr.doryamy.rpgengine.neoforge.bridge.RpgEngineBridgeApi";

    private Method clearDialogueContinueHandlerMethod;
    private Method clearDialogueChoiceHandlerMethod;
    private Method clearDialogueFinishHandlerMethod;

    private Method showDialogueMethod;
    private Method dismissDialogueMethod;

    private DialogueRunner dialogueRunner;

    /**
     * Initialise le bridge et enregistre auprès du mod
     * les callbacks utilisés par le système de dialogue.
     *
     * @return {@code true} si l'API NeoForge attendue
     *         a été trouvée et initialisée
     */
    public boolean initialize() {

        try {
            Class<?> bridgeClass =
                    Class.forName(
                            BRIDGE_CLASS
                    );

            /*
             * Client → serveur : CONTINUE.
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
             * Client → serveur : CHOICE.
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
             * Client → serveur : FINISH.
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
             * Serveur → client : affichage.
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

            /*
             * Serveur → client : fermeture.
             */
            dismissDialogueMethod =
                    bridgeClass.getMethod(
                            "dismissDialogue",
                            UUID.class
                    );

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

            RpgLogger.info(
                    "Bridge NeoForge initialisé."
            );

            return true;

        } catch (ReflectiveOperationException e) {

            RpgLogger.error(
                    "Impossible d'initialiser le bridge NeoForge : "
                            + e.getMessage()
            );

            return false;
        }
    }

    /**
     * Branche le runtime des dialogues au bridge.
     *
     * <p>Cette méthode est appelée après la création
     * du {@link DialogueRunner} afin d'éviter une dépendance
     * circulaire pendant l'initialisation du plugin.
     *
     * @param dialogueRunner runtime serveur des dialogues
     */
    public void setDialogueRunner(
            DialogueRunner dialogueRunner
    ) {
        this.dialogueRunner =
                dialogueRunner;
    }

    /**
     * Envoie l'état visible courant d'un dialogue
     * au client NeoForge du joueur.
     *
     * <p>Les objets de présentation du plugin sont convertis
     * en types Java standards avant l'appel réflexif afin
     * que le mod ne dépende jamais des classes du plugin.
     *
     * @param playerUuid joueur destinataire
     * @param view       état visible du dialogue
     *
     * @return {@code true} si le mod a accepté l'envoi
     */
    public boolean showDialogue(
            UUID playerUuid,
            DialogueView view
    ) {
        if (showDialogueMethod == null) {
            return false;
        }

        int size =
                view.choices().size();

        int[] positions =
                new int[size];

        String[] labels =
                new String[size];

        for (int i = 0; i < size; i++) {

            DialogueChoiceView choice =
                    view.choices().get(i);

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
                            view.interactionType().name(),
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
     * Demande au mod NeoForge de fermer l'interface
     * de dialogue du joueur.
     *
     * @param playerUuid joueur destinataire
     *
     * @return {@code true} si le mod a accepté l'envoi
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
     *
     * @param playerUuid joueur concerné
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
     *
     * @param playerUuid joueur concerné
     * @param position   position du choix sélectionné
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
     *
     * @param playerUuid joueur concerné
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
     * Supprime les callbacks enregistrés dans le mod
     * et libère les références conservées par le bridge.
     *
     * <p>Ce nettoyage est important lors de l'arrêt ou
     * du rechargement du plugin afin que le mod ne conserve
     * aucune référence vers l'ancien ClassLoader Bukkit.
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

        clearDialogueContinueHandlerMethod =
                null;

        clearDialogueChoiceHandlerMethod =
                null;

        clearDialogueFinishHandlerMethod =
                null;

        showDialogueMethod =
                null;

        dismissDialogueMethod =
                null;

        RpgLogger.info(
                "Bridge NeoForge arrêté."
        );
    }

    /**
     * Appelle une méthode de nettoyage exposée
     * par le mod NeoForge.
     *
     * @param method méthode réflexive à appeler
     * @param name   nom du handler utilisé dans les logs
     */
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