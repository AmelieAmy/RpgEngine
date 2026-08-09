package fr.doryamy.rpgengine;

import fr.doryamy.rpgengine.action.ActionManager;
import fr.doryamy.rpgengine.action.executors.DialogActionExecutor;
import fr.doryamy.rpgengine.action.executors.MessageActionExecutor;
import fr.doryamy.rpgengine.action.executors.PlayerVariableActionExecutor;
import fr.doryamy.rpgengine.command.CommandManager;
import fr.doryamy.rpgengine.command.RpgCommand;
import fr.doryamy.rpgengine.condition.ConditionManager;
import fr.doryamy.rpgengine.condition.expression.ExpressionEvaluator;
import fr.doryamy.rpgengine.condition.expression.ExpressionParser;
import fr.doryamy.rpgengine.condition.providers.PlayerConditionProvider;
import fr.doryamy.rpgengine.database.DatabaseManager;
import fr.doryamy.rpgengine.dialogue.*;
import fr.doryamy.rpgengine.dialogue.command.*;
import fr.doryamy.rpgengine.listener.NPCListener;
import fr.doryamy.rpgengine.repository.PlayerVariableRepository;
import fr.doryamy.rpgengine.trigger.TriggerManager;
import fr.doryamy.rpgengine.util.RpgLogger;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Point d'entrée principal de RPGEngine.
 *
 * Cette classe constitue le point de composition
 * des différents composants du moteur.
 *
 * Elle est responsable de l'initialisation :
 * - de la configuration ;
 * - de la base de données ;
 * - des repositories ;
 * - des services ;
 * - des composants runtime ;
 * - des managers ;
 * - des providers ;
 * - des executors ;
 * - des commandes ;
 * - des listeners.
 *
 * Elle ne contient aucune logique métier.
 */
public final class RpgEngine extends JavaPlugin {

    private static RpgEngine instance;

    private DatabaseManager databaseManager;
    private TriggerManager triggerManager;
    private DialogueService dialogueService;

    @Override
    public void onEnable() {

        instance = this;

        initializeConfiguration();
        initializeDatabase();

        /*
         * Repositories
         */
        PlayerVariableRepository playerVariableRepository =
                createPlayerVariableRepository();

        DialogueRepository dialogueRepository =
                createDialogueRepository();

        /*
         * Expression Engine
         */
        ExpressionParser expressionParser =
                new ExpressionParser();

        ExpressionEvaluator expressionEvaluator =
                new ExpressionEvaluator();

        /*
         * Services métier
         */
        dialogueService =
                createDialogueService(
                        dialogueRepository,
                        expressionParser
                );

        /*
         * Rule Engine
         */
        ConditionManager conditionManager =
                createConditionManager(
                        playerVariableRepository,
                        expressionParser,
                        expressionEvaluator
                );

        ActionManager actionManager =
                createActionManager();

        /*
         * Runtime des dialogues
         */
        DialogueValidator dialogueValidator =
                createDialogueValidator();

        DialogueNavigator dialogueNavigator =
                createDialogueNavigator(
                        conditionManager
                );

        DialogueSessionManager dialogueSessionManager =
                createDialogueSessionManager();

        DialogueRunner dialogueRunner =
                createDialogueRunner(
                        dialogueValidator,
                        dialogueNavigator,
                        dialogueSessionManager,
                        actionManager
                );

        /*
         * Executors
         *
         * Ils sont enregistrés après la création
         * du DialogueRunner afin d'éviter une
         * dépendance circulaire de construction.
         */
        registerActionExecutors(
                actionManager,
                playerVariableRepository,
                dialogueRepository,
                dialogueRunner
        );

        /*
         * Trigger Engine
         */
        triggerManager =
                createTriggerManager(
                        conditionManager,
                        actionManager
                );

        /*
         * Administration
         */
        CommandManager commandManager =
                createCommandManager(
                        dialogueService,
                        dialogueRunner
                );

        registerCommands(
                commandManager
        );

        /*
         * Adaptateurs externes
         */
        registerListeners();

        RpgLogger.info(
                "RPGEngine démarré."
        );
    }

    @Override
    public void onDisable() {

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
                new DatabaseManager(this);

        databaseManager.connect();
    }

    /**
     * Crée le repository des variables joueur.
     *
     * @return repository configuré
     */
    private PlayerVariableRepository
    createPlayerVariableRepository() {

        return new PlayerVariableRepository(
                databaseManager.getConnection()
        );
    }

    /**
     * Crée le repository des dialogues.
     *
     * @return repository configuré
     */
    private DialogueRepository createDialogueRepository() {

        return new DialogueRepository(
                databaseManager.getConnection()
        );
    }

    /**
     * Crée le service métier des dialogues.
     *
     * @param dialogueRepository repository des dialogues
     *
     * @return service configuré
     */
    private DialogueService createDialogueService(
            DialogueRepository dialogueRepository,
            ExpressionParser expressionParser
    ) {

        return new DialogueService(
                dialogueRepository,
                expressionParser
        );
    }

    /**
     * Crée et configure le système de conditions.
     *
     * @param playerVariableRepository repository
     *                                 des variables joueur
     *
     * @return ConditionManager configuré
     */
    private ConditionManager createConditionManager(
            PlayerVariableRepository playerVariableRepository,
            ExpressionParser expressionParser,
            ExpressionEvaluator expressionEvaluator
    ) {

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

        return conditionManager;
    }

    /**
     * Crée le système d'actions.
     *
     * Les executors sont enregistrés séparément
     * après la création du runtime des dialogues.
     *
     * @return ActionManager vide
     */
    private ActionManager createActionManager() {

        return new ActionManager();
    }

    /**
     * Crée le validateur structurel
     * des graphes de dialogue.
     *
     * @return validateur configuré
     */
    private DialogueValidator createDialogueValidator() {

        return new DialogueValidator();
    }

    /**
     * Crée le navigateur utilisé
     * par le runtime des dialogues.
     *
     * @param conditionManager système de conditions
     *
     * @return navigateur configuré
     */
    private DialogueNavigator createDialogueNavigator(
            ConditionManager conditionManager
    ) {

        return new DialogueNavigator(
                conditionManager
        );
    }

    /**
     * Crée le gestionnaire des sessions
     * de dialogue actives.
     *
     * @return gestionnaire configuré
     */
    private DialogueSessionManager
    createDialogueSessionManager() {

        return new DialogueSessionManager();
    }

    /**
     * Crée le runtime des dialogues.
     *
     * @param dialogueValidator validateur des dialogues
     * @param dialogueNavigator navigateur des dialogues
     * @param dialogueSessionManager gestionnaire des sessions
     * @param actionManager système d'actions
     *
     * @return DialogueRunner configuré
     */
    private DialogueRunner createDialogueRunner(
            DialogueValidator dialogueValidator,
            DialogueNavigator dialogueNavigator,
            DialogueSessionManager dialogueSessionManager,
            ActionManager actionManager
    ) {

        return new DialogueRunner(
                dialogueValidator,
                dialogueNavigator,
                dialogueSessionManager,
                actionManager
        );
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
     */
    private void registerActionExecutors(
            ActionManager actionManager,
            PlayerVariableRepository playerVariableRepository,
            DialogueRepository dialogueRepository,
            DialogueRunner dialogueRunner
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
    }

    /**
     * Crée le manager principal des triggers.
     *
     * @param conditionManager système de conditions
     * @param actionManager système d'actions
     *
     * @return TriggerManager configuré
     */
    private TriggerManager createTriggerManager(
            ConditionManager conditionManager,
            ActionManager actionManager
    ) {

        return new TriggerManager(
                databaseManager.getConnection(),
                conditionManager,
                actionManager
        );
    }

    /**
     * Crée et configure le système
     * de commandes administrateur.
     *
     * @param dialogueService service des dialogues
     * @param dialogueRunner runtime des dialogues
     *
     * @return CommandManager configuré
     */
    private CommandManager createCommandManager(
            DialogueService dialogueService,
            DialogueRunner dialogueRunner
    ) {

        /********** Dialog commands ***********/

        CommandManager commandManager =
                new CommandManager();

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
                new ChooseDialogueCommand(
                        dialogueRunner
                )
        );

        /********** Node Dialogue commands ***********/

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

        /********** Preview Dialogue commands ***********/

        commandManager.register(
                new PreviewDialogueCommand(
                        dialogueService,
                        dialogueRunner
                )
        );

        /********** Transition Dialogue commands ***********/

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

        /********** Transition Action Dialogue commands ***********/

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

        /********** Transition Condition Dialogue commands ***********/

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
                getCommand("rpg");

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
    private void registerListeners() {

        Bukkit.getPluginManager()
                .registerEvents(
                        new NPCListener(
                                triggerManager
                        ),
                        this
                );
    }

    public static RpgEngine getInstance() {
        return instance;
    }

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