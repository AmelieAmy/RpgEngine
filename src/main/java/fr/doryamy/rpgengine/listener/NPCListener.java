package fr.doryamy.rpgengine.listener;

import fr.doryamy.rpgengine.model.TriggerType;
import fr.doryamy.rpgengine.trigger.TriggerContext;
import fr.doryamy.rpgengine.trigger.TriggerManager;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Adapte les événements provenant des PNJ Citizens
 * vers le système de triggers de RPGEngine.
 *
 * Cette classe ne contient aucune logique métier.
 * Elle transforme uniquement les événements Citizens en
 * {@link TriggerContext} puis les transmet au {@link TriggerManager}.
 */
public final class NPCListener implements Listener {

    private final TriggerManager triggerManager;

    /**
     * Construit un listener Citizens.
     *
     * @param triggerManager gestionnaire des triggers
     */
    public NPCListener(TriggerManager triggerManager) {
        this.triggerManager = triggerManager;
    }

    /**
     * Déclenché lorsqu'un joueur effectue un clic droit
     * sur un PNJ Citizens.
     *
     * L'événement est converti en {@link TriggerContext}
     * puis transmis au moteur.
     *
     * @param event événement Citizens
     */
    @EventHandler
    public void onNPCClick(NPCRightClickEvent event) {

        TriggerContext context =
                TriggerContext.builder()
                        .player(event.getClicker())
                        .triggerType(TriggerType.NPC)
                        .targetId(String.valueOf(event.getNPC().getId()))
                        .attribute(
                                "npcName",
                                event.getNPC().getName()
                        )
                        .build();

        triggerManager.handleTrigger(context);
    }
}