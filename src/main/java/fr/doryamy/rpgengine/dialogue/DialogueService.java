package fr.doryamy.rpgengine.dialogue;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Gère le cycle de vie des agrégats Dialogue.
 *
 * <p>Ce service ne connaît pas les détails
 * de la topologie du graphe.
 *
 * <p>Les mutations structurelles sont déléguées
 * à DialogueGraphService, les règles à
 * DialogueRuleService et le contenu à
 * DialogueContentService.
 */
public final class DialogueService {

    private final DialogueRepository repository;
    private final DialogueGraphService graphService;
    private final DialogueValidator validator;
    private final DialogueKeyGenerator keyGenerator;

    public DialogueService(
            DialogueRepository repository,
            DialogueGraphService graphService,
            DialogueValidator validator,
            DialogueKeyGenerator keyGenerator
    ) {

        this.repository =
                Objects.requireNonNull(
                        repository,
                        "repository"
                );

        this.graphService =
                Objects.requireNonNull(
                        graphService,
                        "graphService"
                );

        this.validator =
                Objects.requireNonNull(
                        validator,
                        "validator"
                );

        this.keyGenerator =
                Objects.requireNonNull(
                        keyGenerator,
                        "keyGenerator"
                );
    }

    /**
     * Crée et persiste un nouveau dialogue.
     *
     * <pre>
     * Start -> End
     * </pre>
     */
    public Dialogue create(
            String name
    ) {

        DialogueKey key =
                nextKey();

        /*
         * Une collision UUID est extrêmement improbable,
         * mais l'invariant d'identité ne doit pas dépendre
         * d'une probabilité.
         */
        if (repository.findByKey(key)
                .isPresent()) {

            throw new DialogueAlreadyExistsException(
                    key
            );
        }

        DialogueGraph graph =
                graphService.createEmptyGraph();

        Dialogue dialogue =
                new Dialogue(
                        key,
                        name,
                        DialogueRules.empty(),
                        graph
                );

        requireValid(
                dialogue
        );

        repository.insert(
                dialogue
        );

        return dialogue;
    }

    /**
     * Recherche un dialogue.
     */
    public Optional<Dialogue> find(
            DialogueKey key
    ) {

        Objects.requireNonNull(
                key,
                "key"
        );

        return repository.findByKey(
                key
        );
    }

    /**
     * Retourne un dialogue ou échoue
     * s'il n'existe pas.
     */
    public Dialogue require(
            DialogueKey key
    ) {

        Objects.requireNonNull(
                key,
                "key"
        );

        return repository.findByKey(
                        key
                )
                .orElseThrow(() ->
                        new DialogueNotFoundException(
                                key
                        )
                );
    }

    /**
     * Retourne tous les dialogues.
     */
    public List<Dialogue> findAll() {

        return List.copyOf(
                repository.findAll()
        );
    }

    /**
     * Renomme un dialogue.
     */
    public Dialogue rename(
            DialogueKey key,
            String name
    ) {

        Dialogue current =
                require(
                        key
                );

        Dialogue updated =
                new Dialogue(
                        current.key(),
                        name,
                        current.rules(),
                        current.graph()
                );

        requireValid(
                updated
        );

        repository.update(
                updated
        );

        return updated;
    }

    /**
     * Persiste un nouveau graphe pour
     * un dialogue existant.
     *
     * <p>Cette méthode ne construit ni ne transforme
     * le graphe. Elle constitue uniquement la frontière
     * de persistance après une mutation métier.
     */
    public Dialogue replaceGraph(
            DialogueKey key,
            DialogueGraph graph
    ) {

        Objects.requireNonNull(
                graph,
                "graph"
        );

        Dialogue current =
                require(
                        key
                );

        Dialogue updated =
                new Dialogue(
                        current.key(),
                        current.name(),
                        current.rules(),
                        graph
                );

        requireValid(
                updated
        );

        repository.update(
                updated
        );

        return updated;
    }



    /**
     * Persiste de nouvelles règles globales pour
     * un dialogue existant sans modifier son graphe.
     */
    public Dialogue replaceRules(
            DialogueKey key,
            DialogueRules rules
    ) {

        Objects.requireNonNull(
                rules,
                "rules"
        );

        Dialogue current =
                require(
                        key
                );

        Dialogue updated =
                new Dialogue(
                        current.key(),
                        current.name(),
                        rules,
                        current.graph()
                );

        requireValid(
                updated
        );

        repository.update(
                updated
        );

        return updated;
    }
    /**
     * Supprime définitivement un dialogue.
     */
    public void delete(
            DialogueKey key
    ) {

        Objects.requireNonNull(
                key,
                "key"
        );

        /*
         * On vérifie explicitement son existence
         * afin que la sémantique du service soit
         * indépendante du comportement SQL futur.
         */
        require(
                key
        );

        repository.delete(
                key
        );
    }

    private DialogueKey nextKey() {

        return Objects.requireNonNull(
                keyGenerator.generate(),
                "Le générateur de clés de dialogue "
                        + "a retourné null."
        );
    }

    private void requireValid(
            Dialogue dialogue
    ) {

        DialogueValidationResult result =
                validator.validate(
                        dialogue
                );

        if (result.isValid()) {
            return;
        }

        throw new IllegalStateException(
                "Le dialogue "
                        + dialogue.key()
                        + " est invalide : "
                        + String.join(
                        " | ",
                        result.errors()
                )
        );
    }
}