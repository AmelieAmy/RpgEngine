package fr.doryamy.rpgengine.dialogue;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Ensemble immuable des participants explicitement déclarés par un dialogue.
 */
public final class DialogueParticipants {

    private final Map<DialogueParticipantKey, DialogueParticipant> byKey;

    public DialogueParticipants(
            Collection<DialogueParticipant> participants
    ) {
        Objects.requireNonNull(participants, "participants");

        Map<DialogueParticipantKey, DialogueParticipant> values =
                new LinkedHashMap<>();

        for (DialogueParticipant participant : participants) {
            Objects.requireNonNull(participant, "participant");

            DialogueParticipant previous =
                    values.put(participant.key(), participant);

            if (previous != null) {
                throw new IllegalArgumentException(
                        "Plusieurs participants utilisent la clé "
                                + participant.key() + "."
                );
            }
        }

        this.byKey = Map.copyOf(values);
    }

    public static DialogueParticipants empty() {
        return new DialogueParticipants(List.of());
    }

    public List<DialogueParticipant> values() {
        return List.copyOf(byKey.values());
    }

    public Optional<DialogueParticipant> find(
            DialogueParticipantKey key
    ) {
        Objects.requireNonNull(key, "key");
        return Optional.ofNullable(byKey.get(key));
    }

    public DialogueParticipant require(
            DialogueParticipantKey key
    ) {
        return find(key).orElseThrow(() ->
                new IllegalArgumentException(
                        "Participant de dialogue introuvable : " + key
                )
        );
    }

    public boolean isEmpty() {
        return byKey.isEmpty();
    }

    public int size() {
        return byKey.size();
    }
}
