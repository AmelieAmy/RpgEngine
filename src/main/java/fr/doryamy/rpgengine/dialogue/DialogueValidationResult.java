package fr.doryamy.rpgengine.dialogue;

import java.util.List;

/**
 * Représente le résultat de la validation
 * structurelle d'un dialogue.
 *
 * Un résultat contient :
 * - un indicateur de validité ;
 * - la liste des erreurs détectées.
 *
 * Cette classe est immuable.
 */
public final class DialogueValidationResult {

    private final boolean valid;
    private final List<String> errors;

    public DialogueValidationResult(
            boolean valid,
            List<String> errors
    ) {
        this.valid = valid;
        this.errors = List.copyOf(errors);
    }

    public boolean isValid() {
        return valid;
    }

    public List<String> getErrors() {
        return errors;
    }
}