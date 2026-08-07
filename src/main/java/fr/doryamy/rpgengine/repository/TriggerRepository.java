package fr.doryamy.rpgengine.repository;

import fr.doryamy.rpgengine.model.Action;
import fr.doryamy.rpgengine.model.Condition;
import fr.doryamy.rpgengine.model.Trigger;
import fr.doryamy.rpgengine.model.TriggerType;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository chargé de reconstruire les triggers à partir de la base de données.
 *
 * Cette classe est responsable :
 *   de la lecture des triggers ;
 *   du chargement de leurs conditions ;
 *   du chargement de leurs actions.
 *
 * Elle ne contient aucune logique métier.
 */
public final class TriggerRepository {

    private final Connection connection;
    private final ActionRepository actionRepository;
    private final ConditionRepository conditionRepository;

    public TriggerRepository(Connection connection) {
        this.connection = connection;
        this.actionRepository = new ActionRepository(connection);
        this.conditionRepository = new ConditionRepository(connection);
    }

    /**
     * Recherche tous les triggers actifs correspondant
     * au type et à la cible donnés.
     *
     * Les conditions et les actions associées sont chargées automatiquement.
     *
     * @param type type de trigger
     * @param targetId identifiant de la cible
     *
     * @return liste des triggers correspondants
     */
    public List<Trigger> find(
            TriggerType type,
            String targetId
    ) {
        List<Trigger> triggers = new ArrayList<>();

        String sql = """
                SELECT id, name, type, target_id
                FROM trigger
                WHERE type = ?
                AND target_id = ?
                AND enabled = 1
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, type.name());
            statement.setString(2, targetId);

            try (ResultSet rs = statement.executeQuery()) {

                while (rs.next()) {
                    int triggerId = rs.getInt("id");

                    List<Action> actions =
                            actionRepository.findByTriggerId(
                                    triggerId
                            );

                    List<Condition> conditions =
                            conditionRepository.findByTriggerId(
                                    triggerId
                            );

                    triggers.add(
                            new Trigger(
                                    triggerId,
                                    rs.getString("name"),
                                    rs.getString("target_id"),
                                    TriggerType.valueOf(
                                            rs.getString("type")
                                    ),
                                    conditions,
                                    actions
                            )
                    );
                }
            }

        } catch (SQLException e) {
            RpgLogger.error(
                    "Impossible de trouver le trigger "
                            + e.getMessage()
            );
        }

        return triggers;
    }
}