package fr.doryamy.rpgengine.dialogue.editor;

import fr.doryamy.rpgengine.dialogue.Dialogue;
import fr.doryamy.rpgengine.dialogue.DialogueKey;
import fr.doryamy.rpgengine.dialogue.DialogueRepository;

import java.util.Objects;

/** Service métier dédié au renommage d'un dialogue. */
public final class DialogueNamingService {

    private final DialogueRepository dialogueRepository;

    public DialogueNamingService(DialogueRepository dialogueRepository) {
        this.dialogueRepository = Objects.requireNonNull(dialogueRepository, "dialogueRepository");
    }

    public Dialogue rename(DialogueKey dialogueKey, String name) {
        Objects.requireNonNull(dialogueKey, "dialogueKey");
        Objects.requireNonNull(name, "name");

        String normalizedName = name.trim();
        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException("Le nom d'un dialogue ne peut pas être vide.");
        }

        Dialogue current = dialogueRepository.findByKey(dialogueKey)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Dialogue introuvable : " + dialogueKey.value()
                ));

        Dialogue renamed = new Dialogue(
                current.key(),
                normalizedName,
                current.rules(),
                current.graph()
        );

        dialogueRepository.update(renamed);
        return renamed;
    }
}
