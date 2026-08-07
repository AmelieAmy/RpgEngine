package fr.doryamy.rpgengine;

import fr.doryamy.rpgengine.action.ActionManager;
import fr.doryamy.rpgengine.action.executors.MessageActionExecutor;
import fr.doryamy.rpgengine.action.executors.PlayerVariableActionExecutor;
import fr.doryamy.rpgengine.condition.ConditionManager;
import fr.doryamy.rpgengine.condition.providers.PlayerConditionProvider;
import fr.doryamy.rpgengine.database.DatabaseManager;
import fr.doryamy.rpgengine.listener.NPCListener;
import fr.doryamy.rpgengine.repository.PlayerVariableRepository;
import fr.doryamy.rpgengine.trigger.TriggerManager;
import fr.doryamy.rpgengine.util.RpgLogger;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Point d'entrée principal de RPGEngine.
 *
 * Cette classe est responsable de l'assemblage
 * des différents composants du moteur :
 *
 *   configuration ;
 *   base de données ;
 *   repositories ;
 *   managers ;
 *   providers ;
 *   executors ;
 *   listeners.
 *
 * Elle ne contient aucune logique métier.
 */
public final class RpgEngine extends JavaPlugin {

    private static RpgEngine instance;

    private DatabaseManager databaseManager;
    private TriggerManager triggerManager;

    @Override
    public void onEnable() {
        instance = this;

        initializeConfiguration();
        initializeDatabase();

        PlayerVariableRepository playerVariableRepository =
                createPlayerVariableRepository();

        ConditionManager conditionManager =
                createConditionManager(
                        playerVariableRepository
                );

        ActionManager actionManager =
                createActionManager(
                        playerVariableRepository
                );

        triggerManager =
                createTriggerManager(
                        conditionManager,
                        actionManager
                );

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
     * Initialise la connexion SQLite
     * et applique les migrations.
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
     * Crée et configure le système de conditions.
     *
     * @param playerVariableRepository repository des variables joueur
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
     * @param playerVariableRepository repository des variables joueur
     *
     * @return ActionManager configuré
     */
    private ActionManager createActionManager(
            PlayerVariableRepository playerVariableRepository
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
     * Enregistre les listeners utilisés par RPGEngine.
     */
    private void registerListeners() {
        Bukkit.getPluginManager()
                .registerEvents(
                        new NPCListener(triggerManager),
                        this
                );
    }

    public static RpgEngine getInstance() {
        return instance;
    }

    public TriggerManager getTriggerManager() {
        return triggerManager;
    }
}