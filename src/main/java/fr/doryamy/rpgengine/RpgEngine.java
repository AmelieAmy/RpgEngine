package fr.doryamy.rpgengine;

import fr.doryamy.rpgengine.action.ActionManager;
import fr.doryamy.rpgengine.action.executors.DialogActionExecutor;
import fr.doryamy.rpgengine.action.executors.MessageActionExecutor;
import fr.doryamy.rpgengine.action.executors.PlayerVariableActionExecutor;
import fr.doryamy.rpgengine.action.executors.QuestActionExecutor;
import fr.doryamy.rpgengine.bridge.NeoForgeBridge;
import fr.doryamy.rpgengine.command.CommandManager;
import fr.doryamy.rpgengine.command.CommandResult;
import fr.doryamy.rpgengine.command.RpgCommand;
import fr.doryamy.rpgengine.condition.ConditionManager;
import fr.doryamy.rpgengine.condition.expression.ExpressionEvaluator;
import fr.doryamy.rpgengine.condition.expression.ExpressionParser;
import fr.doryamy.rpgengine.condition.providers.PlayerConditionProvider;
import fr.doryamy.rpgengine.condition.providers.QuestConditionProvider;
import fr.doryamy.rpgengine.database.DatabaseManager;
import fr.doryamy.rpgengine.dialogue.*;
import fr.doryamy.rpgengine.dialogue.command.*;
import fr.doryamy.rpgengine.dialogue.editor.DialogueEditorService;
import fr.doryamy.rpgengine.dialogue.editor.DialogueScenarioService;
import fr.doryamy.rpgengine.dialogue.editor.DialogueTriggerSelectionService;
import fr.doryamy.rpgengine.dialogue.editor.SwitchDialogueEditorStateRequest;
import fr.doryamy.rpgengine.dialogue.editor.InsertDialogueNpcReplyRequest;
import fr.doryamy.rpgengine.dialogue.editor.CreateDialogueBranchRequest;
import fr.doryamy.rpgengine.dialogue.presentation.DialoguePresenter;
import fr.doryamy.rpgengine.dialogue.presentation.ModdedDialoguePresenter;
import fr.doryamy.rpgengine.listener.NPCListener;
import fr.doryamy.rpgengine.quest.QuestService;
import fr.doryamy.rpgengine.quest.ftb.FtbQuestService;
import fr.doryamy.rpgengine.repository.ActionRepository;
import fr.doryamy.rpgengine.repository.ConditionRepository;
import fr.doryamy.rpgengine.repository.PlayerVariableRepository;
import fr.doryamy.rpgengine.repository.TriggerRepository;
import fr.doryamy.rpgengine.trigger.TriggerManager;
import fr.doryamy.rpgengine.trigger.TriggerService;
import fr.doryamy.rpgengine.util.RpgLogger;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;

/**
 * Point d'entrée principal de RPGEngine.
 *
 * <p>Cette classe constitue le composition root
 * de l'application.
 *
 * <p>Elle est responsable de la création et du câblage :
 *
 * <ul>
 *     <li>de la configuration ;</li>
 *     <li>de la base de données ;</li>
 *     <li>des repositories ;</li>
 *     <li>des services ;</li>
 *     <li>des composants runtime ;</li>
 *     <li>des managers ;</li>
 *     <li>des providers ;</li>
 *     <li>des executors ;</li>
 *     <li>des commandes ;</li>
 *     <li>des listeners.</li>
 * </ul>
 *
 * <p>Elle ne contient aucune logique métier.
 */
public final class RpgEngine extends JavaPlugin {

    private static RpgEngine instance;

    /*
     * Composants conservés pendant toute
     * la durée de vie du plugin.
     */
    private DatabaseManager databaseManager;
    private TriggerManager triggerManager;
    private DialogueService dialogueService;
    private NeoForgeBridge neoForgeBridge;

    @Override
    public void onEnable() {

        instance =
                this;

        initializeConfiguration();

        initializeDatabase();

        Connection connection =
                databaseManager.getConnection();

        /*
         * ====================================================
         * Repositories
         * ====================================================
         */

        PlayerVariableRepository playerVariableRepository =
                new PlayerVariableRepository(
                        connection
                );

        ActionRepository actionRepository =
                new ActionRepository(
                        connection
                );

        ConditionRepository conditionRepository =
                new ConditionRepository(
                        connection
                );

        TriggerRepository triggerRepository =
                new TriggerRepository(
                        connection,
                        actionRepository,
                        conditionRepository
                );

        DialogueRepository dialogueRepository =
                new DialogueRepository(
                        connection
                );

        /*
         * ====================================================
         * Expression Engine
         * ====================================================
         */

        ExpressionParser expressionParser =
                new ExpressionParser();

        ExpressionEvaluator expressionEvaluator =
                new ExpressionEvaluator();

        /*
         * ====================================================
         * Services métier
         * ====================================================
         */

        dialogueService =
                new DialogueService(
                        dialogueRepository,
                        expressionParser
                );

        TriggerService triggerService =
                new TriggerService(
                        triggerRepository,
                        conditionRepository,
                        actionRepository
                );

        DialogueScenarioService dialogueScenarioService =
                new DialogueScenarioService(
                        dialogueService,
                        triggerRepository,
                        triggerService,
                        conditionRepository
                );

        /*
         * ====================================================
         * Rule Engine
         * ====================================================
         */

        ConditionManager conditionManager =
                new ConditionManager(
                        expressionParser,
                        expressionEvaluator
                );

        conditionManager.register(
                new PlayerConditionProvider(
                        playerVariableRepository
                )
        );

        ActionManager actionManager =
                new ActionManager();

        /*
         * ====================================================
         * Bridge NeoForge
         * ====================================================
         */

        neoForgeBridge =
                new NeoForgeBridge();

        boolean bridgeInitialized =
                neoForgeBridge.initialize();

        if (!bridgeInitialized) {

            RpgLogger.error(
                    "Le bridge NeoForge n'a pas pu être initialisé."
            );
        }

        /*
         * ====================================================
         * Quest integration
         * ====================================================
         */

        QuestService questService =
                new FtbQuestService(
                        neoForgeBridge
                );

        /*
         * L'éditeur a besoin du QuestService afin de
         * résoudre les noms affichables des quêtes et
         * de formater les actions liées aux quêtes.
         *
         * Il est donc construit seulement après
         * l'initialisation du bridge et du QuestService.
         */
        DialogueEditorService dialogueEditorService =
                new DialogueEditorService(
                        dialogueService,
                        triggerRepository,
                        questService
                );


        DialogueTriggerSelectionService triggerSelectionService =
                new DialogueTriggerSelectionService(
                        triggerRepository,
                        triggerService
                );

        conditionManager.register(
                new QuestConditionProvider(
                        questService
                )
        );

        /*
         * ====================================================
         * Runtime des dialogues
         * ====================================================
         */

        DialogueValidator dialogueValidator =
                new DialogueValidator();

        DialogueNavigator dialogueNavigator =
                new DialogueNavigator(
                        conditionManager
                );

        DialogueSessionManager dialogueSessionManager =
                new DialogueSessionManager();

        DialoguePresenter dialoguePresenter =
                new ModdedDialoguePresenter(
                        neoForgeBridge
                );

        DialogueRunner dialogueRunner =
                new DialogueRunner(
                        dialogueValidator,
                        dialogueNavigator,
                        dialogueSessionManager,
                        actionManager,
                        dialoguePresenter
                );

        /*
         * Le runtime est maintenant entièrement construit.
         * Il peut être branché au bridge.
         */
        neoForgeBridge.setDialogueRunner(
                dialogueRunner
        );

        /*
         * ====================================================
         * Callbacks de l'éditeur
         * ====================================================
         */

        neoForgeBridge.setDialogueEditorRequestHandler(
                (playerUuid, dialogueKey) ->

                        dialogueEditorService
                                .buildView(
                                        dialogueKey
                                )
                                .ifPresent(
                                        view ->
                                                neoForgeBridge
                                                        .showDialogueEditor(
                                                                playerUuid,
                                                                view
                                                        )
                                )
        );


        neoForgeBridge.setDialogueTriggerSelectionStartHandler(
                (playerUuid, dialogueKey) -> {

                    boolean started =
                            triggerSelectionService.startSelection(
                                    playerUuid,
                                    dialogueKey
                            );

                    org.bukkit.entity.Player player =
                            Bukkit.getPlayer(
                                    playerUuid
                            );

                    if (!started) {

                        if (player != null) {

                            player.sendMessage(
                                    "§cImpossible de démarrer la sélection du déclencheur."
                            );
                        }

                        /*
                         * Le client a déjà fermé l'écran.
                         * En cas d'échec, on lui renvoie donc
                         * immédiatement la vue courante.
                         */
                        dialogueEditorService
                                .buildView(
                                        dialogueKey
                                )
                                .ifPresent(
                                        view ->
                                                neoForgeBridge
                                                        .showDialogueEditor(
                                                                playerUuid,
                                                                view
                                                        )
                                );

                        return;
                    }

                    if (player != null) {

                        player.sendMessage(
                                "§e§lSélection du déclencheur"
                        );

                        player.sendMessage(
                                "§7Faites un clic droit sur le PNJ à utiliser."
                        );
                    }
                }
        );


        neoForgeBridge.setDialogueQuestListRequestHandler(
                (playerUuid, dialogueKey) ->
                        neoForgeBridge.showDialogueQuestSelector(
                                playerUuid,
                                dialogueKey,
                                questService.findAll()
                        )
        );


        neoForgeBridge.setDialogueConditionEditorRequestHandler(
                (playerUuid, data) -> {

                    String dialogueKey =
                            data.get(
                                    "dialogueKey"
                            );

                    String transitionKey =
                            data.get(
                                    "transitionKey"
                            );

                    int conditionPosition;

                    try {

                        conditionPosition =
                                Integer.parseInt(
                                        data.getOrDefault(
                                                "conditionPosition",
                                                "-1"
                                        )
                                );

                    } catch (NumberFormatException e) {

                        RpgLogger.error(
                                "Position de condition invalide reçue depuis NeoForge."
                        );

                        return;
                    }

                    dialogueService
                            .getTransitionConditions(
                                    dialogueKey,
                                    transitionKey
                            )
                            .filter(conditions ->
                                    conditionPosition > 0
                                            && conditionPosition <= conditions.size()
                            )
                            .map(conditions ->
                                    conditions.get(
                                            conditionPosition - 1
                                    )
                            )
                            .ifPresentOrElse(
                                    condition ->
                                            neoForgeBridge
                                                    .showDialogueConditionEditor(
                                                            playerUuid,
                                                            dialogueKey,
                                                            transitionKey,
                                                            conditionPosition,
                                                            condition.getProvider(),
                                                            condition.getExpression(),
                                                            questService.findAll()
                                                    ),
                                    () ->
                                            RpgLogger.error(
                                                    "Condition introuvable pour l'éditeur : "
                                                            + dialogueKey
                                                            + " | transition="
                                                            + transitionKey
                                                            + " | position="
                                                            + conditionPosition
                                            )
                            );
                }
        );


        neoForgeBridge.setDialogueConditionUpdateHandler(
                (playerUuid, data) -> {

                    String dialogueKey =
                            data.get(
                                    "dialogueKey"
                            );

                    String transitionKey =
                            data.get(
                                    "transitionKey"
                            );

                    String provider =
                            data.get(
                                    "provider"
                            );

                    String expression =
                            data.get(
                                    "expression"
                            );

                    int conditionPosition;

                    try {

                        conditionPosition =
                                Integer.parseInt(
                                        data.getOrDefault(
                                                "conditionPosition",
                                                "-1"
                                        )
                                );

                    } catch (NumberFormatException e) {

                        RpgLogger.error(
                                "Position de condition invalide reçue depuis NeoForge."
                        );

                        return;
                    }

                    CommandResult result =
                            dialogueService
                                    .updateTransitionCondition(
                                            dialogueKey,
                                            transitionKey,
                                            conditionPosition,
                                            provider,
                                            expression
                                    );

                    if (!result.isSuccess()) {

                        RpgLogger.error(
                                "Impossible de modifier la condition "
                                        + conditionPosition
                                        + " de la transition "
                                        + transitionKey
                                        + " : "
                                        + result.getMessage()
                        );

                        return;
                    }

                    dialogueEditorService
                            .buildView(
                                    dialogueKey
                            )
                            .ifPresentOrElse(
                                    view ->
                                            neoForgeBridge
                                                    .showDialogueEditor(
                                                            playerUuid,
                                                            view
                                                    ),
                                    () ->
                                            RpgLogger.error(
                                                    "Condition modifiée, mais impossible "
                                                            + "de reconstruire l'éditeur : "
                                                            + dialogueKey
                                            )
                            );
                }
        );


        neoForgeBridge.setDialogueActionEditorRequestHandler(
                (playerUuid, data) -> {

                    String dialogueKey =
                            data.get(
                                    "dialogueKey"
                            );

                    String transitionKey =
                            data.get(
                                    "transitionKey"
                            );

                    int actionPosition;

                    try {

                        actionPosition =
                                Integer.parseInt(
                                        data.getOrDefault(
                                                "actionPosition",
                                                "-1"
                                        )
                                );

                    } catch (NumberFormatException e) {

                        RpgLogger.error(
                                "Position d'action invalide reçue depuis NeoForge."
                        );

                        return;
                    }

                    dialogueService
                            .getTransitionActions(
                                    dialogueKey,
                                    transitionKey
                            )
                            .flatMap(actions ->
                                    actions.stream()
                                            .filter(action ->
                                                    action.getPosition()
                                                            == actionPosition
                                            )
                                            .findFirst()
                            )
                            .ifPresentOrElse(
                                    action ->
                                            neoForgeBridge
                                                    .showDialogueActionEditor(
                                                            playerUuid,
                                                            dialogueKey,
                                                            transitionKey,
                                                            actionPosition,
                                                            action.getProvider(),
                                                            action.getExpression(),
                                                            questService.findAll()
                                                    ),
                                    () ->
                                            RpgLogger.error(
                                                    "Action introuvable pour l'éditeur : "
                                                            + dialogueKey
                                                            + " | transition="
                                                            + transitionKey
                                                            + " | position="
                                                            + actionPosition
                                            )
                            );
                }
        );


        neoForgeBridge.setDialogueActionUpdateHandler(
                (playerUuid, data) -> {

                    String dialogueKey =
                            data.get(
                                    "dialogueKey"
                            );

                    String transitionKey =
                            data.get(
                                    "transitionKey"
                            );

                    String provider =
                            data.get(
                                    "provider"
                            );

                    String expression =
                            data.get(
                                    "expression"
                            );

                    int actionPosition;

                    try {

                        actionPosition =
                                Integer.parseInt(
                                        data.getOrDefault(
                                                "actionPosition",
                                                "-1"
                                        )
                                );

                    } catch (NumberFormatException e) {

                        RpgLogger.error(
                                "Position d'action invalide reçue depuis NeoForge."
                        );

                        return;
                    }

                    CommandResult result =
                            dialogueService
                                    .updateTransitionAction(
                                            dialogueKey,
                                            transitionKey,
                                            actionPosition,
                                            provider,
                                            expression
                                    );

                    if (!result.isSuccess()) {

                        RpgLogger.error(
                                "Impossible de modifier l'action "
                                        + actionPosition
                                        + " de la transition "
                                        + transitionKey
                                        + " : "
                                        + result.getMessage()
                        );

                        return;
                    }

                    dialogueEditorService
                            .buildView(
                                    dialogueKey
                            )
                            .ifPresentOrElse(
                                    view ->
                                            neoForgeBridge
                                                    .showDialogueEditor(
                                                            playerUuid,
                                                            view
                                                    ),
                                    () ->
                                            RpgLogger.error(
                                                    "Action modifiée, mais impossible "
                                                            + "de reconstruire l'éditeur : "
                                                            + dialogueKey
                                            )
                            );
                }
        );


        neoForgeBridge.setDialogueNodeTextUpdateHandler(
                (playerUuid, data) -> {

                    String dialogueKey =
                            data.get("dialogueKey");

                    String nodeKey =
                            data.get("nodeKey");

                    String text =
                            data.get("text");

                    CommandResult result =
                            dialogueService.updateNodeText(
                                    dialogueKey,
                                    nodeKey,
                                    text
                            );

                    if (!result.isSuccess()) {

                        RpgLogger.error(
                                "Impossible de modifier le texte du node "
                                        + nodeKey
                                        + " : "
                                        + result.getMessage()
                        );

                        return;
                    }

                    dialogueEditorService
                            .buildView(
                                    dialogueKey
                            )
                            .ifPresentOrElse(
                                    view ->
                                            neoForgeBridge
                                                    .showDialogueEditor(
                                                            playerUuid,
                                                            view
                                                    ),
                                    () ->
                                            RpgLogger.error(
                                                    "Texte modifié, mais impossible "
                                                            + "de reconstruire l'éditeur : "
                                                            + dialogueKey
                                            )
                            );
                }
        );


        neoForgeBridge.setDialogueChoiceLabelUpdateHandler(
                (playerUuid, data) -> {

                    String dialogueKey =
                            data.get(
                                    "dialogueKey"
                            );

                    String transitionKey =
                            data.get(
                                    "transitionKey"
                            );

                    String label =
                            data.get(
                                    "label"
                            );

                    CommandResult result =
                            dialogueService.updateChoiceLabel(
                                    dialogueKey,
                                    transitionKey,
                                    label
                            );

                    if (!result.isSuccess()) {

                        RpgLogger.error(
                                "Impossible de modifier la réplique Joueur "
                                        + transitionKey
                                        + " : "
                                        + result.getMessage()
                        );

                        return;
                    }

                    dialogueEditorService
                            .buildView(
                                    dialogueKey
                            )
                            .ifPresentOrElse(
                                    view ->
                                            neoForgeBridge
                                                    .showDialogueEditor(
                                                            playerUuid,
                                                            view
                                                    ),
                                    () ->
                                            RpgLogger.error(
                                                    "Réplique Joueur modifiée, "
                                                            + "mais impossible de reconstruire "
                                                            + "l'éditeur : "
                                                            + dialogueKey
                                            )
                            );
                }
        );


        neoForgeBridge.setDialoguePlayerReplyAddHandler(
                (playerUuid, data) -> {

                    String dialogueKey =
                            data.get(
                                    "dialogueKey"
                            );

                    String transitionKey =
                            data.get(
                                    "transitionKey"
                            );

                    String text =
                            data.get(
                                    "text"
                            );

                    int afterPosition;

                    try {
                        afterPosition =
                                Integer.parseInt(
                                        data.getOrDefault(
                                                "afterPosition",
                                                "0"
                                        )
                                );

                    } catch (NumberFormatException e) {

                        RpgLogger.error(
                                "Position de réplique Joueur invalide reçue depuis NeoForge."
                        );

                        return;
                    }

                    CommandResult result =
                            dialogueService
                                    .insertPlayerReply(
                                            dialogueKey,
                                            transitionKey,
                                            afterPosition,
                                            text
                                    );

                    if (!result.isSuccess()) {

                        RpgLogger.error(
                                "Impossible d'ajouter la réplique Joueur "
                                        + "sur la transition "
                                        + transitionKey
                                        + " : "
                                        + result.getMessage()
                        );

                        return;
                    }

                    dialogueEditorService
                            .buildView(
                                    dialogueKey
                            )
                            .ifPresentOrElse(
                                    view ->
                                            neoForgeBridge
                                                    .showDialogueEditor(
                                                            playerUuid,
                                                            view
                                                    ),
                                    () ->
                                            RpgLogger.error(
                                                    "Réplique Joueur ajoutée, "
                                                            + "mais impossible de reconstruire "
                                                            + "l'éditeur : "
                                                            + dialogueKey
                                            )
                            );
                }
        );


        neoForgeBridge.setDialogueTransitionTerminalHandler(
                (playerUuid, request) -> {

                    CommandResult result =
                            dialogueService
                                    .setTransitionTerminal(
                                            request.dialogueKey(),
                                            request.transitionKey()
                                    );

                    if (!result.isSuccess()) {

                        RpgLogger.error(
                                "Impossible de terminer la transition "
                                        + request.transitionKey()
                                        + " : "
                                        + result.getMessage()
                        );

                        return;
                    }

                    dialogueEditorService
                            .buildView(
                                    request.dialogueKey()
                            )
                            .ifPresentOrElse(
                                    view ->
                                            neoForgeBridge
                                                    .showDialogueEditor(
                                                            playerUuid,
                                                            view
                                                    ),
                                    () ->
                                            RpgLogger.error(
                                                    "Transition terminée, "
                                                            + "mais impossible de reconstruire "
                                                            + "l'éditeur : "
                                                            + request.dialogueKey()
                                            )
                            );
                }
        );


        neoForgeBridge.setDialogueNpcReplyInsertHandler(
                (playerUuid, request) -> {

                    CommandResult result =
                            dialogueService
                                    .insertNpcReply(
                                            request.dialogueKey(),
                                            request.transitionKey(),
                                            request.insertionKind(),
                                            request.position(),
                                            request.text()
                                    );

                    if (!result.isSuccess()) {
                        RpgLogger.error(
                                "Impossible d'ajouter la réplique PNJ "
                                        + "sur la transition "
                                        + request.transitionKey()
                                        + " : "
                                        + result.getMessage()
                        );
                        return;
                    }

                    dialogueEditorService
                            .buildView(
                                    request.dialogueKey()
                            )
                            .ifPresentOrElse(
                                    view ->
                                            neoForgeBridge
                                                    .showDialogueEditor(
                                                            playerUuid,
                                                            view
                                                    ),
                                    () ->
                                            RpgLogger.error(
                                                    "Réplique PNJ ajoutée, "
                                                            + "mais impossible de reconstruire "
                                                            + "l'éditeur : "
                                                            + request.dialogueKey()
                                            )
                            );
                }
        );



        neoForgeBridge.setDialogueBranchCreateHandler(
                (playerUuid, request) -> {

                    CommandResult result =
                            dialogueService
                                    .createBranch(
                                            request.dialogueKey(),
                                            request.transitionKey(),
                                            request.insertionKind(),
                                            request.position(),
                                            request.existingChoiceLabel(),
                                            request.newChoiceLabel()
                                    );

                    if (!result.isSuccess()) {
                        RpgLogger.error(
                                "Impossible de créer l'embranchement "
                                        + "depuis la transition "
                                        + request.transitionKey()
                                        + " : "
                                        + result.getMessage()
                        );
                        return;
                    }

                    dialogueEditorService
                            .buildView(
                                    request.dialogueKey()
                            )
                            .ifPresentOrElse(
                                    view ->
                                            neoForgeBridge
                                                    .showDialogueEditor(
                                                            playerUuid,
                                                            view
                                                    ),
                                    () ->
                                            RpgLogger.error(
                                                    "Embranchement créé, "
                                                            + "mais impossible de reconstruire "
                                                            + "l'éditeur : "
                                                            + request.dialogueKey()
                                            )
                            );
                }
        );


        neoForgeBridge.setDialogueElementDeleteHandler(
                (playerUuid, data) -> {

                    String dialogueKey = data.get("dialogueKey");
                    String transitionKey = data.get("transitionKey");
                    String deletionKindValue = data.get("deletionKind");

                    int position;
                    try {
                        position = Integer.parseInt(
                                data.getOrDefault("position", "0")
                        );
                    } catch (NumberFormatException e) {
                        RpgLogger.error(
                                "Position de suppression invalide reçue depuis NeoForge."
                        );
                        return;
                    }

                    DialogueDeletionKind deletionKind;
                    try {
                        deletionKind = DialogueDeletionKind.valueOf(
                                deletionKindValue == null
                                        ? ""
                                        : deletionKindValue
                        );
                    } catch (IllegalArgumentException e) {
                        RpgLogger.error(
                                "Type de suppression invalide reçu depuis NeoForge : "
                                        + deletionKindValue
                        );
                        return;
                    }

                    CommandResult result = dialogueService.deleteEditorElement(
                            dialogueKey,
                            transitionKey,
                            deletionKind,
                            position
                    );

                    if (!result.isSuccess()) {
                        RpgLogger.error(
                                "Impossible de supprimer l'élément de dialogue : "
                                        + result.getMessage()
                        );
                        return;
                    }

                    dialogueEditorService
                            .buildView(dialogueKey)
                            .ifPresentOrElse(
                                    view -> neoForgeBridge.showDialogueEditor(
                                            playerUuid,
                                            view
                                    ),
                                    () -> RpgLogger.error(
                                            "Élément supprimé, mais impossible de reconstruire "
                                                    + "l'éditeur : "
                                                    + dialogueKey
                                    )
                            );
                }
        );


        neoForgeBridge.setDialogueEditorStateSwitchHandler(
                (playerUuid, request) ->

                        dialogueEditorService
                                .switchState(
                                        request.dialogueKey(),
                                        request.targetState()
                                )
                                .ifPresentOrElse(
                                        view ->
                                                neoForgeBridge
                                                        .showDialogueEditor(
                                                                playerUuid,
                                                                view
                                                        ),
                                        () ->
                                                RpgLogger.error(
                                                        "Impossible de charger la variante "
                                                                + request.targetState()
                                                                + " pour le dialogue "
                                                                + request.dialogueKey()
                                                )
                                )
        );

        neoForgeBridge.setDialogueScenarioDeleteHandler(
                (playerUuid, dialogueKey) -> {

                    boolean deleted =
                            dialogueScenarioService.deleteScenario(
                                    dialogueKey
                            );

                    if (!deleted) {

                        RpgLogger.error(
                                "Impossible de supprimer le scénario : "
                                        + dialogueKey
                        );

                        return;
                    }

                    neoForgeBridge.showDialogueManager(
                            playerUuid,
                            dialogueEditorService.findScenarios()
                    );
                }
        );

        neoForgeBridge.setDialogueScenarioCreateHandler(
                (playerUuid, request) -> {

                    CommandResult result =
                            dialogueScenarioService.createScenario(
                                    request
                            );

                    if (!result.isSuccess()) {

                        RpgLogger.error(
                                "Impossible de créer le scénario : "
                                        + result.getMessage()
                        );

                        return;
                    }

                    /*
                     * La création ayant réussi,
                     * on renvoie immédiatement la liste
                     * reconstruite depuis la base.
                     */
                    neoForgeBridge.showDialogueManager(
                            playerUuid,
                            dialogueEditorService.findScenarios()
                    );
                }
        );

        /*
         * ====================================================
         * Executors
         * ====================================================
         *
         * Ils sont enregistrés après la création
         * du DialogueRunner afin d'éviter une dépendance
         * circulaire de construction.
         */

        registerActionExecutors(
                actionManager,
                playerVariableRepository,
                dialogueRepository,
                dialogueRunner,
                questService
        );

        /*
         * ====================================================
         * Trigger Engine
         * ====================================================
         */

        triggerManager =
                new TriggerManager(
                        triggerRepository,
                        conditionManager,
                        actionManager
                );

        /*
         * ====================================================
         * Administration
         * ====================================================
         */

        CommandManager commandManager =
                createCommandManager(
                        dialogueService,
                        dialogueRunner,
                        dialogueEditorService,
                        neoForgeBridge
                );

        registerCommands(
                commandManager
        );

        /*
         * ====================================================
         * Adaptateurs externes
         * ====================================================
         */

        registerListeners(
                triggerSelectionService,
                dialogueEditorService,
                neoForgeBridge
        );

        RpgLogger.info(
                "RPGEngine démarré."
        );
    }

    @Override
    public void onDisable() {

        /*
         * Le bridge est fermé avant la base de données
         * afin de libérer les callbacks conservés
         * par le ClassLoader NeoForge.
         */
        if (neoForgeBridge != null) {

            neoForgeBridge.shutdown();
        }

        if (databaseManager != null) {

            databaseManager.close();
        }

        RpgLogger.info(
                "RPGEngine arrêté."
        );

        instance =
                null;
    }

    /**
     * Initialise la configuration du plugin
     * et le système de logs.
     */
    private void initializeConfiguration() {

        saveDefaultConfig();

        RpgLogger.init();
    }

    /**
     * Initialise SQLite et applique
     * les migrations nécessaires.
     */
    private void initializeDatabase() {

        databaseManager =
                new DatabaseManager(
                        this
                );

        databaseManager.connect();
    }

    /**
     * Enregistre les executors disponibles
     * auprès du système d'actions.
     *
     * @param actionManager système d'actions
     * @param playerVariableRepository repository
     *                                 des variables joueur
     * @param dialogueRepository repository
     *                           des dialogues
     * @param dialogueRunner runtime des dialogues
     * @param questService service de quêtes
     */
    private void registerActionExecutors(
            ActionManager actionManager,
            PlayerVariableRepository playerVariableRepository,
            DialogueRepository dialogueRepository,
            DialogueRunner dialogueRunner,
            QuestService questService
    ) {

        actionManager.register(
                new MessageActionExecutor()
        );

        actionManager.register(
                new PlayerVariableActionExecutor(
                        playerVariableRepository
                )
        );

        actionManager.register(
                new DialogActionExecutor(
                        dialogueRepository,
                        dialogueRunner
                )
        );

        actionManager.register(
                new QuestActionExecutor(
                        questService
                )
        );
    }

    /**
     * Crée et configure le système
     * de commandes d'administration.
     *
     * <p>Les anciennes commandes runtime
     * {@code dialog continue}, {@code dialog choose}
     * et {@code dialog finish} ne sont plus enregistrées :
     * ces interactions passent désormais par le mod client.
     *
     * @param dialogueService service d'administration
     *                        des dialogues
     * @param dialogueRunner runtime utilisé
     *                       par la prévisualisation
     * @param dialogueEditorService service de l'éditeur
     * @param neoForgeBridge façade vers le mod NeoForge
     *
     * @return CommandManager configuré
     */
    private CommandManager createCommandManager(
            DialogueService dialogueService,
            DialogueRunner dialogueRunner,
            DialogueEditorService dialogueEditorService,
            NeoForgeBridge neoForgeBridge
    ) {

        CommandManager commandManager =
                new CommandManager();

        /*
         * ------------------------------------------------
         * Dialogues
         * ------------------------------------------------
         */

        commandManager.register(
                new CreateDialogueCommand(
                        dialogueService
                )
        );

        commandManager.register(
                new DeleteDialogueCommand(
                        dialogueService
                )
        );

        commandManager.register(
                new ListDialogueCommand(
                        dialogueService
                )
        );

        commandManager.register(
                new InfoDialogueCommand(
                        dialogueService
                )
        );

        commandManager.register(
                new PreviewDialogueCommand(
                        dialogueService,
                        dialogueRunner
                )
        );

        commandManager.register(
                new OpenDialogueEditorCommand(
                        dialogueEditorService,
                        neoForgeBridge
                )
        );

        /*
         * ------------------------------------------------
         * Nodes
         * ------------------------------------------------
         */

        commandManager.register(
                new AddNodeDialogueCommand(
                        dialogueService
                )
        );

        commandManager.register(
                new SetStartNodeDialogueCommand(
                        dialogueService
                )
        );

        commandManager.register(
                new UpdateNodeTextDialogueCommand(
                        dialogueService
                )
        );

        /*
         * ------------------------------------------------
         * Transitions
         * ------------------------------------------------
         */

        commandManager.register(
                new AddTransitionDialogueCommand(
                        dialogueService
                )
        );

        commandManager.register(
                new SetTransitionDialogueCommand(
                        dialogueService
                )
        );

        /*
         * ------------------------------------------------
         * Actions de transition
         * ------------------------------------------------
         */

        commandManager.register(
                new AddTransitionActionDialogueCommand(
                        dialogueService
                )
        );

        commandManager.register(
                new RemoveTransitionActionDialogueCommand(
                        dialogueService
                )
        );

        commandManager.register(
                new ListTransitionActionDialogueCommand(
                        dialogueService
                )
        );

        /*
         * ------------------------------------------------
         * Conditions de transition
         * ------------------------------------------------
         */

        commandManager.register(
                new AddTransitionConditionDialogueCommand(
                        dialogueService
                )
        );

        commandManager.register(
                new RemoveTransitionConditionDialogueCommand(
                        dialogueService
                )
        );

        commandManager.register(
                new ListTransitionConditionDialogueCommand(
                        dialogueService
                )
        );

        return commandManager;
    }

    /**
     * Enregistre la commande principale /rpg.
     *
     * @param commandManager gestionnaire
     *                       des sous-commandes
     */
    private void registerCommands(
            CommandManager commandManager
    ) {

        PluginCommand command =
                getCommand(
                        "rpg"
                );

        if (command == null) {

            RpgLogger.error(
                    "La commande /rpg n'est pas déclarée dans plugin.yml."
            );

            return;
        }

        command.setExecutor(
                new RpgCommand(
                        commandManager
                )
        );
    }

    /**
     * Enregistre les listeners utilisés
     * par RPGEngine.
     */
    private void registerListeners(
            DialogueTriggerSelectionService triggerSelectionService,
            DialogueEditorService dialogueEditorService,
            NeoForgeBridge neoForgeBridge
    ) {

        Bukkit.getPluginManager()
                .registerEvents(
                        new NPCListener(
                                triggerManager,
                                triggerSelectionService,
                                dialogueEditorService,
                                neoForgeBridge
                        ),
                        this
                );
    }

    /**
     * Retourne l'instance courante du plugin.
     */
    public static RpgEngine getInstance() {

        return instance;
    }

    /**
     * Retourne le manager des triggers.
     */
    public TriggerManager getTriggerManager() {

        return triggerManager;
    }

    /**
     * Retourne le service d'administration
     * des dialogues.
     *
     * @return service des dialogues
     */
    public DialogueService getDialogueService() {

        return dialogueService;
    }
}