package fr.doryamy.rpgengine.dialogue;

import fr.doryamy.rpgengine.action.ActionManager;
import fr.doryamy.rpgengine.dialogue.presentation.DialogueChoiceView;
import fr.doryamy.rpgengine.dialogue.presentation.DialogueInteractionType;
import fr.doryamy.rpgengine.dialogue.presentation.DialoguePresenter;
import fr.doryamy.rpgengine.dialogue.presentation.DialogueView;
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
 * Le runner constitue l'autorité serveur sur la progression
 * d'un dialogue. Les interactions reçues du joueur ne font
 * qu'appeler {@link #advance(UUID)}, {@link #choose(UUID, int)}
 * ou {@link #finish(UUID)}.
 *
 * Elle délègue :
 *   la validation à {@link DialogueValidator}
 *   la navigation à {@link DialogueNavigator}
 *   les sessions à {@link DialogueSessionManager}
 *   les actions à {@link ActionManager}
 *   la présentation à {@link DialoguePresenter}
 *
 *  Le runner ne dépend d'aucune technologie cliente.
 */
public final class DialogueRunner {

    private final DialogueValidator validator;
    private final DialogueNavigator navigator;
    private final DialogueSessionManager sessionManager;
    private final ActionManager actionManager;
    private final DialoguePresenter presenter;

    public DialogueRunner(
            DialogueValidator validator,
            DialogueNavigator navigator,
            DialogueSessionManager sessionManager,
            ActionManager actionManager,
            DialoguePresenter presenter
    ) {
        this.validator = validator;
        this.navigator = navigator;
        this.sessionManager = sessionManager;
        this.actionManager = actionManager;
        this.presenter = presenter;
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
     * Prévisualise le node de départ
     * sans créer de session runtime.
     *
     * La preview reste un outil d'administration
     * et n'utilise pas le presenter runtime.
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

    /**
     * Analyse le node courant et construit
     * son état de présentation.
     */
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

        if (node.isStructural()) {
            processStructuralNode(
                    dialogue,
                    session,
                    node,
                    transitions
            );
            return;
        }

        processTransitions(
                dialogue,
                session,
                node,
                transitions
        );
    }

    /**
     * Traite un node structurel sans générer de réplique PNJ artificielle.
     */
    private void processStructuralNode(
            Dialogue dialogue,
            DialogueSession session,
            DialogueNode node,
            List<DialogueTransition> transitions
    ) {
        List<DialogueTransition> autoTransitions =
                transitions.stream()
                        .filter(transition ->
                                transition.getType() == DialogueTransitionType.AUTO)
                        .toList();

        List<DialogueTransition> choiceTransitions =
                transitions.stream()
                        .filter(transition ->
                                transition.getType() == DialogueTransitionType.CHOICE)
                        .toList();

        if (autoTransitions.size() == 1
                && choiceTransitions.isEmpty()) {
            DialogueTransition transition = autoTransitions.getFirst();

            Optional<DialoguePlayerReply> firstReply =
                    navigator.findNextAvailablePlayerReply(
                            transition,
                            session,
                            0
                    );

            if (firstReply.isPresent()) {
                displayAutoPlayerReply(
                        session,
                        transition,
                        firstReply.get()
                );
            } else {
                followTransition(
                        dialogue,
                        session,
                        transition
                );
            }
            return;
        }

        if (autoTransitions.isEmpty()
                && !choiceTransitions.isEmpty()) {
            displayChoices(
                    session,
                    node,
                    choiceTransitions
            );
            return;
        }

        RpgLogger.error(
                "Node structurel ambigu dans le dialogue '"
                        + dialogue.getKey()
                        + "' : "
                        + node.getKey()
        );
        endSession(session);
    }

    /**
     * Détermine le mode d'interaction
     * correspondant aux transitions disponibles.
     */
    private void processTransitions(
            Dialogue dialogue,
            DialogueSession session,
            DialogueNode node,
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

        /*
         * AUTO
         *
         * AUTO signifie qu'il n'existe aucune
         * décision narrative.
         *
         * La progression visuelle attend toutefois
         * une action CONTINUE du joueur.
         */
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

            displayContinue(
                    session,
                    node
            );

            return;
        }

        /*
         * CHOICE
         */
        if (!choiceTransitions.isEmpty()) {

            displayChoices(
                    session,
                    node,
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

    /**
     * Présente un node attendant une progression simple.
     */
    private void displayContinue(
            DialogueSession session,
            DialogueNode node
    ) {
        DialogueView view =
                new DialogueView(
                        null,
                        node.getText(),
                        DialogueInteractionType.CONTINUE,
                        List.of()
                );

        presenter.show(
                session.getContext()
                        .getPlayer(),
                view
        );

        RpgLogger.debug(
                "Dialogue en attente de CONTINUE : "
                        + session.getDialogueKey()
                        + " | node="
                        + node.getKey()
        );
    }

    /**
     * Présente une réplique Joueur simple portée
     * par une transition AUTO.
     */
    private void displayAutoPlayerReply(
            DialogueSession session,
            DialogueTransition transition,
            DialoguePlayerReply reply
    ) {
        DialogueView view =
                new DialogueView(
                        "Joueur",
                        reply.getText(),
                        DialogueInteractionType.CONTINUE,
                        List.of()
                );

        presenter.show(
                session.getContext()
                        .getPlayer(),
                view
        );

        session.beginAutoPlayerReply(
                transition.getKey(),
                reply.getPosition()
        );

        RpgLogger.debug(
                "Réplique Joueur AUTO affichée : "
                        + session.getDialogueKey()
                        + " | transition="
                        + transition.getKey()
                        + " | position="
                        + reply.getPosition()
        );
    }

    /**
     * Présente les choix actuellement disponibles.
     */
    private void displayChoices(
            DialogueSession session,
            DialogueNode node,
            List<DialogueTransition> choices
    ) {
        List<DialogueChoiceView> choiceViews =
                choices.stream()
                        .map(choice ->
                                new DialogueChoiceView(
                                        choice.getPosition(),
                                        choice.getLabel()
                                )
                        )
                        .toList();

        DialogueView view =
                new DialogueView(
                        null,
                        node.isStructural()
                                ? ""
                                : node.getText(),
                        DialogueInteractionType.CHOICE,
                        choiceViews
                );

        presenter.show(
                session.getContext()
                        .getPlayer(),
                view
        );

        RpgLogger.debug(
                choices.size()
                        + " choix disponible(s) pour le dialogue '"
                        + session.getDialogueKey()
                        + "'."
        );
    }

    /**
     * Compatibilité temporaire avec l'ancien flux END.
     *
     * <p>Les transitions persistées depuis V12 utilisent
     * désormais AUTO / CHOICE + terminal et ne passent
     * normalement plus par cette présentation.
     */
    private void displayClose(
            DialogueSession session,
            DialogueNode node
    ) {
        DialogueView view =
                new DialogueView(
                        null,
                        node.getText(),
                        DialogueInteractionType.CLOSE,
                        List.of()
                );

        presenter.show(
                session.getContext()
                        .getPlayer(),
                view
        );

        RpgLogger.debug(
                "Dialogue en attente de FINISH : "
                        + session.getDialogueKey()
                        + " | node="
                        + node.getKey()
        );
    }

    /**
     * Demande au moteur de suivre
     * la transition AUTO disponible.
     *
     * @param playerUuid UUID du joueur
     *
     * @return true si la progression a été effectuée
     */
    public boolean advance(
            UUID playerUuid
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

        /*
         * Une réplique Joueur AUTO est actuellement affichée.
         *
         * On exécute d'abord les actions propres à cette réplique,
         * puis on recherche la prochaine réplique disponible.
         * Les conditions de la transition ne sont pas réévaluées entre
         * les répliques : la transition exacte reste mémorisée.
         */
        if (session.isAutoPlayerReplyPhase()) {

            String transitionKey =
                    session.getPendingTransitionKey();

            int replyPosition =
                    session.getPendingPlayerReplyPosition();

            Optional<DialogueTransition> pendingTransition =
                    dialogue.findTransition(
                            transitionKey
                    );

            if (pendingTransition.isEmpty()) {
                RpgLogger.error(
                        "Transition AUTO en attente introuvable : "
                                + transitionKey
                                + " | dialogue="
                                + dialogue.getKey()
                );

                endSession(session);
                return false;
            }

            DialogueTransition transition =
                    pendingTransition.get();

            if (transition.getType()
                    != DialogueTransitionType.AUTO) {
                RpgLogger.error(
                        "La transition en attente n'est plus AUTO : "
                                + transition.getKey()
                );

                endSession(session);
                return false;
            }

            Optional<DialoguePlayerReply> currentReply =
                    transition.getPlayerReplies()
                            .stream()
                            .filter(reply ->
                                    reply.getPosition()
                                            == replyPosition
                            )
                            .findFirst();

            if (currentReply.isEmpty()) {
                RpgLogger.error(
                        "Réplique Joueur en attente introuvable : transition="
                                + transition.getKey()
                                + " | position="
                                + replyPosition
                );

                endSession(session);
                return false;
            }

            executePlayerReplyActions(
                    session,
                    currentReply.get()
            );

            Optional<DialoguePlayerReply> nextReply =
                    navigator.findNextAvailablePlayerReply(
                            transition,
                            session,
                            replyPosition
                    );

            if (nextReply.isPresent()) {
                displayAutoPlayerReply(
                        session,
                        transition,
                        nextReply.get()
                );

                return true;
            }

            session.clearPresentationPhase();

            followTransition(
                    dialogue,
                    session,
                    transition
            );

            return true;
        }

        List<DialogueTransition> availableTransitions =
                navigator.getAvailableTransitions(
                        dialogue,
                        session
                );

        if (availableTransitions.size() != 1) {
            return false;
        }

        DialogueTransition transition =
                availableTransitions.get(0);

        if (transition.getType()
                != DialogueTransitionType.AUTO) {
            return false;
        }

        Optional<DialoguePlayerReply> firstReply =
                navigator.findNextAvailablePlayerReply(
                        transition,
                        session,
                        0
                );

        if (firstReply.isPresent()) {
            displayAutoPlayerReply(
                    session,
                    transition,
                    firstReply.get()
            );

            return true;
        }

        RpgLogger.debug(
                "Progression AUTO du dialogue : "
                        + dialogue.getKey()
                        + " | transition="
                        + transition.getKey()
                        + " | joueur="
                        + session.getContext()
                        .getPlayer()
                        .getName()
        );

        followTransition(
                dialogue,
                session,
                transition
        );

        return true;
    }

    /**
     * Sélectionne un choix dans la session active
     * d'un joueur.
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

    /**
     * Compatibilité temporaire avec l'ancien protocole END.
     *
     * <p>Les nouvelles transitions terminales AUTO / CHOICE
     * sont terminées directement dans followTransition().
     *
     * @param playerUuid UUID du joueur
     *
     * @return true si le dialogue a été terminé
     */
    public boolean finish(
            UUID playerUuid
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

        if (availableTransitions.size() != 1) {
            return false;
        }

        DialogueTransition transition =
                availableTransitions.get(0);

        if (transition.getType()
                != DialogueTransitionType.END) {

            return false;
        }

        RpgLogger.debug(
                "Fin de dialogue confirmée : "
                        + dialogue.getKey()
                        + " | joueur="
                        + session.getContext()
                        .getPlayer()
                        .getName()
        );

        executeTransitionActions(
                session,
                transition
        );

        endSession(
                session
        );

        return true;
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

        /*
         * Une transition terminale est une destination
         * du dialogue, indépendamment de son type AUTO
         * ou CHOICE.
         *
         * Les actions de transition sont exécutées avant
         * la fermeture de la session.
         */
        if (transition.isTerminal()) {

            RpgLogger.debug(
                    "Transition terminale atteinte : "
                            + dialogue.getKey()
                            + " | transition="
                            + transition.getKey()
            );

            endSession(
                    session
            );

            return;
        }

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

    private void executePlayerReplyActions(
            DialogueSession session,
            DialoguePlayerReply reply
    ) {
        for (Action action :
                reply.getActions()) {

            actionManager.execute(
                    session.getContext(),
                    action
            );
        }
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

    private void endSession(
            DialogueSession session
    ) {
        presenter.close(
                session.getContext()
                        .getPlayer()
        );

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