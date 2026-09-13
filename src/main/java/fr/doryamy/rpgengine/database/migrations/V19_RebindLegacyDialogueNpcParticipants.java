package fr.doryamy.rpgengine.database.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Réaffecte les participants NPC legacy vers le Character canonique
 * du Citizens ciblé par l'unique trigger NPC qui lance leur dialogue.
 *
 * La relation Trigger -> Dialogue est celle du moteur :
 * action.provider = 'DIALOG' et action.expression = dialogue.key.
 *
 * Les cas ambigus ou incomplets sont volontairement laissés intacts.
 */
public final class V19_RebindLegacyDialogueNpcParticipants implements Migration {

    @Override
    public int getVersion() {
        return 19;
    }

    @Override
    public void apply(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    UPDATE dialogue_participant
                    SET character_profile_id = (
                        SELECT canonical.id
                        FROM dialogue d
                        JOIN action a
                          ON a.provider = 'DIALOG'
                         AND a.expression = d.key
                        JOIN trigger t
                          ON t.id = a.trigger_id
                         AND t.type = 'NPC'
                        JOIN dialogue_character_profile canonical
                          ON canonical.citizens_npc_id = NULLIF(trim(t.target_id), '')
                        WHERE d.id = dialogue_participant.dialogue_id
                          AND (
                              SELECT COUNT(DISTINCT t_count.id)
                              FROM action a_count
                              JOIN trigger t_count
                                ON t_count.id = a_count.trigger_id
                               AND t_count.type = 'NPC'
                              WHERE a_count.provider = 'DIALOG'
                                AND a_count.expression = d.key
                          ) = 1
                    )
                    WHERE type = 'NPC'
                      AND character_profile_id IN (
                          SELECT legacy.id
                          FROM dialogue_character_profile legacy
                          WHERE legacy.citizens_npc_id IS NULL
                            AND legacy.key LIKE 'legacy-npc-%'
                      )
                      AND EXISTS (
                          SELECT 1
                          FROM dialogue d
                          JOIN action a
                            ON a.provider = 'DIALOG'
                           AND a.expression = d.key
                          JOIN trigger t
                            ON t.id = a.trigger_id
                           AND t.type = 'NPC'
                          JOIN dialogue_character_profile canonical
                            ON canonical.citizens_npc_id = NULLIF(trim(t.target_id), '')
                          WHERE d.id = dialogue_participant.dialogue_id
                            AND (
                                SELECT COUNT(DISTINCT t_count.id)
                                FROM action a_count
                                JOIN trigger t_count
                                  ON t_count.id = a_count.trigger_id
                                 AND t_count.type = 'NPC'
                                WHERE a_count.provider = 'DIALOG'
                                  AND a_count.expression = d.key
                            ) = 1
                      );
                    """);
        }
    }
}
