package fr.doryamy.rpgengine.repository;

import fr.doryamy.rpgengine.util.RpgLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Objects;
import java.util.UUID;

/**
 * Repository chargé des variables persistantes des joueurs.
 *
 * Les valeurs sont stockées sous forme de chaînes de caractères.
 * Leur interprétation appartient aux composants du moteur.
 */
public final class PlayerVariableRepository {

    private final Connection connection;

    public PlayerVariableRepository(
            Connection connection
    ) {
        this.connection = Objects.requireNonNull(
                connection,
                "Connection ne peut pas être null."
        );
    }

    /**
     * Retourne la valeur d'une variable joueur.
     *
     * @param playerUuid identifiant du joueur
     * @param key clé de la variable
     *
     * @return valeur stockée, ou null si aucune valeur
     *         n'a pu être retournée
     */
    public String get(
            UUID playerUuid,
            String key
    ) {
        Objects.requireNonNull(
                playerUuid,
                "UUID joueur ne peut pas être null."
        );

        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException(
                    "La clé d'une variable joueur ne peut pas être vide."
            );
        }

        String sql = """
                SELECT value
                FROM player_variable
                WHERE player_uuid = ?
                  AND key = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
        ) {
            statement.setString(
                    1,
                    playerUuid.toString()
            );

            statement.setString(
                    2,
                    key
            );

            try (
                    ResultSet result =
                            statement.executeQuery()
            ) {
                if (result.next()) {
                    return result.getString(
                            "value"
                    );
                }
            }

        } catch (SQLException e) {

            RpgLogger.error(
                    "Impossible de charger la variable joueur "
                            + key
                            + " pour "
                            + playerUuid
                            + " : "
                            + e.getMessage()
            );
        }

        return null;
    }

    /**
     * Crée ou remplace une variable joueur.
     *
     * @param playerUuid identifiant du joueur
     * @param key clé de la variable
     * @param value valeur à stocker
     */
    public void set(
            UUID playerUuid,
            String key,
            String value
    ) {
        Objects.requireNonNull(
                playerUuid,
                "UUID joueur ne peut pas être null."
        );

        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException(
                    "La clé d'une variable joueur ne peut pas être vide."
            );
        }

        Objects.requireNonNull(
                value,
                "La valeur d'une variable joueur ne peut pas être null."
        );

        String sql = """
                INSERT INTO player_variable (
                    player_uuid,
                    key,
                    value
                )
                VALUES (?, ?, ?)

                ON CONFLICT(player_uuid, key)
                DO UPDATE SET
                    value = excluded.value
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
        ) {
            statement.setString(
                    1,
                    playerUuid.toString()
            );

            statement.setString(
                    2,
                    key
            );

            statement.setString(
                    3,
                    value
            );

            statement.executeUpdate();

        } catch (SQLException e) {

            RpgLogger.error(
                    "Impossible d'enregistrer la variable joueur "
                            + key
                            + " pour "
                            + playerUuid
                            + " : "
                            + e.getMessage()
            );
        }
    }

    /**
     * Vérifie si une variable existe pour un joueur.
     *
     * @param playerUuid identifiant du joueur
     * @param key clé de la variable
     *
     * @return true si la variable existe
     */
    public boolean exists(
            UUID playerUuid,
            String key
    ) {
        Objects.requireNonNull(
                playerUuid,
                "UUID joueur ne peut pas être null."
        );

        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException(
                    "La clé d'une variable joueur ne peut pas être vide."
            );
        }

        String sql = """
                SELECT 1
                FROM player_variable
                WHERE player_uuid = ?
                  AND key = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
        ) {
            statement.setString(
                    1,
                    playerUuid.toString()
            );

            statement.setString(
                    2,
                    key
            );

            try (
                    ResultSet result =
                            statement.executeQuery()
            ) {
                return result.next();
            }

        } catch (SQLException e) {

            RpgLogger.error(
                    "Impossible de vérifier la variable joueur "
                            + key
                            + " pour "
                            + playerUuid
                            + " : "
                            + e.getMessage()
            );

            return false;
        }
    }
}