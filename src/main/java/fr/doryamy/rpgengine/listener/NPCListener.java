package fr.doryamy.rpgengine.listener;

import fr.doryamy.rpgengine.bridge.NeoForgeBridge;
import fr.doryamy.rpgengine.dialogue.editor.DialogueEditorService;
import fr.doryamy.rpgengine.dialogue.editor.DialogueTriggerSelectionService;
import fr.doryamy.rpgengine.model.TriggerType;
import fr.doryamy.rpgengine.trigger.TriggerContext;
import fr.doryamy.rpgengine.trigger.TriggerManager;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Optional;
import java.util.UUID;

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
    private final DialogueTriggerSelectionService triggerSelectionService;
    private final DialogueEditorService dialogueEditorService;
    private final NeoForgeBridge neoForgeBridge;

    /**
     * Construit un listener Citizens.
     *
     * @param triggerManager gestionnaire des triggers
     */
    public NPCListener(
            TriggerManager triggerManager,
            DialogueTriggerSelectionService triggerSelectionService,
            DialogueEditorService dialogueEditorService,
            NeoForgeBridge neoForgeBridge
    ) {
        this.triggerManager =
                triggerManager;

        this.triggerSelectionService =
                triggerSelectionService;

        this.dialogueEditorService =
                dialogueEditorService;

        this.neoForgeBridge =
                neoForgeBridge;
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

        UUID playerUuid =
                event.getClicker()
                        .getUniqueId();

        String npcId =
                String.valueOf(
                        event.getNPC()
                                .getId()
                );

        if (triggerSelectionService.isSelecting(
                playerUuid
        )) {

            Optional<DialogueTriggerSelectionService.SelectionResult> result =
                    triggerSelectionService.selectNpc(
                            playerUuid,
                            npcId
                    );

            if (result.isEmpty()) {

                event.getClicker()
                        .sendMessage(
                                "§cImpossible de modifier le déclencheur."
                        );

                return;
            }

            DialogueTriggerSelectionService.SelectionResult selection =
                    result.get();

            event.getClicker()
                    .sendMessage(
                            "§aDéclencheur modifié : NPC "
                                    + selection.npcId()
                    );

            dialogueEditorService
                    .buildView(
                            selection.dialogueKey()
                    )
                    .ifPresentOrElse(
                            view ->
                                    neoForgeBridge.showDialogueEditor(
                                            playerUuid,
                                            view
                                    ),
                            () ->
                                    event.getClicker()
                                            .sendMessage(
                                                    "§cLe déclencheur a été modifié, "
                                                            + "mais l'éditeur n'a pas pu être rouvert."
                                            )
                    );

            return;
        }

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

    /**
     * Nettoie une éventuelle sélection temporaire
     * lorsque le joueur quitte le serveur.
     */
    @EventHandler
    public void onPlayerQuit(
            PlayerQuitEvent event
    ) {
        triggerSelectionService.cancelSelection(
                event.getPlayer()
                        .getUniqueId()
        );
    }

}