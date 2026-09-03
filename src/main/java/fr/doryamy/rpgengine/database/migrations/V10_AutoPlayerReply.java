package fr.doryamy.rpgengine.database.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Autorise une transition AUTO à porter une réplique Joueur.
 *
 * AUTO + label NULL      : PNJ -> PNJ
 * AUTO + label non vide : PNJ -> Joueur -> PNJ
 *
 * SQLite ne permettant pas de modifier directement un CHECK,
 * la table des transitions est reconstruite. Les deux tables
 * enfants sont également reconstruites afin de conserver des
 * foreign keys valides pendant la transaction de migration.
 */
public final class V10_AutoPlayerReply implements Migration {

    @Override
    public int getVersion() {
        return 10;
    }

    @Override
    public void apply(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {

            statement.execute("""
                    CREATE TABLE dialogue_transition_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        dialogue_id INTEGER NOT NULL,
                        key TEXT NOT NULL,
                        source_node_id INTEGER NOT NULL,
                        target_node_id INTEGER,
                        type TEXT NOT NULL,
                        label TEXT,
                        position INTEGER NOT NULL,

                        UNIQUE(dialogue_id, key),
                        UNIQUE(source_node_id, position),

                        FOREIGN KEY(dialogue_id)
                            REFERENCES dialogue(id)
                            ON DELETE CASCADE,

                        FOREIGN KEY(source_node_id)
                            REFERENCES dialogue_node(id)
                            ON DELETE CASCADE,

                        FOREIGN KEY(target_node_id)
                            REFERENCES dialogue_node(id)
                            ON DELETE CASCADE,

                        CHECK (type IN ('AUTO', 'CHOICE', 'END')),

                        CHECK (
                            (
                                type = 'AUTO'
                                AND target_node_id IS NOT NULL
                                AND (
                                    label IS NULL
                                    OR length(trim(label)) > 0
                                )
                            )
                            OR
                            (
                                type = 'CHOICE'
                                AND target_node_id IS NOT NULL
                                AND label IS NOT NULL
                                AND length(trim(label)) > 0
                            )
                            OR
                            (
                                type = 'END'
                                AND target_node_id IS NULL
                                AND label IS NULL
                            )
                        )
                    );
                    """);

            statement.execute("""
                    INSERT INTO dialogue_transition_new (
                        id, dialogue_id, key, source_node_id,
                        target_node_id, type, label, position
                    )
                    SELECT
                        id, dialogue_id, key, source_node_id,
                        target_node_id, type, label, position
                    FROM dialogue_transition;
                    """);

            statement.execute("""
                    CREATE TABLE dialogue_transition_condition_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        transition_id INTEGER NOT NULL,
                        provider TEXT NOT NULL,
                        expression TEXT NOT NULL,

                        FOREIGN KEY(transition_id)
                            REFERENCES dialogue_transition_new(id)
                            ON DELETE CASCADE
                    );
                    """);

            statement.execute("""
                    INSERT INTO dialogue_transition_condition_new (
                        id, transition_id, provider, expression
                    )
                    SELECT
                        id, transition_id, provider, expression
                    FROM dialogue_transition_condition;
                    """);

            statement.execute("""
                    CREATE TABLE dialogue_transition_action_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        transition_id INTEGER NOT NULL,
                        provider TEXT NOT NULL,
                        expression TEXT NOT NULL,
                        position INTEGER NOT NULL,

                        UNIQUE(transition_id, position),

                        FOREIGN KEY(transition_id)
                            REFERENCES dialogue_transition_new(id)
                            ON DELETE CASCADE
                    );
                    """);

            statement.execute("""
                    INSERT INTO dialogue_transition_action_new (
                        id, transition_id, provider, expression, position
                    )
                    SELECT
                        id, transition_id, provider, expression, position
                    FROM dialogue_transition_action;
                    """);

            statement.execute("DROP TABLE dialogue_transition_condition;");
            statement.execute("DROP TABLE dialogue_transition_action;");
            statement.execute("DROP TABLE dialogue_transition;");

            statement.execute("""
                    ALTER TABLE dialogue_transition_new
                    RENAME TO dialogue_transition;
                    """);

            statement.execute("""
                    ALTER TABLE dialogue_transition_condition_new
                    RENAME TO dialogue_transition_condition;
                    """);

            statement.execute("""
                    ALTER TABLE dialogue_transition_action_new
                    RENAME TO dialogue_transition_action;
                    """);

            statement.execute("""
                    CREATE INDEX idx_dialogue_transition_dialogue
                    ON dialogue_transition(dialogue_id);
                    """);

            statement.execute("""
                    CREATE INDEX idx_dialogue_transition_source
                    ON dialogue_transition(source_node_id);
                    """);
        }
    }
}