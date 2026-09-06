package fr.doryamy.rpgengine.dialogue;

import java.util.*;

/**
 * Ensemble des règles appartenant à un élément
 * de dialogue.
 *
 * <p>Les Conditions et Actions restent les concepts
 * génériques du moteur RPGEngine. Les Entry ajoutent
 * uniquement leur identité stable au sein du dialogue.
 */
public record DialogueRules(
        List<DialogueConditionEntry> conditions,
        List<DialogueActionEntry> actions
) {

    public DialogueRules {

        conditions =
                conditions == null
                        ? List.of()
                        : validateConditions(
                        conditions
                );

        actions =
                actions == null
                        ? List.of()
                        : normalizeActions(
                        actions
                );

        validateUniqueKeys(
                conditions,
                actions
        );
    }

    /**
     * Retourne un ensemble de règles vide.
     */
    public static DialogueRules empty() {

        return new DialogueRules(
                List.of(),
                List.of()
        );
    }

    private static List<DialogueConditionEntry>
    validateConditions(
            List<DialogueConditionEntry> source
    ) {

        List<DialogueConditionEntry> result =
                new ArrayList<>(
                        source
                );

        result.forEach(entry ->
                Objects.requireNonNull(
                        entry,
                        "Une condition de dialogue "
                                + "ne peut pas être null."
                )
        );

        return List.copyOf(
                result
        );
    }

    private static List<DialogueActionEntry>
    normalizeActions(
            List<DialogueActionEntry> source
    ) {

        List<DialogueActionEntry> result =
                new ArrayList<>(
                        source
                );

        Set<Integer> positions =
                new HashSet<>();

        for (DialogueActionEntry entry : result) {

            Objects.requireNonNull(
                    entry,
                    "Une action de dialogue "
                            + "ne peut pas être null."
            );

            int position =
                    entry.action()
                            .getPosition();

            if (!positions.add(position)) {
                throw new IllegalArgumentException(
                        "Deux actions d'un même élément "
                                + "ne peuvent pas partager "
                                + "la position "
                                + position
                                + "."
                );
            }
        }

        result.sort(
                Comparator.comparingInt(
                        entry ->
                                entry.action()
                                        .getPosition()
                )
        );

        return List.copyOf(
                result
        );
    }

    /**
     * Une même clé ne peut pas identifier
     * simultanément plusieurs règles.
     */
    private static void validateUniqueKeys(
            List<DialogueConditionEntry> conditions,
            List<DialogueActionEntry> actions
    ) {

        Set<DialogueRuleKey> keys =
                new HashSet<>();

        for (DialogueConditionEntry entry : conditions) {

            if (!keys.add(entry.key())) {
                throw new IllegalArgumentException(
                        "Clé de règle dupliquée : "
                                + entry.key()
                );
            }
        }

        for (DialogueActionEntry entry : actions) {

            if (!keys.add(entry.key())) {
                throw new IllegalArgumentException(
                        "Clé de règle dupliquée : "
                                + entry.key()
                );
            }
        }
    }
}