package fr.doryamy.rpgengine.dialogue.command;

import fr.doryamy.rpgengine.command.RpgSubcommand;
import fr.doryamy.rpgengine.dialogue.*;
import org.bukkit.command.CommandSender;

import java.util.Optional;

/**
 * Affiche les informations détaillées
 * d'un dialogue.
 *
 * Syntaxe :
 * /rpg dialog info <key>
 */
public final class InfoDialogueCommand
        implements RpgSubcommand {

    private final DialogueService dialogueService;

    public InfoDialogueCommand(
            DialogueService dialogueService
    ) {
        this.dialogueService =
                dialogueService;
    }

    @Override
    public String getPath() {
        return "dialog info";
    }

    @Override
    public boolean execute(
            CommandSender sender,
            String[] args
    ) {
        if (args.length != 1) {
            sender.sendMessage(
                    "Usage : /rpg dialog info <key>"
            );

            return false;
        }

        String key =
                args[0];

        Optional<Dialogue> result =
                dialogueService.find(
                        key
                );

        if (result.isEmpty()) {
            sender.sendMessage(
                    "Dialogue introuvable : "
                            + key
            );

            return false;
        }

        Dialogue dialogue =
                result.get();

        sender.sendMessage(
                "------------------------------"
        );

        sender.sendMessage(
                "Dialogue : "
                        + dialogue.getKey()
        );

        sender.sendMessage(
                "Nom : "
                        + dialogue.getName()
        );

        sender.sendMessage(
                "Node de départ : "
                        + (
                        dialogue.getStartNodeKey() != null
                                ? dialogue.getStartNodeKey()
                                : "<non défini>"
                )
        );

        sender.sendMessage("");

        sender.sendMessage(
                "Nodes : "
                        + dialogue.getNodes().size()
        );

        for (DialogueNode node :
                dialogue.getNodes()) {

            sender.sendMessage(
                    "- "
                            + node.getKey()
                            + " : "
                            + node.getText()
            );
        }

        sender.sendMessage("");

        sender.sendMessage(
                "Transitions : "
                        + dialogue.getTransitions().size()
        );

        for (DialogueTransition transition :
                dialogue.getTransitions()) {

            String target =
                    transition.getType()
                            == DialogueTransitionType.END
                            ? "END"
                            : (
                            transition.getTargetNodeKey() != null
                                    ? transition.getTargetNodeKey()
                                    : "<cible absente>"
                    );

            String label =
                    transition.getLabel() != null
                            ? " | \"" + transition.getLabel() + "\""
                            : "";

            sender.sendMessage(
                    "- "
                            + transition.getKey()
                            + " | pos="
                            + transition.getPosition()
                            + " | "
                            + transition.getSourceNodeKey()
                            + " -> "
                            + target
                            + " ["
                            + transition.getType()
                            + "]"
                            + label
            );
        }

        sender.sendMessage(
                "------------------------------"
        );

        return true;
    }
}