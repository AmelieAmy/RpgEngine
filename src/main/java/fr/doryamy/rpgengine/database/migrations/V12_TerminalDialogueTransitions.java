package fr.doryamy.rpgengine.database.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Sépare la nature d'une transition de sa destination.
 *
 * Une transition est désormais :
 * - AUTO ou CHOICE ;
 * - terminale ou non terminale.
 *
 * Les anciennes transitions END sont migrées vers
 * des transitions AUTO terminales.
 */
public final class V12_TerminalDialogueTransitions
        implements Migration {

    @Override
    public int getVersion() {
        return 12;
    }

    @Override
    public void apply(
            Connection connection
    ) throws SQLException {

        try (Statement statement =
                     connection.createStatement()) {

            /*
             * Les tables enfants référencent dialogue_transition.
             * On désactive temporairement les FK pendant
             * la reconstruction, puis on les réactive à la fin.
             */
            statement.execute(
                    "PRAGMA foreign_keys = OFF"
            );

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
                        terminal INTEGER NOT NULL DEFAULT 0,

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

                        CHECK (type IN ('AUTO', 'CHOICE')),

                        CHECK (
                            terminal IN (0, 1)
                        ),

                        CHECK (
                            (
                                terminal = 0
                                AND target_node_id IS NOT NULL
                            )
                            OR
                            (
                                terminal = 1
                                AND target_node_id IS NULL
                            )
                        ),

                        CHECK (
                            (
                                type = 'AUTO'
                                AND label IS NULL
                            )
                            OR
                            (
                                type = 'CHOICE'
                                AND label IS NOT NULL
                                AND length(trim(label)) > 0
                            )
                        ),

                        CHECK (position > 0)
                    );
                    """);

            /*
             * Migration :
             *
             * END devient AUTO terminal.
             *
             * Les AUTO / CHOICE existants restent
             * non terminaux puisqu'ils possèdent
             * actuellement obligatoirement une cible.
             */
            statement.execute("""
                    INSERT INTO dialogue_transition_new (
                        id,
                        dialogue_id,
                        key,
                        source_node_id,
                        target_node_id,
                        type,
                        label,
                        position,
                        terminal
                    )
                    SELECT
                        id,
                        dialogue_id,
                        key,
                        source_node_id,

                        CASE
                            WHEN type = 'END'
                                THEN NULL
                            ELSE target_node_id
                        END,

                        CASE
                            WHEN type = 'END'
                                THEN 'AUTO'
                            ELSE type
                        END,

                        CASE
                            WHEN type = 'END'
                                THEN NULL
                            ELSE label
                        END,

                        position,

                        CASE
                            WHEN type = 'END'
                                THEN 1
                            ELSE 0
                        END

                    FROM dialogue_transition;
                    """);

            statement.execute(
                    "DROP TABLE dialogue_transition"
            );

            statement.execute("""
                    ALTER TABLE dialogue_transition_new
                    RENAME TO dialogue_transition
                    """);

            statement.execute("""
                    CREATE INDEX idx_dialogue_transition_dialogue
                    ON dialogue_transition(dialogue_id)
                    """);

            statement.execute("""
                    CREATE INDEX idx_dialogue_transition_source
                    ON dialogue_transition(source_node_id)
                    """);

            statement.execute("""
                    CREATE INDEX idx_dialogue_transition_target
                    ON dialogue_transition(target_node_id)
                    """);

            statement.execute(
                    "PRAGMA foreign_keys = ON"
            );
        }
    }
}