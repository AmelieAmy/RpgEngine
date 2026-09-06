package fr.doryamy.rpgengine.listener;

import fr.doryamy.rpgengine.model.TriggerType;
import fr.doryamy.rpgengine.trigger.TriggerContext;
import fr.doryamy.rpgengine.trigger.TriggerManager;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.Objects;

/**
 * Adapte les événements provenant des PNJ Citizens
 * vers le moteur de triggers RPGEngine.
 *
 * <p>Cette classe ne contient aucune logique métier.
 * Elle transforme uniquement un événement Citizens
 * en TriggerContext puis le transmet au TriggerManager.
 *
 * <p>Les clics consommés par l'administration
 * sont ignorés grâce à ignoreCancelled = true.
 */
public final class NPCListener implements Listener {

    private final TriggerManager triggerManager;

    public NPCListener(
            TriggerManager triggerManager
    ) {

        this.triggerManager =
                Objects.requireNonNull(
                        triggerManager,
                        "triggerManager"
                );
    }

    @EventHandler(
            ignoreCancelled = true
    )
    public void onNPCClick(
            NPCRightClickEvent event
    ) {

        TriggerContext context =
                TriggerContext.builder()
                        .player(
                                event.getClicker()
                        )
                        .triggerType(
                                TriggerType.NPC
                        )
                        .targetId(
                                String.valueOf(
                                        event.getNPC()
                                                .getId()
                                )
                        )
                        .attribute(
                                "npcName",
                                event.getNPC()
                                        .getName()
                        )
                        .build();

        triggerManager.handleTrigger(
                context
        );
    }
}
