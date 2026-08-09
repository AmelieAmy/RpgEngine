package fr.doryamy.rpgengine.dialogue;

import fr.doryamy.rpgengine.model.Action;
import fr.doryamy.rpgengine.model.Condition;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.sql.*;
import java.util.*;

/**
 * Repository chargé de la persistance
 * des dialogues de RPGEngine.
 *
 * Un dialogue est manipulé comme un agrégat complet :
 *
 * - métadonnées ;
 * - nodes ;
 * - transitions ;
 * - conditions des transitions ;
 * - actions des transitions.
 *
 * Le repository ne contient aucune logique métier
 * d'édition ou d'exécution.
 */
public final class DialogueRepository {

    private final Connection connection;

    /**
     * Construit le repository.
     *
     * @param connection connexion SQLite
     */
    public DialogueRepository(
            Connection connection
    ) {
        this.connection = connection;
    }

    /**
     * Recherche un dialogue à partir
     * de sa clé métier.
     *
     * @param key clé du dialogue
     * @return dialogue complet s'il existe
     */
    public Optional<Dialogue> findByKey(
            String key
    ) {
        String sql = """
                SELECT
                    d.id,
                    d.key,
                    d.name,
                    start_node.key AS start_node_key
                FROM dialogue d
                LEFT JOIN dialogue_node start_node
                    ON start_node.id = d.start_node_id
                WHERE d.key = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(
                    1,
                    key
            );

            try (
                    ResultSet result =
                            statement.executeQuery()
            ) {
                if (!result.next()) {
                    return Optional.empty();
                }

                long dialogueId =
                        result.getLong("id");

                String dialogueKey =
                        result.getString("key");

                String dialogueName =
                        result.getString("name");

                String startNodeKey =
                        result.getString("start_node_key");

                List<DialogueNode> nodes =
                        loadNodes(
                                dialogueId
                        );

                List<DialogueTransition> transitions =
                        loadTransitions(
                                dialogueId
                        );

                return Optional.of(
                        new Dialogue(
                                dialogueKey,
                                dialogueName,
                                startNodeKey,
                                nodes,
                                transitions
                        )
                );
            }

        } catch (
                SQLException
                | IllegalArgumentException e
        ) {
            RpgLogger.error(
                    "Impossible de charger le dialogue '"
                            + key
                            + "' : "
                            + e.getMessage()
            );

            return Optional.empty();
        }
    }

    /**
     * Retourne tous les dialogues disponibles.
     *
     * @return liste des dialogues
     */
    public List<Dialogue> findAll() {

        List<Dialogue> dialogues =
                new ArrayList<>();

        String sql = """
                SELECT key
                FROM dialogue
                ORDER BY key
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet result =
                        statement.executeQuery()
        ) {
            while (result.next()) {

                findByKey(
                        result.getString("key")
                ).ifPresent(
                        dialogues::add
                );
            }

        } catch (SQLException e) {

            RpgLogger.error(
                    "Impossible de charger la liste des dialogues : "
                            + e.getMessage()
            );
        }

        return dialogues;
    }

    /**
     * Vérifie l'existence d'un dialogue.
     *
     * @param key clé métier
     * @return true si le dialogue existe
     */
    public boolean exists(
            String key
    ) {
        String sql = """
                SELECT 1
                FROM dialogue
                WHERE key = ?
                LIMIT 1
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(
                    1,
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
                    "Impossible de vérifier l'existence du dialogue '"
                            + key
                            + "' : "
                            + e.getMessage()
            );

            return false;
        }
    }

    /**
     * Crée un dialogue vide.
     *
     * Aucun node de départ n'est défini
     * lors de la création.
     *
     * @param key clé métier
     * @param name nom lisible
     * @return true si la création a réussi
     */
    public boolean create(
            String key,
            String name
    ) {
        String sql = """
                INSERT INTO dialogue (
                    key,
                    name,
                    start_node_id
                )
                VALUES (?, ?, NULL)
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(
                    1,
                    key
            );

            statement.setString(
                    2,
                    name
            );

            return statement.executeUpdate() == 1;

        } catch (SQLException e) {

            RpgLogger.error(
                    "Impossible de créer le dialogue '"
                            + key
                            + "' : "
                            + e.getMessage()
            );

            return false;
        }
    }

    /**
     * Supprime un dialogue.
     *
     * Les nodes, transitions, conditions
     * et actions associés sont supprimés
     * automatiquement par les cascades SQLite.
     *
     * @param key clé métier
     * @return true si un dialogue a été supprimé
     */
    public boolean delete(
            String key
    ) {
        String sql = """
                DELETE FROM dialogue
                WHERE key = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(
                    1,
                    key
            );

            return statement.executeUpdate() == 1;

        } catch (SQLException e) {

            RpgLogger.error(
                    "Impossible de supprimer le dialogue '"
                            + key
                            + "' : "
                            + e.getMessage()
            );

            return false;
        }
    }

    /**
     * Sauvegarde l'état complet d'un dialogue existant.
     *
     * Les données composant le graphe sont réécrites
     * dans une transaction unique.
     *
     * @param dialogue dialogue à sauvegarder
     * @return true si la sauvegarde a réussi
     */
    public boolean save(
            Dialogue dialogue
    ) {
        boolean previousAutoCommit;

        try {
            previousAutoCommit =
                    connection.getAutoCommit();

        } catch (SQLException e) {

            RpgLogger.error(
                    "Impossible de lire l'état de la connexion avant "
                            + "la sauvegarde du dialogue '"
                            + dialogue.getKey()
                            + "' : "
                            + e.getMessage()
            );

            return false;
        }

        try {
            connection.setAutoCommit(false);

            long dialogueId =
                    findDialogueId(
                            dialogue.getKey()
                    );

            if (dialogueId == -1) {

                RpgLogger.error(
                        "Impossible de sauvegarder le dialogue '"
                                + dialogue.getKey()
                                + "' : dialogue introuvable."
                );

                connection.rollback();

                return false;
            }

            /*
             * Le start node doit être détaché avant
             * de supprimer l'ancien graphe.
             */
            clearStartNode(
                    dialogueId
            );

            deleteGraph(
                    dialogueId
            );

            updateDialogueMetadata(
                    dialogueId,
                    dialogue.getName()
            );

            Map<String, Long> nodeIds =
                    insertNodes(
                            dialogueId,
                            dialogue.getNodes()
                    );

            insertTransitions(
                    dialogueId,
                    dialogue.getTransitions(),
                    nodeIds
            );

            updateStartNode(
                    dialogueId,
                    dialogue.getStartNodeKey(),
                    nodeIds
            );

            connection.commit();

            return true;

        } catch (SQLException e) {

            try {
                connection.rollback();

            } catch (SQLException rollbackException) {

                RpgLogger.error(
                        "Impossible d'annuler la sauvegarde du dialogue '"
                                + dialogue.getKey()
                                + "' : "
                                + rollbackException.getMessage()
                );
            }

            RpgLogger.error(
                    "Impossible de sauvegarder le dialogue '"
                            + dialogue.getKey()
                            + "' : "
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
                        "Impossible de restaurer l'état autoCommit "
                                + "après la sauvegarde du dialogue '"
                                + dialogue.getKey()
                                + "' : "
                                + e.getMessage()
                );
            }
        }
    }

    /**
     * Charge les nodes d'un dialogue.
     */
    private List<DialogueNode> loadNodes(
            long dialogueId
    ) throws SQLException {

        List<DialogueNode> nodes =
                new ArrayList<>();

        String sql = """
                SELECT
                    key,
                    text
                FROM dialogue_node
                WHERE dialogue_id = ?
                ORDER BY id
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setLong(
                    1,
                    dialogueId
            );

            try (
                    ResultSet result =
                            statement.executeQuery()
            ) {
                while (result.next()) {

                    nodes.add(
                            new DialogueNode(
                                    result.getString("key"),
                                    result.getString("text")
                            )
                    );
                }
            }
        }

        return nodes;
    }

    /**
     * Charge les transitions d'un dialogue
     * ainsi que leurs conditions et actions.
     */
    private List<DialogueTransition> loadTransitions(
            long dialogueId
    ) throws SQLException {

        List<DialogueTransition> transitions =
                new ArrayList<>();

        String sql = """
                SELECT
                    dt.id,
                    dt.key AS transition_key,
                    source.key AS source_node_key,
                    target.key AS target_node_key,
                    dt.type,
                    dt.label,
                    dt.position
                FROM dialogue_transition dt
                JOIN dialogue_node source
                    ON source.id = dt.source_node_id
                LEFT JOIN dialogue_node target
                    ON target.id = dt.target_node_id
                WHERE dt.dialogue_id = ?
                ORDER BY
                    source.key,
                    dt.position
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setLong(
                    1,
                    dialogueId
            );

            try (
                    ResultSet result =
                            statement.executeQuery()
            ) {
                while (result.next()) {

                    long transitionId =
                            result.getLong("id");

                    DialogueTransitionType type =
                            DialogueTransitionType.valueOf(
                                    result.getString("type")
                            );

                    List<Condition> conditions =
                            loadTransitionConditions(
                                    transitionId
                            );

                    List<Action> actions =
                            loadTransitionActions(
                                    transitionId
                            );

                    transitions.add(
                            new DialogueTransition(
                                    result.getString(
                                            "transition_key"
                                    ),
                                    result.getString(
                                            "source_node_key"
                                    ),
                                    result.getString(
                                            "target_node_key"
                                    ),
                                    type,
                                    result.getString("label"),
                                    result.getInt("position"),
                                    conditions,
                                    actions
                            )
                    );
                }
            }
        }

        return transitions;
    }

    /**
     * Charge les conditions d'une transition.
     */
    private List<Condition> loadTransitionConditions(
            long transitionId
    ) throws SQLException {

        List<Condition> conditions =
                new ArrayList<>();

        String sql = """
                SELECT
                    provider,
                    expression
                FROM dialogue_transition_condition
                WHERE transition_id = ?
                ORDER BY id
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setLong(
                    1,
                    transitionId
            );

            try (
                    ResultSet result =
                            statement.executeQuery()
            ) {
                while (result.next()) {

                    conditions.add(
                            new Condition(
                                    result.getString("provider"),
                                    result.getString("expression")
                            )
                    );
                }
            }
        }

        return conditions;
    }

    /**
     * Charge les actions d'une transition
     * dans leur ordre d'exécution.
     */
    private List<Action> loadTransitionActions(
            long transitionId
    ) throws SQLException {

        List<Action> actions =
                new ArrayList<>();

        String sql = """
                SELECT
                    provider,
                    expression,
                    position
                FROM dialogue_transition_action
                WHERE transition_id = ?
                ORDER BY position
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setLong(
                    1,
                    transitionId
            );

            try (
                    ResultSet result =
                            statement.executeQuery()
            ) {
                while (result.next()) {

                    actions.add(
                            new Action(
                                    result.getString("provider"),
                                    result.getString("expression"),
                                    result.getInt("position")
                            )
                    );
                }
            }
        }

        return actions;
    }

    /**
     * Recherche l'identifiant interne
     * d'un dialogue.
     */
    private long findDialogueId(
            String key
    ) throws SQLException {

        String sql = """
                SELECT id
                FROM dialogue
                WHERE key = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(
                    1,
                    key
            );

            try (
                    ResultSet result =
                            statement.executeQuery()
            ) {
                if (result.next()) {
                    return result.getLong("id");
                }
            }
        }

        return -1;
    }

    /**
     * Retire temporairement le point d'entrée
     * avant la reconstruction du graphe.
     */
    private void clearStartNode(
            long dialogueId
    ) throws SQLException {

        String sql = """
                UPDATE dialogue
                SET start_node_id = NULL
                WHERE id = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setLong(
                    1,
                    dialogueId
            );

            statement.executeUpdate();
        }
    }

    /**
     * Supprime l'ancien graphe.
     */
    private void deleteGraph(
            long dialogueId
    ) throws SQLException {

        String deleteTransitions = """
                DELETE FROM dialogue_transition
                WHERE dialogue_id = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                deleteTransitions
                        )
        ) {
            statement.setLong(
                    1,
                    dialogueId
            );

            statement.executeUpdate();
        }

        String deleteNodes = """
                DELETE FROM dialogue_node
                WHERE dialogue_id = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                deleteNodes
                        )
        ) {
            statement.setLong(
                    1,
                    dialogueId
            );

            statement.executeUpdate();
        }
    }

    /**
     * Met à jour les métadonnées
     * du dialogue.
     */
    private void updateDialogueMetadata(
            long dialogueId,
            String name
    ) throws SQLException {

        String sql = """
                UPDATE dialogue
                SET name = ?
                WHERE id = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(
                    1,
                    name
            );

            statement.setLong(
                    2,
                    dialogueId
            );

            statement.executeUpdate();
        }
    }

    /**
     * Insère tous les nodes et retourne
     * leur identifiant SQL par clé métier.
     */
    private Map<String, Long> insertNodes(
            long dialogueId,
            List<DialogueNode> nodes
    ) throws SQLException {

        Map<String, Long> nodeIds =
                new HashMap<>();

        String sql = """
                INSERT INTO dialogue_node (
                    dialogue_id,
                    key,
                    text
                )
                VALUES (?, ?, ?)
                """;

        for (DialogueNode node : nodes) {

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    sql,
                                    Statement.RETURN_GENERATED_KEYS
                            )
            ) {
                statement.setLong(
                        1,
                        dialogueId
                );

                statement.setString(
                        2,
                        node.getKey()
                );

                statement.setString(
                        3,
                        node.getText()
                );

                statement.executeUpdate();

                try (
                        ResultSet generatedKeys =
                                statement.getGeneratedKeys()
                ) {
                    if (!generatedKeys.next()) {

                        throw new SQLException(
                                "Impossible de récupérer l'id du node '"
                                        + node.getKey()
                                        + "'."
                        );
                    }

                    nodeIds.put(
                            node.getKey(),
                            generatedKeys.getLong(1)
                    );
                }
            }
        }

        return nodeIds;
    }

    /**
     * Insère toutes les transitions du graphe.
     */
    private void insertTransitions(
            long dialogueId,
            List<DialogueTransition> transitions,
            Map<String, Long> nodeIds
    ) throws SQLException {

        String sql = """
                INSERT INTO dialogue_transition (
                    dialogue_id,
                    key,
                    source_node_id,
                    target_node_id,
                    type,
                    label,
                    position
                )
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        for (DialogueTransition transition :
                transitions) {

            Long sourceId =
                    nodeIds.get(
                            transition.getSourceNodeKey()
                    );

            if (sourceId == null) {
                throw new SQLException(
                        "Node source introuvable : "
                                + transition.getSourceNodeKey()
                );
            }

            Long targetId = null;

            if (transition.getTargetNodeKey()
                    != null) {

                targetId =
                        nodeIds.get(
                                transition.getTargetNodeKey()
                        );

                if (targetId == null) {
                    throw new SQLException(
                            "Node cible introuvable : "
                                    + transition.getTargetNodeKey()
                    );
                }
            }

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    sql,
                                    Statement.RETURN_GENERATED_KEYS
                            )
            ) {
                statement.setLong(
                        1,
                        dialogueId
                );

                statement.setString(
                        2,
                        transition.getKey()
                );

                statement.setLong(
                        3,
                        sourceId
                );

                if (targetId == null) {
                    statement.setNull(
                            4,
                            java.sql.Types.INTEGER
                    );
                } else {
                    statement.setLong(
                            4,
                            targetId
                    );
                }

                statement.setString(
                        5,
                        transition.getType()
                                .name()
                );

                statement.setString(
                        6,
                        transition.getLabel()
                );

                statement.setInt(
                        7,
                        transition.getPosition()
                );

                statement.executeUpdate();

                try (
                        ResultSet generatedKeys =
                                statement.getGeneratedKeys()
                ) {
                    if (!generatedKeys.next()) {
                        throw new SQLException(
                                "Impossible de récupérer l'id d'une transition."
                        );
                    }

                    long transitionId =
                            generatedKeys.getLong(1);

                    insertTransitionConditions(
                            transitionId,
                            transition.getConditions()
                    );

                    insertTransitionActions(
                            transitionId,
                            transition.getActions()
                    );
                }
            }
        }
    }

    private void insertTransitionConditions(
            long transitionId,
            List<Condition> conditions
    ) throws SQLException {

        String sql = """
                INSERT INTO dialogue_transition_condition (
                    transition_id,
                    provider,
                    expression
                )
                VALUES (?, ?, ?)
                """;

        for (Condition condition : conditions) {

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(sql)
            ) {
                statement.setLong(
                        1,
                        transitionId
                );

                statement.setString(
                        2,
                        condition.getProvider()
                );

                statement.setString(
                        3,
                        condition.getExpression()
                );

                statement.executeUpdate();
            }
        }
    }

    private void insertTransitionActions(
            long transitionId,
            List<Action> actions
    ) throws SQLException {

        String sql = """
                INSERT INTO dialogue_transition_action (
                    transition_id,
                    provider,
                    expression,
                    position
                )
                VALUES (?, ?, ?, ?)
                """;

        for (Action action : actions) {

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(sql)
            ) {
                statement.setLong(
                        1,
                        transitionId
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

                statement.executeUpdate();
            }
        }
    }

    /**
     * Définit le node de départ
     * après reconstruction du graphe.
     */
    private void updateStartNode(
            long dialogueId,
            String startNodeKey,
            Map<String, Long> nodeIds
    ) throws SQLException {

        if (startNodeKey == null) {
            return;
        }

        Long startNodeId =
                nodeIds.get(
                        startNodeKey
                );

        if (startNodeId == null) {
            throw new SQLException(
                    "Node de départ introuvable : "
                            + startNodeKey
            );
        }

        String sql = """
                UPDATE dialogue
                SET start_node_id = ?
                WHERE id = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setLong(
                    1,
                    startNodeId
            );

            statement.setLong(
                    2,
                    dialogueId
            );

            statement.executeUpdate();
        }
    }
}