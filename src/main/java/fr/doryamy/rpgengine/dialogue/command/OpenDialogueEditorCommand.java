package fr.doryamy.rpgengine.dialogue.command;

import fr.doryamy.rpgengine.bridge.NeoForgeBridge;
import fr.doryamy.rpgengine.command.RpgSubcommand;
import fr.doryamy.rpgengine.dialogue.editor.DialogueEditorService;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueEditorScenarioSummaryView;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueEditorView;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;

/**
 * Ouvre l'éditeur visuel d'un scénario
 * à partir d'un dialogue existant.
 *
 * <p>Syntaxe :
 * {@code /rpg dialog editor <dialogueKey>}
 */
public final class OpenDialogueEditorCommand
        implements RpgSubcommand {

    private final DialogueEditorService editorService;
    private final NeoForgeBridge bridge;

    public OpenDialogueEditorCommand(
            DialogueEditorService editorService,
            NeoForgeBridge bridge
    ) {
        this.editorService =
                editorService;

        this.bridge =
                bridge;
    }

    @Override
    public String getPath() {
        return "dialog editor";
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

        /*
         * /rpg dialog editor
         *
         * Ouvre le gestionnaire principal.
         */
        if (args.length == 0) {

            List<DialogueEditorScenarioSummaryView> scenarios =
                    editorService.findScenarios();

            boolean success =
                    bridge.showDialogueManager(
                            player.getUniqueId(),
                            scenarios
                    );

            if (!success) {

                sender.sendMessage(
                        "Impossible d'ouvrir le gestionnaire NeoForge."
                );

                return false;
            }

            return true;
        }

        /*
         * /rpg dialog editor <dialogueKey>
         *
         * Ouvre directement un scénario.
         */
        if (args.length == 1) {

            Optional<DialogueEditorView> viewResult =
                    editorService.buildView(
                            args[0]
                    );

            if (viewResult.isEmpty()) {

                sender.sendMessage(
                        "Impossible de construire la vue d'édition pour ce dialogue."
                );

                return false;
            }

            boolean success =
                    bridge.showDialogueEditor(
                            player.getUniqueId(),
                            viewResult.get()
                    );

            if (!success) {

                sender.sendMessage(
                        "Impossible d'ouvrir l'éditeur NeoForge."
                );

                return false;
            }

            return true;
        }

        sender.sendMessage(
                "Usage : /rpg dialog editor [dialogueKey]"
        );

        return false;
    }
}