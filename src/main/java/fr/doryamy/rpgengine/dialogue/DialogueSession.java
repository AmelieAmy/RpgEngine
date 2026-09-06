package fr.doryamy.rpgengine.dialogue;

import fr.doryamy.rpgengine.trigger.TriggerContext;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * État runtime d'un dialogue actuellement
 * exécuté pour un joueur.
 *
 * <p>Une session appartient à un seul joueur
 * et à un seul dialogue.
 *
 * <p>Elle mémorise explicitement l'interaction
 * actuellement attendue afin que les réponses
 * du client soient validées contre ce qui a
 * réellement été présenté.
 */
public final class DialogueSession {

    private final UUID playerUuid;
    private final Dialogue dialogue;
    private final TriggerContext context;

    private DialogueElementKey currentElementKey;
    private DialogueSessionPhase phase;

    private List<DialogueElementKey> presentedChoiceKeys;

    public DialogueSession(
            UUID playerUuid,
            Dialogue dialogue,
            TriggerContext context
    ) {

        this.playerUuid =
                Objects.requireNonNull(
                        playerUuid,
                        "playerUuid"
                );

        this.dialogue =
                Objects.requireNonNull(
                        dialogue,
                        "dialogue"
                );

        this.context =
                Objects.requireNonNull(
                        context,
                        "context"
                );

        if (!context.getPlayer()
                .getUniqueId()
                .equals(playerUuid)) {

            throw new IllegalArgumentException(
                    "Le joueur du TriggerContext "
                            + "ne correspond pas à la session."
            );
        }

        this.currentElementKey =
                dialogue.graph()
                        .start()
                        .key();

        this.phase =
                DialogueSessionPhase.PROCESSING;

        this.presentedChoiceKeys =
                List.of();
    }

    public UUID playerUuid() {
        return playerUuid;
    }

    public Dialogue dialogue() {
        return dialogue;
    }

    public TriggerContext context() {
        return context;
    }

    public DialogueElementKey currentElementKey() {
        return currentElementKey;
    }

    public DialogueSessionPhase phase() {
        return phase;
    }

    public List<DialogueElementKey> presentedChoiceKeys() {
        return presentedChoiceKeys;
    }

    /**
     * Place le runtime sur un nouvel élément.
     *
     * <p>L'ancienne interaction devient immédiatement
     * invalide.
     */
    public void moveTo(
            DialogueElementKey elementKey
    ) {

        Objects.requireNonNull(
                elementKey,
                "elementKey"
        );

        dialogue.graph()
                .require(
                        elementKey
                );

        currentElementKey =
                elementKey;

        phase =
                DialogueSessionPhase.PROCESSING;

        presentedChoiceKeys =
                List.of();
    }

    /**
     * Indique qu'une réplique vient d'être présentée
     * et que le runtime attend CONTINUE.
     */
    public void waitForContinue() {

        requireProcessing();

        DialogueElement current =
                dialogue.graph()
                        .require(
                                currentElementKey
                        );

        if (!(current instanceof DialogueReply)) {
            throw new IllegalStateException(
                    "CONTINUE ne peut être attendu que "
                            + "sur une DialogueReply. Élément courant : "
                            + currentElementKey
            );
        }

        phase =
                DialogueSessionPhase.WAITING_CONTINUE;

        presentedChoiceKeys =
                List.of();
    }

    /**
     * Indique qu'un ensemble précis de choix vient
     * d'être présenté au joueur.
     */
    public void waitForChoice(
            List<DialogueElementKey> choiceKeys
    ) {

        requireProcessing();

        Objects.requireNonNull(
                choiceKeys,
                "choiceKeys"
        );

        DialogueElement current =
                dialogue.graph()
                        .require(
                                currentElementKey
                        );

        if (!(current instanceof DialogueBranch)) {
            throw new IllegalStateException(
                    "CHOICE ne peut être attendu que "
                            + "sur un DialogueBranch. Élément courant : "
                            + currentElementKey
            );
        }

        if (choiceKeys.isEmpty()) {
            throw new IllegalArgumentException(
                    "Une interaction CHOICE doit présenter "
                            + "au moins un choix."
            );
        }

        List<DialogueElementKey> copy =
                List.copyOf(
                        choiceKeys
                );

        List<DialogueElementKey> branchChoiceKeys =
                dialogue.graph()
                        .choicesOf(
                                currentElementKey
                        )
                        .stream()
                        .map(
                                DialogueChoice::key
                        )
                        .toList();

        for (DialogueElementKey key : copy) {

            Objects.requireNonNull(
                    key,
                    "Une clé de choix ne peut pas être null."
            );

            if (!branchChoiceKeys.contains(key)) {
                throw new IllegalArgumentException(
                        "Le choix "
                                + key
                                + " n'appartient pas à l'embranchement courant "
                                + currentElementKey
                                + "."
                );
            }
        }

        long distinctCount =
                copy.stream()
                        .distinct()
                        .count();

        if (distinctCount != copy.size()) {
            throw new IllegalArgumentException(
                    "Un même choix ne peut pas être "
                            + "présenté plusieurs fois."
            );
        }

        phase =
                DialogueSessionPhase.WAITING_CHOICE;

        presentedChoiceKeys =
                copy;
    }

    /**
     * Vérifie que le runtime attend bien CONTINUE,
     * puis repasse en traitement.
     */
    public void acceptContinue() {

        if (phase
                != DialogueSessionPhase.WAITING_CONTINUE) {

            throw new IllegalStateException(
                    "La session n'attend pas CONTINUE."
            );
        }

        phase =
                DialogueSessionPhase.PROCESSING;
    }

    /**
     * Vérifie que le choix faisait partie de ceux
     * réellement présentés, puis repasse en traitement.
     */
    public void acceptChoice(
            DialogueElementKey choiceKey
    ) {

        Objects.requireNonNull(
                choiceKey,
                "choiceKey"
        );

        if (phase
                != DialogueSessionPhase.WAITING_CHOICE) {

            throw new IllegalStateException(
                    "La session n'attend pas de choix."
            );
        }

        if (!presentedChoiceKeys.contains(
                choiceKey
        )) {

            throw new IllegalArgumentException(
                    "Le choix "
                            + choiceKey
                            + " n'a pas été présenté "
                            + "dans l'interaction courante."
            );
        }

        phase =
                DialogueSessionPhase.PROCESSING;

        presentedChoiceKeys =
                List.of();
    }

    private void requireProcessing() {

        if (phase
                != DialogueSessionPhase.PROCESSING) {

            throw new IllegalStateException(
                    "Une nouvelle interaction ne peut être "
                            + "présentée que pendant PROCESSING."
            );
        }
    }
}