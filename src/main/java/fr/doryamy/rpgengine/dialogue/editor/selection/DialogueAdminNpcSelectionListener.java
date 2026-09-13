package fr.doryamy.rpgengine.dialogue.editor.selection;

import fr.doryamy.rpgengine.dialogue.character.CharacterAdminController;
import fr.doryamy.rpgengine.dialogue.editor.DialogueEditorController;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/** Adaptateur Citizens dédié à la sélection administrative d'un PNJ. */
public final class DialogueAdminNpcSelectionListener implements Listener {

    private final DialogueAdminNpcSelectionService selectionService;
    private final DialogueEditorController dialogueEditorController;
    private final CharacterAdminController characterAdminController;

    public DialogueAdminNpcSelectionListener(
            DialogueAdminNpcSelectionService selectionService,
            DialogueEditorController dialogueEditorController,
            CharacterAdminController characterAdminController
    ) {
        this.selectionService = Objects.requireNonNull(selectionService, "selectionService");
        this.dialogueEditorController = Objects.requireNonNull(dialogueEditorController, "dialogueEditorController");
        this.characterAdminController = Objects.requireNonNull(characterAdminController, "characterAdminController");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onNpcSelection(NPCRightClickEvent event) {
        UUID playerUuid = event.getClicker().getUniqueId();
        String npcId = String.valueOf(event.getNPC().getId());
        String npcName = event.getNPC().getName();

        Optional<NpcSelectionCompletion> completionResult =
                selectionService.complete(playerUuid, npcId, npcName);

        if (completionResult.isEmpty()) return;

        event.setCancelled(true);

        NpcSelectionCompletion completion = completionResult.get();

        switch (completion.intent()) {
            case CreateDialogueNpcSelectionIntent ignored ->
                    dialogueEditorController.completeNpcSelection(
                            playerUuid,
                            completion.selection().npcId(),
                            completion.selection().npcName()
                    );

            case EditDialogueTriggerNpcSelectionIntent edit ->
                    dialogueEditorController.completeDialogueTriggerNpcSelection(
                            playerUuid,
                            edit.dialogueKey(),
                            completion.selection().npcId()
                    );

            case CharacterNpcSelectionIntent ignored ->
                    characterAdminController.completeNpcSelection(
                            playerUuid,
                            completion.selection().npcId(),
                            completion.selection().npcName()
                    );

            case CharacterTriggerNpcSelectionIntent ignored ->
                    characterAdminController.completeNpcSelection(
                            playerUuid,
                            completion.selection().npcId(),
                            completion.selection().npcName()
                    );
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        selectionService.cancel(event.getPlayer().getUniqueId());
    }
}
