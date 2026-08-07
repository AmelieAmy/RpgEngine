package fr.doryamy.rpgengine.trigger;

import fr.doryamy.rpgengine.model.TriggerType;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * TriggerContext constitue le point d'entrée commun de tous les événements du moteur.
 *
 * Les listeners et intégrations externes construisent ce contexte,
 * puis le transmettent au TriggerManager.
 *
 * Il transporte toutes les informations nécessaires à l'évaluation
 * des conditions et à l'exécution des actions.
 *
 * Cette classe est immuable et ne contient aucune logique métier.
 */
public final class TriggerContext {

    private final Player player;
    private final TriggerType triggerType;
    private final String targetId;
    private final Map<String, Object> attributes;

    private TriggerContext(Builder builder) {
        this.player = Objects.requireNonNull(
                builder.player,
                "Le joueur ne peut pas être null."
        );

        this.triggerType = Objects.requireNonNull(
                builder.triggerType,
                "Le type de trigger ne peut pas être null."
        );

        if (builder.targetId == null || builder.targetId.isBlank()) {
            throw new IllegalArgumentException(
                    "L'identifiant de la cible ne peut pas être vide."
            );
        }

        this.targetId = builder.targetId;

        this.attributes = Collections.unmodifiableMap(
                new HashMap<>(builder.attributes)
        );
    }

    /**
     * Construit progressivement un TriggerContext.
     *
     * Le Builder garantit qu'un contexte valide est toujours créé
     * avant d'être transmis au TriggerManager.
     */
    public static Builder builder() {
        return new Builder();
    }

    public Player getPlayer() {
        return player;
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public String getTargetId() {
        return targetId;
    }

    /**
     * Retourne une vue immuable de tous les attributs spécifiques à l'événement.
     *
     * Les informations communes (joueur, trigger, cible...)
     * sont accessibles via leurs getters dédiés.
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Retourne un attribut sans conversion de type.
     *
     * Les attributs permettent d'étendre
     * TriggerContext sans modifier sa structure.
     */
    public Optional<Object> getAttribute(String key) {
        return Optional.ofNullable(
                attributes.get(key)
        );
    }

    /**
     * Retourne un attribut uniquement s'il correspond au type demandé.
     *
     * Exemple :
     *
     * context.getAttribute("location", Location.class)
     */
    public <T> Optional<T> getAttribute(
            String key,
            Class<T> expectedType
    ) {
        Objects.requireNonNull(
                key,
                "La clé ne peut pas être null."
        );

        Objects.requireNonNull(
                expectedType,
                "Le type attendu ne peut pas être null."
        );

        Object value = attributes.get(key);

        if (!expectedType.isInstance(value)) {
            return Optional.empty();
        }

        return Optional.of(
                expectedType.cast(value)
        );
    }

    /**
     * Constructeur progressif d'un TriggerContext.
     */
    public static final class Builder {

        private Player player;
        private TriggerType triggerType;
        private String targetId;
        private final Map<String, Object> attributes =
                new HashMap<>();

        private Builder() {
        }

        public Builder player(Player player) {
            this.player = player;
            return this;
        }

        public Builder triggerType(
                TriggerType triggerType
        ) {
            this.triggerType = triggerType;
            return this;
        }

        public Builder targetId(String targetId) {
            this.targetId = targetId;
            return this;
        }

        /**
         * Ajoute une information spécifique à l'événement.
         *
         * Une même clé ne peut pas être ajoutée deux fois
         * silencieusement.
         */
        public Builder attribute(
                String key,
                Object value
        ) {
            if (key == null || key.isBlank()) {
                throw new IllegalArgumentException(
                        "La clé d'un attribut ne peut pas être vide."
                );
            }

            Objects.requireNonNull(
                    value,
                    "La valeur d'un attribut ne peut pas être null."
            );

            if (attributes.containsKey(key)) {
                throw new IllegalArgumentException(
                        "L'attribut '" + key + "' existe déjà."
                );
            }

            attributes.put(key, value);

            return this;
        }

        public TriggerContext build() {
            return new TriggerContext(this);
        }
    }
}