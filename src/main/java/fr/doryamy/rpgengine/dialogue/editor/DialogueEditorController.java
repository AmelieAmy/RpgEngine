package fr.doryamy.rpgengine.dialogue.editor;

import fr.doryamy.rpgengine.bridge.NeoForgeBridge;
import fr.doryamy.rpgengine.dialogue.Dialogue;
import fr.doryamy.rpgengine.dialogue.DialogueKey;
import fr.doryamy.rpgengine.dialogue.DialogueElementKey;
import fr.doryamy.rpgengine.dialogue.DialogueRuleKey;
import fr.doryamy.rpgengine.dialogue.DialogueEditingService;
import fr.doryamy.rpgengine.dialogue.DialogueService;
import fr.doryamy.rpgengine.dialogue.editor.selection.DialogueAdminNpcSelectionService;
import fr.doryamy.rpgengine.dialogue.editor.selection.NpcSelection;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueAdminView;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueEditorView;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueEditorViewMapper;
import fr.doryamy.rpgengine.model.Trigger;
import fr.doryamy.rpgengine.quest.QuestActionOperation;
import fr.doryamy.rpgengine.quest.QuestService;
import fr.doryamy.rpgengine.quest.QuestState;
import fr.doryamy.rpgengine.model.TriggerType;
import fr.doryamy.rpgengine.trigger.TriggerQueryService;
import fr.doryamy.rpgengine.trigger.TriggerService;
import fr.doryamy.rpgengine.util.RpgLogger;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/** Contrôleur applicatif de l'administration des dialogues. */
public final class DialogueEditorController {

    private final DialogueService dialogueService;
    private final DialogueCreationService dialogueCreationService;
    private final DialogueDeletionService dialogueDeletionService;
    private final DialogueNamingService dialogueNamingService;
    private final DialogueEditingService dialogueEditingService;
    private final DialogueEditorViewMapper editorViewMapper;
    private final DialogueAdminService dialogueAdminService;
    private final DialogueTriggerPresentationService triggerPresentationService;
    private final TriggerQueryService triggerQueryService;
    private final TriggerService triggerService;
    private final DialogueAdminNpcSelectionService npcSelectionService;
    private final NeoForgeBridge neoForgeBridge;
    private final QuestService questService;

    public DialogueEditorController(
            DialogueService dialogueService,
            DialogueCreationService dialogueCreationService,
            DialogueDeletionService dialogueDeletionService,
            DialogueNamingService dialogueNamingService,
            DialogueEditingService dialogueEditingService,
            DialogueEditorViewMapper editorViewMapper,
            DialogueAdminService dialogueAdminService,
            DialogueTriggerPresentationService triggerPresentationService,
            TriggerQueryService triggerQueryService,
            TriggerService triggerService,
            DialogueAdminNpcSelectionService npcSelectionService,
            NeoForgeBridge neoForgeBridge,
            QuestService questService
    ) {
        this.dialogueService = Objects.requireNonNull(dialogueService, "dialogueService");
        this.dialogueCreationService = Objects.requireNonNull(dialogueCreationService, "dialogueCreationService");
        this.dialogueDeletionService = Objects.requireNonNull(dialogueDeletionService, "dialogueDeletionService");
        this.dialogueNamingService = Objects.requireNonNull(dialogueNamingService, "dialogueNamingService");
        this.dialogueEditingService = Objects.requireNonNull(dialogueEditingService, "dialogueEditingService");
        this.editorViewMapper = Objects.requireNonNull(editorViewMapper, "editorViewMapper");
        this.dialogueAdminService = Objects.requireNonNull(dialogueAdminService, "dialogueAdminService");
        this.triggerPresentationService = Objects.requireNonNull(triggerPresentationService, "triggerPresentationService");
        this.triggerQueryService = Objects.requireNonNull(triggerQueryService, "triggerQueryService");
        this.triggerService = Objects.requireNonNull(triggerService, "triggerService");
        this.npcSelectionService = Objects.requireNonNull(npcSelectionService, "npcSelectionService");
        this.neoForgeBridge = Objects.requireNonNull(neoForgeBridge, "neoForgeBridge");
        this.questService = Objects.requireNonNull(questService, "questService");
    }

    public void openEditor(UUID playerUuid, String dialogueKeyValue) {
        Objects.requireNonNull(playerUuid, "playerUuid");
        Objects.requireNonNull(dialogueKeyValue, "dialogueKeyValue");

        final DialogueKey dialogueKey;
        try {
            dialogueKey = new DialogueKey(dialogueKeyValue);
        } catch (RuntimeException e) {
            RpgLogger.error("REQUEST_DIALOGUE_EDITOR contient une clé invalide pour le joueur "
                    + playerUuid + " : " + dialogueKeyValue);
            return;
        }

        openEditor(playerUuid, dialogueKey);
    }

    private void openEditor(UUID playerUuid, DialogueKey dialogueKey) {
        Dialogue dialogue = dialogueService.find(dialogueKey).orElse(null);
        if (dialogue == null) {
            RpgLogger.error("REQUEST_DIALOGUE_EDITOR impossible : dialogue introuvable " + dialogueKey);
            return;
        }

        DialogueEditorView view = editorViewMapper.present(
                dialogue,
                triggerPresentationService.present(dialogueKey),
                questService.findAll()
        );

        if (!neoForgeBridge.openDialogueEditor(playerUuid, view)) {
            RpgLogger.error("Impossible d'envoyer l'éditeur du dialogue "
                    + dialogueKey + " au joueur " + playerUuid);
        }
    }

    /** Renvoie la liste d'administration reconstruite depuis l'état serveur courant. */
    public void openAdmin(UUID playerUuid) {
        Objects.requireNonNull(playerUuid, "playerUuid");
        DialogueAdminView adminView = dialogueAdminService.present();
        if (!neoForgeBridge.openDialogueAdmin(playerUuid, adminView)) {
            RpgLogger.error("Impossible de renvoyer l'administration des dialogues au joueur " + playerUuid);
        }
    }

    /** Renomme un dialogue puis renvoie immédiatement l'éditeur actualisé. */
    public void renameDialogue(UUID playerUuid, Map<String, String> request) {
        Objects.requireNonNull(playerUuid, "playerUuid");
        Objects.requireNonNull(request, "request");

        String dialogueKeyValue = requireRequest(request, "dialogueKey");
        String name = requireRequest(request, "name");

        final DialogueKey dialogueKey;
        try {
            dialogueKey = new DialogueKey(dialogueKeyValue);
        } catch (RuntimeException e) {
            RpgLogger.error("REQUEST_RENAME_DIALOGUE contient une clé invalide : " + dialogueKeyValue);
            return;
        }

        try {
            dialogueNamingService.rename(dialogueKey, name);
        } catch (RuntimeException e) {
            RpgLogger.error("Impossible de renommer le dialogue " + dialogueKey + " : " + e.getMessage());
            return;
        }

        openEditor(playerUuid, dialogueKey);
    }

    public void createDialogue(UUID playerUuid, String name) {
        Objects.requireNonNull(playerUuid, "playerUuid");
        Objects.requireNonNull(name, "name");
        String normalizedName = name.trim();

        if (normalizedName.isBlank()) {
            RpgLogger.error("REQUEST_CREATE_DIALOGUE contient un nom vide pour le joueur " + playerUuid);
            return;
        }

        NpcSelection npcSelection = npcSelectionService.findSelection(playerUuid).orElse(null);
        if (npcSelection == null) {
            RpgLogger.error("REQUEST_CREATE_DIALOGUE refusé : aucun PNJ sélectionné pour le joueur " + playerUuid);
            return;
        }

        dialogueCreationService.createNpcDialogue(normalizedName, npcSelection.npcId());
        npcSelectionService.clearSelection(playerUuid);

        DialogueAdminView adminView = dialogueAdminService.present();
        if (!neoForgeBridge.openDialogueAdmin(playerUuid, adminView)) {
            RpgLogger.error("Le dialogue et son trigger NPC ont été créés mais l'administration n'a pas pu être renvoyée au joueur " + playerUuid);
        }
    }

    /** Supprime un dialogue et renvoie immédiatement la liste d'administration actualisée. */
    public void deleteDialogue(UUID playerUuid, String dialogueKeyValue) {
        Objects.requireNonNull(playerUuid, "playerUuid");
        Objects.requireNonNull(dialogueKeyValue, "dialogueKeyValue");

        final DialogueKey dialogueKey;
        try {
            dialogueKey = new DialogueKey(dialogueKeyValue);
        } catch (RuntimeException e) {
            RpgLogger.error("REQUEST_DELETE_DIALOGUE contient une clé invalide pour le joueur "
                    + playerUuid + " : " + dialogueKeyValue);
            return;
        }

        try {
            dialogueDeletionService.delete(dialogueKey);
        } catch (RuntimeException e) {
            RpgLogger.error("Impossible de supprimer le dialogue "
                    + dialogueKey + " : " + e.getMessage());
            return;
        }

        DialogueAdminView adminView = dialogueAdminService.present();
        if (!neoForgeBridge.openDialogueAdmin(playerUuid, adminView)) {
            RpgLogger.error("Le dialogue " + dialogueKey
                    + " a été supprimé mais l'administration n'a pas pu être renvoyée au joueur "
                    + playerUuid);
        }
    }

    /** Sélection NPC du formulaire de création. */
    public void beginNpcSelection(UUID playerUuid) {
        Objects.requireNonNull(playerUuid, "playerUuid");
        npcSelectionService.beginCreate(playerUuid);
    }

    /** Sélection NPC destinée à modifier un dialogue existant. */
    public void beginDialogueTriggerNpcSelection(UUID playerUuid, String dialogueKeyValue) {
        Objects.requireNonNull(playerUuid, "playerUuid");
        Objects.requireNonNull(dialogueKeyValue, "dialogueKeyValue");

        final DialogueKey dialogueKey;
        try {
            dialogueKey = new DialogueKey(dialogueKeyValue);
        } catch (RuntimeException e) {
            RpgLogger.error("REQUEST_DIALOGUE_TRIGGER_NPC_SELECTION contient une clé invalide : " + dialogueKeyValue);
            return;
        }

        if (dialogueService.find(dialogueKey).isEmpty()) {
            RpgLogger.error("Sélection NPC refusée : dialogue introuvable " + dialogueKey);
            return;
        }

        List<Trigger> triggers = triggerQueryService.findDialogTriggers(dialogueKey);
        if (!hasOnlyNpcTriggers(triggers)) {
            RpgLogger.error("Sélection NPC refusée pour " + dialogueKey
                    + " : le dialogue doit posséder au moins un trigger et tous ses triggers doivent être de type NPC.");
            return;
        }

        npcSelectionService.beginEdit(playerUuid, dialogueKey);
    }

    /** Résultat de la sélection NPC utilisée par la création. */
    public void completeNpcSelection(UUID playerUuid, String npcId, String npcName) {
        Objects.requireNonNull(playerUuid, "playerUuid");
        Objects.requireNonNull(npcId, "npcId");
        Objects.requireNonNull(npcName, "npcName");

        if (!neoForgeBridge.showDialogueNpcSelectionResult(playerUuid, npcId, npcName)) {
            RpgLogger.error("Impossible de renvoyer la sélection NPC au joueur "
                    + playerUuid + " | npcId=" + npcId);
        }
    }

    /** Applique autoritairement le nouveau PNJ puis renvoie l'éditeur actualisé. */
    public void completeDialogueTriggerNpcSelection(
            UUID playerUuid,
            DialogueKey dialogueKey,
            String npcId
    ) {
        Objects.requireNonNull(playerUuid, "playerUuid");
        Objects.requireNonNull(dialogueKey, "dialogueKey");
        Objects.requireNonNull(npcId, "npcId");

        List<Trigger> triggers = triggerQueryService.findDialogTriggers(dialogueKey);
        if (!hasOnlyNpcTriggers(triggers)) {
            RpgLogger.error("Modification du trigger NPC refusée pour " + dialogueKey
                    + " : l'association de triggers a changé ou est ambiguë.");
            return;
        }

        List<Integer> triggerIds = triggers.stream().map(Trigger::getId).toList();
        if (!triggerService.updateTargets(triggerIds, TriggerType.NPC, npcId)) {
            RpgLogger.error("Impossible de modifier les triggers NPC du dialogue " + dialogueKey);
            return;
        }

        openEditor(playerUuid, dialogueKey);
    }

    /** Enregistre une condition ou une action QUEST appartenant à une Reply/Choice. */
    public void saveQuestRule(
            UUID playerUuid,
            Map<String, String> request
    ) {
        Objects.requireNonNull(playerUuid, "playerUuid");
        Objects.requireNonNull(request, "request");

        try {
            DialogueKey dialogueKey = new DialogueKey(requireRequest(request, "dialogueKey"));
            DialogueElementKey ownerKey = new DialogueElementKey(requireRequest(request, "ownerKey"));
            String ruleKind = requireRequest(request, "ruleKind").toUpperCase();
            String ruleKeyValue = request.getOrDefault("ruleKey", "").trim();
            String questId = requireRequest(request, "questId");
            String value = requireRequest(request, "value").toUpperCase();

            boolean questExists = questService.findAll().stream()
                    .anyMatch(quest -> quest.id().equals(questId));

            if (!questExists) {
                throw new IllegalArgumentException("Quête inconnue : " + questId);
            }

            if ("CONDITION".equals(ruleKind)) {
                QuestState state = QuestState.valueOf(value);
                if (state == QuestState.UNAVAILABLE) {
                    throw new IllegalArgumentException("UNAVAILABLE n'est pas un état sélectionnable.");
                }

                String expression = questId + "==" + state.name();

                if (ruleKeyValue.isEmpty()) {
                    dialogueEditingService.addCondition(
                            dialogueKey, ownerKey, "QUEST", expression
                    );
                } else {
                    dialogueEditingService.updateCondition(
                            dialogueKey,
                            ownerKey,
                            new DialogueRuleKey(ruleKeyValue),
                            "QUEST",
                            expression
                    );
                }
            } else if ("ACTION".equals(ruleKind)) {
                QuestActionOperation operation = QuestActionOperation.valueOf(value);
                String expression = operation.toExpression(questId);

                if (ruleKeyValue.isEmpty()) {
                    dialogueEditingService.addAction(
                            dialogueKey, ownerKey, "QUEST", expression
                    );
                } else {
                    dialogueEditingService.updateAction(
                            dialogueKey,
                            ownerKey,
                            new DialogueRuleKey(ruleKeyValue),
                            "QUEST",
                            expression
                    );
                }
            } else {
                throw new IllegalArgumentException("Type de règle QUEST inconnu : " + ruleKind);
            }

            openEditor(playerUuid, dialogueKey);

        } catch (RuntimeException e) {
            RpgLogger.error(
                    "REQUEST_SAVE_DIALOGUE_QUEST_RULE refusé pour le joueur "
                            + playerUuid
                            + " : "
                            + e.getMessage()
            );
        }
    }

    private static String requireRequest(
            Map<String, String> request,
            String key
    ) {
        String value = request.get(key);
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Champ de requête manquant : " + key);
        }
        return value.trim();
    }

    private boolean hasOnlyNpcTriggers(List<Trigger> triggers) {
        return !triggers.isEmpty()
                && triggers.stream().allMatch(trigger -> trigger.getType() == TriggerType.NPC);
    }
}
