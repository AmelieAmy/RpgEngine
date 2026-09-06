package fr.doryamy.rpgengine.command.dialogue;

import fr.doryamy.rpgengine.command.RpgSubcommand;
import fr.doryamy.rpgengine.dialogue.editor.DialogueEditorController;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

/** Raccourci développeur ouvrant directement l'éditeur d'un dialogue. */
public final class OpenDialogueEditorCommand implements RpgSubcommand {

    private final DialogueEditorController dialogueEditorController;

    public OpenDialogueEditorCommand(DialogueEditorController dialogueEditorController) {
        this.dialogueEditorController = Objects.requireNonNull(
                dialogueEditorController,
                "dialogueEditorController"
        );
    }

    @Override
    public String getPath() {
        return "dialog editor";
    }

    @Override
    public boolean execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Cette commande doit être exécutée par un joueur.");
            return false;
        }

        if (args.length != 1) {
            sender.sendMessage("Usage : /rpg dialog editor <dialogueKey>");
            return false;
        }

        dialogueEditorController.openEditor(
                player.getUniqueId(),
                args[0]
        );
        return true;
    }
}
