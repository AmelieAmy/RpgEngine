package fr.doryamy.rpgengine.dialogue.editor.view;

import fr.doryamy.rpgengine.dialogue.Dialogue;
import fr.doryamy.rpgengine.dialogue.DialogueActionEntry;
import fr.doryamy.rpgengine.dialogue.DialogueBranch;
import fr.doryamy.rpgengine.dialogue.DialogueChoice;
import fr.doryamy.rpgengine.dialogue.DialogueConditionEntry;
import fr.doryamy.rpgengine.dialogue.DialogueElement;
import fr.doryamy.rpgengine.dialogue.DialogueEnd;
import fr.doryamy.rpgengine.dialogue.DialogueCharacterProfile;
import fr.doryamy.rpgengine.dialogue.DialogueCharacterProfileService;
import fr.doryamy.rpgengine.dialogue.DialogueParticipant;
import fr.doryamy.rpgengine.dialogue.DialogueParticipantType;
import fr.doryamy.rpgengine.dialogue.DialogueLink;
import fr.doryamy.rpgengine.dialogue.DialogueReply;
import fr.doryamy.rpgengine.dialogue.DialogueStart;
import fr.doryamy.rpgengine.dialogue.editor.DialogueTriggerPresentation;
import fr.doryamy.rpgengine.quest.QuestSummary;

import java.util.List;
import java.util.Objects;

/** Transforme le domaine Dialogue en projection destinée à l'administration. */
public final class DialogueEditorViewMapper {

    private final DialogueCharacterProfileService characterProfileService;

    public DialogueEditorViewMapper(
            DialogueCharacterProfileService characterProfileService
    ) {
        this.characterProfileService = Objects.requireNonNull(
                characterProfileService,
                "characterProfileService"
        );
    }

    public DialogueEditorView present(Dialogue dialogue) {
        return present(dialogue, List.of(), List.of());
    }

    public DialogueEditorView present(
            Dialogue dialogue,
            List<DialogueTriggerPresentation> triggers
    ) {
        return present(dialogue, triggers, List.of());
    }

    public DialogueEditorView present(
            Dialogue dialogue,
            List<DialogueTriggerPresentation> triggers,
            List<QuestSummary> quests
    ) {
        Objects.requireNonNull(dialogue, "dialogue");
        Objects.requireNonNull(triggers, "triggers");
        Objects.requireNonNull(quests, "quests");

        List<DialogueEditorElementView> elements = dialogue.graph()
                .elements()
                .values()
                .stream()
                .map(element -> presentElement(element, dialogue))
                .toList();

        List<DialogueEditorLinkView> links = dialogue.graph()
                .links()
                .stream()
                .map(this::presentLink)
                .toList();

        List<DialogueEditorTriggerView> triggerViews = triggers.stream()
                .map(trigger -> new DialogueEditorTriggerView(
                        trigger.id(),
                        trigger.name(),
                        trigger.type(),
                        trigger.targetId(),
                        trigger.displayTarget()
                ))
                .toList();

        List<DialogueEditorQuestView> questViews = quests.stream()
                .map(quest -> new DialogueEditorQuestView(
                        quest.id(),
                        quest.name(),
                        quest.description()
                ))
                .toList();

        return new DialogueEditorView(
                dialogue.key().value(),
                dialogue.name(),
                triggerViews,
                questViews,
                elements,
                links
        );
    }

    private DialogueEditorElementView presentElement(
            DialogueElement element,
            Dialogue dialogue
    ) {
        fr.doryamy.rpgengine.dialogue.DialogueRules dialogueRules =
                dialogue.rules();
        return switch (element) {
            case DialogueStart start -> new DialogueEditorStartView(
                    start.key().value(),
                    presentConditions(dialogueRules.conditions()),
                    presentActions(dialogueRules.actions())
            );
            case DialogueReply reply -> presentReply(reply, dialogue);
            case DialogueBranch branch -> new DialogueEditorBranchView(branch.key().value());
            case DialogueChoice choice -> new DialogueEditorChoiceView(
                    choice.key().value(),
                    choice.text(),
                    choice.position(),
                    presentConditions(choice.rules().conditions()),
                    presentActions(choice.rules().actions())
            );
            case DialogueEnd end -> new DialogueEditorEndView(end.key().value());
        };
    }

    private DialogueEditorReplyView presentReply(
            DialogueReply reply,
            Dialogue dialogue
    ) {
        DialogueParticipant participant =
                dialogue.participants().require(reply.participantKey());

        String characterProfileKey = null;
        String characterDisplayName = null;

        if (participant.type() == DialogueParticipantType.NPC) {
            DialogueCharacterProfile profile =
                    characterProfileService.require(participant.characterProfileKey());

            characterProfileKey = profile.key().value();
            characterDisplayName = profile.displayName();
        }

        return new DialogueEditorReplyView(
                reply.key().value(),
                participant.type().name(),
                reply.text(),
                characterProfileKey,
                characterDisplayName,
                presentConditions(reply.rules().conditions()),
                presentActions(reply.rules().actions())
        );
    }

    private DialogueEditorLinkView presentLink(DialogueLink link) {
        return new DialogueEditorLinkView(
                link.source().value(),
                link.target().value()
        );
    }

    private List<DialogueEditorConditionView> presentConditions(
            List<DialogueConditionEntry> conditions
    ) {
        return conditions.stream()
                .map(entry -> new DialogueEditorConditionView(
                        entry.key().value(),
                        entry.condition().getProvider(),
                        entry.condition().getExpression()
                ))
                .toList();
    }

    private List<DialogueEditorActionView> presentActions(
            List<DialogueActionEntry> actions
    ) {
        return actions.stream()
                .map(entry -> new DialogueEditorActionView(
                        entry.key().value(),
                        entry.action().getProvider(),
                        entry.action().getExpression(),
                        entry.action().getPosition()
                ))
                .toList();
    }
}
