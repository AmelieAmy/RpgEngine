package fr.doryamy.rpgengine.database.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class V3_ConditionSystem implements Migration {

    @Override
    public int getVersion() {
        return 3;
    }

    @Override
    public void apply(Connection connection) throws SQLException {
        try(Statement statement = connection.createStatement()) {

            statement.execute("""            
                CREATE TABLE condition (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    trigger_id INTEGER NOT NULL,
                    type TEXT NOT NULL,
                    value TEXT,
                    FOREIGN KEY(trigger_id)
                    REFERENCES trigger(id)
                );
            """);
        }
    }
}
