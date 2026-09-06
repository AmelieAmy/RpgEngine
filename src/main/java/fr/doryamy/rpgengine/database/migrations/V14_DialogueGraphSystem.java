package fr.doryamy.rpgengine.database.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Remplace intégralement l'ancien système de dialogues
 * par le nouveau modèle orienté graphe.
 *
 * <p>Cette migration est volontairement destructive
 * pour les anciennes données de dialogue.
 *
 * <p>Le nouveau stockage reflète directement
 * le domaine :
 *
 * <pre>
 * dialogue
 *   |
 *   +-- dialogue_graph_element
 *   |       |
 *   |       +-- dialogue_condition
 *   |       +-- dialogue_action
 *   |
 *   +-- dialogue_graph_link
 * </pre>
 *
 * <p>Les éléments du graphe sont :
 *
 * <ul>
 *     <li>START</li>
 *     <li>REPLY</li>
 *     <li>BRANCH</li>
 *     <li>CHOICE</li>
 *     <li>END</li>
 * </ul>
 *
 * <p>Les liens ne portent aucune logique métier.
 * Les conditions et actions appartiennent uniquement
 * aux éléments métier qui peuvent posséder des règles.
 */
public final class V14_DialogueGraphSystem
        implements Migration {

    @Override
    public int getVersion() {
        return 14;
    }

    @Override
    public void apply(
            Connection connection
    ) throws SQLException {

        try (
                Statement statement =
                        connection.createStatement()
        ) {

            /*
             * MigrationManager exécute déjà les migrations
             * dans une transaction.
             *
             * On ne tente donc pas de désactiver
             * PRAGMA foreign_keys ici.
             *
             * Les anciennes tables dialogue possèdent
             * notamment des références croisées entre
             * dialogue et dialogue_node.
             */
            statement.execute(
                    "PRAGMA defer_foreign_keys = ON;"
            );

            /*
             * ====================================================
             * Suppression du nouveau schéma éventuel
             * ====================================================
             *
             * Utile pendant le développement si une ancienne
             * version locale de V14 a laissé ces tables.
             */

            statement.execute("""
                    DROP TABLE IF EXISTS dialogue_action;
                    """);

            statement.execute("""
                    DROP TABLE IF EXISTS dialogue_condition;
                    """);

            statement.execute("""
                    DROP TABLE IF EXISTS dialogue_graph_link;
                    """);

            statement.execute("""
                    DROP TABLE IF EXISTS dialogue_graph_element;
                    """);

            /*
             * ====================================================
             * Suppression de l'ancien système de dialogue
             * ====================================================
             */

            statement.execute("""
                    DROP TABLE IF EXISTS dialogue_node_action;
                    """);

            statement.execute("""
                    DROP TABLE IF EXISTS dialogue_node_condition;
                    """);

            statement.execute("""
                    DROP TABLE IF EXISTS dialogue_player_reply_action;
                    """);

            statement.execute("""
                    DROP TABLE IF EXISTS dialogue_player_reply_condition;
                    """);

            statement.execute("""
                    DROP TABLE IF EXISTS dialogue_transition_player_reply;
                    """);

            statement.execute("""
                    DROP TABLE IF EXISTS dialogue_transition_action;
                    """);

            statement.execute("""
                    DROP TABLE IF EXISTS dialogue_transition_condition;
                    """);

            statement.execute("""
                    DROP TABLE IF EXISTS dialogue_transition;
                    """);

            statement.execute("""
                    DROP TABLE IF EXISTS dialogue_node;
                    """);

            /*
             * Ancienne table V7.
             *
             * Elle ne devrait normalement plus exister après V8,
             * mais la suppression reste sans danger.
             */
            statement.execute("""
                    DROP TABLE IF EXISTS dialogue_line;
                    """);

            statement.execute("""
                    DROP TABLE IF EXISTS dialogue;
                    """);

            /*
             * ====================================================
             * Dialogue
             * ====================================================
             */

            statement.execute("""
                    CREATE TABLE dialogue (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,

                        key TEXT NOT NULL,
                        name TEXT NOT NULL,

                        UNIQUE(key),

                        CHECK(trim(key) <> ''),
                        CHECK(trim(name) <> '')
                    );
                    """);

            /*
             * ====================================================
             * Eléments du graphe
             * ====================================================
             *
             * START
             *   aucun speaker/text/position
             *
             * REPLY
             *   speaker = NPC | PLAYER
             *   text obligatoire
             *
             * BRANCH
             *   aucun speaker/text/position
             *
             * CHOICE
             *   text obligatoire
             *   position >= 0
             *
             * END
             *   aucun speaker/text/position
             */

            statement.execute("""
                    CREATE TABLE dialogue_graph_element (
                        id INTEGER PRIMARY KEY AUTOINCREMENT,

                        dialogue_id INTEGER NOT NULL,

                        key TEXT NOT NULL,
                        type TEXT NOT NULL,

                        speaker TEXT,
                        text TEXT,
                        position INTEGER,

                        FOREIGN KEY (dialogue_id)
                            REFERENCES dialogue(id)
                            ON DELETE CASCADE,

                        UNIQUE(dialogue_id, key),

                        /*
                         * Nécessaire aux FK composites
                         * de dialogue_graph_link.
                         */
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
                                AND speaker IS NULL
                                AND text IS NULL
                                AND position IS NULL
                            )
                            OR
                            (
                                type = 'REPLY'
                                AND speaker IN ('NPC', 'PLAYER')
                                AND text IS NOT NULL
                                AND trim(text) <> ''
                                AND position IS NULL
                            )
                            OR
                            (
                                type = 'BRANCH'
                                AND speaker IS NULL
                                AND text IS NULL
                                AND position IS NULL
                            )
                            OR
                            (
                                type = 'CHOICE'
                                AND speaker IS NULL
                                AND text IS NOT NULL
                                AND trim(text) <> ''
                                AND position IS NOT NULL
                                AND position >= 0
                            )
                            OR
                            (
                                type = 'END'
                                AND speaker IS NULL
                                AND text IS NULL
                                AND position IS NULL
                            )
                        )
                    );
                    """);

            statement.execute("""
                    CREATE INDEX idx_dialogue_graph_element_dialogue
                    ON dialogue_graph_element(dialogue_id);
                    """);

            /*
             * Un DialogueGraph ne peut contenir
             * qu'un seul START.
             *
             * L'existence obligatoire d'un START
             * reste vérifiée par DialogueGraph /
             * DialogueValidator.
             */
            statement.execute("""
                    CREATE UNIQUE INDEX ux_dialogue_graph_element_start
                    ON dialogue_graph_element(dialogue_id)
                    WHERE type = 'START';
                    """);

            /*
             * ====================================================
             * Liens du graphe
             * ====================================================
             *
             * Un lien représente uniquement :
             *
             * source -> target
             *
             * Aucune condition, action, étiquette,
             * position ou notion terminale.
             *
             * Les FK composites garantissent que
             * source et target appartiennent bien
             * au même dialogue.
             */

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

                        FOREIGN KEY (
                            source_element_id,
                            dialogue_id
                        )
                            REFERENCES dialogue_graph_element(
                                id,
                                dialogue_id
                            )
                            ON DELETE CASCADE,

                        FOREIGN KEY (
                            target_element_id,
                            dialogue_id
                        )
                            REFERENCES dialogue_graph_element(
                                id,
                                dialogue_id
                            )
                            ON DELETE CASCADE
                    );
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

            /*
             * ====================================================
             * Conditions
             * ====================================================
             *
             * Une condition appartient à un élément
             * métier du graphe.
             *
             * DialogueRules garantit côté domaine
             * que seuls REPLY et CHOICE peuvent
             * posséder des règles.
             */

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
                    CREATE INDEX idx_dialogue_condition_element
                    ON dialogue_condition(element_id);
                    """);

            /*
             * ====================================================
             * Actions
             * ====================================================
             *
             * La position représente l'ordre
             * d'exécution des actions du propriétaire.
             */

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
                    CREATE INDEX idx_dialogue_action_element
                    ON dialogue_action(element_id);
                    """);
        }
    }
}