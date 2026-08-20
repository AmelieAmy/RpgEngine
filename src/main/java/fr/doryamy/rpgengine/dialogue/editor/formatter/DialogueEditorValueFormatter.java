package fr.doryamy.rpgengine.dialogue.editor.formatter;

import fr.doryamy.rpgengine.quest.QuestService;

public final class DialogueEditorValueFormatter {

    private final QuestService questService;

    public DialogueEditorValueFormatter(
            QuestService questService
    ) {
        this.questService =
                questService;
    }

    public String formatAction(
            String provider,
            String expression
    ) {

        if (provider == null
                || provider.isBlank()) {

            return fallback(
                    expression
            );
        }

        if (expression == null
                || expression.isBlank()) {

            return provider;
        }

        if ("QUEST".equalsIgnoreCase(
                provider
        )) {

            String formatted =
                    formatQuestAction(
                            expression
                    );

            if (formatted != null) {
                return formatted;
            }
        }

        return provider
                + " : "
                + expression;
    }

    public String formatCondition(
            String provider,
            String expression
    ) {

        if (provider == null
                || provider.isBlank()) {

            return fallback(
                    expression
            );
        }

        if (expression == null
                || expression.isBlank()) {

            return provider;
        }

        return provider
                + " : "
                + expression;
    }

    private String formatQuestAction(
            String expression
    ) {

        String[] parts =
                expression.split(
                        ":",
                        2
                );

        if (parts.length != 2) {
            return null;
        }

        String operation =
                parts[0].trim();

        String questId =
                parts[1].trim();

        if (questId.isBlank()) {
            return null;
        }

        if ("START".equalsIgnoreCase(
                operation
        )) {

            String questName =
                    questService.getDisplayName(
                            questId
                    );

            return "Démarrer la quête : "
                    + questName;
        }

        return null;
    }

    private String fallback(
            String value
    ) {

        return value == null
                || value.isBlank()
                ? "-"
                : value;
    }
}