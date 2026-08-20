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
 * Repository chargé de la persistance
 * des actions associées aux triggers.
 *
 * <p>Cette classe ne contient aucune logique métier.
 */
public final class ActionRepository {

    private final Connection connection;

    public ActionRepository(
            Connection connection
    ) {
        this.connection =
                connection;
    }

    /**
     * Charge toutes les actions appartenant
     * à un trigger dans leur ordre d'exécution.
     *
     * @param triggerId identifiant du trigger
     *
     * @return actions associées
     */
    public List<Action> findByTriggerId(
            int triggerId
    ) {
        List<Action> actions =
                new ArrayList<>();

        String sql = """
                SELECT
                    provider,
                    expression,
                    position
                FROM action
                WHERE trigger_id = ?
                ORDER BY position
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

                    actions.add(
                            new Action(
                                    result.getString(
                                            "provider"
                                    ),
                                    result.getString(
                                            "expression"
                                    ),
                                    result.getInt(
                                            "position"
                                    )
                            )
                    );
                }
            }

        } catch (SQLException e) {

            RpgLogger.error(
                    "Impossible de charger les actions du trigger "
                            + triggerId
                            + " : "
                            + e.getMessage()
            );
        }

        return actions;
    }

    /**
     * Crée une action associée à un trigger.
     *
     * @param triggerId identifiant du trigger
     * @param action action à créer
     *
     * @return true si l'action a été créée
     */
    public boolean create(
            int triggerId,
            Action action
    ) {
        String sql = """
                INSERT INTO action (
                    trigger_id,
                    provider,
                    expression,
                    position
                )
                VALUES (?, ?, ?, ?)
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
                    action.getProvider()
            );

            statement.setString(
                    3,
                    action.getExpression()
            );

            statement.setInt(
                    4,
                    action.getPosition()
            );

            return statement.executeUpdate()
                    == 1;

        } catch (SQLException e) {

            RpgLogger.error(
                    "Impossible de créer l'action du trigger "
                            + triggerId
                            + " : "
                            + e.getMessage()
            );

            return false;
        }
    }
}