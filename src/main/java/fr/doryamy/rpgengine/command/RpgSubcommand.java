package fr.doryamy.rpgengine.command;

import org.bukkit.command.CommandSender;

/**
 * Contrat commun des sous-commandes
 * d'administration de RPGEngine.
 *
 * Chaque implémentation est responsable
 * d'une seule opération.
 */
public interface RpgSubcommand {

    /**
     * Retourne le chemin pris en charge
     * par la sous-commande.
     *
     * Exemple :
     * dialog create
     * dialog delete
     * quest create
     *
     * @return chemin de la commande
     */
    String getPath();

    /**
     * Exécute la sous-commande.
     *
     * @param sender expéditeur de la commande
     * @param args arguments restants après le chemin
     *
     * @return true si la commande a été traitée
     */
    boolean execute(
            CommandSender sender,
            String[] args
    );
}