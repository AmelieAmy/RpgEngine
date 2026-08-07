package fr.doryamy.rpgengine.database.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class V1_Init implements Migration {

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public void apply(Connection connection)
            throws SQLException {

        try(Statement statement =
                    connection.createStatement()) {

            statement.execute("""
            CREATE TABLE IF NOT EXISTS trigger (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                type TEXT NOT NULL,
                target_id TEXT NOT NULL,
                enabled INTEGER DEFAULT 1
            );
            """);

        }
    }

}