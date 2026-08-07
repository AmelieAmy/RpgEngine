package fr.doryamy.rpgengine.repository;

import fr.doryamy.rpgengine.model.Condition;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository chargé de lire les conditions associées à un trigger.
 */
public final class ConditionRepository {
    private final Connection connection;

    public ConditionRepository(Connection connection) {
        this.connection = connection;
    }

    /**
     * Charge toutes les conditions appartenant à un trigger.
     */
    public List<Condition> findByTriggerId(int triggerId) {
        List<Condition> conditions = new ArrayList<>();

        String sql = """
        SELECT provider, expression
        FROM condition
        WHERE trigger_id = ?
        """;

        try(
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, triggerId);
            ResultSet rs = statement.executeQuery();

            while(rs.next()) {
                conditions.add(
                        new Condition(
                                rs.getString("provider"),
                                rs.getString("expression")
                        )
                );
            }
        } catch(SQLException e) {
            RpgLogger.error(
                    "Impossible de charger les conditions du trigger "
                            + triggerId
                            + " : "
                            + e.getMessage()
            );
        }
        return conditions;
    }

}