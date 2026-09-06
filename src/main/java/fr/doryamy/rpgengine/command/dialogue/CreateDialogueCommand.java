package fr.doryamy.rpgengine.command.dialogue;

import fr.doryamy.rpgengine.command.RpgSubcommand;
import fr.doryamy.rpgengine.dialogue.Dialogue;
import fr.doryamy.rpgengine.dialogue.DialogueService;
import org.bukkit.command.CommandSender;

import java.util.Objects;

/**
 * Crée un nouveau dialogue RPGEngine.
 *
 * <p>La commande ne construit elle-même
 * aucune structure de graphe.
 *
 * <p>La création complète de l'agrégat,
 * notamment le graphe initial :
 *
 * <pre>
 * START -> END
 * </pre>
 *
 * est déléguée au DialogueService.
 */
public final class CreateDialogueCommand
        implements RpgSubcommand {

    private final DialogueService dialogueService;

    public CreateDialogueCommand(
            DialogueService dialogueService
    ) {

        this.dialogueService =
                Objects.requireNonNull(
                        dialogueService,
                        "dialogueService"
                );
    }

    @Override
    public String getPath() {

        return "dialog create";
    }

    @Override
    public boolean execute(
            CommandSender sender,
            String[] args
    ) {

        if (args.length == 0) {

            sender.sendMessage(
                    "Usage : /rpg dialog create <nom>"
            );

            return true;
        }

        String name =
                String.join(
                                " ",
                                args
                        )
                        .trim();

        if (name.isEmpty()) {

            sender.sendMessage(
                    "Le nom du dialogue ne peut pas être vide."
            );

            return true;
        }

        Dialogue dialogue =
                dialogueService.create(
                        name
                );

        sender.sendMessage(
                "Dialogue créé : "
                        + dialogue.name()
        );

        sender.sendMessage(
                "Clé : "
                        + dialogue.key().value()
        );

        return true;
    }
}