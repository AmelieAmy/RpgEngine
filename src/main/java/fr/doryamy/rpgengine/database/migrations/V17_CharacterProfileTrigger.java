package fr.doryamy.rpgengine.database.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Associe optionnellement un profil de personnage à un trigger RPGEngine.
 *
 * <p>L'association est globale et unique : un trigger ne peut initialiser
 * qu'un seul profil de personnage. La suppression du trigger conserve le
 * profil mais libère son association.
 */
public final class V17_CharacterProfileTrigger
        implements Migration {

    @Override
    public int getVersion() {
        return 17;
    }

    @Override
    public void apply(
            Connection connection
    ) throws SQLException {

        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    ALTER TABLE dialogue_character_profile
                    ADD COLUMN trigger_id INTEGER
                        REFERENCES trigger(id)
                        ON DELETE SET NULL;
                    """);

            statement.execute("""
                    CREATE UNIQUE INDEX ux_dialogue_character_profile_trigger
                    ON dialogue_character_profile(trigger_id)
                    WHERE trigger_id IS NOT NULL;
                    """);
        }
    }
}
