package fr.doryamy.rpgengine.infrastructure.persistence.sqlite.dialogue;

import fr.doryamy.rpgengine.dialogue.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Persistance SQLite de la bibliothèque globale des profils de personnages.
 */
public final class SqliteDialogueCharacterProfileRepository
        implements DialogueCharacterProfileRepository {

    private final Connection connection;

    public SqliteDialogueCharacterProfileRepository(
            Connection connection
    ) {
        this.connection = Objects.requireNonNull(connection, "connection");
    }

    @Override
    public Optional<DialogueCharacterProfile> findByKey(
            DialogueCharacterProfileKey key
    ) {
        Objects.requireNonNull(key, "key");

        String sql = """
                SELECT key, display_name, portrait_resource, citizens_npc_id
                FROM dialogue_character_profile
                WHERE key = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, key.value());

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return Optional.empty();
                }
                return Optional.of(map(result));
            }
        } catch (SQLException e) {
            throw persistence(
                    "Impossible de charger le profil " + key + ".",
                    e
            );
        }
    }

    @Override
    public Optional<DialogueCharacterProfile> findByCitizensNpcId(
            String citizensNpcId
    ) {
        if (citizensNpcId == null || citizensNpcId.isBlank()) {
            throw new IllegalArgumentException(
                    "L'identifiant Citizens ne peut pas être vide."
            );
        }

        String sql = """
                SELECT key, display_name, portrait_resource, citizens_npc_id
                FROM dialogue_character_profile
                WHERE citizens_npc_id = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, citizensNpcId.trim());

            try (ResultSet result = statement.executeQuery()) {
                if (!result.next()) {
                    return Optional.empty();
                }
                return Optional.of(map(result));
            }
        } catch (SQLException e) {
            throw persistence(
                    "Impossible de charger le profil associé au PNJ Citizens "
                            + citizensNpcId + ".",
                    e
            );
        }
    }

    @Override
    public List<DialogueCharacterProfile> findAll() {
        String sql = """
                SELECT key, display_name, portrait_resource, citizens_npc_id
                FROM dialogue_character_profile
                ORDER BY display_name COLLATE NOCASE, id
                """;

        List<DialogueCharacterProfile> profiles = new ArrayList<>();

        try (
                PreparedStatement statement = connection.prepareStatement(sql);
                ResultSet result = statement.executeQuery()
        ) {
            while (result.next()) {
                profiles.add(map(result));
            }
            return List.copyOf(profiles);
        } catch (SQLException e) {
            throw persistence(
                    "Impossible de charger la bibliothèque de personnages.",
                    e
            );
        }
    }

    @Override
    public void insert(
            DialogueCharacterProfile profile
    ) {
        Objects.requireNonNull(profile, "profile");

        String sql = """
                INSERT INTO dialogue_character_profile (
                    key,
                    display_name,
                    portrait_resource,
                    citizens_npc_id
                )
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            bindProfile(statement, profile);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw persistence(
                    "Impossible d'insérer le profil " + profile.key() + ".",
                    e
            );
        }
    }

    @Override
    public void update(
            DialogueCharacterProfile profile
    ) {
        Objects.requireNonNull(profile, "profile");

        String sql = """
                UPDATE dialogue_character_profile
                SET display_name = ?,
                    portrait_resource = ?,
                    citizens_npc_id = ?
                WHERE key = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, profile.displayName());
            bindNullableString(statement, 2, profile.portraitResource());
            bindNullableString(statement, 3, profile.citizensNpcId());
            statement.setString(4, profile.key().value());

            if (statement.executeUpdate() != 1) {
                throw new IllegalArgumentException(
                        "Profil de personnage introuvable : "
                                + profile.key()
                );
            }
        } catch (SQLException e) {
            throw persistence(
                    "Impossible de mettre à jour le profil "
                            + profile.key() + ".",
                    e
            );
        }
    }

    @Override
    public void delete(
            DialogueCharacterProfileKey key
    ) {
        Objects.requireNonNull(key, "key");

        String sql = """
                DELETE FROM dialogue_character_profile
                WHERE key = ?
                """;

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, key.value());

            if (statement.executeUpdate() != 1) {
                throw new IllegalArgumentException(
                        "Profil de personnage introuvable : " + key
                );
            }
        } catch (SQLException e) {
            throw persistence(
                    "Impossible de supprimer le profil " + key
                            + ". Il est peut-être encore utilisé "
                            + "par un dialogue.",
                    e
            );
        }
    }

    private DialogueCharacterProfile map(
            ResultSet result
    ) throws SQLException {
        return new DialogueCharacterProfile(
                new DialogueCharacterProfileKey(
                        result.getString("key")
                ),
                result.getString("display_name"),
                result.getString("portrait_resource"),
                result.getString("citizens_npc_id")
        );
    }

    private void bindProfile(
            PreparedStatement statement,
            DialogueCharacterProfile profile
    ) throws SQLException {
        statement.setString(1, profile.key().value());
        statement.setString(2, profile.displayName());
        bindNullableString(statement, 3, profile.portraitResource());
        bindNullableString(statement, 4, profile.citizensNpcId());
    }

    private void bindNullableString(
            PreparedStatement statement,
            int index,
            String value
    ) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.VARCHAR);
        } else {
            statement.setString(index, value);
        }
    }

    private void bindNullableInteger(
            PreparedStatement statement,
            int index,
            Integer value
    ) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.INTEGER);
        } else {
            statement.setInt(index, value);
        }
    }

    private Integer nullableInteger(
            ResultSet result,
            String column
    ) throws SQLException {
        int value = result.getInt(column);
        return result.wasNull() ? null : value;
    }

    private DialoguePersistenceException persistence(
            String message,
            SQLException cause
    ) {
        return new DialoguePersistenceException(message, cause);
    }
}
