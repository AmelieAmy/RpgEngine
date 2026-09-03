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
 * Repository chargé de la persistance
 * des conditions associées aux triggers.
 *
 * <p>Cette classe ne contient aucune logique métier.
 */
public final class ConditionRepository {

    private final Connection connection;

    public ConditionRepository(
            Connection connection
    ) {
        this.connection =
                connection;
    }

    /**
     * Charge toutes les conditions
     * appartenant à un trigger.
     *
     * @param triggerId identifiant du trigger
     *
     * @return conditions associées
     */
    public List<Condition> findByTriggerId(
            int triggerId
    ) {
        List<Condition> conditions =
                new ArrayList<>();

        String sql = """
                SELECT
                    provider,
                    expression
                FROM condition
                WHERE trigger_id = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
        ) {
            statement.setInt(
                    1,
                    triggerId
            );

            try (
                    ResultSet result =
                            statement.executeQuery()
            ) {
                while (result.next()) {

                    conditions.add(
                            new Condition(
                                    result.getString(
                                            "provider"
                                    ),
                                    result.getString(
                                            "expression"
                                    )
                            )
                    );
                }
            }

        } catch (SQLException e) {

            RpgLogger.error(
                    "Impossible de charger les conditions du trigger "
                            + triggerId
                            + " : "
                            + e.getMessage()
            );
        }

        return conditions;
    }

    /**
     * Crée une condition associée
     * à un trigger.
     *
     * @param triggerId identifiant du trigger
     * @param condition condition à créer
     *
     * @return true si la condition a été créée
     */
    public boolean create(
            int triggerId,
            Condition condition
    ) {
        String sql = """
                INSERT INTO condition (
                    trigger_id,
                    provider,
                    expression
                )
                VALUES (?, ?, ?)
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
        ) {
            statement.setInt(
                    1,
                    triggerId
            );

            statement.setString(
                    2,
                    condition.getProvider()
            );

            statement.setString(
                    3,
                    condition.getExpression()
            );

            return statement.executeUpdate()
                    == 1;

        } catch (SQLException e) {

            RpgLogger.error(
                    "Impossible de créer la condition du trigger "
                            + triggerId
                            + " : "
                            + e.getMessage()
            );

            return false;
        }
    }
    /**
     * Remplace l'expression d'une condition QUEST précise
     * appartenant à un trigger.
     *
     * La clause sur l'ancienne expression évite de modifier
     * une éventuelle autre condition QUEST du même trigger.
     */
    public boolean updateQuestExpression(
            int triggerId,
            String oldExpression,
            String newExpression
    ) {
        String sql = """
                UPDATE condition
                SET expression = ?
                WHERE trigger_id = ?
                  AND UPPER(provider) = 'QUEST'
                  AND expression = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, newExpression);
            statement.setInt(2, triggerId);
            statement.setString(3, oldExpression);

            return statement.executeUpdate() == 1;

        } catch (SQLException e) {
            RpgLogger.error(
                    "Impossible de modifier la condition QUEST du trigger "
                            + triggerId
                            + " : "
                            + e.getMessage()
            );
            return false;
        }
    }


}