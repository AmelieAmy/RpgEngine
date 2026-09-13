package fr.doryamy.rpgengine.database.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Migre les répliques du rôle historique speaker vers un participant explicite.
 *
 * <p>La migration matérialise les anciennes sémantiques :
 * PLAYER devient le participant local "player" et NPC le participant local
 * "npc". Pour les anciens dialogues, le profil NPC reçoit le nom historique
 * neutre "PNJ" car aucune identité de personnage n'était stockée auparavant.
 *
 * <p>Après migration, dialogue_graph_element ne possède plus de colonne
 * speaker : une REPLY référence obligatoirement dialogue_participant.
 */
public final class V16_DialogueReplyParticipants
        implements Migration {

    @Override
    public int getVersion() {
        return 16;
    }

    @Override
    public void apply(
            Connection connection
    ) throws SQLException {

        try (Statement statement = connection.createStatement()) {

            /*
             * Chaque dialogue possède un joueur explicite.
             * Cela couvre aussi les Choice, dont l'interaction appartient
             * au joueur même si aucune Reply PLAYER n'existe.
             */
            statement.execute("""
                    INSERT OR IGNORE INTO dialogue_participant (
                        dialogue_id,
                        key,
                        type,
                        character_profile_id
                    )
                    SELECT
                        d.id,
                        'player',
                        'PLAYER',
                        NULL
                    FROM dialogue d;
                    """);

            /*
             * Les anciens dialogues ne stockaient aucune identité de PNJ.
             * On matérialise donc exactement l'information historique connue :
             * un participant NPC générique nommé "PNJ".
             */
            statement.execute("""
                    INSERT OR IGNORE INTO dialogue_character_profile (
                        key,
                        display_name,
                        portrait_resource
                    )
                    SELECT
                        'legacy-npc-' || d.key,
                        'PNJ',
                        NULL
                    FROM dialogue d
                    WHERE EXISTS (
                        SELECT 1
                        FROM dialogue_graph_element e
                        WHERE e.dialogue_id = d.id
                          AND e.type = 'REPLY'
                          AND e.speaker = 'NPC'
                    )
                      AND NOT EXISTS (
                        SELECT 1
                        FROM dialogue_participant p
                        WHERE p.dialogue_id = d.id
                          AND p.type = 'NPC'
                    );
                    """);

            statement.execute("""
                    INSERT OR IGNORE INTO dialogue_participant (
                        dialogue_id,
                        key,
                        type,
                        character_profile_id
                    )
                    SELECT
                        d.id,
                        'npc',
                        'NPC',
                        cp.id
                    FROM dialogue d
                    JOIN dialogue_character_profile cp
                      ON cp.key = 'legacy-npc-' || d.key
                    WHERE EXISTS (
                        SELECT 1
                        FROM dialogue_graph_element e
                        WHERE e.dialogue_id = d.id
                          AND e.type = 'REPLY'
                          AND e.speaker = 'NPC'
                    )
                      AND NOT EXISTS (
                        SELECT 1
                        FROM dialogue_participant p
                        WHERE p.dialogue_id = d.id
                          AND p.type = 'NPC'
                    );
                    """);

            /*
             * Autorise une FK composite garantissant qu'un élément ne peut
             * référencer qu'un participant de son propre dialogue.
             */
            statement.execute("""
                    CREATE UNIQUE INDEX ux_dialogue_participant_id_dialogue
                    ON dialogue_participant(id, dialogue_id);
                    """);

            /*
             * Les index explicites doivent être libérés avant de recréer
             * les tables sous leurs noms définitifs.
             */
            statement.execute("DROP INDEX idx_dialogue_condition_element;");
            statement.execute("DROP INDEX idx_dialogue_action_element;");
            statement.execute("DROP INDEX idx_dialogue_graph_link_source;");
            statement.execute("DROP INDEX idx_dialogue_graph_link_target;");
            statement.execute("DROP INDEX idx_dialogue_graph_element_dialogue;");
            statement.execute("DROP INDEX ux_dialogue_graph_element_start;");

            statement.execute("""
                    ALTER TABLE dialogue_condition
                    RENAME TO dialogue_condition_v15;
                    """);

            statement.execute("""
                    ALTER TABLE dialogue_action
                    RENAME TO dialogue_action_v15;
                    """);

            statement.execute("""
                    ALTER TABLE dialogue_graph_link
                    RENAME TO dialogue_graph_link_v15;
                    """);

            statement.execute("""
                    ALTER TABLE dialogue_graph_element
                    RENAME TO dialogue_graph_element_v15;
                    """);

            statement.execute("""
                    CREATE TABLE dialogue_graph_element (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        dialogue_id INTEGER NOT NULL,
                        key TEXT NOT NULL,
                        type TEXT NOT NULL,
                        participant_id INTEGER,
                        text TEXT,
                        position INTEGER,

                        FOREIGN KEY (dialogue_id)
                            REFERENCES dialogue(id)
                            ON DELETE CASCADE,

                        FOREIGN KEY (participant_id, dialogue_id)
                            REFERENCES dialogue_participant(id, dialogue_id)
                            ON DELETE RESTRICT,

                        UNIQUE(dialogue_id, key),
                        UNIQUE(id, dialogue_id),

                        CHECK(trim(key) <> ''),
                        CHECK(
                            type IN (
                                'START',
                                'REPLY',
                                'BRANCH',
                                'CHOICE',
                                'END'
                            )
                        ),
                        CHECK(
                            (
                                type = 'START'
                                AND participant_id IS NULL
                                AND text IS NULL
                                AND position IS NULL
                            )
                            OR
                            (
                                type = 'REPLY'
                                AND participant_id IS NOT NULL
                                AND text IS NOT NULL
                                AND trim(text) <> ''
                                AND position IS NULL
                            )
                            OR
                            (
                                type = 'BRANCH'
                                AND participant_id IS NULL
                                AND text IS NULL
                                AND position IS NULL
                            )
                            OR
                            (
                                type = 'CHOICE'
                                AND participant_id IS NULL
                                AND text IS NOT NULL
                                AND trim(text) <> ''
                                AND position IS NOT NULL
                                AND position >= 0
                            )
                            OR
                            (
                                type = 'END'
                                AND participant_id IS NULL
                                AND text IS NULL
                                AND position IS NULL
                            )
                        )
                    );
                    """);

            statement.execute("""
                    INSERT INTO dialogue_graph_element (
                        id,
                        dialogue_id,
                        key,
                        type,
                        participant_id,
                        text,
                        position
                    )
                    SELECT
                        e.id,
                        e.dialogue_id,
                        e.key,
                        e.type,
                        CASE
                            WHEN e.type <> 'REPLY' THEN NULL
                            WHEN e.speaker = 'PLAYER' THEN (
                                SELECT p.id
                                FROM dialogue_participant p
                                WHERE p.dialogue_id = e.dialogue_id
                                  AND p.key = 'player'
                            )
                            WHEN e.speaker = 'NPC' THEN (
                                SELECT p.id
                                FROM dialogue_participant p
                                WHERE p.dialogue_id = e.dialogue_id
                                  AND p.key = 'npc'
                            )
                            ELSE NULL
                        END,
                        e.text,
                        e.position
                    FROM dialogue_graph_element_v15 e;
                    """);

            statement.execute("""
                    CREATE INDEX idx_dialogue_graph_element_dialogue
                    ON dialogue_graph_element(dialogue_id);
                    """);

            statement.execute("""
                    CREATE UNIQUE INDEX ux_dialogue_graph_element_start
                    ON dialogue_graph_element(dialogue_id)
                    WHERE type = 'START';
                    """);

            statement.execute("""
                    CREATE TABLE dialogue_graph_link (
                        dialogue_id INTEGER NOT NULL,
                        source_element_id INTEGER NOT NULL,
                        target_element_id INTEGER NOT NULL,

                        PRIMARY KEY (
                            dialogue_id,
                            source_element_id,
                            target_element_id
                        ),

                        FOREIGN KEY (dialogue_id)
                            REFERENCES dialogue(id)
                            ON DELETE CASCADE,

                        FOREIGN KEY (source_element_id, dialogue_id)
                            REFERENCES dialogue_graph_element(id, dialogue_id)
                            ON DELETE CASCADE,

                        FOREIGN KEY (target_element_id, dialogue_id)
                            REFERENCES dialogue_graph_element(id, dialogue_id)
                            ON DELETE CASCADE
                    );
                    """);

            statement.execute("""
                    INSERT INTO dialogue_graph_link
                    SELECT *
                    FROM dialogue_graph_link_v15;
                    """);

            statement.execute("""
                    CREATE INDEX idx_dialogue_graph_link_source
                    ON dialogue_graph_link(
                        dialogue_id,
                        source_element_id
                    );
                    """);

            statement.execute("""
                    CREATE INDEX idx_dialogue_graph_link_target
                    ON dialogue_graph_link(
                        dialogue_id,
                        target_element_id
                    );
                    """);

            statement.execute("""
                    CREATE TABLE dialogue_condition (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        element_id INTEGER NOT NULL,
                        key TEXT NOT NULL,
                        provider TEXT NOT NULL,
                        expression TEXT NOT NULL,

                        FOREIGN KEY (element_id)
                            REFERENCES dialogue_graph_element(id)
                            ON DELETE CASCADE,

                        UNIQUE(element_id, key),

                        CHECK(trim(key) <> ''),
                        CHECK(trim(provider) <> '')
                    );
                    """);

            statement.execute("""
                    INSERT INTO dialogue_condition
                    SELECT *
                    FROM dialogue_condition_v15;
                    """);

            statement.execute("""
                    CREATE INDEX idx_dialogue_condition_element
                    ON dialogue_condition(element_id);
                    """);

            statement.execute("""
                    CREATE TABLE dialogue_action (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        element_id INTEGER NOT NULL,
                        key TEXT NOT NULL,
                        provider TEXT NOT NULL,
                        expression TEXT NOT NULL,
                        position INTEGER NOT NULL,

                        FOREIGN KEY (element_id)
                            REFERENCES dialogue_graph_element(id)
                            ON DELETE CASCADE,

                        UNIQUE(element_id, key),
                        UNIQUE(element_id, position),

                        CHECK(trim(key) <> ''),
                        CHECK(trim(provider) <> ''),
                        CHECK(position >= 0)
                    );
                    """);

            statement.execute("""
                    INSERT INTO dialogue_action
                    SELECT *
                    FROM dialogue_action_v15;
                    """);

            statement.execute("""
                    CREATE INDEX idx_dialogue_action_element
                    ON dialogue_action(element_id);
                    """);

            statement.execute("DROP TABLE dialogue_condition_v15;");
            statement.execute("DROP TABLE dialogue_action_v15;");
            statement.execute("DROP TABLE dialogue_graph_link_v15;");
            statement.execute("DROP TABLE dialogue_graph_element_v15;");
        }
    }
}
