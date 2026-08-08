package fr.doryamy.rpgengine;

import fr.doryamy.rpgengine.action.ActionManager;
import fr.doryamy.rpgengine.action.executors.DialogActionExecutor;
import fr.doryamy.rpgengine.action.executors.MessageActionExecutor;
import fr.doryamy.rpgengine.action.executors.PlayerVariableActionExecutor;
import fr.doryamy.rpgengine.command.CommandManager;
import fr.doryamy.rpgengine.command.RpgCommand;
import fr.doryamy.rpgengine.condition.ConditionManager;
import fr.doryamy.rpgengine.condition.providers.PlayerConditionProvider;
import fr.doryamy.rpgengine.database.DatabaseManager;
import fr.doryamy.rpgengine.dialogue.DialogueRepository;
import fr.doryamy.rpgengine.dialogue.DialogueRunner;
import fr.doryamy.rpgengine.dialogue.DialogueService;
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
 * - des composants d'exécution ;
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
         * Services métier
         */
        dialogueService = createDialogueService(dialogueRepository);

        /*
         * Runtime des features
         */
        DialogueRunner dialogueRunner = createDialogueRunner();

        /*
         * Rule Engine
         */
        ConditionManager conditionManager =
                createConditionManager(
                        playerVariableRepository
                );

        ActionManager actionManager =
                createActionManager(
                        playerVariableRepository,
                        dialogueRepository,
                        dialogueRunner
                );

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
            DialogueRepository dialogueRepository
    ) {

        return new DialogueService(
                dialogueRepository
        );
    }

    /**
     * Crée le composant responsable
     * de l'exécution des dialogues en jeu.
     *
     * @return runner configuré
     */
    private DialogueRunner createDialogueRunner() {

        return new DialogueRunner();
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
            PlayerVariableRepository playerVariableRepository
    ) {

        ConditionManager conditionManager =
                new ConditionManager();

        conditionManager.register(
                new PlayerConditionProvider(
                        playerVariableRepository
                )
        );

        return conditionManager;
    }

    /**
     * Crée et configure le système d'actions.
     *
     * @param playerVariableRepository repository
     *                                 des variables joueur
     * @param dialogueRepository repository
     *                           des dialogues
     * @param dialogueRunner composant d'exécution
     *                       des dialogues
     *
     * @return ActionManager configuré
     */
    private ActionManager createActionManager(
            PlayerVariableRepository playerVariableRepository,
            DialogueRepository dialogueRepository,
            DialogueRunner dialogueRunner
    ) {

        ActionManager actionManager =
                new ActionManager();

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

        return actionManager;
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
     *
     * @return CommandManager configuré
     */
    private CommandManager createCommandManager(
            DialogueService dialogueService,
            DialogueRunner dialogueRunner
    ) {
        CommandManager commandManager = new CommandManager();
        commandManager.register(
                new CreateDialogueCommand(
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
                new AddLineDialogueCommand(
                        dialogueService
                )
        );
        commandManager.register(
                new PlayDialogueCommand(
                        dialogueService,
                        dialogueRunner
                )
        );

        return commandManager;
    }

    /**
     * Enregistre la commande principale /rpg.
     *
     * @param commandManager gestionnaire des sous-commandes
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