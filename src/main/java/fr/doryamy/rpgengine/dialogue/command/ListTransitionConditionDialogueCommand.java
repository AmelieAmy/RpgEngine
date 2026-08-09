package fr.doryamy.rpgengine.dialogue.command;

import fr.doryamy.rpgengine.command.RpgSubcommand;
import fr.doryamy.rpgengine.dialogue.DialogueService;
import fr.doryamy.rpgengine.model.Condition;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.Optional;

/**
 * Affiche les conditions associées
 * à une transition.
 *
 * Syntaxe :
 * /rpg dialog transition condition list
 * <dialogueKey> <transitionKey>
 */
public final class ListTransitionConditionDialogueCommand
        implements RpgSubcommand {

    private final DialogueService dialogueService;

    public ListTransitionConditionDialogueCommand(
            DialogueService dialogueService
    ) {
        this.dialogueService =
                dialogueService;
    }

    @Override
    public String getPath() {
        return "dialog transition condition list";
    }

    @Override
    public boolean execute(
            CommandSender sender,
            String[] args
    ) {
        if (args.length != 2) {

            sender.sendMessage(
                    "Usage : /rpg dialog transition condition list "
                            + "<dialogueKey> <transitionKey>"
            );

            return false;
        }

        String dialogueKey =
                args[0];

        String transitionKey =
                args[1];

        Optional<List<Condition>> result =
                dialogueService.getTransitionConditions(
                        dialogueKey,
                        transitionKey
                );

        if (result.isEmpty()) {

            sender.sendMessage(
                    "Dialogue ou transition introuvable."
            );

            return false;
        }

        List<Condition> conditions =
                result.get();

        if (conditions.isEmpty()) {

            sender.sendMessage(
                    "Aucune condition sur la transition '"
                            + transitionKey
                            + "'."
            );

            return true;
        }

        sender.sendMessage(
                "Conditions de la transition '"
                        + transitionKey
                        + "' :"
        );

        for (int i = 0; i < conditions.size(); i++) {

            Condition condition =
                    conditions.get(i);

            sender.sendMessage(
                    (i + 1)
                            + " - "
                            + condition.getProvider()
                            + " | "
                            + condition.getExpression()
            );
        }

        return true;
    }
}