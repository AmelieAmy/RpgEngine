package fr.doryamy.rpgengine.dialogue;

import fr.doryamy.rpgengine.action.ActionManager;
import fr.doryamy.rpgengine.model.Action;
import fr.doryamy.rpgengine.trigger.TriggerContext;
import fr.doryamy.rpgengine.util.RpgLogger;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Orchestre l'exécution runtime des dialogues de RPGEngine.
 *
 * Cette classe est responsable :
 *   du démarrage d'un dialogue ;
 *   de l'affichage du node courant ;
 *   du traitement des transitions ;
 *   de l'exécution des actions ;
 *   de la progression du dialogue ;
 *   de sa terminaison.
 *
 * Elle délègue :
 *   la validation à DialogueValidator ;
 *   la navigation à DialogueNavigator ;
 *   les sessions à DialogueSessionManager ;
 *   l'exécution des actions à ActionManager.
 */
public final class DialogueRunner {

    private final DialogueValidator validator;
    private final DialogueNavigator navigator;
    private final DialogueSessionManager sessionManager;
    private final ActionManager actionManager;

    public DialogueRunner(
            DialogueValidator validator,
            DialogueNavigator navigator,
            DialogueSessionManager sessionManager,
            ActionManager actionManager
    ) {
        this.validator = validator;
        this.navigator = navigator;
        this.sessionManager = sessionManager;
        this.actionManager = actionManager;
    }

    /**
     * Démarre un dialogue.
     *
     * @param context contexte d'exécution
     * @param dialogue dialogue à démarrer
     */
    public void start(
            TriggerContext context,
            Dialogue dialogue
    ) {
        DialogueValidationResult validation =
                validator.validate(
                        dialogue
                );

        if (!validation.isValid()) {

            RpgLogger.error(
                    "Impossible de démarrer le dialogue '"
                            + dialogue.getKey()
                            + "' : structure invalide."
            );

            validation.getErrors()
                    .forEach(error ->
                            RpgLogger.error(
                                    "Dialogue '"
                                            + dialogue.getKey()
                                            + "' : "
                                            + error
                            )
                    );

            return;
        }

        DialogueSession session =
                sessionManager.create(
                        dialogue,
                        context
                );

        RpgLogger.debug(
                "Dialogue démarré : "
                        + dialogue.getKey()
                        + " | joueur="
                        + context.getPlayer()
                        .getName()
        );

        processCurrentNode(
                dialogue,
                session
        );
    }

    /**
     * Prévisualise le node de départ sans créer de session runtime.
     */
    public void preview(
            Player player,
            Dialogue dialogue
    ) {
        DialogueValidationResult validation =
                validator.validate(
                        dialogue
                );

        if (!validation.isValid()) {

            player.sendMessage(
                    "Le dialogue est invalide."
            );

            validation.getErrors()
                    .forEach(error ->
                            player.sendMessage(
                                    "- " + error
                            )
                    );

            return;
        }

        Optional<DialogueNode> startNode =
                dialogue.getStartNode();

        if (startNode.isEmpty()) {

            player.sendMessage(
                    "Node de départ introuvable."
            );

            return;
        }

        player.sendMessage(
                startNode.get()
                        .getText()
        );

        RpgLogger.debug(
                "Prévisualisation du dialogue '"
                        + dialogue.getKey()
                        + "' pour "
                        + player.getName()
        );
    }

    private void processCurrentNode(
            Dialogue dialogue,
            DialogueSession session
    ) {
        Optional<DialogueNode> currentNode =
                dialogue.findNode(
                        session.getCurrentNodeKey()
                );

        if (currentNode.isEmpty()) {

            RpgLogger.error(
                    "Node courant introuvable : "
                            + session.getCurrentNodeKey()
                            + " | dialogue="
                            + dialogue.getKey()
            );

            endSession(
                    session
            );

            return;
        }

        DialogueNode node =
                currentNode.get();

        session.getContext()
                .getPlayer()
                .sendMessage(
                        node.getText()
                );

        List<DialogueTransition> transitions =
                navigator.getAvailableTransitions(
                        dialogue,
                        session
                );

        if (transitions.isEmpty()) {

            RpgLogger.error(
                    "Aucune transition disponible depuis le node '"
                            + node.getKey()
                            + "' du dialogue '"
                            + dialogue.getKey()
                            + "'."
            );

            endSession(
                    session
            );

            return;
        }

        processTransitions(
                dialogue,
                session,
                transitions
        );
    }

    private void processTransitions(
            Dialogue dialogue,
            DialogueSession session,
            List<DialogueTransition> transitions
    ) {
        List<DialogueTransition> autoTransitions =
                transitions.stream()
                        .filter(transition ->
                                transition.getType()
                                        == DialogueTransitionType.AUTO
                        )
                        .toList();

        List<DialogueTransition> choiceTransitions =
                transitions.stream()
                        .filter(transition ->
                                transition.getType()
                                        == DialogueTransitionType.CHOICE
                        )
                        .toList();

        List<DialogueTransition> endTransitions =
                transitions.stream()
                        .filter(transition ->
                                transition.getType()
                                        == DialogueTransitionType.END
                        )
                        .toList();

        if (!endTransitions.isEmpty()) {

            if (transitions.size() > 1) {

                RpgLogger.error(
                        "Une transition END est disponible en même temps "
                                + "qu'une autre transition dans le dialogue '"
                                + dialogue.getKey()
                                + "'."
                );

                endSession(
                        session
                );

                return;
            }

            executeTransitionActions(
                    session,
                    endTransitions.get(0)
            );

            endSession(
                    session
            );

            return;
        }

        if (!autoTransitions.isEmpty()) {

            if (autoTransitions.size() > 1
                    || !choiceTransitions.isEmpty()) {

                RpgLogger.error(
                        "Transitions ambiguës depuis le node '"
                                + session.getCurrentNodeKey()
                                + "' du dialogue '"
                                + dialogue.getKey()
                                + "'."
                );

                endSession(
                        session
                );

                return;
            }

            followTransition(
                    dialogue,
                    session,
                    autoTransitions.get(0)
            );

            return;
        }

        if (!choiceTransitions.isEmpty()) {

            displayChoices(
                    session,
                    choiceTransitions
            );

            return;
        }

        RpgLogger.error(
                "Aucune transition exploitable depuis le node '"
                        + session.getCurrentNodeKey()
                        + "'."
        );

        endSession(
                session
        );
    }

    private void displayChoices(
            DialogueSession session,
            List<DialogueTransition> choices
    ) {
        Player player =
                session.getContext()
                        .getPlayer();

        player.sendMessage(
                "Choix disponibles :"
        );

        for (DialogueTransition choice : choices) {

            player.sendMessage(
                    choice.getPosition()
                            + " - "
                            + choice.getLabel()
            );
        }

        RpgLogger.debug(
                choices.size()
                        + " choix disponible(s) pour le dialogue '"
                        + session.getDialogueKey()
                        + "'."
        );
    }

    private void followTransition(
            Dialogue dialogue,
            DialogueSession session,
            DialogueTransition transition
    ) {
        executeTransitionActions(
                session,
                transition
        );

        Optional<DialogueNode> nextNode =
                navigator.getNextNode(
                        dialogue,
                        transition
                );

        if (nextNode.isEmpty()) {

            RpgLogger.error(
                    "Node cible introuvable pour une transition depuis '"
                            + transition.getSourceNodeKey()
                            + "'."
            );

            endSession(
                    session
            );

            return;
        }

        session.setCurrentNodeKey(
                nextNode.get()
                        .getKey()
        );

        processCurrentNode(
                dialogue,
                session
        );
    }

    private void executeTransitionActions(
            DialogueSession session,
            DialogueTransition transition
    ) {
        for (Action action :
                transition.getActions()) {

            actionManager.execute(
                    session.getContext(),
                    action
            );
        }
    }

    /**
     * Sélectionne un choix dans la session active
     * d'un joueur.
     *
     * La position correspond à la position
     * de la transition CHOICE affichée.
     *
     * @param playerUuid UUID du joueur
     * @param position position du choix
     *
     * @return true si le choix a été traité
     */
    public boolean choose(
            UUID playerUuid,
            int position
    ) {
        Optional<DialogueSession> sessionResult =
                sessionManager.find(
                        playerUuid
                );

        if (sessionResult.isEmpty()) {
            return false;
        }

        DialogueSession session =
                sessionResult.get();

        Dialogue dialogue =
                session.getDialogue();

        List<DialogueTransition> availableTransitions =
                navigator.getAvailableTransitions(
                        dialogue,
                        session
                );

        Optional<DialogueTransition> choiceResult =
                availableTransitions.stream()
                        .filter(transition ->
                                transition.getType()
                                        == DialogueTransitionType.CHOICE
                        )
                        .filter(transition ->
                                transition.getPosition()
                                        == position
                        )
                        .findFirst();

        if (choiceResult.isEmpty()) {
            return false;
        }

        DialogueTransition choice =
                choiceResult.get();

        RpgLogger.debug(
                "Choix de dialogue sélectionné : "
                        + dialogue.getKey()
                        + " | transition="
                        + choice.getKey()
                        + " | joueur="
                        + session.getContext()
                        .getPlayer()
                        .getName()
        );

        followTransition(
                dialogue,
                session,
                choice
        );

        return true;
    }

    private void endSession(
            DialogueSession session
    ) {
        sessionManager.remove(
                session.getPlayerUuid()
        );

        RpgLogger.debug(
                "Dialogue terminé : "
                        + session.getDialogueKey()
        );
    }

    /**
     * Vérifie si un joueur possède une session active.
     */
    public boolean hasSession(
            UUID playerUuid
    ) {
        return sessionManager.hasSession(
                playerUuid
        );
    }
}