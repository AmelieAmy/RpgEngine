package fr.doryamy.rpgengine.database.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Ajoute la suppression en cascade des actions
 * et conditions lorsqu'un trigger est supprimé.
 *
 * <p>SQLite ne permet pas de modifier directement
 * une contrainte FOREIGN KEY existante.
 * Les tables doivent donc être reconstruites.
 */
public final class V9_TriggerCascadeDelete
        implements Migration {

    @Override
    public int getVersion() {
        return 9;
    }

    @Override
    public void apply(
            Connection connection
    ) throws SQLException {

        /*
         * Les migrations sont exécutées avant
         * le runtime normal du plugin.
         *
         * On désactive temporairement les FK
         * pendant la reconstruction.
         */
        try (
                Statement statement =
                        connection.createStatement()
        ) {
            statement.execute(
                    "PRAGMA foreign_keys = OFF"
            );

            /*
             * ------------------------------------------------
             * ACTION
             * ------------------------------------------------
             */

            statement.execute("""
                    CREATE TABLE action_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        trigger_id INTEGER NOT NULL,
                        provider TEXT NOT NULL,
                        expression TEXT,
                        position INTEGER,

                        FOREIGN KEY(trigger_id)
                        REFERENCES trigger(id)
                        ON DELETE CASCADE
                    )
                    """);

            statement.execute("""
                    INSERT INTO action_new (
                        id,
                        trigger_id,
                        provider,
                        expression,
                        position
                    )
                    SELECT
                        id,
                        trigger_id,
                        provider,
                        expression,
                        position
                    FROM action
                    """);

            statement.execute(
                    "DROP TABLE action"
            );

            statement.execute(
                    "ALTER TABLE action_new RENAME TO action"
            );

            /*
             * ------------------------------------------------
             * CONDITION
             * ------------------------------------------------
             */

            statement.execute("""
                    CREATE TABLE condition_new (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        trigger_id INTEGER NOT NULL,
                        provider TEXT NOT NULL,
                        expression TEXT,

                        FOREIGN KEY(trigger_id)
                        REFERENCES trigger(id)
                        ON DELETE CASCADE
                    )
                    """);

            statement.execute("""
                    INSERT INTO condition_new (
                        id,
                        trigger_id,
                        provider,
                        expression
                    )
                    SELECT
                        id,
                        trigger_id,
                        provider,
                        expression
                    FROM condition
                    """);

            statement.execute(
                    "DROP TABLE condition"
            );

            statement.execute(
                    "ALTER TABLE condition_new RENAME TO condition"
            );

            statement.execute(
                    "PRAGMA foreign_keys = ON"
            );
        }
    }
}