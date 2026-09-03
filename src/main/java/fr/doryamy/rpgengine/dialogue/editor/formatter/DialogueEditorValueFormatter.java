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

        if ("QUEST".equalsIgnoreCase(
                provider
        )) {

            String formatted =
                    formatQuestCondition(
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

    private String formatQuestCondition(
            String expression
    ) {

        int separator =
                expression.indexOf(
                        "=="
                );

        if (separator <= 0
                || separator >= expression.length() - 2) {

            return null;
        }

        String questId =
                expression.substring(
                        0,
                        separator
                ).trim();

        String state =
                expression.substring(
                                separator + 2
                        ).trim()
                        .toUpperCase();

        if (questId.isBlank()) {
            return null;
        }

        String stateLabel =
                switch (state) {
                    case "NOT_STARTED" -> "Non commencée";
                    case "ACTIVE" -> "En cours";
                    case "COMPLETED" -> "Terminée";
                    default -> null;
                };

        if (stateLabel == null) {
            return null;
        }

        String questName =
                questService.getDisplayName(
                        questId
                );

        return questName
                + " : "
                + stateLabel;
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