package fr.doryamy.rpgengine.database.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class V4_PlayerVariables implements Migration {

    @Override
    public int getVersion() {
        return 4;
    }

    @Override
    public void apply(Connection connection)
            throws SQLException {

        try(Statement statement = connection.createStatement()) {
            statement.execute("""            
                CREATE TABLE player_variable (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    player_uuid TEXT NOT NULL,
                    key TEXT NOT NULL,
                    value TEXT,
    
                    UNIQUE(player_uuid, key)
                );
            """);
        }
    }
}