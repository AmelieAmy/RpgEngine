package fr.doryamy.rpgengine.repository;

import fr.doryamy.rpgengine.model.Action;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository chargé de lire les actions associées à un trigger.
 */
public final class ActionRepository {
    private final Connection connection;

    public ActionRepository(Connection connection) {
        this.connection = connection;
    }

    /**
     * Charge toutes les actions appartenant à un trigger.
     *
     * Les actions sont retournées dans leur ordre d'exécution.
     */
    public List<Action> findByTriggerId(int triggerId) {
        List<Action> actions = new ArrayList<>();
        String sql = """
                SELECT provider, expression, position
                FROM action
                WHERE trigger_id = ?
                ORDER BY position
                """;

        try(
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, triggerId);
            ResultSet rs =
                    statement.executeQuery();

            while(rs.next()) {
                actions.add(
                        new Action(
                                rs.getString("provider"),
                                rs.getString("expression"),
                                rs.getInt("position")
                        )
                );
            }

        } catch(SQLException e) {
            RpgLogger.error(
                    "Impossible de charger les actions du trigger "
                            + triggerId
                            + " : "
                            + e.getMessage()
            );
        }

        return actions;
    }

}