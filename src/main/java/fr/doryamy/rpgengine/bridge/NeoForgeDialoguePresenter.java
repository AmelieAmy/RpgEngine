package fr.doryamy.rpgengine.bridge;

import fr.doryamy.rpgengine.dialogue.runtime.view.DialoguePresenter;
import fr.doryamy.rpgengine.dialogue.runtime.view.DialogueView;
import fr.doryamy.rpgengine.util.RpgLogger;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * Adaptateur de présentation des dialogues
 * vers le bridge NeoForge.
 *
 * <p>Le moteur de dialogue ne connaît que
 * DialoguePresenter. Cette classe traduit ce port
 * vers l'infrastructure NeoForge.
 */
public final class NeoForgeDialoguePresenter
        implements DialoguePresenter {

    private final NeoForgeBridge neoForgeBridge;

    public NeoForgeDialoguePresenter(
            NeoForgeBridge neoForgeBridge
    ) {

        this.neoForgeBridge =
                Objects.requireNonNull(
                        neoForgeBridge,
                        "neoForgeBridge"
                );
    }

    @Override
    public void show(
            Player player,
            DialogueView view
    ) {

        Objects.requireNonNull(
                player,
                "player"
        );

        Objects.requireNonNull(
                view,
                "view"
        );

        boolean shown =
                neoForgeBridge.showDialogue(
                        player.getUniqueId(),
                        view
                );

        if (!shown) {

            RpgLogger.error(
                    "Impossible d'afficher le dialogue "
                            + "pour le joueur "
                            + player.getUniqueId()
                            + "."
            );
        }
    }

    @Override
    public void close(
            Player player
    ) {

        Objects.requireNonNull(
                player,
                "player"
        );

        boolean dismissed =
                neoForgeBridge.dismissDialogue(
                        player.getUniqueId()
                );

        if (!dismissed) {

            RpgLogger.error(
                    "Impossible de fermer le dialogue "
                            + "pour le joueur "
                            + player.getUniqueId()
                            + "."
            );
        }
    }
}