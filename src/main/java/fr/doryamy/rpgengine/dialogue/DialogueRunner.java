package fr.doryamy.rpgengine.dialogue;

import fr.doryamy.rpgengine.util.RpgLogger;
import org.bukkit.entity.Player;

/**
 * Exécute l'affichage d'un dialogue
 * auprès d'un joueur.
 *
 * Cette classe constitue le moteur
 * d'exécution des dialogues.
 *
 * Elle ne connaît :
 *   ni les triggers ;
 *   ni les actions ;
 *   ni les commandes ;
 *   ni la base de données.
 *
 * Elle reçoit uniquement un Dialogue
 * déjà chargé et l'affiche au joueur.
 */
public final class DialogueRunner {

    /**
     * Joue un dialogue pour un joueur.
     *
     * @param player joueur destinataire
     * @param dialogue dialogue à jouer
     */
    public void play(
            Player player,
            Dialogue dialogue
    ) {
        RpgLogger.debug(
                "Lecture du dialogue '"
                        + dialogue.getKey()
                        + "' pour "
                        + player.getName()
        );

        if (dialogue.getLines().isEmpty()) {
            RpgLogger.warn(
                    "Le dialogue '"
                            + dialogue.getKey()
                            + "' ne contient aucune ligne."
            );
            return;
        }

        for (DialogueLine line : dialogue.getLines()) {
            player.sendMessage(
                    line.getText()
            );
        }
    }
}