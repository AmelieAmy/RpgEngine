package fr.doryamy.rpgengine.dialogue.editor;

import fr.doryamy.rpgengine.dialogue.Dialogue;
import fr.doryamy.rpgengine.dialogue.DialogueCharacterProfileKey;
import fr.doryamy.rpgengine.dialogue.DialogueCharacterProfileService;
import fr.doryamy.rpgengine.dialogue.DialogueElement;
import fr.doryamy.rpgengine.dialogue.DialogueElementKey;
import fr.doryamy.rpgengine.dialogue.DialogueGraph;
import fr.doryamy.rpgengine.dialogue.DialogueKey;
import fr.doryamy.rpgengine.dialogue.DialogueParticipant;
import fr.doryamy.rpgengine.dialogue.DialogueParticipantKey;
import fr.doryamy.rpgengine.dialogue.DialogueParticipantType;
import fr.doryamy.rpgengine.dialogue.DialogueParticipants;
import fr.doryamy.rpgengine.dialogue.DialogueReply;
import fr.doryamy.rpgengine.dialogue.DialogueService;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Orchestre l'édition métier d'une réplique PNJ.
 *
 * <p>Une Reply continue de référencer un participant. La sélection d'un
 * personnage résout ou crée un participant NPC lié au profil demandé, puis
 * réaffecte uniquement la Reply éditée à ce participant.
 */
public final class DialogueReplyEditingService {

    private final DialogueService dialogueService;
    private final DialogueCharacterProfileService characterProfileService;

    public DialogueReplyEditingService(
            DialogueService dialogueService,
            DialogueCharacterProfileService characterProfileService
    ) {
        this.dialogueService = Objects.requireNonNull(dialogueService, "dialogueService");
        this.characterProfileService = Objects.requireNonNull(
                characterProfileService,
                "characterProfileService"
        );
    }

    public Dialogue updateNpcReply(
            DialogueKey dialogueKey,
            DialogueElementKey replyKey,
            DialogueCharacterProfileKey characterProfileKey,
            String text
    ) {
        Objects.requireNonNull(dialogueKey, "dialogueKey");
        Objects.requireNonNull(replyKey, "replyKey");
        Objects.requireNonNull(characterProfileKey, "characterProfileKey");
        Objects.requireNonNull(text, "text");

        String normalizedText = text.trim();
        if (normalizedText.isEmpty()) {
            throw new IllegalArgumentException("Le texte d'une réplique ne peut pas être vide.");
        }

        characterProfileService.require(characterProfileKey);

        Dialogue current = dialogueService.require(dialogueKey);
        DialogueReply reply = requireReply(current, replyKey);
        DialogueParticipant currentParticipant =
                current.participants().require(reply.participantKey());

        if (currentParticipant.type() != DialogueParticipantType.NPC) {
            throw new IllegalArgumentException(
                    "Seule une réplique PNJ peut recevoir un profil de personnage."
            );
        }

        DialogueParticipant targetParticipant =
                findParticipantForProfile(current, characterProfileKey);

        if (targetParticipant == null) {
            targetParticipant = new DialogueParticipant(
                    nextParticipantKey(current, characterProfileKey),
                    DialogueParticipantType.NPC,
                    characterProfileKey
            );

            List<DialogueParticipant> participants =
                    new ArrayList<>(current.participants().values());
            participants.add(targetParticipant);

            dialogueService.replaceParticipants(
                    dialogueKey,
                    new DialogueParticipants(participants)
            );

            current = dialogueService.require(dialogueKey);
        }

        DialogueReply updatedReply = new DialogueReply(
                reply.key(),
                targetParticipant.key(),
                normalizedText,
                reply.rules()
        );

        List<DialogueElement> elements = current.graph()
                .elements()
                .values()
                .stream()
                .map(element -> element.key().equals(replyKey)
                        ? updatedReply
                        : element)
                .toList();

        DialogueGraph updatedGraph = new DialogueGraph(
                elements,
                current.graph().links()
        );

        return dialogueService.replaceGraph(dialogueKey, updatedGraph);
    }

    private DialogueReply requireReply(
            Dialogue dialogue,
            DialogueElementKey replyKey
    ) {
        DialogueElement element = dialogue.graph()
                .elements()
                .values()
                .stream()
                .filter(candidate -> candidate.key().equals(replyKey))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "Réplique introuvable : " + replyKey
                ));

        if (!(element instanceof DialogueReply reply)) {
            throw new IllegalArgumentException(
                    "L'élément " + replyKey + " n'est pas une réplique."
            );
        }

        return reply;
    }

    private DialogueParticipant findParticipantForProfile(
            Dialogue dialogue,
            DialogueCharacterProfileKey profileKey
    ) {
        List<DialogueParticipant> matches = dialogue.participants()
                .values()
                .stream()
                .filter(participant -> participant.type() == DialogueParticipantType.NPC)
                .filter(participant -> profileKey.equals(participant.characterProfileKey()))
                .toList();

        if (matches.size() > 1) {
            throw new IllegalStateException(
                    "Le dialogue contient plusieurs participants NPC pour le profil "
                            + profileKey + "."
            );
        }

        return matches.isEmpty() ? null : matches.getFirst();
    }

    private DialogueParticipantKey nextParticipantKey(
            Dialogue dialogue,
            DialogueCharacterProfileKey profileKey
    ) {
        String base = "npc-" + profileKey.value();
        String candidate = base;
        int suffix = 2;

        while (dialogue.participants()
                .find(new DialogueParticipantKey(candidate))
                .isPresent()) {
            candidate = base + "-" + suffix++;
        }

        return new DialogueParticipantKey(candidate);
    }
}
