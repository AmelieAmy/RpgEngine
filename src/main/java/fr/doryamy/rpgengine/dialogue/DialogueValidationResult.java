package fr.doryamy.rpgengine.dialogue;

import java.util.List;

/**
 * Résultat de validation d'un dialogue.
 *
 * <p>La validité est entièrement déterminée
 * par l'absence d'erreurs.
 */
public record DialogueValidationResult(
        List<String> errors
) {

    public DialogueValidationResult {

        errors =
                errors == null
                        ? List.of()
                        : List.copyOf(errors);
    }

    /**
     * Retourne un résultat valide.
     */
    public static DialogueValidationResult valid() {

        return new DialogueValidationResult(
                List.of()
        );
    }

    /**
     * Retourne un résultat invalide.
     */
    public static DialogueValidationResult invalid(
            List<String> errors
    ) {

        if (errors == null || errors.isEmpty()) {
            throw new IllegalArgumentException(
                    "Un résultat invalide doit contenir "
                            + "au moins une erreur."
            );
        }

        return new DialogueValidationResult(
                errors
        );
    }

    /**
     * Indique si le dialogue est valide.
     */
    public boolean isValid() {

        return errors.isEmpty();
    }
}