package fr.doryamy.rpgengine.dialogue;

import fr.doryamy.rpgengine.util.RpgLogger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Repository chargé de la persistance
 * des dialogues de RPGEngine.
 *
 * Cette classe est responsable uniquement
 * des opérations SQL liées aux dialogues
 * et à leurs lignes.
 *
 * Elle ne contient aucune logique métier
 * d'administration ou d'affichage.
 */
public final class DialogueRepository {

    private final Connection connection;

    /**
     * Construit le repository.
     *
     * @param connection connexion SQLite
     */
    public DialogueRepository(Connection connection) {
        this.connection = connection;
    }

    /**
     * Recherche un dialogue à partir
     * de sa clé métier.
     *
     * @param key clé du dialogue
     * @return dialogue correspondant s'il existe
     */
    public Optional<Dialogue> findByKey(String key) {

        String sql = """
                SELECT
                    d.id AS dialogue_id,
                    d.key AS dialogue_key,
                    d.name AS dialogue_name,
                    dl.position,
                    dl.text
                FROM dialogue d
                LEFT JOIN dialogue_line dl
                    ON dl.dialogue_id = d.id
                WHERE d.key = ?
                ORDER BY dl.position
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, key);

            try (ResultSet result = statement.executeQuery()) {

                if (!result.next()) {
                    return Optional.empty();
                }

                String dialogueKey =
                        result.getString("dialogue_key");

                String dialogueName =
                        result.getString("dialogue_name");

                List<DialogueLine> lines =
                        new ArrayList<>();

                do {
                    String text =
                            result.getString("text");

                    /*
                     * Le LEFT JOIN permet à un dialogue
                     * d'exister sans contenir encore de ligne.
                     */
                    if (text != null) {
                        lines.add(
                                new DialogueLine(
                                        result.getInt("position"),
                                        text
                                )
                        );
                    }

                } while (result.next());

                return Optional.of(
                        new Dialogue(
                                dialogueKey,
                                dialogueName,
                                lines
                        )
                );
            }

        } catch (SQLException e) {

            RpgLogger.error(
                    "Impossible de charger le dialogue '"
                            + key
                            + "' : "
                            + e.getMessage()
            );

            return Optional.empty();
        }
    }

    /**
     * Retourne tous les dialogues disponibles.
     *
     * @return liste des dialogues
     */
    public List<Dialogue> findAll() {

        List<Dialogue> dialogues =
                new ArrayList<>();

        String sql = """
                SELECT key
                FROM dialogue
                ORDER BY key
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql);

                ResultSet result =
                        statement.executeQuery()
        ) {
            while (result.next()) {

                findByKey(
                        result.getString("key")
                ).ifPresent(
                        dialogues::add
                );
            }

        } catch (SQLException e) {

            RpgLogger.error(
                    "Impossible de charger la liste des dialogues : "
                            + e.getMessage()
            );
        }

        return dialogues;
    }

    /**
     * Vérifie l'existence d'un dialogue.
     *
     * @param key clé du dialogue
     * @return true si le dialogue existe
     */
    public boolean exists(String key) {

        String sql = """
                SELECT 1
                FROM dialogue
                WHERE key = ?
                LIMIT 1
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, key);

            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }

        } catch (SQLException e) {

            RpgLogger.error(
                    "Impossible de vérifier le dialogue '"
                            + key
                            + "' : "
                            + e.getMessage()
            );

            return false;
        }
    }

    /**
     * Crée un dialogue.
     *
     * @param key clé métier
     * @param name nom lisible
     * @return true si la création a réussi
     */
    public boolean create(
            String key,
            String name
    ) {

        String sql = """
                INSERT INTO dialogue (
                    key,
                    name
                )
                VALUES (?, ?)
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, key);
            statement.setString(2, name);

            return statement.executeUpdate() == 1;

        } catch (SQLException e) {

            RpgLogger.error(
                    "Impossible de créer le dialogue '"
                            + key
                            + "' : "
                            + e.getMessage()
            );

            return false;
        }
    }

    /**
     * Supprime un dialogue.
     *
     * Ses lignes sont supprimées automatiquement
     * grâce à la contrainte ON DELETE CASCADE.
     *
     * @param key clé du dialogue
     * @return true si un dialogue a été supprimé
     */
    public boolean delete(String key) {

        String sql = """
                DELETE FROM dialogue
                WHERE key = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, key);

            return statement.executeUpdate() == 1;

        } catch (SQLException e) {

            RpgLogger.error(
                    "Impossible de supprimer le dialogue '"
                            + key
                            + "' : "
                            + e.getMessage()
            );

            return false;
        }
    }

    /**
     * Retourne la prochaine position disponible
     * pour une nouvelle ligne.
     *
     * @param key clé du dialogue
     * @return prochaine position
     */
    public int getNextLinePosition(String key) {

        String sql = """
                SELECT COALESCE(MAX(dl.position), 0) + 1
                    AS next_position
                FROM dialogue d
                LEFT JOIN dialogue_line dl
                    ON dl.dialogue_id = d.id
                WHERE d.key = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, key);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return result.getInt("next_position");
                }
            }

        } catch (SQLException e) {

            RpgLogger.error(
                    "Impossible de déterminer la prochaine ligne du dialogue '"
                            + key
                            + "' : "
                            + e.getMessage()
            );
        }

        return 1;
    }

    /**
     * Ajoute une ligne à un dialogue.
     *
     * @param key clé du dialogue
     * @param position position de la ligne
     * @param text texte
     * @return true si l'ajout a réussi
     */
    public boolean addLine(
            String key,
            int position,
            String text
    ) {

        String sql = """
                INSERT INTO dialogue_line (
                    dialogue_id,
                    position,
                    text
                )
                SELECT
                    id,
                    ?,
                    ?
                FROM dialogue
                WHERE key = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setInt(1, position);
            statement.setString(2, text);
            statement.setString(3, key);

            return statement.executeUpdate() == 1;

        } catch (SQLException e) {

            RpgLogger.error(
                    "Impossible d'ajouter une ligne au dialogue '"
                            + key
                            + "' : "
                            + e.getMessage()
            );

            return false;
        }
    }

    /**
     * Modifie une ligne existante.
     *
     * @param key clé du dialogue
     * @param position position de la ligne
     * @param text nouveau texte
     * @return true si la ligne a été modifiée
     */
    public boolean updateLine(
            String key,
            int position,
            String text
    ) {

        String sql = """
                UPDATE dialogue_line
                SET text = ?
                WHERE dialogue_id = (
                    SELECT id
                    FROM dialogue
                    WHERE key = ?
                )
                AND position = ?
                """;

        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, text);
            statement.setString(2, key);
            statement.setInt(3, position);

            return statement.executeUpdate() == 1;

        } catch (SQLException e) {

            RpgLogger.error(
                    "Impossible de modifier la ligne "
                            + position
                            + " du dialogue '"
                            + key
                            + "' : "
                            + e.getMessage()
            );

            return false;
        }
    }

    /**
     * Supprime une ligne d'un dialogue.
     *
     * @param key clé du dialogue
     * @param position position de la ligne
     * @return true si une ligne a été supprimée
     */
    public boolean removeLine(
            String key,
            int position
    ) {
        String sql = """
                DELETE FROM dialogue_line
                WHERE dialogue_id = (
                    SELECT id
                    FROM dialogue
                    WHERE key = ?
                )
                AND position = ?
                """;
        try (
                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {
            statement.setString(1, key);
            statement.setInt(2, position);
            return statement.executeUpdate() == 1;

        } catch (SQLException e) {
            RpgLogger.error(
                    "Impossible de supprimer la ligne "
                            + position
                            + " du dialogue '"
                            + key
                            + "' : "
                            + e.getMessage()
            );
            return false;
        }
    }
}