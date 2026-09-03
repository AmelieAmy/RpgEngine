package fr.doryamy.rpgengine.database.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Introduit les répliques Joueur séquentielles sur les transitions AUTO.
 *
 * Chaque réplique peut posséder zéro ou plusieurs conditions et actions.
 * Les anciens labels AUTO introduits en V10 sont migrés automatiquement
 * vers une première réplique Joueur en position 1, puis supprimés du label.
 */
public final class V11_PlayerReplySequence implements Migration {

    @Override
    public int getVersion() {
        return 11;
    }

    @Override
    public void apply(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {

            statement.execute("""
                    CREATE TABLE dialogue_transition_player_reply (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        transition_id INTEGER NOT NULL,
                        text TEXT NOT NULL,
                        position INTEGER NOT NULL,

                        UNIQUE(transition_id, position),

                        FOREIGN KEY(transition_id)
                            REFERENCES dialogue_transition(id)
                            ON DELETE CASCADE,

                        CHECK (length(trim(text)) > 0),
                        CHECK (position > 0)
                    );
                    """);

            statement.execute("""
                    CREATE TABLE dialogue_player_reply_condition (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        player_reply_id INTEGER NOT NULL,
                        provider TEXT NOT NULL,
                        expression TEXT NOT NULL,

                        FOREIGN KEY(player_reply_id)
                            REFERENCES dialogue_transition_player_reply(id)
                            ON DELETE CASCADE
                    );
                    """);

            statement.execute("""
                    CREATE TABLE dialogue_player_reply_action (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,
                        player_reply_id INTEGER NOT NULL,
                        provider TEXT NOT NULL,
                        expression TEXT NOT NULL,
                        position INTEGER NOT NULL,

                        UNIQUE(player_reply_id, position),

                        FOREIGN KEY(player_reply_id)
                            REFERENCES dialogue_transition_player_reply(id)
                            ON DELETE CASCADE,

                        CHECK (position > 0)
                    );
                    """);

            /*
             * Migration des répliques AUTO créées avec le modèle V10.
             */
            statement.execute("""
                    INSERT INTO dialogue_transition_player_reply (
                        transition_id,
                        text,
                        position
                    )
                    SELECT
                        id,
                        label,
                        1
                    FROM dialogue_transition
                    WHERE type = 'AUTO'
                      AND label IS NOT NULL
                      AND length(trim(label)) > 0;
                    """);

            /*
             * Le label redevient réservé à CHOICE côté modèle métier.
             * Le CHECK de V10 autorise déjà AUTO + label NULL, donc aucune
             * reconstruction risquée de dialogue_transition n'est nécessaire.
             */
            statement.execute("""
                    UPDATE dialogue_transition
                    SET label = NULL
                    WHERE type = 'AUTO';
                    """);

            statement.execute("""
                    CREATE INDEX idx_dialogue_player_reply_transition
                    ON dialogue_transition_player_reply(transition_id);
                    """);

            statement.execute("""
                    CREATE INDEX idx_dialogue_player_reply_condition_reply
                    ON dialogue_player_reply_condition(player_reply_id);
                    """);

            statement.execute("""
                    CREATE INDEX idx_dialogue_player_reply_action_reply
                    ON dialogue_player_reply_action(player_reply_id);
                    """);
        }
    }
}
