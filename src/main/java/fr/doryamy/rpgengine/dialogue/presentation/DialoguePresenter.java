package fr.doryamy.rpgengine.dialogue.presentation;

import org.bukkit.entity.Player;

/**
 * Contrat de présentation d'un dialogue RPGEngine.
 *
 * <p>Le Dialogue Engine produit un {@link DialogueView}
 * sans connaître la technologie utilisée pour l'afficher.
 * Une implémentation de ce presenter est ensuite responsable
 * de transmettre cette vue au joueur.
 *
 * <p>La présentation ne contient aucune logique de progression :
 * le serveur reste l'autorité sur les sessions et transitions.
 */
public interface DialoguePresenter {

    /**
     * Affiche ou met à jour l'état visible d'un dialogue.
     *
     * @param player joueur destinataire
     * @param view   état visible du dialogue
     */
    void show(
            Player player,
            DialogueView view
    );

    /**
     * Ferme la présentation du dialogue pour le joueur.
     *
     * @param player joueur concerné
     */
    void close(
            Player player
    );
}