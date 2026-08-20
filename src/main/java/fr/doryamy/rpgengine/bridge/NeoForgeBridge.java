package fr.doryamy.rpgengine.bridge;

import fr.doryamy.rpgengine.dialogue.DialogueRunner;
import fr.doryamy.rpgengine.dialogue.editor.CreateDialogueScenarioRequest;
import fr.doryamy.rpgengine.dialogue.editor.SwitchDialogueEditorStateRequest;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueEditorScenarioSummaryView;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueEditorView;
import fr.doryamy.rpgengine.dialogue.presentation.DialogueView;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.util.List;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * Façade entre RPGEngine Plugin et le mod RPGEngine NeoForge.
 *
 * <p>Le plugin accède au mod uniquement par réflexion afin
 * de ne conserver aucune dépendance compile-time vers NeoForge.
 *
 * <p>Les responsabilités spécialisées sont déléguées
 * à des bridges dédiés :
 *
 * <pre>
 * DialogueRuntimeBridge
 *   showDialogue(...)
 *   dismissDialogue(...)
 *   CONTINUE
 *   CHOICE
 *   FINISH
 *
 * DialogueEditorBridge
 *   showDialogueManager(...)
 *   showDialogueEditor(...)
 *   ouverture d'un scénario
 *   suppression d'un scénario
 *
 * QuestBridge
 *   getQuestState(...)
 *   startQuest(...)
 * </pre>
 *
 * <p>Cette classe constitue uniquement la façade publique
 * utilisée par le reste du plugin.
 *
 * <p>La logique métier reste entièrement dans RPGEngine.
 */
public final class NeoForgeBridge {

    private static final String BRIDGE_CLASS =
            "fr.doryamy.rpgengine.neoforge.bridge.RpgEngineBridgeApi";

    /*
     * Runtime des dialogues.
     */
    private final DialogueRuntimeBridge dialogueRuntimeBridge =
            new DialogueRuntimeBridge();

    /*
     * Administration / éditeur de dialogues.
     */
    private final DialogueEditorBridge dialogueEditorBridge =
            new DialogueEditorBridge();

    /*
     * Intégration des quêtes.
     */
    private final QuestBridge questBridge =
            new QuestBridge();

    /**
     * Initialise la façade et tous
     * les bridges spécialisés.
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
             * ------------------------------------------------
             * Runtime dialogue
             * ------------------------------------------------
             */
            dialogueRuntimeBridge.initialize(
                    bridgeClass
            );

            /*
             * ------------------------------------------------
             * Administration / éditeur
             * ------------------------------------------------
             */
            dialogueEditorBridge.initialize(
                    bridgeClass
            );

            /*
             * ------------------------------------------------
             * Quêtes
             * ------------------------------------------------
             */
            questBridge.initialize(
                    bridgeClass
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

            shutdownAfterInitializationFailure();

            return false;

        } catch (RuntimeException e) {

            RpgLogger.error(
                    "Erreur inattendue pendant l'initialisation "
                            + "du bridge NeoForge : "
                            + e.getClass().getSimpleName()
                            + " | "
                            + e.getMessage()
            );

            shutdownAfterInitializationFailure();

            return false;
        }
    }

    /**
     * Branche le runtime des dialogues.
     *
     * @param dialogueRunner runtime serveur des dialogues
     */
    public void setDialogueRunner(
            DialogueRunner dialogueRunner
    ) {
        dialogueRuntimeBridge.setDialogueRunner(
                dialogueRunner
        );
    }

    /**
     * Définit le traitement des demandes
     * d'ouverture d'un scénario depuis
     * le gestionnaire client.
     *
     * @param handler handler plugin
     */
    public void setDialogueEditorRequestHandler(
            BiConsumer<UUID, String> handler
    ) {
        dialogueEditorBridge.setDialogueEditorRequestHandler(
                handler
        );
    }

    /**
     * Définit le traitement des demandes
     * de suppression d'un scénario.
     *
     * @param handler handler plugin
     */
    /**
     * Définit le traitement des demandes
     * de changement de variante d'état.
     */
    public void setDialogueEditorStateSwitchHandler(
            BiConsumer<
                    UUID,
                    SwitchDialogueEditorStateRequest
                    > handler
    ) {
        dialogueEditorBridge.setDialogueEditorStateSwitchHandler(
                handler
        );
    }

    public void setDialogueScenarioDeleteHandler(
            BiConsumer<UUID, String> handler
    ) {
        dialogueEditorBridge.setDialogueScenarioDeleteHandler(
                handler
        );
    }

    /**
     * Définit le traitement des demandes
     * de création d'un scénario.
     *
     * @param handler handler plugin
     */
    public void setDialogueScenarioCreateHandler(
            BiConsumer<
                    UUID,
                    CreateDialogueScenarioRequest
                    > handler
    ) {
        dialogueEditorBridge.setDialogueScenarioCreateHandler(
                handler
        );
    }

    /**
     * Envoie l'état visible courant d'un dialogue
     * au client NeoForge.
     *
     * @param playerUuid joueur destinataire
     * @param view état visible du dialogue
     *
     * @return {@code true} si l'envoi a réussi
     */
    public boolean showDialogue(
            UUID playerUuid,
            DialogueView view
    ) {
        return dialogueRuntimeBridge.showDialogue(
                playerUuid,
                view
        );
    }

    /**
     * Demande la fermeture de l'interface
     * de dialogue du joueur.
     *
     * @param playerUuid joueur destinataire
     *
     * @return {@code true} si l'envoi a réussi
     */
    public boolean dismissDialogue(
            UUID playerUuid
    ) {
        return dialogueRuntimeBridge.dismissDialogue(
                playerUuid
        );
    }

    /**
     * Envoie la liste des scénarios
     * au gestionnaire de dialogues.
     *
     * @param playerUuid joueur destinataire
     * @param scenarios scénarios disponibles
     *
     * @return {@code true} si l'envoi a réussi
     */
    public boolean showDialogueManager(
            UUID playerUuid,
            List<DialogueEditorScenarioSummaryView> scenarios
    ) {
        return dialogueEditorBridge.showDialogueManager(
                playerUuid,
                scenarios
        );
    }

    /**
     * Envoie une vue complète de l'éditeur
     * au client NeoForge.
     *
     * @param playerUuid joueur destinataire
     * @param view vue de l'éditeur
     *
     * @return {@code true} si l'envoi a réussi
     */
    public boolean showDialogueEditor(
            UUID playerUuid,
            DialogueEditorView view
    ) {
        return dialogueEditorBridge.showDialogueEditor(
                playerUuid,
                view
        );
    }

    /**
     * Lit l'état d'une quête via le mod NeoForge.
     *
     * @param playerUuid joueur concerné
     * @param questId identifiant externe de la quête
     *
     * @return état retourné par le mod
     */
    public String getQuestState(
            UUID playerUuid,
            String questId
    ) {
        return questBridge.getQuestState(
                playerUuid,
                questId
        );
    }

    /**
     * Demande au mod NeoForge de démarrer
     * une quête pour un joueur.
     *
     * @param playerUuid joueur concerné
     * @param questId identifiant externe de la quête
     *
     * @return {@code true} si l'opération a réussi
     */
    public boolean startQuest(
            UUID playerUuid,
            String questId
    ) {
        return questBridge.startQuest(
                playerUuid,
                questId
        );
    }

    /**
     * Retourne le nom affichable d'une quête.
     *
     * @param questId identifiant externe de la quête
     *
     * @return nom affichable de la quête
     */
    public String getQuestDisplayName(
            String questId
    ) {
        return questBridge.getQuestDisplayName(
                questId
        );
    }

    /**
     * Arrête proprement tous les bridges
     * spécialisés.
     */
    public void shutdown() {

        dialogueRuntimeBridge.shutdown();

        dialogueEditorBridge.shutdown();

        questBridge.shutdown();

        RpgLogger.info(
                "Bridge NeoForge arrêté."
        );
    }

    /**
     * Nettoie les bridges lorsqu'une erreur survient
     * pendant leur initialisation.
     *
     * <p>Chaque bridge sait gérer un shutdown partiel :
     * les références non initialisées sont ignorées.
     */
    private void shutdownAfterInitializationFailure() {

        dialogueRuntimeBridge.shutdown();

        dialogueEditorBridge.shutdown();

        questBridge.shutdown();
    }

}