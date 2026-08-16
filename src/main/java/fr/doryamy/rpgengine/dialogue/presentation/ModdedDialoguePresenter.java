package fr.doryamy.rpgengine.dialogue.presentation;

import fr.doryamy.rpgengine.bridge.NeoForgeBridge;
import fr.doryamy.rpgengine.util.RpgLogger;
import org.bukkit.entity.Player;

/**
 * Présentation des dialogues utilisant le mod client RPGEngine.
 *
 * Cette implémentation délègue l'affichage et la fermeture
 * au {@link NeoForgeBridge}. Elle ne dépend directement
 * d'aucune classe NeoForge.
 */
public final class ModdedDialoguePresenter
        implements DialoguePresenter {

    private final NeoForgeBridge bridge;

    /**
     * Crée le presenter utilisant le bridge NeoForge.
     *
     * @param bridge bridge vers le mod RPGEngine
     */
    public ModdedDialoguePresenter(
            NeoForgeBridge bridge
    ) {
        this.bridge =
                bridge;
    }

    @Override
    public void show(
            Player player,
            DialogueView view
    ) {
        boolean success =
                bridge.showDialogue(
                        player.getUniqueId(),
                        view
                );

        if (!success) {

            RpgLogger.error(
                    "Impossible d'afficher le dialogue via NeoForge "
                            + "pour le joueur "
                            + player.getName()
                            + "."
            );
        }
    }

    @Override
    public void close(
            Player player
    ) {
        boolean success =
                bridge.dismissDialogue(
                        player.getUniqueId()
                );

        if (!success) {

            RpgLogger.error(
                    "Impossible de fermer le dialogue via NeoForge "
                            + "pour le joueur "
                            + player.getName()
                            + "."
            );
        }
    }
}