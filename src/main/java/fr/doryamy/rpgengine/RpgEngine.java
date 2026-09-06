package fr.doryamy.rpgengine;

import fr.doryamy.rpgengine.action.ActionManager;
import fr.doryamy.rpgengine.action.assignment.AssignmentExecutor;
import fr.doryamy.rpgengine.action.assignment.AssignmentParser;
import fr.doryamy.rpgengine.action.executors.DialogActionExecutor;
import fr.doryamy.rpgengine.action.executors.MessageActionExecutor;
import fr.doryamy.rpgengine.action.executors.PlayerVariableActionExecutor;
import fr.doryamy.rpgengine.action.executors.QuestActionExecutor;
import fr.doryamy.rpgengine.bridge.NeoForgeBridge;
import fr.doryamy.rpgengine.bridge.NeoForgeDialoguePresenter;
import fr.doryamy.rpgengine.command.CommandManager;
import fr.doryamy.rpgengine.command.RpgCommand;
import fr.doryamy.rpgengine.command.dialogue.CreateDialogueCommand;
import fr.doryamy.rpgengine.command.dialogue.OpenDialogueAdminCommand;
import fr.doryamy.rpgengine.command.dialogue.OpenDialogueEditorCommand;
import fr.doryamy.rpgengine.condition.ConditionManager;
import fr.doryamy.rpgengine.condition.expression.ExpressionEvaluator;
import fr.doryamy.rpgengine.condition.expression.ExpressionParser;
import fr.doryamy.rpgengine.condition.providers.PlayerConditionProvider;
import fr.doryamy.rpgengine.condition.providers.QuestConditionProvider;
import fr.doryamy.rpgengine.database.DatabaseManager;
import fr.doryamy.rpgengine.dialogue.*;
import fr.doryamy.rpgengine.dialogue.editor.DialogueAdminService;
import fr.doryamy.rpgengine.dialogue.editor.DialogueCreationService;
import fr.doryamy.rpgengine.dialogue.editor.DialogueDeletionService;
import fr.doryamy.rpgengine.dialogue.editor.DialogueEditorController;
import fr.doryamy.rpgengine.dialogue.editor.DialogueNamingService;
import fr.doryamy.rpgengine.dialogue.editor.DialogueTriggerPresentationService;
import fr.doryamy.rpgengine.dialogue.editor.selection.DialogueAdminNpcSelectionListener;
import fr.doryamy.rpgengine.dialogue.editor.selection.DialogueAdminNpcSelectionService;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueAdminViewMapper;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueEditorViewMapper;
import fr.doryamy.rpgengine.dialogue.runtime.view.DialoguePresenter;
import fr.doryamy.rpgengine.infrastructure.persistence.sqlite.dialogue.SqliteDialogueRepository;
import fr.doryamy.rpgengine.listener.NPCListener;
import fr.doryamy.rpgengine.npc.NpcService;
import fr.doryamy.rpgengine.npc.citizens.CitizensNpcService;
import fr.doryamy.rpgengine.quest.QuestService;
import fr.doryamy.rpgengine.quest.ftb.FtbQuestService;
import fr.doryamy.rpgengine.repository.ActionRepository;
import fr.doryamy.rpgengine.repository.ConditionRepository;
import fr.doryamy.rpgengine.repository.PlayerVariableRepository;
import fr.doryamy.rpgengine.repository.TriggerRepository;
import fr.doryamy.rpgengine.trigger.TriggerManager;
import fr.doryamy.rpgengine.trigger.TriggerQueryService;
import fr.doryamy.rpgengine.trigger.TriggerService;
import fr.doryamy.rpgengine.util.RpgLogger;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;

/**
 * Point d'entrée principal de RPGEngine.
 *
 * <p>Cette classe constitue le composition root
 * de l'application.
 *
 * <p>Elle est exclusivement responsable
 * de la création et du câblage des composants.
 *
 * <p>Aucune logique métier ne doit être placée ici.
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

        instance = this;

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
                new SqliteDialogueRepository(
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

        AssignmentParser assignmentParser =
                new AssignmentParser();

        AssignmentExecutor assignmentExecutor =
                new AssignmentExecutor();

        /*
         * ====================================================
         * Trigger Services
         * ====================================================
         */

        TriggerService triggerService =
                new TriggerService(
                        triggerRepository,
                        conditionRepository,
                        actionRepository
                );

        TriggerQueryService triggerQueryService =
                new TriggerQueryService(
                        triggerRepository
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
         * Quest Integration
         * ====================================================
         */

        QuestService questService =
                new FtbQuestService(
                        neoForgeBridge
                );

        conditionManager.register(
                new QuestConditionProvider(
                        questService
                )
        );

        /*
         * ====================================================
         * Dialogue Domain
         * ====================================================
         */

        DialogueValidator dialogueValidator =
                new DialogueValidator();

        DialogueElementKeyGenerator dialogueElementKeyGenerator =
                new UuidDialogueElementKeyGenerator();

        DialogueRuleKeyGenerator dialogueRuleKeyGenerator =
                new UuidDialogueRuleKeyGenerator();

        DialogueKeyGenerator dialogueKeyGenerator =
                new UuidDialogueKeyGenerator();

        /*
         * Structure du graphe.
         */
        DialogueGraphService dialogueGraphService =
                new DialogueGraphService(
                        dialogueValidator,
                        dialogueElementKeyGenerator
                );

        /*
         * Contenu éditable.
         */
        DialogueContentService dialogueContentService =
                new DialogueContentService(
                        dialogueValidator
                );

        /*
         * Conditions et Actions appartenant
         * aux Reply / Choice.
         */
        DialogueRuleService dialogueRuleService =
                new DialogueRuleService(
                        dialogueValidator,
                        dialogueRuleKeyGenerator
                );

        /*
         * Cycle de vie et persistance
         * de l'agrégat Dialogue.
         */
        dialogueService =
                new DialogueService(
                        dialogueRepository,
                        dialogueGraphService,
                        dialogueValidator,
                        dialogueKeyGenerator
                );

        /*
         * Orchestration des opérations
         * provenant de l'éditeur.
         */
        DialogueEditingService dialogueEditingService =
                new DialogueEditingService(
                        dialogueService,
                        dialogueGraphService,
                        dialogueContentService,
                        dialogueRuleService
                );

        /*
         * ====================================================
         * Dialogue Administration
         * ====================================================
         */

        DialogueEditorViewMapper dialogueEditorViewMapper =
                new DialogueEditorViewMapper();

        DialogueAdminViewMapper dialogueAdminViewMapper =
                new DialogueAdminViewMapper();

        NpcService npcService =
                new CitizensNpcService();

        DialogueTriggerPresentationService dialogueTriggerPresentationService =
                new DialogueTriggerPresentationService(
                        triggerQueryService,
                        npcService
                );

        DialogueAdminService dialogueAdminService =
                new DialogueAdminService(
                        dialogueService,
                        dialogueTriggerPresentationService,
                        dialogueAdminViewMapper
                );

        DialogueAdminNpcSelectionService npcSelectionService =
                new DialogueAdminNpcSelectionService();

        DialogueCreationService dialogueCreationService =
                new DialogueCreationService(
                        dialogueService,
                        triggerService
                );

        DialogueDeletionService dialogueDeletionService =
                new DialogueDeletionService(
                        dialogueService,
                        triggerService
                );

        DialogueNamingService dialogueNamingService =
                new DialogueNamingService(
                        dialogueRepository
                );

        DialogueEditorController dialogueEditorController =
                new DialogueEditorController(
                        dialogueService,
                        dialogueCreationService,
                        dialogueDeletionService,
                        dialogueNamingService,
                        dialogueEditingService,
                        dialogueEditorViewMapper,
                        dialogueAdminService,
                        dialogueTriggerPresentationService,
                        triggerQueryService,
                        triggerService,
                        npcSelectionService,
                        neoForgeBridge,
                        questService
                );

        /*
         * Le client NeoForge émet seulement
         * une intention accompagnée d'une clé.
         *
         * Le plugin recharge toujours l'agrégat
         * courant avant de produire sa projection.
         */
        neoForgeBridge.setDialogueEditorRequestHandler(
                dialogueEditorController::openEditor
        );

        neoForgeBridge.setDialogueCreateRequestHandler(
                dialogueEditorController::createDialogue
        );

        neoForgeBridge.setDialogueNpcSelectionRequestHandler(
                dialogueEditorController::beginNpcSelection
        );

        neoForgeBridge.setDialogueTriggerNpcSelectionRequestHandler(
                dialogueEditorController::beginDialogueTriggerNpcSelection
        );

        neoForgeBridge.setDialogueDeleteRequestHandler(
                dialogueEditorController::deleteDialogue
        );

        neoForgeBridge.setDialogueQuestRuleRequestHandler(
                dialogueEditorController::saveQuestRule
        );

        neoForgeBridge.setDialogueAdminRequestHandler(
                dialogueEditorController::openAdmin
        );

        neoForgeBridge.setDialogueRenameRequestHandler(
                dialogueEditorController::renameDialogue
        );

        /*
         * ====================================================
         * Dialogue Runtime
         * ====================================================
         */

        DialogueNavigator dialogueNavigator =
                new DialogueNavigator(
                        conditionManager
                );

        DialogueSessionManager dialogueSessionManager =
                new DialogueSessionManager();

        DialoguePresenter dialoguePresenter =
                new NeoForgeDialoguePresenter(
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
         * Le runtime serveur reste l'autorité
         * sur les interactions du dialogue.
         */
        neoForgeBridge.setDialogueRunner(
                dialogueRunner
        );

        /*
         * ====================================================
         * Action Executors
         * ====================================================
         */

        registerActionExecutors(
                actionManager,
                playerVariableRepository,
                assignmentParser,
                assignmentExecutor,
                dialogueService,
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
         * Administration Commands
         * ====================================================
         */

        CommandManager commandManager =
                createCommandManager(
                        dialogueEditorController,
                        dialogueAdminService
                );

        registerCommands(
                commandManager
        );

        /*
         * ====================================================
         * Listeners
         * ====================================================
         */

        getServer()
                .getPluginManager()
                .registerEvents(
                        new DialogueAdminNpcSelectionListener(
                                npcSelectionService,
                                dialogueEditorController
                        ),
                        this
                );

        getServer()
                .getPluginManager()
                .registerEvents(
                        new NPCListener(
                                triggerManager
                        ),
                        this
                );

        RpgLogger.info(
                "RPGEngine démarré."
        );
    }

    @Override
    public void onDisable() {

        /*
         * Le bridge est fermé avant la base
         * afin de libérer les callbacks
         * éventuellement conservés côté NeoForge.
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

        instance = null;
    }

    /**
     * Initialise la configuration du plugin
     * ainsi que le système de logs.
     */
    private void initializeConfiguration() {

        saveDefaultConfig();

        RpgLogger.init();
    }

    /**
     * Initialise SQLite et applique
     * les migrations disponibles.
     */
    private void initializeDatabase() {

        databaseManager =
                new DatabaseManager(
                        this
                );

        databaseManager.connect();
    }

    /**
     * Enregistre les ActionExecutor disponibles.
     */
    private void registerActionExecutors(
            ActionManager actionManager,
            PlayerVariableRepository playerVariableRepository,
            AssignmentParser assignmentParser,
            AssignmentExecutor assignmentExecutor,
            DialogueService dialogueService,
            DialogueRunner dialogueRunner,
            QuestService questService
    ) {

        actionManager.register(
                new MessageActionExecutor()
        );

        actionManager.register(
                new PlayerVariableActionExecutor(
                        playerVariableRepository,
                        assignmentParser,
                        assignmentExecutor
                )
        );

        actionManager.register(
                new DialogActionExecutor(
                        dialogueService,
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
     * Crée le système de commandes
     * d'administration.
     */
    private CommandManager createCommandManager(
            DialogueEditorController dialogueEditorController,
            DialogueAdminService dialogueAdminService
    ) {

        CommandManager commandManager =
                new CommandManager();

        commandManager.register(
                new OpenDialogueAdminCommand(
                        dialogueAdminService,
                        neoForgeBridge
                )
        );

        commandManager.register(
                new CreateDialogueCommand(
                        dialogueService
                )
        );

        commandManager.register(
                new OpenDialogueEditorCommand(
                        dialogueEditorController
                )
        );

        return commandManager;
    }

    /**
     * Enregistre la commande principale /rpg.
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
     * Retourne le service principal
     * des dialogues.
     */
    public DialogueService getDialogueService() {

        return dialogueService;
    }
}