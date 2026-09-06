package fr.doryamy.rpgengine.dialogue.editor.selection;

import fr.doryamy.rpgengine.dialogue.DialogueKey;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** Gère les sessions temporaires de sélection de PNJ pour l'administration. */
public final class DialogueAdminNpcSelectionService {

    private final Map<UUID, NpcSelectionIntent> pendingSelections =
            new ConcurrentHashMap<>();

    private final Map<UUID, NpcSelection> creationSelections =
            new ConcurrentHashMap<>();

    public void begin(UUID playerUuid) {
        beginCreate(playerUuid);
    }

    public void beginCreate(UUID playerUuid) {
        UUID uuid = Objects.requireNonNull(playerUuid, "playerUuid");
        creationSelections.remove(uuid);
        pendingSelections.put(uuid, new CreateDialogueNpcSelectionIntent());
    }

    public void beginEdit(UUID playerUuid, DialogueKey dialogueKey) {
        UUID uuid = Objects.requireNonNull(playerUuid, "playerUuid");
        Objects.requireNonNull(dialogueKey, "dialogueKey");
        creationSelections.remove(uuid);
        pendingSelections.put(uuid, new EditDialogueTriggerNpcSelectionIntent(dialogueKey));
    }

    public boolean isSelecting(UUID playerUuid) {
        return pendingSelections.containsKey(Objects.requireNonNull(playerUuid, "playerUuid"));
    }

    public Optional<NpcSelectionCompletion> complete(
            UUID playerUuid,
            String npcId,
            String npcName
    ) {
        UUID uuid = Objects.requireNonNull(playerUuid, "playerUuid");
        NpcSelectionIntent intent = pendingSelections.remove(uuid);
        if (intent == null) return Optional.empty();

        NpcSelection selection = new NpcSelection(npcId, npcName);
        if (intent instanceof CreateDialogueNpcSelectionIntent) {
            creationSelections.put(uuid, selection);
        }

        return Optional.of(new NpcSelectionCompletion(intent, selection));
    }

    public Optional<NpcSelection> findSelection(UUID playerUuid) {
        return Optional.ofNullable(
                creationSelections.get(Objects.requireNonNull(playerUuid, "playerUuid"))
        );
    }

    public void clearSelection(UUID playerUuid) {
        creationSelections.remove(Objects.requireNonNull(playerUuid, "playerUuid"));
    }

    public void cancel(UUID playerUuid) {
        UUID uuid = Objects.requireNonNull(playerUuid, "playerUuid");
        pendingSelections.remove(uuid);
        creationSelections.remove(uuid);
    }
}
