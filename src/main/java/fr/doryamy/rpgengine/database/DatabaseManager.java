package fr.doryamy.rpgengine.database;

import fr.doryamy.rpgengine.RpgEngine;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Gère la connexion SQLite utilisée par RPGEngine.
 *
 * Cette classe est responsable :
 *   de la création du fichier de base de données ;
 *   de l'ouverture de la connexion ;
 *   de l'activation des contraintes de clés étrangères ;
 *   du lancement des migrations ;
 *   de la fermeture de la connexion.
 */
public final class DatabaseManager {
    private final RpgEngine plugin;
    private Connection connection;

    /**
     * Crée le gestionnaire de base de données.
     *
     * @param plugin instance principale de RPGEngine
     */
    public DatabaseManager(RpgEngine plugin) {
        this.plugin = plugin;
    }

    /**
     * Ouvre la connexion SQLite et applique les migrations nécessaires.
     */
    public void connect() {
        try {
            File file = new File(
                    plugin.getDataFolder(),
                    "rpgengine.db"
            );

            if(!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            connection = DriverManager.getConnection(
                    "jdbc:sqlite:" + file
            );

            try (var statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
            }

            RpgLogger.info("SQLite connecté");

            MigrationManager migrationManager =
                    new MigrationManager();

            migrationManager.migrate(connection);

        } catch(SQLException e) {
            RpgLogger.error(
                    "Impossible de se connecter à SQLite : "
                            + e.getMessage()
            );
        }
    }

    public Connection getConnection() {
        return connection;
    }

    /**
     * Ferme la connexion SQLite si elle est ouverte.
     */
    public void close() {
        try {
            if(connection != null)
                connection.close();
        } catch(SQLException e){
            RpgLogger.error(
                    "Impossible de fermer la connexion SQLite : "
                            + e.getMessage()
            );
        }
    }

}