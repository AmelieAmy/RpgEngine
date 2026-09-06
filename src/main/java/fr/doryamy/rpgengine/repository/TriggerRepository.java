package fr.doryamy.rpgengine.repository;

import fr.doryamy.rpgengine.model.Action;
import fr.doryamy.rpgengine.model.Condition;
import fr.doryamy.rpgengine.model.Trigger;
import fr.doryamy.rpgengine.model.TriggerType;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

/**
 * Repository chargé de la persistance
 * et de la reconstruction des triggers.
 *
 * <p>Les actions et conditions associées sont
 * chargées via leurs repositories respectifs.
 *
 * <p>Cette classe ne contient aucune logique métier.
 */
public final class TriggerRepository {

    private final Connection connection;
    private final ActionRepository actionRepository;
    private final ConditionRepository conditionRepository;

    public TriggerRepository(
            Connection connection,
            ActionRepository actionRepository,
            ConditionRepository conditionRepository
    ) {
        this.connection =
                connection;

        this.actionRepository =
                actionRepository;

        this.conditionRepository =
                conditionRepository;
    }

    /**
     * Recherche tous les triggers actifs
     * correspondant au type et à la cible.
     *
     * @param type type de trigger
     * @param targetId identifiant de la cible
     *
     * @return triggers correspondants
     */
    public List<Trigger> find(
            TriggerType type,
            String targetId
    ) {
        String sql = """
                SELECT
                    id,
                    name,
                    type,
                    target_id
                FROM trigger
                WHERE type = ?
                  AND target_id = ?
                  AND enabled = 1
                ORDER BY id
                """;

        List<Trigger> triggers =
                new ArrayList<>();

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
        ) {
            statement.setString(
                    1,
                    type.name()
            );

            statement.setString(
                    2,
                    targetId
            );

            try (
                    ResultSet result =
                            statement.executeQuery()
            ) {
                while (result.next()) {

                    triggers.add(
                            mapTrigger(
                                    result
                            )
                    );
                }
            }

        } catch (SQLException e) {

            RpgLogger.error(
                    "Impossible de charger les triggers "
                            + type
                            + " / "
                            + targetId
                            + " : "
                            + e.getMessage()
            );
        }

        return triggers;
    }

    /**
     * Retourne tous les triggers actifs.
     *
     * <p>Cette méthode est notamment utilisée
     * par les outils d'administration
     * et l'éditeur.
     *
     * @return tous les triggers actifs
     */
    public List<Trigger> findAll() {

        String sql = """
                SELECT
                    id,
                    name,
                    type,
                    target_id
                FROM trigger
                WHERE enabled = 1
                ORDER BY id
                """;

        List<Trigger> triggers =
                new ArrayList<>();

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        );

                ResultSet result =
                        statement.executeQuery()
        ) {
            while (result.next()) {

                triggers.add(
                        mapTrigger(
                                result
                        )
                );
            }

        } catch (SQLException e) {

            RpgLogger.error(
                    "Impossible de charger les triggers : "
                            + e.getMessage()
            );
        }

        return triggers;
    }

    /**
     * Crée un trigger.
     *
     * <p>Les actions et conditions sont créées
     * séparément par leurs repositories.
     *
     * @param name nom du trigger
     * @param type type du trigger
     * @param targetId cible du trigger
     *
     * @return identifiant généré,
     *         ou vide en cas d'échec
     */
    public OptionalInt create(
            String name,
            TriggerType type,
            String targetId
    ) {
        String sql = """
                INSERT INTO trigger (
                    name,
                    type,
                    target_id,
                    enabled
                )
                VALUES (?, ?, ?, 1)
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {
            statement.setString(
                    1,
                    name
            );

            statement.setString(
                    2,
                    type.name()
            );

            statement.setString(
                    3,
                    targetId
            );

            if (statement.executeUpdate()
                    != 1) {

                return OptionalInt.empty();
            }

            try (
                    ResultSet keys =
                            statement.getGeneratedKeys()
            ) {
                if (!keys.next()) {

                    RpgLogger.error(
                            "Trigger créé mais aucun ID généré : "
                                    + name
                    );

                    return OptionalInt.empty();
                }

                return OptionalInt.of(
                        keys.getInt(
                                1
                        )
                );
            }

        } catch (SQLException e) {

            RpgLogger.error(
                    "Impossible de créer le trigger "
                            + name
                            + " : "
                            + e.getMessage()
            );

            return OptionalInt.empty();
        }
    }

    /**
     * Modifie le type et la cible de plusieurs triggers.
     *
     * <p>L'opération est transactionnelle afin que les
     * variantes d'un même scénario restent synchronisées.
     */
    public boolean updateTargets(
            List<Integer> triggerIds,
            TriggerType type,
            String targetId
    ) {
        if (triggerIds == null
                || triggerIds.isEmpty()
                || type == null
                || targetId == null
                || targetId.isBlank()) {

            return false;
        }

        String sql = """
                UPDATE trigger
                SET type = ?,
                    target_id = ?
                WHERE id = ?
                """;

        boolean previousAutoCommit;

        try {
            previousAutoCommit =
                    connection.getAutoCommit();

        } catch (SQLException e) {

            RpgLogger.error(
                    "Impossible de lire l'état transactionnel : "
                            + e.getMessage()
            );

            return false;
        }

        try {
            connection.setAutoCommit(
                    false
            );

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    sql
                            )
            ) {
                for (int triggerId :
                        triggerIds) {

                    statement.setString(
                            1,
                            type.name()
                    );

                    statement.setString(
                            2,
                            targetId
                    );

                    statement.setInt(
                            3,
                            triggerId
                    );

                    statement.addBatch();
                }

                int[] results =
                        statement.executeBatch();

                for (int result :
                        results) {

                    if (result != 1
                            && result != Statement.SUCCESS_NO_INFO) {

                        connection.rollback();

                        return false;
                    }
                }
            }

            connection.commit();

            return true;

        } catch (SQLException e) {

            try {
                connection.rollback();

            } catch (SQLException rollbackException) {

                RpgLogger.error(
                        "Impossible d'annuler la modification partielle : "
                                + rollbackException.getMessage()
                );
            }

            RpgLogger.error(
                    "Impossible de modifier les triggers : "
                            + e.getMessage()
            );

            return false;

        } finally {

            try {
                connection.setAutoCommit(
                        previousAutoCommit
                );

            } catch (SQLException e) {

                RpgLogger.error(
                        "Impossible de restaurer l'état transactionnel : "
                                + e.getMessage()
                );
            }
        }
    }

    /**
     * Supprime transactionnellement toutes les actions correspondant
     * exactement à une référence provider/expression.
     *
     * <p>Un trigger touché qui ne possède plus aucune action est supprimé ;
     * ses conditions éventuelles suivent la cascade SQLite. Un trigger qui
     * conserve d'autres actions reste intact.
     */
    public boolean removeActionReferences(
            String provider,
            String expression
    ) {
        if (provider == null
                || provider.isBlank()
                || expression == null) {
            return false;
        }

        String findSql = """
                SELECT DISTINCT trigger_id
                FROM action
                WHERE provider = ?
                  AND expression = ?
                ORDER BY trigger_id
                """;

        String deleteActionsSql = """
                DELETE FROM action
                WHERE provider = ?
                  AND expression = ?
                """;

        String deleteEmptyTriggerSql = """
                DELETE FROM trigger
                WHERE id = ?
                  AND NOT EXISTS (
                      SELECT 1
                      FROM action
                      WHERE action.trigger_id = trigger.id
                  )
                """;

        boolean previousAutoCommit;

        try {
            previousAutoCommit = connection.getAutoCommit();
        } catch (SQLException e) {
            RpgLogger.error(
                    "Impossible de lire l'état transactionnel : "
                            + e.getMessage()
            );
            return false;
        }

        try {
            connection.setAutoCommit(false);

            List<Integer> affectedTriggerIds = new ArrayList<>();

            try (PreparedStatement statement = connection.prepareStatement(findSql)) {
                statement.setString(1, provider);
                statement.setString(2, expression);

                try (ResultSet result = statement.executeQuery()) {
                    while (result.next()) {
                        affectedTriggerIds.add(result.getInt("trigger_id"));
                    }
                }
            }

            try (PreparedStatement statement = connection.prepareStatement(deleteActionsSql)) {
                statement.setString(1, provider);
                statement.setString(2, expression);
                statement.executeUpdate();
            }

            try (PreparedStatement statement = connection.prepareStatement(deleteEmptyTriggerSql)) {
                for (int triggerId : affectedTriggerIds) {
                    statement.setInt(1, triggerId);
                    statement.addBatch();
                }

                if (!affectedTriggerIds.isEmpty()) {
                    statement.executeBatch();
                }
            }

            connection.commit();
            return true;

        } catch (SQLException e) {
            try {
                connection.rollback();
            } catch (SQLException rollbackException) {
                RpgLogger.error(
                        "Impossible d'annuler la suppression des références d'action : "
                                + rollbackException.getMessage()
                );
            }

            RpgLogger.error(
                    "Impossible de supprimer les références d'action "
                            + provider
                            + " / "
                            + expression
                            + " : "
                            + e.getMessage()
            );
            return false;

        } finally {
            try {
                connection.setAutoCommit(previousAutoCommit);
            } catch (SQLException e) {
                RpgLogger.error(
                        "Impossible de restaurer l'état transactionnel : "
                                + e.getMessage()
                );
            }
        }
    }

    /**
     * Supprime un trigger.
     *
     * <p>Les actions et conditions associées
     * sont supprimées par les cascades SQLite.
     *
     * @param triggerId identifiant du trigger
     *
     * @return true si un trigger a été supprimé
     */
    public boolean delete(
            int triggerId
    ) {
        String sql = """
                DELETE FROM trigger
                WHERE id = ?
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

            return statement.executeUpdate()
                    == 1;

        } catch (SQLException e) {

            RpgLogger.error(
                    "Impossible de supprimer le trigger "
                            + triggerId
                            + " : "
                            + e.getMessage()
            );

            return false;
        }
    }

    /**
     * Reconstruit un trigger complet
     * à partir d'une ligne SQL.
     *
     * <p>Les actions et conditions associées
     * sont chargées par leurs repositories.
     */
    private Trigger mapTrigger(
            ResultSet result
    ) throws SQLException {

        int triggerId =
                result.getInt(
                        "id"
                );

        List<Action> actions =
                actionRepository.findByTriggerId(
                        triggerId
                );

        List<Condition> conditions =
                conditionRepository.findByTriggerId(
                        triggerId
                );

        return new Trigger(
                triggerId,
                result.getString(
                        "name"
                ),
                result.getString(
                        "target_id"
                ),
                TriggerType.valueOf(
                        result.getString(
                                "type"
                        )
                ),
                conditions,
                actions
        );
    }
}