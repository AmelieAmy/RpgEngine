package fr.doryamy.rpgengine.command.dialogue;

import fr.doryamy.rpgengine.bridge.NeoForgeBridge;
import fr.doryamy.rpgengine.command.RpgSubcommand;
import fr.doryamy.rpgengine.dialogue.editor.DialogueAdminService;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueAdminView;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Ouvre l'écran principal d'administration des dialogues.
 */
public final class OpenDialogueAdminCommand
        implements RpgSubcommand {

    private final DialogueAdminService dialogueAdminService;
    private final NeoForgeBridge neoForgeBridge;

    public OpenDialogueAdminCommand(
            DialogueAdminService dialogueAdminService,
            NeoForgeBridge neoForgeBridge
    ) {

        this.dialogueAdminService =
                Objects.requireNonNull(
                        dialogueAdminService,
                        "dialogueAdminService"
                );

        this.neoForgeBridge =
                Objects.requireNonNull(
                        neoForgeBridge,
                        "neoForgeBridge"
                );
    }

    @Override
    public String getPath() {
        return "dialog";
    }

    @Override
    public boolean execute(
            CommandSender sender,
            String[] args
    ) {

        if (!(sender instanceof Player player)) {

            sender.sendMessage(
                    "Cette commande doit être exécutée par un joueur."
            );

            return false;
        }

        if (args.length != 0) {

            sender.sendMessage(
                    "Usage : /rpg dialog"
            );

            return false;
        }

        DialogueAdminView view =
                dialogueAdminService.present();

        boolean opened =
                neoForgeBridge.openDialogueAdmin(
                        player.getUniqueId(),
                        view
                );

        if (!opened) {

            sender.sendMessage(
                    "Impossible d'ouvrir "
                            + "l'administration des dialogues."
            );

            return false;
        }

        return true;
    }
}
