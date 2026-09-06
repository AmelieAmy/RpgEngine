package fr.doryamy.rpgengine.dialogue;

import fr.doryamy.rpgengine.action.ActionManager;
import fr.doryamy.rpgengine.dialogue.runtime.view.DialogueChoiceView;
import fr.doryamy.rpgengine.dialogue.runtime.view.DialogueInteractionType;
import fr.doryamy.rpgengine.dialogue.runtime.view.DialoguePresenter;
import fr.doryamy.rpgengine.dialogue.runtime.view.DialogueView;
import fr.doryamy.rpgengine.trigger.TriggerContext;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.util.*;

/**
 * Orchestre l'exécution runtime des dialogues.
 *
 * <p>Le Runner :
 * valide le dialogue,
 * crée et fait progresser les sessions,
 * délègue les Conditions au Navigator,
 * délègue les Actions à ActionManager
 * et délègue l'affichage au DialoguePresenter.
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

        this.validator =
                Objects.requireNonNull(
                        validator,
                        "validator"
                );

        this.navigator =
                Objects.requireNonNull(
                        navigator,
                        "navigator"
                );

        this.sessionManager =
                Objects.requireNonNull(
                        sessionManager,
                        "sessionManager"
                );

        this.actionManager =
                Objects.requireNonNull(
                        actionManager,
                        "actionManager"
                );

        this.presenter =
                Objects.requireNonNull(
                        presenter,
                        "presenter"
                );
    }

    /**
     * Démarre un dialogue pour le joueur
     * contenu dans le TriggerContext.
     */
    public void start(
            Dialogue dialogue,
            TriggerContext context
    ) {

        Objects.requireNonNull(
                dialogue,
                "dialogue"
        );

        Objects.requireNonNull(
                context,
                "context"
        );

        DialogueValidationResult validation =
                validator.validate(
                        dialogue
                );

        if (!validation.isValid()) {
            throw new IllegalArgumentException(
                    "Impossible de démarrer le dialogue "
                            + dialogue.key()
                            + " : "
                            + String.join(
                            " | ",
                            validation.errors()
                    )
            );
        }

        DialogueSession session =
                sessionManager.create(
                        dialogue,
                        context
                );

        processUntilInteraction(
                session
        );
    }

    /**
     * Traite la réponse CONTINUE du joueur.
     */
    public void continueDialogue(
            UUID playerUuid
    ) {

        Objects.requireNonNull(
                playerUuid,
                "playerUuid"
        );

        DialogueSession session =
                sessionManager.require(
                        playerUuid
                );

        DialogueElement current =
                currentElement(
                        session
                );

        if (!(current instanceof DialogueReply reply)) {
            throw new IllegalStateException(
                    "CONTINUE reçu alors que l'élément courant "
                            + current.key()
                            + " n'est pas une réplique."
            );
        }

        session.acceptContinue();

        executeActions(
                session.context(),
                reply.rules()
        );

        session.moveTo(
                navigator.continuationOf(
                        session.dialogue()
                                .graph(),
                        reply.key()
                )
        );

        processUntilInteraction(
                session
        );
    }

    /**
     * Traite la sélection d'un choix.
     */
    public void choose(
            UUID playerUuid,
            DialogueElementKey choiceKey
    ) {

        Objects.requireNonNull(
                playerUuid,
                "playerUuid"
        );

        Objects.requireNonNull(
                choiceKey,
                "choiceKey"
        );

        DialogueSession session =
                sessionManager.require(
                        playerUuid
                );

        /*
         * acceptChoice valide notamment que cette clé
         * faisait partie des choix réellement présentés.
         */
        session.acceptChoice(
                choiceKey
        );

        DialogueElement selected =
                session.dialogue()
                        .graph()
                        .require(
                                choiceKey
                        );

        if (!(selected instanceof DialogueChoice choice)) {
            throw new IllegalStateException(
                    "Le choix présenté "
                            + choiceKey
                            + " n'est pas un DialogueChoice."
            );
        }

        executeActions(
                session.context(),
                choice.rules()
        );

        session.moveTo(
                navigator.continuationOf(
                        session.dialogue()
                                .graph(),
                        choice.key()
                )
        );

        processUntilInteraction(
                session
        );
    }

    /**
     * Ferme explicitement le dialogue actif
     * du joueur.
     */
    public void close(
            UUID playerUuid
    ) {

        Objects.requireNonNull(
                playerUuid,
                "playerUuid"
        );

        sessionManager.remove(
                        playerUuid
                )
                .ifPresent(session ->
                        presenter.close(
                                session.context()
                                        .getPlayer()
                        )
                );
    }

    /**
     * Fait progresser automatiquement la session
     * jusqu'à ce qu'une interaction joueur soit
     * nécessaire ou que le dialogue se termine.
     */
    private void processUntilInteraction(
            DialogueSession session
    ) {

        Set<DialogueElementKey> automaticallyVisited =
                new HashSet<>();

        while (true) {

            DialogueElement current =
                    currentElement(
                            session
                    );

            /*
             * Si le même élément est revisité sans
             * qu'aucune interaction n'ait été présentée,
             * le contexte courant produit une boucle
             * automatique impossible à résoudre.
             */
            if (!automaticallyVisited.add(
                    current.key()
            )) {

                failSession(
                        session,
                        "Boucle automatique détectée sur l'élément "
                                + current.key()
                                + "."
                );

                return;
            }

            if (current instanceof DialogueStart start) {

                session.moveTo(
                        navigator.continuationOf(
                                session.dialogue()
                                        .graph(),
                                start.key()
                        )
                );

                continue;
            }

            if (current instanceof DialogueReply reply) {

                if (!navigator.isAvailable(
                        session.context(),
                        reply
                )) {

                    session.moveTo(
                            navigator.continuationOf(
                                    session.dialogue()
                                            .graph(),
                                    reply.key()
                            )
                    );

                    continue;
                }

                presentReply(
                        session,
                        reply
                );

                return;
            }

            if (current instanceof DialogueBranch branch) {

                List<DialogueChoice> available =
                        navigator.availableChoices(
                                session.dialogue()
                                        .graph(),
                                branch,
                                session.context()
                        );

                if (available.isEmpty()) {

                    failSession(
                            session,
                            "L'embranchement "
                                    + branch.key()
                                    + " ne possède aucun choix "
                                    + "disponible dans le contexte courant."
                    );

                    return;
                }

                presentChoices(
                        session,
                        available
                );

                return;
            }

            if (current instanceof DialogueChoice choice) {

                /*
                 * Un Choice ne doit normalement jamais devenir
                 * l'élément courant autrement qu'après sa sélection.
                 *
                 * choose(...) exécute immédiatement ses Actions
                 * puis déplace la session vers sa continuation.
                 */
                failSession(
                        session,
                        "Le runtime a atteint directement le choix "
                                + choice.key()
                                + " sans sélection."
                );

                return;
            }

            if (current instanceof DialogueEnd) {

                finishSession(
                        session
                );

                return;
            }

            failSession(
                    session,
                    "Type d'élément de dialogue non pris en charge : "
                            + current.getClass()
                            .getName()
            );

            return;
        }
    }

    /**
     * Présente une réplique et suspend
     * le runtime jusqu'à CONTINUE.
     */
    private void presentReply(
            DialogueSession session,
            DialogueReply reply
    ) {

        session.waitForContinue();

        presenter.show(
                session.context()
                        .getPlayer(),
                new DialogueView(
                        speakerLabel(
                                reply.speaker()
                        ),
                        reply.text(),
                        DialogueInteractionType.CONTINUE,
                        List.of()
                )
        );
    }

    /**
     * Présente exactement les choix disponibles
     * et suspend le runtime jusqu'à sélection.
     */
    private void presentChoices(
            DialogueSession session,
            List<DialogueChoice> choices
    ) {

        List<DialogueElementKey> choiceKeys =
                choices.stream()
                        .map(
                                DialogueChoice::key
                        )
                        .toList();

        session.waitForChoice(
                choiceKeys
        );

        List<DialogueChoiceView> views =
                new ArrayList<>();

        /*
         * La position de la vue correspond à l'ordre
         * réellement visible dans cette interaction.
         *
         * Exemple :
         * positions métier disponibles 0, 3, 7
         * deviennent positions d'affichage 0, 1, 2.
         *
         * L'identité reste portée par key.
         */
        for (int index = 0;
             index < choices.size();
             index++) {

            DialogueChoice choice =
                    choices.get(
                            index
                    );

            views.add(
                    new DialogueChoiceView(
                            choice.key()
                                    .value(),
                            index,
                            choice.text()
                    )
            );
        }

        presenter.show(
                session.context()
                        .getPlayer(),
                new DialogueView(
                        null,
                        null,
                        DialogueInteractionType.CHOICE,
                        views
                )
        );
    }

    /**
     * Exécute séquentiellement les Actions
     * appartenant à un élément.
     */
    private void executeActions(
            TriggerContext context,
            DialogueRules rules
    ) {

        for (DialogueActionEntry entry :
                rules.actions()) {

            actionManager.execute(
                    context,
                    entry.action()
            );
        }
    }

    /**
     * Termine normalement une session.
     */
    private void finishSession(
            DialogueSession session
    ) {

        UUID playerUuid =
                session.playerUuid();

        sessionManager.remove(
                playerUuid
        );

        presenter.close(
                session.context()
                        .getPlayer()
        );
    }

    /**
     * Termine une session après une incohérence
     * runtime et journalise précisément la cause.
     */
    private void failSession(
            DialogueSession session,
            String reason
    ) {

        RpgLogger.error(
                "Dialogue "
                        + session.dialogue()
                        .key()
                        + " interrompu pour le joueur "
                        + session.playerUuid()
                        + " : "
                        + reason
        );

        finishSession(
                session
        );
    }

    /**
     * Retourne l'élément courant.
     */
    private DialogueElement currentElement(
            DialogueSession session
    ) {

        return session.dialogue()
                .graph()
                .require(
                        session.currentElementKey()
                );
    }

    /**
     * Convertit le type métier du locuteur
     * en libellé de présentation.
     *
     * <p>Aucune logique métier ne doit jamais
     * être déduite de ce libellé.
     */
    private String speakerLabel(
            DialogueReplySpeaker speaker
    ) {

        return switch (speaker) {
            case NPC -> "PNJ";
            case PLAYER -> "Joueur";
        };
    }
}