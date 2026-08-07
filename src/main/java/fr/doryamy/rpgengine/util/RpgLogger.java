package fr.doryamy.rpgengine.util;

import fr.doryamy.rpgengine.RpgEngine;

/**
 * Centralise les logs produits par RPGEngine.
 *
 * Les niveaux disponibles sont :
 * DEBUG : informations détaillées de développement ;
 * INFO  : fonctionnement normal du moteur ;
 * WARN  : situation inhabituelle, mais récupérable ;
 * ERROR : erreur empêchant une opération de fonctionner correctement.
 *
 * Les messages DEBUG sont affichés uniquement
 * lorsque l'option "debug" est activée dans le fichier config.yml.
 */
public final class RpgLogger {

    private static boolean debug;

    private RpgLogger() {
        // Classe utilitaire : aucune instance nécessaire.
    }

    /**
     * Initialise le système de logs
     * à partir de la configuration du plugin.
     */
    public static void init() {
        debug =
                RpgEngine.getInstance()
                        .getConfig()
                        .getBoolean(
                                "debug",
                                false
                        );
    }

    /**
     * Affiche un message de debug si le mode
     * debug est activé.
     *
     * @param message message à afficher
     */
    public static void debug(String message) {
        if (!debug) {
            return;
        }

        RpgEngine.getInstance()
                .getLogger()
                .info(
                        "[DEBUG] " + message
                );
    }

    /**
     * Affiche une information concernant
     * le fonctionnement normal du moteur.
     *
     * @param message message à afficher
     */
    public static void info(String message) {
        RpgEngine.getInstance()
                .getLogger()
                .info(message);
    }

    /**
     * Affiche un avertissement correspondant
     * à une situation inhabituelle, mais non bloquante.
     *
     * @param message message à afficher
     */
    public static void warn(String message) {
        RpgEngine.getInstance()
                .getLogger()
                .warning(message);
    }

    /**
     * Affiche une erreur empêchant une opération
     * du moteur de fonctionner correctement.
     *
     * @param message message à afficher
     */
    public static void error(String message) {
        RpgEngine.getInstance()
                .getLogger()
                .severe(message);
    }
}