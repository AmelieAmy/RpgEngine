package fr.doryamy.rpgengine.dialogue.editor;

import fr.doryamy.rpgengine.command.CommandResult;
import fr.doryamy.rpgengine.dialogue.DialogueService;
import fr.doryamy.rpgengine.dialogue.editor.DialogueScenarioResolver.QuestBinding;
import fr.doryamy.rpgengine.model.Action;
import fr.doryamy.rpgengine.model.Condition;
import fr.doryamy.rpgengine.model.Trigger;
import fr.doryamy.rpgengine.quest.QuestState;
import fr.doryamy.rpgengine.repository.TriggerRepository;
import fr.doryamy.rpgengine.trigger.TriggerService;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Fournit les opérations métier portant
 * sur un scénario de dialogue complet.
 *
 * <p>Un scénario n'est pas une nouvelle entité
 * persistée. Il reste une composition des éléments
 * existants de RPGEngine :
 *
 * <pre>
 * Dialogue
 * + Trigger
 * + Conditions
 * + Actions
 * </pre>
 *
 * <p>Ce service orchestre leurs mutations sans
 * introduire de modèle parallèle.
 */
public final class DialogueScenarioService {

    private final DialogueService dialogueService;
    private final TriggerService triggerService;
    private final TriggerRepository triggerRepository;
    private final DialogueScenarioResolver scenarioResolver;

    public DialogueScenarioService(
            DialogueService dialogueService,
            TriggerRepository triggerRepository,
            TriggerService triggerService
    ) {
        this.dialogueService =
                dialogueService;

        this.triggerRepository =
                triggerRepository;

        this.triggerService =
                triggerService;

        this.scenarioResolver =
                new DialogueScenarioResolver(
                        triggerRepository
                );
    }

    /**
     * Crée un nouveau scénario éditorial.
     *
     * <p>La clé technique du dialogue est générée
     * automatiquement à partir du nom fourni.
     *
     * <p>Le scénario est composé exclusivement
     * d'éléments existants du moteur :
     *
     * <pre>
     * Dialogue
     * + Trigger
     * + Condition QUEST éventuelle
     * + Action DIALOG
     * </pre>
     *
     * @param request données de création
     *
     * @return résultat de l'opération
     */
    public CommandResult createScenario(
            CreateDialogueScenarioRequest request
    ) {
        /*
         * ------------------------------------------------
         * Validation générale
         * ------------------------------------------------
         */

        if (request == null) {

            return CommandResult.failure(
                    "La demande de création est invalide."
            );
        }

        String name =
                request.name();

        if (name == null
                || name.isBlank()) {

            return CommandResult.failure(
                    "Le nom du scénario ne peut pas être vide."
            );
        }

        if (request.triggerType() == null) {

            return CommandResult.failure(
                    "Le type de déclencheur doit être défini."
            );
        }

        String targetId =
                request.targetId();

        if (targetId == null
                || targetId.isBlank()) {

            return CommandResult.failure(
                    "La cible du déclencheur doit être définie."
            );
        }

        /*
         * ------------------------------------------------
         * Validation quête
         * ------------------------------------------------
         *
         * questId et initialState doivent être
         * tous les deux présents ou tous les deux absents.
         */

        boolean hasQuest =
                request.questId() != null
                        && !request.questId().isBlank();

        boolean hasState =
                request.initialState() != null;

        if (hasQuest != hasState) {

            return CommandResult.failure(
                    "Une quête et son état initial doivent être définis ensemble."
            );
        }

        if (request.initialState()
                == QuestState.UNAVAILABLE) {

            return CommandResult.failure(
                    "UNAVAILABLE est un état technique "
                            + "et ne peut pas être utilisé dans un scénario."
            );
        }

        /*
         * ------------------------------------------------
         * Génération de la clé
         * ------------------------------------------------
         */

        String dialogueKey =
                generateUniqueDialogueKey(
                        name
                );

        /*
         * ------------------------------------------------
         * Création du dialogue
         * ------------------------------------------------
         */

        CommandResult dialogueResult =
                dialogueService.create(
                        dialogueKey,
                        name
                );

        if (!dialogueResult.isSuccess()) {

            return dialogueResult;
        }

        /*
         * ------------------------------------------------
         * Construction des conditions
         * ------------------------------------------------
         */

        List<Condition> conditions;

        if (hasQuest) {

            String questExpression =
                    request.questId().trim()
                            + "=="
                            + request.initialState().name();

            conditions =
                    List.of(
                            new Condition(
                                    "QUEST",
                                    questExpression
                            )
                    );

        } else {

            conditions =
                    List.of();
        }

        /*
         * ------------------------------------------------
         * Construction des actions
         * ------------------------------------------------
         */

        List<Action> actions =
                List.of(
                        new Action(
                                "DIALOG",
                                dialogueKey,
                                1
                        )
                );

        /*
         * ------------------------------------------------
         * Création du trigger complet
         * ------------------------------------------------
         */

        String triggerName =
                "dialog_"
                        + dialogueKey;

        OptionalInt triggerResult =
                triggerService.create(
                        triggerName,
                        request.triggerType(),
                        targetId.trim(),
                        conditions,
                        actions
                );

        if (triggerResult.isEmpty()) {

            /*
             * Le dialogue a été créé mais le trigger
             * a échoué : on nettoie le dialogue.
             */
            CommandResult cleanupResult =
                    dialogueService.delete(
                            dialogueKey
                    );

            if (!cleanupResult.isSuccess()) {

                RpgLogger.error(
                        "Impossible de nettoyer le dialogue "
                                + dialogueKey
                                + " après l'échec de création du trigger : "
                                + cleanupResult.getMessage()
                );
            }

            return CommandResult.failure(
                    "Impossible de créer le déclencheur du scénario."
            );
        }

        RpgLogger.debug(
                "Scénario créé : "
                        + name
                        + " | dialogue="
                        + dialogueKey
                        + " | trigger="
                        + triggerResult.getAsInt()
        );

        return CommandResult.success(
                "Scénario créé : "
                        + name
        );
    }

    /**
     * Supprime l'ensemble d'un scénario éditorial
     * à partir d'une de ses variantes.
     *
     * <p>Deux cas sont gérés :
     *
     * <ul>
     *     <li>
     *         scénario avec quête :
     *         toutes les variantes partageant le même
     *         déclencheur, la même cible et la même quête
     *         sont supprimées ;
     *     </li>
     *     <li>
     *         scénario sans quête :
     *         seul le trigger correspondant au dialogue
     *         sélectionné appartient au scénario.
     *     </li>
     * </ul>
     *
     * <p>Un dialogue encore référencé par un trigger
     * extérieur au scénario est conservé.
     *
     * @param dialogueKey dialogue servant
     *                    de point d'entrée
     *
     * @return true si l'opération a réussi
     */
    public boolean deleteScenario(
            String dialogueKey
    ) {
        if (dialogueKey == null
                || dialogueKey.isBlank()) {

            return false;
        }

        /*
         * ------------------------------------------------
         * Trigger de référence
         * ------------------------------------------------
         */
        Optional<Trigger> referenceTriggerResult =
                scenarioResolver.findTriggerForDialogue(
                        dialogueKey
                );

        if (referenceTriggerResult.isEmpty()) {

            RpgLogger.error(
                    "Impossible de supprimer le scénario : "
                            + "aucun trigger trouvé pour le dialogue "
                            + dialogueKey
            );

            return false;
        }

        Trigger referenceTrigger =
                referenceTriggerResult.get();

        /*
         * ------------------------------------------------
         * Résolution du scénario
         * ------------------------------------------------
         *
         * Avec quête :
         * plusieurs triggers peuvent représenter
         * les variantes NOT_STARTED / ACTIVE / COMPLETED.
         *
         * Sans quête :
         * un trigger constitue à lui seul le scénario.
         */
        Optional<QuestBinding> questBindingResult =
                scenarioResolver.findQuestBinding(
                        referenceTrigger
                );

        List<Trigger> scenarioTriggers;

        if (questBindingResult.isPresent()) {

            scenarioTriggers =
                    scenarioResolver.findScenarioTriggers(
                            referenceTrigger,
                            questBindingResult.get()
                    );

        } else {

            scenarioTriggers =
                    List.of(
                            referenceTrigger
                    );
        }

        if (scenarioTriggers.isEmpty()) {

            RpgLogger.error(
                    "Impossible de supprimer le scénario : "
                            + "aucun trigger associé à "
                            + dialogueKey
            );

            return false;
        }

        /*
         * ------------------------------------------------
         * Dialogues associés
         * ------------------------------------------------
         *
         * Les clés doivent être capturées avant
         * la suppression des triggers puisque
         * les actions DIALOG disparaîtront ensuite
         * via les cascades SQLite.
         */
        List<String> dialogueKeys =
                scenarioTriggers.stream()
                        .map(
                                scenarioResolver::findDialogAction
                        )
                        .flatMap(
                                Optional::stream
                        )
                        .map(
                                Action::getExpression
                        )
                        .filter(key ->
                                key != null
                                        && !key.isBlank()
                        )
                        .distinct()
                        .toList();

        if (dialogueKeys.isEmpty()) {

            RpgLogger.error(
                    "Impossible de supprimer le scénario : "
                            + "aucun dialogue associé à "
                            + dialogueKey
            );

            return false;
        }

        /*
         * ------------------------------------------------
         * Protection des dialogues partagés
         * ------------------------------------------------
         *
         * Cette vérification doit être effectuée AVANT
         * la suppression des triggers du scénario.
         */
        Set<String> sharedDialogueKeys =
                dialogueKeys.stream()
                        .filter(key ->
                                isDialogueReferencedOutsideScenario(
                                        key,
                                        scenarioTriggers
                                )
                        )
                        .collect(
                                Collectors.toSet()
                        );

        /*
         * ------------------------------------------------
         * Suppression des triggers
         * ------------------------------------------------
         *
         * Les actions et conditions associées
         * sont supprimées automatiquement
         * par ON DELETE CASCADE.
         */
        for (Trigger trigger : scenarioTriggers) {

            if (!triggerRepository.delete(
                    trigger.getId()
            )) {

                RpgLogger.error(
                        "Impossible de supprimer le trigger "
                                + trigger.getId()
                                + " du scénario "
                                + dialogueKey
                );

                return false;
            }
        }

        /*
         * ------------------------------------------------
         * Suppression des dialogues
         * ------------------------------------------------
         *
         * Un dialogue encore utilisé par un trigger
         * extérieur au scénario est conservé.
         */
        for (String key : dialogueKeys) {

            if (sharedDialogueKeys.contains(
                    key
            )) {

                RpgLogger.debug(
                        "Dialogue conservé car encore référencé : "
                                + key
                );

                continue;
            }

            CommandResult result =
                    dialogueService.delete(
                            key
                    );

            if (!result.isSuccess()) {

                RpgLogger.error(
                        "Impossible de supprimer le dialogue "
                                + key
                                + " lors de la suppression "
                                + "du scénario : "
                                + result.getMessage()
                );

                return false;
            }
        }

        return true;
    }

    /**
     * Indique si un dialogue est référencé
     * par un trigger extérieur au scénario.
     */
    private boolean isDialogueReferencedOutsideScenario(
            String dialogueKey,
            List<Trigger> scenarioTriggers
    ) {
        Set<Integer> scenarioTriggerIds =
                scenarioTriggers.stream()
                        .map(
                                Trigger::getId
                        )
                        .collect(
                                Collectors.toSet()
                        );

        return triggerRepository.findAll()
                .stream()
                .filter(trigger ->
                        !scenarioTriggerIds.contains(
                                trigger.getId()
                        )
                )
                .anyMatch(trigger ->
                        scenarioResolver.findDialogAction(
                                        trigger
                                )
                                .map(
                                        Action::getExpression
                                )
                                .filter(
                                        dialogueKey::equals
                                )
                                .isPresent()
                );
    }

    /**
     * Génère une clé technique normalisée
     * à partir du nom lisible.
     */
    String generateBaseDialogueKey(
            String name
    ) {
        String normalized =
                Normalizer.normalize(
                        name,
                        Normalizer.Form.NFD
                );

        normalized =
                normalized.replaceAll(
                        "\\p{M}",
                        ""
                );

        normalized =
                normalized.toLowerCase(
                        Locale.ROOT
                );

        normalized =
                normalized.replaceAll(
                        "[^a-z0-9]+",
                        "_"
                );

        normalized =
                normalized.replaceAll(
                        "^_+|_+$",
                        ""
                );

        return normalized;
    }

    /**
     * Génère une clé technique unique.
     *
     * <p>Exemple :
     *
     * <pre>
     * les_poules_du_chef
     * les_poules_du_chef_2
     * les_poules_du_chef_3
     * </pre>
     */
    String generateUniqueDialogueKey(
            String name
    ) {
        String baseKey =
                generateBaseDialogueKey(
                        name
                );

        if (baseKey.isBlank()) {
            baseKey =
                    "dialogue";
        }

        String candidate =
                baseKey;

        int suffix =
                2;

        while (dialogueService.find(
                candidate
        ).isPresent()) {

            candidate =
                    baseKey
                            + "_"
                            + suffix;

            suffix++;
        }

        return candidate;
    }
}