package fr.doryamy.rpgengine.dialogue;

import fr.doryamy.rpgengine.trigger.TriggerContext;

import java.util.*;

/**
 * Registre des sessions de dialogue actuellement
 * actives.
 */
public final class DialogueSessionManager {

    private final Map<UUID, DialogueSession> sessions =
            new HashMap<>();

    /**
     * Crée et enregistre une nouvelle session.
     *
     * <p>Un joueur ne peut avoir qu'un dialogue
     * actif à la fois.
     */
    public DialogueSession create(
            Dialogue dialogue,
            TriggerContext context
    ) {

        Objects.requireNonNull(
                dialogue,
                "dialogue"
        );

        Objects.requireNonNull(
                context,
                "context"
        );

        UUID playerUuid =
                context.getPlayer()
                        .getUniqueId();

        if (sessions.containsKey(
                playerUuid
        )) {

            throw new IllegalStateException(
                    "Le joueur "
                            + playerUuid
                            + " possède déjà une session "
                            + "de dialogue active."
            );
        }

        DialogueSession session =
                new DialogueSession(
                        playerUuid,
                        dialogue,
                        context
                );

        sessions.put(
                playerUuid,
                session
        );

        return session;
    }

    /**
     * Recherche la session active d'un joueur.
     */
    public Optional<DialogueSession> find(
            UUID playerUuid
    ) {

        Objects.requireNonNull(
                playerUuid,
                "playerUuid"
        );

        return Optional.ofNullable(
                sessions.get(
                        playerUuid
                )
        );
    }

    /**
     * Retourne la session active ou échoue.
     */
    public DialogueSession require(
            UUID playerUuid
    ) {

        return find(
                playerUuid
        ).orElseThrow(() ->
                new IllegalStateException(
                        "Aucune session de dialogue active "
                                + "pour le joueur "
                                + playerUuid
                                + "."
                )
        );
    }

    /**
     * Termine et retire la session d'un joueur.
     *
     * @return session qui était active
     */
    public Optional<DialogueSession> remove(
            UUID playerUuid
    ) {

        Objects.requireNonNull(
                playerUuid,
                "playerUuid"
        );

        return Optional.ofNullable(
                sessions.remove(
                        playerUuid
                )
        );
    }

    /**
     * Indique si le joueur possède actuellement
     * une session.
     */
    public boolean hasActiveSession(
            UUID playerUuid
    ) {

        Objects.requireNonNull(
                playerUuid,
                "playerUuid"
        );

        return sessions.containsKey(
                playerUuid
        );
    }

    /**
     * Supprime toutes les sessions.
     *
     * <p>Principalement destiné à l'arrêt
     * du moteur/plugin.
     */
    public void clear() {
        sessions.clear();
    }
}