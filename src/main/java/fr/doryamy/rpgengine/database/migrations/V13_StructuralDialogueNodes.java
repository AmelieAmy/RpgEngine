package fr.doryamy.rpgengine.database.migrations;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Introduit des nodes structurels invisibles dans le graphe de dialogue.
 *
 * <p>Ils permettent de représenter un embranchement à n'importe quel
 * point narratif sans transformer artificiellement une réplique PNJ
 * en source métier de l'embranchement.
 *
 * <p>Les nodes existants restent NPC. Aucun changement de contrainte
 * n'est nécessaire sur dialogue_transition : une branche en cours
 * d'édition pointe vers un node STRUCTURAL ouvert plutôt que vers NULL.
 */
public final class V13_StructuralDialogueNodes implements Migration {

    @Override
    public int getVersion() {
        return 13;
    }

    @Override
    public void apply(Connection connection) throws SQLException {
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    ALTER TABLE dialogue_node
                    ADD COLUMN kind TEXT NOT NULL DEFAULT 'NPC'
                    CHECK (kind IN ('NPC', 'STRUCTURAL'));
                    """);
        }
    }
}
