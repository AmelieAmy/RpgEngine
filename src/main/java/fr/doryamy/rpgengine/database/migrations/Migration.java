package fr.doryamy.rpgengine.database.migrations;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Représente une évolution versionnée
 * du schéma de la base de données.
 *
 * Une migration doit être exécutée une seule fois
 * et ne doit plus être modifiée après déploiement.
 */
public interface Migration {

    /**
     * Retourne la version introduite par cette migration.
     *
     * @return numéro de version
     */
    int getVersion();

    /**
     * Applique la migration sur la connexion fournie.
     *
     * @param connection connexion SQLite
     * @throws SQLException si la migration échoue
     */
    void apply(Connection connection)
            throws SQLException;
}
