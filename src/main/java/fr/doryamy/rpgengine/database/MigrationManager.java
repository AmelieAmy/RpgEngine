package fr.doryamy.rpgengine.database;

import fr.doryamy.rpgengine.database.migrations.*;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.sql.*;
import java.util.List;

/**
 * Gère l'évolution du schéma SQLite de RPGEngine.
 *
 * Le manager compare la version actuelle de la base
 * avec les migrations disponibles et applique
 * uniquement les migrations manquantes.
 */
public final class MigrationManager {
    private final List<Migration> migrations;

    public MigrationManager() {
        migrations = List.of(
                new V1_Init(),
                new V2_ActionSystem(),
                new V3_ConditionSystem(),
                new V4_PlayerVariables(),
                new V5_RenameConditionTypeAndValue(),
                new V6_RenameActionTypeAndValue(),
                new V7_DialogSystem(),
                new V8_DialogGraphSystem(),
                new V9_TriggerCascadeDelete(),
                new V10_AutoPlayerReply(),
                new V11_PlayerReplySequence(),
                new V12_TerminalDialogueTransitions(),
                new V13_StructuralDialogueNodes(),
                new V14_DialogueGraphSystem(),
                new V15_DialogueParticipants(),
                new V16_DialogueReplyParticipants(),
                new V17_CharacterProfileTrigger(),
                new V18_CharacterProfileCitizensNpc(),
                new V19_RebindLegacyDialogueNpcParticipants()
        );
    }

    public void migrate(Connection connection) throws SQLException {

        createVersionTable(connection);
        int currentVersion = getCurrentVersion(connection);

        for (Migration migration : migrations) {
            if (migration.getVersion() <= currentVersion) {
                continue;
            }
            boolean previousAutoCommit = connection.getAutoCommit();
            try {
                connection.setAutoCommit(false);
                migration.apply(connection);
                setVersion(connection, migration.getVersion());
                connection.commit();
                currentVersion = migration.getVersion();
                RpgLogger.info(
                        "Migration V"
                                + currentVersion
                                + " appliquée."
                );
            } catch (SQLException e) {
                connection.rollback();
                RpgLogger.error(
                        "Echec de la migration V"
                                + migration.getVersion()
                                + " : "
                                + e.getMessage()
                );
                throw e;
            } finally {
                connection.setAutoCommit(previousAutoCommit);
            }
        }
    }

    private boolean hasVersion(Connection connection) throws SQLException {
        String sql = "SELECT 1 FROM schema_version LIMIT 1";
        try (
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery(sql)
        ) {
            return result.next();
        }
    }

    private void createVersionTable(
            Connection connection
    ) throws SQLException {

        String sql = """
                CREATE TABLE IF NOT EXISTS schema_version (
                    version INTEGER NOT NULL
                );
                """;
        try(Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }

        // Première installation
        if (!hasVersion(connection)) {
            try (Statement statement = connection.createStatement()) {
                statement.execute(
                        "INSERT INTO schema_version(version) VALUES (0)"
                );
            }
        }
    }

    private int getCurrentVersion(
            Connection connection
    ) throws SQLException {
        String sql =
                "SELECT version FROM schema_version";

        try(
                Statement statement =
                        connection.createStatement();

                ResultSet result =
                        statement.executeQuery(sql)
        ){
            if(result.next()) {
                return result.getInt("version");
            }
        }
        return 0;
    }

    private void setVersion(
            Connection connection,
            int version
    ) throws SQLException {
        String sql =
                "UPDATE schema_version SET version = ?";
        try(PreparedStatement statement =
                    connection.prepareStatement(sql)) {
            statement.setInt(1, version);
            statement.executeUpdate();
        }
    }

}