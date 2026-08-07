package fr.doryamy.rpgengine.repository;

import fr.doryamy.rpgengine.util.RpgLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Repository chargé des variables persistantes des joueurs.
 * Les valeurs sont stockées sous forme de chaînes de caractères.
 * Leur interprétation appartient aux composants du moteur.
 */
public final class PlayerVariableRepository {

    private final Connection connection;

    public PlayerVariableRepository(Connection connection) {
        this.connection = connection;
    }

    public String get(
            String playerUuid,
            String key
    ) {

        String sql = """
            SELECT value
            FROM player_variable
            WHERE player_uuid = ?
            AND key = ?
            """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, playerUuid);
            statement.setString(2, key);
            ResultSet rs = statement.executeQuery();

            if(rs.next()) {
                return rs.getString("value");
            }

        } catch(SQLException e) {
            RpgLogger.error(
                    "Impossible de charger les player variables: "
                            + e.getMessage()
            );
        }
        return null;
    }

    public void set(
            String playerUuid,
            String key,
            String value
    ) {
        String sql = """
                INSERT INTO player_variable
                (player_uuid, key, value)

                VALUES (?, ?, ?)

                ON CONFLICT(player_uuid, key)
                DO UPDATE SET value = excluded.value;
                """;

        try(
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, playerUuid);
            statement.setString(2, key);
            statement.setString(3, value);
            statement.executeUpdate();
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean exists(
            String playerUuid,
            String key
    ) {

        String sql = """
        SELECT 1
        FROM player_variable
        WHERE player_uuid = ?
        AND key = ?
        """;

        try (PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setString(1, playerUuid);
            statement.setString(2, key);
            ResultSet rs = statement.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

}