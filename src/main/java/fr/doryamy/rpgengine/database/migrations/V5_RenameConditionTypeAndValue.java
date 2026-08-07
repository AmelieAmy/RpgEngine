package fr.doryamy.rpgengine.database.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class V5_RenameConditionTypeAndValue implements Migration {
    @Override
    public int getVersion() {
        return 5;
    }

    @Override
    public void apply(Connection connection)
            throws SQLException {

        try(Statement statement = connection.createStatement()) {
            statement.execute("""            
                ALTER TABLE condition
                RENAME COLUMN type TO provider;
            """);

            statement.execute("""            
                ALTER TABLE condition
                RENAME COLUMN value TO expression;
            """);
        }
    }
}
