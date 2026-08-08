package fr.doryamy.rpgengine.database.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Introduit le système de dialogues.
 *
 * Un dialogue possède une clé métier unique
 * et une liste ordonnée de lignes.
 */
public final class V7_DialogSystem implements Migration {

    @Override
    public int getVersion() {
        return 7;
    }

    @Override
    public void apply(Connection connection)
            throws SQLException {

        try (Statement statement =
                     connection.createStatement()) {

            statement.execute("""
                    CREATE TABLE dialogue (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        key TEXT NOT NULL UNIQUE,
                        name TEXT NOT NULL
                    );
                    """);

            statement.execute("""
                    CREATE TABLE dialogue_line (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        dialogue_id INTEGER NOT NULL,
                        position INTEGER NOT NULL,
                        text TEXT NOT NULL,

                        UNIQUE(dialogue_id, position),

                        FOREIGN KEY(dialogue_id)
                        REFERENCES dialogue(id)
                        ON DELETE CASCADE
                    );
                    """);
        }
    }
}