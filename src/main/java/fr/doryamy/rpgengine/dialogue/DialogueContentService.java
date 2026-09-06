package fr.doryamy.rpgengine.dialogue;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Modifie le contenu éditable des éléments
 * d'un dialogue sans modifier sa topologie.
 */
public final class DialogueContentService {

    private final DialogueValidator validator;

    public DialogueContentService(
            DialogueValidator validator
    ) {

        this.validator =
                Objects.requireNonNull(
                        validator,
                        "validator"
                );
    }

    /**
     * Modifie le texte d'une réplique.
     */
    public DialogueGraph updateReplyText(
            DialogueGraph graph,
            DialogueElementKey replyKey,
            String text
    ) {

        Objects.requireNonNull(
                graph,
                "graph"
        );

        Objects.requireNonNull(
                replyKey,
                "replyKey"
        );

        requireValid(
                graph
        );

        DialogueElement element =
                graph.require(
                        replyKey
                );

        if (!(element instanceof DialogueReply reply)) {
            throw new IllegalArgumentException(
                    "L'élément "
                            + replyKey
                            + " n'est pas une réplique."
            );
        }

        DialogueReply updated =
                new DialogueReply(
                        reply.key(),
                        reply.speaker(),
                        text,
                        reply.rules()
                );

        return replaceElement(
                graph,
                updated
        );
    }

    /**
     * Modifie le texte d'un choix.
     */
    public DialogueGraph updateChoiceText(
            DialogueGraph graph,
            DialogueElementKey choiceKey,
            String text
    ) {

        Objects.requireNonNull(
                graph,
                "graph"
        );

        Objects.requireNonNull(
                choiceKey,
                "choiceKey"
        );

        requireValid(
                graph
        );

        DialogueElement element =
                graph.require(
                        choiceKey
                );

        if (!(element instanceof DialogueChoice choice)) {
            throw new IllegalArgumentException(
                    "L'élément "
                            + choiceKey
                            + " n'est pas un choix."
            );
        }

        DialogueChoice updated =
                new DialogueChoice(
                        choice.key(),
                        text,
                        choice.position(),
                        choice.rules()
                );

        return replaceElement(
                graph,
                updated
        );
    }

    private DialogueGraph replaceElement(
            DialogueGraph graph,
            DialogueElement replacement
    ) {

        List<DialogueElement> elements =
                new ArrayList<>(
                        graph.elements()
                                .values()
                );

        boolean replaced = false;

        for (int index = 0;
             index < elements.size();
             index++) {

            if (!elements.get(index)
                    .key()
                    .equals(
                            replacement.key()
                    )) {
                continue;
            }

            elements.set(
                    index,
                    replacement
            );

            replaced = true;
            break;
        }

        if (!replaced) {
            throw new IllegalArgumentException(
                    "Élément introuvable : "
                            + replacement.key()
            );
        }

        DialogueGraph result =
                new DialogueGraph(
                        elements,
                        graph.links()
                );

        requireValid(
                result
        );

        return result;
    }

    private void requireValid(
            DialogueGraph graph
    ) {

        DialogueValidationResult result =
                validator.validate(
                        graph
                );

        if (result.isValid()) {
            return;
        }

        throw new IllegalStateException(
                "Le graphe de dialogue est invalide : "
                        + String.join(
                        " | ",
                        result.errors()
                )
        );
    }
}