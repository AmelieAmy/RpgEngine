package fr.doryamy.rpgengine.dialogue;

import fr.doryamy.rpgengine.command.CommandResult;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.util.List;
import java.util.Optional;

/**
 * Fournit les opérations métier permettant
 * d'administrer les dialogues de RPGEngine.
 *
 * Cette classe constitue le point d'entrée
 * commun des outils d'administration :
 *
 * - commandes ;
 * - interfaces graphiques ;
 * - futures intégrations.
 *
 * Elle ne contient aucune logique de persistance.
 * Celle-ci reste déléguée à DialogueRepository.
 */
public final class DialogueService {

    private final DialogueRepository repository;

    /**
     * Construit le service de dialogues.
     *
     * @param repository repository des dialogues
     */
    public DialogueService(
            DialogueRepository repository
    ) {
        this.repository = repository;
    }

    /**
     * Recherche un dialogue.
     *
     * @param key clé métier du dialogue
     * @return dialogue correspondant s'il existe
     */
    public Optional<Dialogue> find(String key) {
        return repository.findByKey(key);
    }

    /**
     * Retourne tous les dialogues disponibles.
     *
     * @return liste des dialogues
     */
    public List<Dialogue> findAll() {
        return repository.findAll();
    }

    /**
     * Crée un nouveau dialogue.
     *
     * @param key clé métier
     * @param name nom lisible
     *
     * @return résultat de l'opération
     */
    public CommandResult create(
            String key,
            String name
    ) {
        if (key == null || key.isBlank()) {
            return CommandResult.failure(
                    "La clé du dialogue ne peut pas être vide."
            );
        }

        if (name == null || name.isBlank()) {
            return CommandResult.failure(
                    "Le nom du dialogue ne peut pas être vide."
            );
        }

        if (repository.exists(key)) {
            return CommandResult.failure(
                    "Un dialogue portant la clé '"
                            + key
                            + "' existe déjà."
            );
        }

        boolean created =
                repository.create(
                        key,
                        name
                );

        if (!created) {
            return CommandResult.failure(
                    "Impossible de créer le dialogue '"
                            + key
                            + "'."
            );
        }

        RpgLogger.debug(
                "Dialogue créé : "
                        + key
        );

        return CommandResult.success(
                "Dialogue créé : "
                        + key
        );
    }

    /**
     * Supprime un dialogue.
     *
     * Les lignes associées sont supprimées
     * automatiquement par SQLite.
     *
     * @param key clé du dialogue
     * @return résultat de l'opération
     */
    public CommandResult delete(String key) {

        if (key == null || key.isBlank()) {
            return CommandResult.failure(
                    "La clé du dialogue ne peut pas être vide."
            );
        }

        if (!repository.exists(key)) {
            return CommandResult.failure(
                    "Dialogue introuvable : "
                            + key
            );
        }

        boolean deleted =
                repository.delete(key);

        if (!deleted) {
            return CommandResult.failure(
                    "Impossible de supprimer le dialogue '"
                            + key
                            + "'."
            );
        }

        RpgLogger.debug(
                "Dialogue supprimé : "
                        + key
        );

        return CommandResult.success(
                "Dialogue supprimé : "
                        + key
        );
    }

    /**
     * Ajoute une ligne à la fin d'un dialogue.
     *
     * @param key clé du dialogue
     * @param text texte de la ligne
     *
     * @return résultat de l'opération
     */
    public CommandResult addLine(
            String key,
            String text
    ) {
        if (key == null || key.isBlank()) {
            return CommandResult.failure(
                    "La clé du dialogue ne peut pas être vide."
            );
        }

        if (!repository.exists(key)) {
            return CommandResult.failure(
                    "Dialogue introuvable : "
                            + key
            );
        }

        if (text == null || text.isBlank()) {
            return CommandResult.failure(
                    "Le texte de la ligne ne peut pas être vide."
            );
        }

        int nextPosition =
                repository.getNextLinePosition(key);

        boolean added =
                repository.addLine(
                        key,
                        nextPosition,
                        text
                );

        if (!added) {
            return CommandResult.failure(
                    "Impossible d'ajouter une ligne au dialogue '"
                            + key
                            + "'."
            );
        }

        RpgLogger.debug(
                "Ligne ajoutée au dialogue "
                        + key
                        + " | position="
                        + nextPosition
        );

        return CommandResult.success(
                "Ligne "
                        + nextPosition
                        + " ajoutée au dialogue "
                        + key
                        + "."
        );
    }

    /**
     * Modifie le texte d'une ligne existante.
     *
     * @param key clé du dialogue
     * @param position position de la ligne
     * @param text nouveau texte
     *
     * @return résultat de l'opération
     */
    public CommandResult updateLine(
            String key,
            int position,
            String text
    ) {
        if (key == null || key.isBlank()) {
            return CommandResult.failure(
                    "La clé du dialogue ne peut pas être vide."
            );
        }

        if (!repository.exists(key)) {
            return CommandResult.failure(
                    "Dialogue introuvable : "
                            + key
            );
        }

        if (position <= 0) {
            return CommandResult.failure(
                    "La position doit être supérieure à 0."
            );
        }

        if (text == null || text.isBlank()) {
            return CommandResult.failure(
                    "Le texte de la ligne ne peut pas être vide."
            );
        }

        boolean updated =
                repository.updateLine(
                        key,
                        position,
                        text
                );

        if (!updated) {
            return CommandResult.failure(
                    "Aucune ligne trouvée à la position "
                            + position
                            + " dans le dialogue '"
                            + key
                            + "'."
            );
        }

        RpgLogger.debug(
                "Ligne modifiée : "
                        + key
                        + " | position="
                        + position
        );

        return CommandResult.success(
                "Ligne "
                        + position
                        + " modifiée."
        );
    }

    /**
     * Supprime une ligne d'un dialogue.
     *
     * @param key clé du dialogue
     * @param position position de la ligne
     *
     * @return résultat de l'opération
     */
    public CommandResult removeLine(
            String key,
            int position
    ) {
        if (key == null || key.isBlank()) {
            return CommandResult.failure(
                    "La clé du dialogue ne peut pas être vide."
            );
        }

        if (!repository.exists(key)) {
            return CommandResult.failure(
                    "Dialogue introuvable : "
                            + key
            );
        }

        if (position <= 0) {
            return CommandResult.failure(
                    "La position doit être supérieure à 0."
            );
        }

        boolean removed =
                repository.removeLine(
                        key,
                        position
                );

        if (!removed) {
            return CommandResult.failure(
                    "Aucune ligne trouvée à la position "
                            + position
                            + " dans le dialogue '"
                            + key
                            + "'."
            );
        }

        RpgLogger.debug(
                "Ligne supprimée : "
                        + key
                        + " | position="
                        + position
        );

        return CommandResult.success(
                "Ligne "
                        + position
                        + " supprimée."
        );
    }
}