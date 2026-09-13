package fr.doryamy.rpgengine.database.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/** Remplace l'association Character -> Trigger par Character -> Citizens NPC. */
public final class V18_CharacterProfileCitizensNpc implements Migration {
    @Override public int getVersion() { return 18; }

    @Override
    public void apply(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("ALTER TABLE dialogue_character_profile ADD COLUMN citizens_npc_id TEXT;");
            statement.execute("""
                    UPDATE dialogue_character_profile
                    SET citizens_npc_id = (
                        SELECT NULLIF(trim(t.target_id), '')
                        FROM trigger t
                        WHERE t.id = dialogue_character_profile.trigger_id
                    )
                    WHERE trigger_id IS NOT NULL;
                    """);

            // Plusieurs anciens triggers pouvaient viser le même Citizens.
            // Tous leurs participants sont réorientés vers le profil canonique (plus petit id).
            statement.execute("""
                    UPDATE dialogue_participant
                    SET character_profile_id = (
                        SELECT MIN(canonical.id)
                        FROM dialogue_character_profile source
                        JOIN dialogue_character_profile canonical
                          ON canonical.citizens_npc_id = source.citizens_npc_id
                        WHERE source.id = dialogue_participant.character_profile_id
                          AND source.citizens_npc_id IS NOT NULL
                    )
                    WHERE character_profile_id IN (
                        SELECT id FROM dialogue_character_profile
                        WHERE citizens_npc_id IS NOT NULL
                    );
                    """);

            statement.execute("""
                    DELETE FROM dialogue_character_profile
                    WHERE citizens_npc_id IS NOT NULL
                      AND id NOT IN (
                          SELECT MIN(id)
                          FROM dialogue_character_profile
                          WHERE citizens_npc_id IS NOT NULL
                          GROUP BY citizens_npc_id
                      );
                    """);

            statement.execute("DROP INDEX IF EXISTS ux_dialogue_character_profile_trigger;");
            statement.execute("ALTER TABLE dialogue_character_profile DROP COLUMN trigger_id;");
            statement.execute("""
                    CREATE UNIQUE INDEX ux_dialogue_character_profile_citizens_npc
                    ON dialogue_character_profile(citizens_npc_id)
                    WHERE citizens_npc_id IS NOT NULL;
                    """);
        }
    }
}
