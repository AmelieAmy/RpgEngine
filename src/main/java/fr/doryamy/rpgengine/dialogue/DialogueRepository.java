package fr.doryamy.rpgengine.dialogue;

import java.util.List;
import java.util.Optional;

/**
 * Port de persistance des dialogues.
 *
 * <p>Le repository manipule des agrégats Dialogue complets.
 * Il ne contient aucune opération métier sur le graphe.
 *
 * <p>Optional.empty() signifie exclusivement :
 * le dialogue demandé n'existe pas.
 *
 * <p>Une erreur de persistance doit lever une exception
 * et ne doit jamais être transformée en absence de données.
 */
public interface DialogueRepository {

    /**
     * Recherche un dialogue par sa clé.
     */
    Optional<Dialogue> findByKey(
            DialogueKey key
    );

    /**
     * Retourne tous les dialogues persistés.
     */
    List<Dialogue> findAll();

    /**
     * Insère un nouveau dialogue.
     *
     * <p>L'opération doit échouer si cette clé
     * existe déjà.
     */
    void insert(
            Dialogue dialogue
    );

    /**
     * Remplace l'état persisté d'un dialogue existant
     * par l'agrégat fourni.
     *
     * <p>L'opération doit échouer si le dialogue
     * n'existe pas.
     */
    void update(
            Dialogue dialogue
    );

    /**
     * Supprime un dialogue existant.
     *
     * <p>L'opération doit échouer si le dialogue
     * n'existe pas.
     */
    void delete(
            DialogueKey key
    );
}