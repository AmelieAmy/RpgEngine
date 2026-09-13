package fr.doryamy.rpgengine.database.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Introduit :
 * - la bibliothèque globale de profils de personnages ;
 * - les participants explicitement déclarés par chaque dialogue.
 *
 * <p>Cette migration est additive et conserve les dialogues existants.
 */
public final class V15_DialogueParticipants
        implements Migration {

    @Override
    public int getVersion() {
        return 15;
    }

    @Override
    public void apply(
            Connection connection
    ) throws SQLException {

        try (
                Statement statement =
                        connection.createStatement()
        ) {
            statement.execute("""
                    CREATE TABLE dialogue_character_profile (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,

                        key TEXT NOT NULL,
                        display_name TEXT NOT NULL,
                        portrait_resource TEXT,

                        UNIQUE(key),

                        CHECK(trim(key) <> ''),
                        CHECK(trim(display_name) <> ''),
                        CHECK(
                            portrait_resource IS NULL
                            OR trim(portrait_resource) <> ''
                        )
                    );
                    """);

            statement.execute("""
                    CREATE TABLE dialogue_participant (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,

                        dialogue_id INTEGER NOT NULL,
                        key TEXT NOT NULL,
                        type TEXT NOT NULL,
                        character_profile_id INTEGER,

                        FOREIGN KEY (dialogue_id)
                            REFERENCES dialogue(id)
                            ON DELETE CASCADE,

                        FOREIGN KEY (character_profile_id)
                            REFERENCES dialogue_character_profile(id)
                            ON DELETE RESTRICT,

                        UNIQUE(dialogue_id, key),

                        CHECK(trim(key) <> ''),
                        CHECK(type IN ('PLAYER', 'NPC')),

                        CHECK(
                            (
                                type = 'PLAYER'
                                AND character_profile_id IS NULL
                            )
                            OR
                            (
                                type = 'NPC'
                                AND character_profile_id IS NOT NULL
                            )
                        )
                    );
                    """);

            statement.execute("""
                    CREATE INDEX idx_dialogue_participant_dialogue
                    ON dialogue_participant(dialogue_id);
                    """);

            statement.execute("""
                    CREATE INDEX idx_dialogue_participant_profile
                    ON dialogue_participant(character_profile_id);
                    """);
        }
    }
}
