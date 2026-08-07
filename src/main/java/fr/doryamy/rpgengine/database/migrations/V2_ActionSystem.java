package fr.doryamy.rpgengine.database.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class V2_ActionSystem implements Migration {

    @Override
    public int getVersion() {
        return 2;
    }

    @Override
    public void apply(Connection connection)
            throws SQLException {

        try(Statement statement =
                    connection.createStatement()) {

            statement.execute("""
                CREATE TABLE action (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    trigger_id INTEGER NOT NULL,
                    type TEXT NOT NULL,
                    value TEXT,
                    position INTEGER,
    
                    FOREIGN KEY(trigger_id)
                    REFERENCES trigger(id)
                );
            """);

        }
    }

}