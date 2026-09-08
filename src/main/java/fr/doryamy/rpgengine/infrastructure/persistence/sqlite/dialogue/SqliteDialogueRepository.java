package fr.doryamy.rpgengine.infrastructure.persistence.sqlite.dialogue;

import fr.doryamy.rpgengine.dialogue.*;
import fr.doryamy.rpgengine.model.Action;
import fr.doryamy.rpgengine.model.Condition;

import java.sql.*;
import java.util.*;

/**
 * Implémentation SQLite de DialogueRepository.
 *
 * <p>Le repository persiste et reconstruit
 * des agrégats Dialogue complets.
 *
 * <p>Le stockage reflète directement
 * le nouveau modèle de graphe :
 *
 * <pre>
 * dialogue
 *   |
 *   +-- dialogue_graph_element
 *   |       |
 *   |       +-- dialogue_condition
 *   |       +-- dialogue_action
 *   |
 *   +-- dialogue_graph_link
 * </pre>
 *
 * <p>Le repository ne contient aucune logique métier
 * de progression ou de transformation du graphe.
 */
public final class SqliteDialogueRepository
        implements DialogueRepository {

    private final Connection connection;

    public SqliteDialogueRepository(
            Connection connection
    ) {

        this.connection =
                Objects.requireNonNull(
                        connection,
                        "connection"
                );
    }

    @Override
    public Optional<Dialogue> findByKey(
            DialogueKey key
    ) {

        Objects.requireNonNull(
                key,
                "key"
        );

        String sql = """
                SELECT
                    id,
                    key,
                    name
                FROM dialogue
                WHERE key = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
        ) {

            statement.setString(
                    1,
                    key.value()
            );

            try (
                    ResultSet result =
                            statement.executeQuery()
            ) {

                if (!result.next()) {
                    return Optional.empty();
                }

                return Optional.of(
                        loadDialogue(
                                result
                        )
                );
            }

        } catch (SQLException e) {

            throw new DialoguePersistenceException(
                    "Impossible de charger le dialogue "
                            + key
                            + ".",
                    e
            );
        }
    }

    @Override
    public List<Dialogue> findAll() {

        String sql = """
                SELECT
                    id,
                    key,
                    name
                FROM dialogue
                ORDER BY id
                """;

        List<Dialogue> dialogues =
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

                dialogues.add(
                        loadDialogue(
                                result
                        )
                );
            }

            return List.copyOf(
                    dialogues
            );

        } catch (SQLException e) {

            throw new DialoguePersistenceException(
                    "Impossible de charger les dialogues.",
                    e
            );
        }
    }

    @Override
    public void insert(
            Dialogue dialogue
    ) {

        Objects.requireNonNull(
                dialogue,
                "dialogue"
        );

        executeTransaction(() -> {

            long dialogueId =
                    insertDialogueRow(
                            dialogue
                    );

            insertGraph(
                    dialogueId,
                    dialogue
            );
        });
    }

    @Override
    public void update(
            Dialogue dialogue
    ) {

        Objects.requireNonNull(
                dialogue,
                "dialogue"
        );

        executeTransaction(() -> {

            long dialogueId =
                    requireDialogueId(
                            dialogue.key()
                    );

            updateDialogueRow(
                    dialogueId,
                    dialogue
            );

            /*
             * Le graphe persistant est remplacé
             * comme une seule unité.
             *
             * La suppression des éléments entraîne
             * automatiquement la suppression des liens,
             * conditions et actions grâce aux FK
             * ON DELETE CASCADE.
             */
            deleteGraphElements(
                    dialogueId
            );

            insertGraph(
                    dialogueId,
                    dialogue
            );
        });
    }

    @Override
    public void delete(
            DialogueKey key
    ) {

        Objects.requireNonNull(
                key,
                "key"
        );

        executeTransaction(() -> {

            String sql = """
                    DELETE FROM dialogue
                    WHERE key = ?
                    """;

            try (
                    PreparedStatement statement =
                            connection.prepareStatement(
                                    sql
                            )
            ) {

                statement.setString(
                        1,
                        key.value()
                );

                int affected =
                        statement.executeUpdate();

                if (affected != 1) {

                    throw new DialogueNotFoundException(
                            key
                    );
                }
            }
        });
    }

    /*
     * ====================================================
     * LECTURE
     * ====================================================
     */

    private Dialogue loadDialogue(
            ResultSet dialogueResult
    ) throws SQLException {

        long dialogueId =
                dialogueResult.getLong(
                        "id"
                );

        DialogueKey dialogueKey =
                new DialogueKey(
                        dialogueResult.getString(
                                "key"
                        )
                );

        String name =
                dialogueResult.getString(
                        "name"
                );

        Map<Long, List<DialogueConditionEntry>>
                conditionsByElement =
                loadConditions(
                        dialogueId
                );

        Map<Long, List<DialogueActionEntry>>
                actionsByElement =
                loadActions(
                        dialogueId
                );

        LoadedElements loaded =
                loadElements(
                        dialogueId,
                        conditionsByElement,
                        actionsByElement
                );

        Set<DialogueLink> links =
                loadLinks(
                        dialogueId,
                        loaded.keysByDatabaseId()
                );

        DialogueGraph graph =
                new DialogueGraph(
                        loaded.elements(),
                        links
                );

        return new Dialogue(
                dialogueKey,
                name,
                loaded.startRules(),
                graph
        );
    }

    private LoadedElements loadElements(
            long dialogueId,
            Map<Long, List<DialogueConditionEntry>>
                    conditionsByElement,
            Map<Long, List<DialogueActionEntry>>
                    actionsByElement
    ) throws SQLException {

        String sql = """
                SELECT
                    id,
                    key,
                    type,
                    speaker,
                    text,
                    position
                FROM dialogue_graph_element
                WHERE dialogue_id = ?
                ORDER BY id
                """;

        List<DialogueElement> elements =
                new ArrayList<>();

        Map<Long, DialogueElementKey>
                keysByDatabaseId =
                new HashMap<>();

        DialogueRules startRules =
                DialogueRules.empty();

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
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

                    long elementId =
                            result.getLong(
                                    "id"
                            );

                    DialogueElementKey key =
                            new DialogueElementKey(
                                    result.getString(
                                            "key"
                                    )
                            );

                    DialogueRules rules =
                            new DialogueRules(
                                    conditionsByElement.getOrDefault(
                                            elementId,
                                            List.of()
                                    ),
                                    actionsByElement.getOrDefault(
                                            elementId,
                                            List.of()
                                    )
                            );

                    String type = result.getString("type");
                    if ("START".equals(type)) {
                        startRules = rules;
                    }

                    DialogueElement element =
                            buildElement(
                                    result,
                                    key,
                                    rules
                            );

                    elements.add(
                            element
                    );

                    keysByDatabaseId.put(
                            elementId,
                            key
                    );
                }
            }
        }

        return new LoadedElements(
                List.copyOf(
                        elements
                ),
                Map.copyOf(
                        keysByDatabaseId
                ),
                startRules
        );
    }

    private DialogueElement buildElement(
            ResultSet result,
            DialogueElementKey key,
            DialogueRules rules
    ) throws SQLException {

        String type =
                result.getString(
                        "type"
                );

        return switch (type) {

            case "START" ->
                    new DialogueStart(
                            key
                    );

            case "REPLY" -> {

                String speakerValue =
                        result.getString(
                                "speaker"
                        );

                DialogueReplySpeaker speaker;

                try {

                    speaker =
                            DialogueReplySpeaker.valueOf(
                                    speakerValue
                            );

                } catch (
                        IllegalArgumentException
                        | NullPointerException e
                ) {

                    throw new SQLException(
                            "Speaker invalide pour l'élément "
                                    + key
                                    + " : "
                                    + speakerValue,
                            e
                    );
                }

                yield new DialogueReply(
                        key,
                        speaker,
                        result.getString(
                                "text"
                        ),
                        rules
                );
            }

            case "BRANCH" ->
                    new DialogueBranch(
                            key
                    );

            case "CHOICE" ->
                    new DialogueChoice(
                            key,
                            result.getString(
                                    "text"
                            ),
                            result.getInt(
                                    "position"
                            ),
                            rules
                    );

            case "END" ->
                    new DialogueEnd(
                            key
                    );

            default ->
                    throw new SQLException(
                            "Type d'élément de dialogue inconnu : "
                                    + type
                    );
        };
    }

    private Map<Long, List<DialogueConditionEntry>>
    loadConditions(
            long dialogueId
    ) throws SQLException {

        String sql = """
                SELECT
                    c.element_id,
                    c.key,
                    c.provider,
                    c.expression
                FROM dialogue_condition c
                JOIN dialogue_graph_element e
                    ON e.id = c.element_id
                WHERE e.dialogue_id = ?
                ORDER BY
                    c.element_id,
                    c.id
                """;

        Map<Long, List<DialogueConditionEntry>>
                resultMap =
                new HashMap<>();

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
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

                    long elementId =
                            result.getLong(
                                    "element_id"
                            );

                    DialogueConditionEntry entry =
                            new DialogueConditionEntry(
                                    new DialogueRuleKey(
                                            result.getString(
                                                    "key"
                                            )
                                    ),
                                    new Condition(
                                            result.getString(
                                                    "provider"
                                            ),
                                            result.getString(
                                                    "expression"
                                            )
                                    )
                            );

                    resultMap
                            .computeIfAbsent(
                                    elementId,
                                    ignored ->
                                            new ArrayList<>()
                            )
                            .add(
                                    entry
                            );
                }
            }
        }

        return resultMap;
    }

    private Map<Long, List<DialogueActionEntry>>
    loadActions(
            long dialogueId
    ) throws SQLException {

        String sql = """
                SELECT
                    a.element_id,
                    a.key,
                    a.provider,
                    a.expression,
                    a.position
                FROM dialogue_action a
                JOIN dialogue_graph_element e
                    ON e.id = a.element_id
                WHERE e.dialogue_id = ?
                ORDER BY
                    a.element_id,
                    a.position
                """;

        Map<Long, List<DialogueActionEntry>>
                resultMap =
                new HashMap<>();

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
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

                    long elementId =
                            result.getLong(
                                    "element_id"
                            );

                    DialogueActionEntry entry =
                            new DialogueActionEntry(
                                    new DialogueRuleKey(
                                            result.getString(
                                                    "key"
                                            )
                                    ),
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

                    resultMap
                            .computeIfAbsent(
                                    elementId,
                                    ignored ->
                                            new ArrayList<>()
                            )
                            .add(
                                    entry
                            );
                }
            }
        }

        return resultMap;
    }

    private Set<DialogueLink> loadLinks(
            long dialogueId,
            Map<Long, DialogueElementKey>
                    keysByDatabaseId
    ) throws SQLException {

        String sql = """
                SELECT
                    source_element_id,
                    target_element_id
                FROM dialogue_graph_link
                WHERE dialogue_id = ?
                ORDER BY
                    source_element_id,
                    target_element_id
                """;

        Set<DialogueLink> links =
                new LinkedHashSet<>();

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
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

                    long sourceId =
                            result.getLong(
                                    "source_element_id"
                            );

                    long targetId =
                            result.getLong(
                                    "target_element_id"
                            );

                    DialogueElementKey source =
                            keysByDatabaseId.get(
                                    sourceId
                            );

                    DialogueElementKey target =
                            keysByDatabaseId.get(
                                    targetId
                            );

                    if (source == null
                            || target == null) {

                        throw new SQLException(
                                "Une liaison du dialogue "
                                        + dialogueId
                                        + " référence un élément absent."
                        );
                    }

                    links.add(
                            new DialogueLink(
                                    source,
                                    target
                            )
                    );
                }
            }
        }

        return links;
    }

    /*
     * ====================================================
     * ECRITURE
     * ====================================================
     */

    private long insertDialogueRow(
            Dialogue dialogue
    ) throws SQLException {

        String sql = """
                INSERT INTO dialogue (
                    key,
                    name
                )
                VALUES (?, ?)
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
                    dialogue.key()
                            .value()
            );

            statement.setString(
                    2,
                    dialogue.name()
            );

            statement.executeUpdate();

            try (
                    ResultSet keys =
                            statement.getGeneratedKeys()
            ) {

                if (!keys.next()) {

                    throw new SQLException(
                            "SQLite n'a pas retourné "
                                    + "l'identifiant du dialogue créé."
                    );
                }

                return keys.getLong(
                        1
                );
            }
        }
    }

    private void updateDialogueRow(
            long dialogueId,
            Dialogue dialogue
    ) throws SQLException {

        String sql = """
                UPDATE dialogue
                SET name = ?
                WHERE id = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
        ) {

            statement.setString(
                    1,
                    dialogue.name()
            );

            statement.setLong(
                    2,
                    dialogueId
            );

            int affected =
                    statement.executeUpdate();

            if (affected != 1) {

                throw new SQLException(
                        "Le dialogue "
                                + dialogue.key()
                                + " n'a pas pu être mis à jour."
                );
            }
        }
    }

    private void insertGraph(
            long dialogueId,
            Dialogue dialogue
    ) throws SQLException {

        DialogueGraph graph = dialogue.graph();

        Map<DialogueElementKey, Long>
                idsByKey =
                insertElements(
                        dialogueId,
                        graph
                );

        insertLinks(
                dialogueId,
                graph,
                idsByKey
        );

        insertConditions(
                graph,
                dialogue.rules(),
                idsByKey
        );

        insertActions(
                graph,
                dialogue.rules(),
                idsByKey
        );
    }

    private Map<DialogueElementKey, Long>
    insertElements(
            long dialogueId,
            DialogueGraph graph
    ) throws SQLException {

        String sql = """
                INSERT INTO dialogue_graph_element (
                    dialogue_id,
                    key,
                    type,
                    speaker,
                    text,
                    position
                )
                VALUES (?, ?, ?, ?, ?, ?)
                """;

        Map<DialogueElementKey, Long>
                idsByKey =
                new HashMap<>();

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql,
                                Statement.RETURN_GENERATED_KEYS
                        )
        ) {

            for (DialogueElement element :
                    graph.elements()
                            .values()) {

                statement.setLong(
                        1,
                        dialogueId
                );

                statement.setString(
                        2,
                        element.key()
                                .value()
                );

                bindElement(
                        statement,
                        element
                );

                statement.executeUpdate();

                try (
                        ResultSet keys =
                                statement.getGeneratedKeys()
                ) {

                    if (!keys.next()) {

                        throw new SQLException(
                                "SQLite n'a pas retourné "
                                        + "l'identifiant de l'élément "
                                        + element.key()
                                        + "."
                        );
                    }

                    idsByKey.put(
                            element.key(),
                            keys.getLong(
                                    1
                            )
                    );
                }
            }
        }

        return idsByKey;
    }

    private void bindElement(
            PreparedStatement statement,
            DialogueElement element
    ) throws SQLException {

        /*
         * Colonnes :
         *
         * 3 = type
         * 4 = speaker
         * 5 = text
         * 6 = position
         */

        if (element instanceof DialogueStart) {

            statement.setString(
                    3,
                    "START"
            );

            statement.setNull(
                    4,
                    Types.VARCHAR
            );

            statement.setNull(
                    5,
                    Types.VARCHAR
            );

            statement.setNull(
                    6,
                    Types.INTEGER
            );

            return;
        }

        if (element instanceof DialogueReply reply) {

            statement.setString(
                    3,
                    "REPLY"
            );

            statement.setString(
                    4,
                    reply.speaker()
                            .name()
            );

            statement.setString(
                    5,
                    reply.text()
            );

            statement.setNull(
                    6,
                    Types.INTEGER
            );

            return;
        }

        if (element instanceof DialogueBranch) {

            statement.setString(
                    3,
                    "BRANCH"
            );

            statement.setNull(
                    4,
                    Types.VARCHAR
            );

            statement.setNull(
                    5,
                    Types.VARCHAR
            );

            statement.setNull(
                    6,
                    Types.INTEGER
            );

            return;
        }

        if (element instanceof DialogueChoice choice) {

            statement.setString(
                    3,
                    "CHOICE"
            );

            statement.setNull(
                    4,
                    Types.VARCHAR
            );

            statement.setString(
                    5,
                    choice.text()
            );

            statement.setInt(
                    6,
                    choice.position()
            );

            return;
        }

        if (element instanceof DialogueEnd) {

            statement.setString(
                    3,
                    "END"
            );

            statement.setNull(
                    4,
                    Types.VARCHAR
            );

            statement.setNull(
                    5,
                    Types.VARCHAR
            );

            statement.setNull(
                    6,
                    Types.INTEGER
            );

            return;
        }

        throw new SQLException(
                "Type d'élément non supporté : "
                        + element.getClass()
                        .getName()
        );
    }

    private void insertLinks(
            long dialogueId,
            DialogueGraph graph,
            Map<DialogueElementKey, Long>
                    idsByKey
    ) throws SQLException {

        String sql = """
                INSERT INTO dialogue_graph_link (
                    dialogue_id,
                    source_element_id,
                    target_element_id
                )
                VALUES (?, ?, ?)
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
        ) {

            for (DialogueLink link :
                    graph.links()) {

                long sourceId =
                        requireElementId(
                                idsByKey,
                                link.source()
                        );

                long targetId =
                        requireElementId(
                                idsByKey,
                                link.target()
                        );

                statement.setLong(
                        1,
                        dialogueId
                );

                statement.setLong(
                        2,
                        sourceId
                );

                statement.setLong(
                        3,
                        targetId
                );

                statement.addBatch();
            }

            statement.executeBatch();
        }
    }

    private void insertConditions(
            DialogueGraph graph,
            DialogueRules dialogueRules,
            Map<DialogueElementKey, Long>
                    idsByKey
    ) throws SQLException {

        String sql = """
                INSERT INTO dialogue_condition (
                    element_id,
                    key,
                    provider,
                    expression
                )
                VALUES (?, ?, ?, ?)
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
        ) {

            for (DialogueElement element :
                    graph.elements()
                            .values()) {

                DialogueRules rules =
                        rulesOf(
                                element,
                                dialogueRules
                        );

                if (rules == null) {
                    continue;
                }

                long elementId =
                        requireElementId(
                                idsByKey,
                                element.key()
                        );

                for (DialogueConditionEntry entry :
                        rules.conditions()) {

                    statement.setLong(
                            1,
                            elementId
                    );

                    statement.setString(
                            2,
                            entry.key()
                                    .value()
                    );

                    statement.setString(
                            3,
                            entry.condition()
                                    .getProvider()
                    );

                    statement.setString(
                            4,
                            entry.condition()
                                    .getExpression()
                    );

                    statement.addBatch();
                }
            }

            statement.executeBatch();
        }
    }

    private void insertActions(
            DialogueGraph graph,
            DialogueRules dialogueRules,
            Map<DialogueElementKey, Long>
                    idsByKey
    ) throws SQLException {

        String sql = """
                INSERT INTO dialogue_action (
                    element_id,
                    key,
                    provider,
                    expression,
                    position
                )
                VALUES (?, ?, ?, ?, ?)
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
        ) {

            for (DialogueElement element :
                    graph.elements()
                            .values()) {

                DialogueRules rules =
                        rulesOf(
                                element,
                                dialogueRules
                        );

                if (rules == null) {
                    continue;
                }

                long elementId =
                        requireElementId(
                                idsByKey,
                                element.key()
                        );

                for (DialogueActionEntry entry :
                        rules.actions()) {

                    statement.setLong(
                            1,
                            elementId
                    );

                    statement.setString(
                            2,
                            entry.key()
                                    .value()
                    );

                    statement.setString(
                            3,
                            entry.action()
                                    .getProvider()
                    );

                    statement.setString(
                            4,
                            entry.action()
                                    .getExpression()
                    );

                    statement.setInt(
                            5,
                            entry.action()
                                    .getPosition()
                    );

                    statement.addBatch();
                }
            }

            statement.executeBatch();
        }
    }

    private DialogueRules rulesOf(
            DialogueElement element,
            DialogueRules dialogueRules
    ) {

        if (element instanceof DialogueStart) {
            return dialogueRules;
        }

        if (element instanceof DialogueReply reply) {
            return reply.rules();
        }

        if (element instanceof DialogueChoice choice) {
            return choice.rules();
        }

        return null;
    }

    private void deleteGraphElements(
            long dialogueId
    ) throws SQLException {

        String sql = """
                DELETE FROM dialogue_graph_element
                WHERE dialogue_id = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
        ) {

            statement.setLong(
                    1,
                    dialogueId
            );

            statement.executeUpdate();
        }
    }

    /*
     * ====================================================
     * UTILITAIRES
     * ====================================================
     */

    private long requireDialogueId(
            DialogueKey key
    ) throws SQLException {

        String sql = """
                SELECT id
                FROM dialogue
                WHERE key = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(
                                sql
                        )
        ) {

            statement.setString(
                    1,
                    key.value()
            );

            try (
                    ResultSet result =
                            statement.executeQuery()
            ) {

                if (!result.next()) {

                    throw new DialogueNotFoundException(
                            key
                    );
                }

                return result.getLong(
                        "id"
                );
            }
        }
    }

    private long requireElementId(
            Map<DialogueElementKey, Long>
                    idsByKey,
            DialogueElementKey key
    ) throws SQLException {

        Long id =
                idsByKey.get(
                        key
                );

        if (id == null) {

            throw new SQLException(
                    "Impossible de trouver l'identifiant SQL "
                            + "de l'élément "
                            + key
                            + "."
            );
        }

        return id;
    }

    private void executeTransaction(
            SqlOperation operation
    ) {

        boolean previousAutoCommit;

        try {

            previousAutoCommit =
                    connection.getAutoCommit();

        } catch (SQLException e) {

            throw new DialoguePersistenceException(
                    "Impossible de lire l'état transactionnel SQLite.",
                    e
            );
        }

        /*
         * Le repository ne doit pas prendre possession
         * silencieusement d'une transaction déjà ouverte.
         */
        if (!previousAutoCommit) {

            throw new DialoguePersistenceException(
                    "Une transaction SQLite est déjà active "
                            + "sur la connexion de DialogueRepository."
            );
        }

        try {

            connection.setAutoCommit(
                    false
            );

            operation.execute();

            connection.commit();

        } catch (SQLException e) {

            rollbackAfterFailure();

            throw new DialoguePersistenceException(
                    "La transaction de persistance "
                            + "du dialogue a échoué.",
                    e
            );

        } catch (RuntimeException e) {

            rollbackAfterFailure();

            throw e;

        } finally {

            try {

                connection.setAutoCommit(
                        previousAutoCommit
                );

            } catch (SQLException e) {

                throw new DialoguePersistenceException(
                        "Impossible de restaurer "
                                + "l'auto-commit SQLite.",
                        e
                );
            }
        }
    }

    private void rollbackAfterFailure() {

        try {

            connection.rollback();

        } catch (SQLException rollbackError) {

            throw new DialoguePersistenceException(
                    "La transaction a échoué et son rollback "
                            + "a également échoué.",
                    rollbackError
            );
        }
    }

    @FunctionalInterface
    private interface SqlOperation {

        void execute()
                throws SQLException;
    }

    private record LoadedElements(
            List<DialogueElement> elements,
            Map<Long, DialogueElementKey>
            keysByDatabaseId,
            DialogueRules startRules
    ) {
    }
}