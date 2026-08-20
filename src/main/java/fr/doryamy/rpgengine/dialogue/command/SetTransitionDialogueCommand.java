package fr.doryamy.rpgengine.dialogue.command;

import fr.doryamy.rpgengine.command.CommandResult;
import fr.doryamy.rpgengine.command.RpgSubcommand;
import fr.doryamy.rpgengine.dialogue.DialogueService;
import fr.doryamy.rpgengine.dialogue.DialogueTransitionType;
import org.bukkit.command.CommandSender;

import java.util.Arrays;

/**
 * Configure une transition existante.
 *
 * Syntaxes :
 *
 * END :
 * /rpg dialog transition set
 * <dialogueKey> <transitionKey> END
 *
 * AUTO :
 * /rpg dialog transition set
 * <dialogueKey> <transitionKey> AUTO <targetNodeKey>
 *
 * CHOICE :
 * /rpg dialog transition set
 * <dialogueKey> <transitionKey> CHOICE <targetNodeKey> <label>
 */
public final class SetTransitionDialogueCommand
        implements RpgSubcommand {

    private final DialogueService dialogueService;

    public SetTransitionDialogueCommand(
            DialogueService dialogueService
    ) {
        this.dialogueService =
                dialogueService;
    }

    @Override
    public String getPath() {
        return "dialog transition set";
    }

    @Override
    public boolean execute(
            CommandSender sender,
            String[] args
    ) {
        if (args.length < 3) {

            sendUsage(
                    sender
            );

            return false;
        }

        String dialogueKey =
                args[0];

        String transitionKey =
                args[1];

        DialogueTransitionType type;

        try {
            type =
                    DialogueTransitionType.valueOf(
                            args[2].toUpperCase()
                    );

        } catch (IllegalArgumentException e) {

            sender.sendMessage(
                    "Type de transition invalide. "
                            + "Valeurs : AUTO, CHOICE, END."
            );

            return false;
        }

        String targetNodeKey =
                null;

        String label =
                null;

        switch (type) {

            case END -> {

                if (args.length != 3) {

                    sendUsage(
                            sender
                    );

                    return false;
                }
            }

            case AUTO -> {

                if (args.length != 4) {

                    sendUsage(
                            sender
                    );

                    return false;
                }

                targetNodeKey =
                        args[3];
            }

            case CHOICE -> {

                if (args.length < 5) {

                    sendUsage(
                            sender
                    );

                    return false;
                }

                targetNodeKey =
                        args[3];

                label =
                        String.join(
                                " ",
                                Arrays.copyOfRange(
                                        args,
                                        4,
                                        args.length
                                )
                        );
            }
        }

        CommandResult result =
                dialogueService.setTransition(
                        dialogueKey,
                        transitionKey,
                        type,
                        targetNodeKey,
                        label
                );

        sender.sendMessage(
                result.getMessage()
        );

        return result.isSuccess();
    }

    private void sendUsage(
            CommandSender sender
    ) {
        sender.sendMessage(
                "Usage END : /rpg dialog transition set "
                        + "<dialogueKey> <transitionKey> END"
        );

        sender.sendMessage(
                "Usage AUTO : /rpg dialog transition set "
                        + "<dialogueKey> <transitionKey> AUTO <targetNodeKey>"
        );

        sender.sendMessage(
                "Usage CHOICE : /rpg dialog transition set "
                        + "<dialogueKey> <transitionKey> CHOICE "
                        + "<targetNodeKey> <label>"
        );
    }
}