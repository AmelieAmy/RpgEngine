package fr.doryamy.rpgengine.database.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Remplace le système de dialogues linéaires
 * par un modèle narratif orienté graphe.
 *
 * Cette migration introduit :
 * - les nodes narratifs ;
 * - les transitions ;
 * - les conditions de transition ;
 * - les actions de transition.
 *
 * Les anciennes tables de dialogues sont
 * supprimées volontairement.
 */
public final class V8_DialogGraphSystem
        implements Migration {

    @Override
    public int getVersion() {
        return 8;
    }

    @Override
    public void apply(
            Connection connection
    ) throws SQLException {

        try (Statement statement =
                     connection.createStatement()) {

            /*
             * Suppression de l'ancien modèle.
             */
            statement.execute("""
                    DROP TABLE IF EXISTS dialogue_line;
                    """);

            statement.execute("""
                    DROP TABLE IF EXISTS dialogue;
                    """);

            /*
             * Dialogue.
             *
             * start_node_id reste nullable afin
             * de permettre la création progressive
             * d'un dialogue depuis l'éditeur.
             */
            statement.execute("""
                    CREATE TABLE dialogue (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        key TEXT NOT NULL UNIQUE,
                        name TEXT NOT NULL,
                        start_node_id INTEGER,

                        FOREIGN KEY(start_node_id)
                            REFERENCES dialogue_node(id)
                            ON DELETE SET NULL
                    );
                    """);

            /*
             * Node narratif.
             */
            statement.execute("""
                    CREATE TABLE dialogue_node (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        dialogue_id INTEGER NOT NULL,
                        key TEXT NOT NULL,
                        text TEXT NOT NULL,

                        UNIQUE(dialogue_id, key),

                        FOREIGN KEY(dialogue_id)
                            REFERENCES dialogue(id)
                            ON DELETE CASCADE
                    );
                    """);

            /*
             * Transition entre deux nodes.
             */
            statement.execute("""
                    CREATE TABLE dialogue_transition (
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

                        CHECK (
                            type IN (
                                'AUTO',
                                'CHOICE',
                                'END'
                            )
                        ),

                        CHECK (
                            (
                                type = 'AUTO'
                                AND target_node_id IS NOT NULL
                                AND label IS NULL
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

            /*
             * Conditions appliquées aux transitions.
             */
            statement.execute("""
                    CREATE TABLE dialogue_transition_condition (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        transition_id INTEGER NOT NULL,
                        provider TEXT NOT NULL,
                        expression TEXT NOT NULL,

                        FOREIGN KEY(transition_id)
                            REFERENCES dialogue_transition(id)
                            ON DELETE CASCADE
                    );
                    """);

            /*
             * Actions exécutées lorsqu'une
             * transition est empruntée.
             */
            statement.execute("""
                    CREATE TABLE dialogue_transition_action (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        transition_id INTEGER NOT NULL,
                        provider TEXT NOT NULL,
                        expression TEXT NOT NULL,
                        position INTEGER NOT NULL,

                        UNIQUE(transition_id, position),

                        FOREIGN KEY(transition_id)
                            REFERENCES dialogue_transition(id)
                            ON DELETE CASCADE
                    );
                    """);

            /*
             * Index utiles au chargement du graphe.
             */
            statement.execute("""
                    CREATE INDEX idx_dialogue_node_dialogue
                    ON dialogue_node(dialogue_id);
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