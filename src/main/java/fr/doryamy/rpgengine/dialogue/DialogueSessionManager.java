package fr.doryamy.rpgengine.dialogue;

import fr.doryamy.rpgengine.trigger.TriggerContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Gère le cycle de vie des sessions de dialogue actives.
 *
 * Cette classe est responsable :
 *   de créer une session ;
 *   de rechercher une session ;
 *   de supprimer une session ;
 *   de vérifier l'existence d'une session.
 *
 * Elle ne contient aucune logique d'exécution ou de navigation.
 */
public final class DialogueSessionManager {

    private final Map<UUID, DialogueSession> sessions =
            new HashMap<>();

    /**
     * Crée ou remplace la session active d'un joueur.
     *
     * @param dialogue dialogue démarré
     * @param context contexte d'exécution
     *
     * @return session créée
     */
    public DialogueSession create(
            Dialogue dialogue,
            TriggerContext context
    ) {
        DialogueSession session =
                new DialogueSession(
                        context.getPlayer()
                                .getUniqueId(),
                        dialogue,
                        dialogue.getStartNodeKey(),
                        context
                );

        sessions.put(
                session.getPlayerUuid(),
                session
        );

        return session;
    }

    /**
     * Recherche la session active d'un joueur.
     *
     * @param playerUuid UUID du joueur
     * @return session si elle existe
     */
    public Optional<DialogueSession> find(
            UUID playerUuid
    ) {
        return Optional.ofNullable(
                sessions.get(
                        playerUuid
                )
        );
    }

    /**
     * Vérifie si un joueur possède une session active.
     *
     * @param playerUuid UUID du joueur
     * @return true si une session existe
     */
    public boolean hasSession(
            UUID playerUuid
    ) {
        return sessions.containsKey(
                playerUuid
        );
    }

    /**
     * Supprime la session active d'un joueur.
     *
     * @param playerUuid UUID du joueur
     * @return session supprimée si elle existait
     */
    public Optional<DialogueSession> remove(
            UUID playerUuid
    ) {
        return Optional.ofNullable(
                sessions.remove(
                        playerUuid
                )
        );
    }

    /**
     * Supprime toutes les sessions actives.
     *
     * Utile notamment lors de l'arrêt du moteur.
     */
    public void clear() {
        sessions.clear();
    }
}