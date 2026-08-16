package fr.doryamy.rpgengine.dialogue.presentation;

import org.bukkit.entity.Player;

/**
 * Présentation textuelle minimale utilisée lorsqu'aucune
 * interface cliente RPGEngine n'est disponible.
 *
 * <p>Cette implémentation affiche le contenu du dialogue
 * dans le chat mais ne permet pas d'interagir avec lui.
 *
 * <p>Les dialogues interactifs complets nécessitent actuellement
 * le mod client RPGEngine.
 */
public final class VanillaDialoguePresenter
        implements DialoguePresenter {

    @Override
    public void show(
            Player player,
            DialogueView view
    ) {
        if (view.speaker() != null
                && !view.speaker().isBlank()) {

            player.sendMessage(
                    view.speaker()
                            + " : "
                            + view.text()
            );

        } else {

            player.sendMessage(
                    view.text()
            );
        }

        switch (view.interactionType()) {

            case CONTINUE,
                 CHOICE,
                 CLOSE ->
                    player.sendMessage(
                            "Le mod client RPGEngine est requis "
                                    + "pour poursuivre ce dialogue."
                    );
        }
    }

    @Override
    public void close(
            Player player
    ) {
        // Aucune interface graphique à fermer.
    }
}