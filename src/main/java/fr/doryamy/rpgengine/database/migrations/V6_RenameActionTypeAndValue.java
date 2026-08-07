package fr.doryamy.rpgengine.database.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class V6_RenameActionTypeAndValue implements Migration {
    @Override
    public int getVersion() {
        return 6;
    }

    @Override
    public void apply(Connection connection)
            throws SQLException {

        try(Statement statement = connection.createStatement()) {
            statement.execute("""            
                ALTER TABLE action
                RENAME COLUMN type TO provider;
            """);

            statement.execute("""            
                ALTER TABLE action
                RENAME COLUMN value TO expression;
            """);
        }
    }
}
