package fr.doryamy.rpgengine.dialogue.editor.transport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueEditorActionView;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueEditorBranchView;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueEditorChoiceView;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueEditorConditionView;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueEditorElementView;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueEditorEndView;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueEditorLinkView;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueEditorQuestView;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueEditorReplyView;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueEditorStartView;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueEditorTriggerView;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueEditorView;

import java.util.Objects;

/**
 * Encode la projection d'administration d'un dialogue
 * dans le protocole JSON échangé avec le mod NeoForge.
 *
 * <p>Ce composant ne contient aucune logique métier.
 *
 * <p>Le format produit constitue un contrat de transport
 * explicite et indépendant de l'implémentation Java
 * des DTO.
 */
public final class DialogueEditorTransportEncoder {

    public static final int PROTOCOL_VERSION = 3;

    private final Gson gson =
            new GsonBuilder()
                    .disableHtmlEscaping()
                    .create();

    /**
     * Encode une vue d'éditeur complète.
     */
    public String encode(
            DialogueEditorView view
    ) {

        Objects.requireNonNull(
                view,
                "view"
        );

        JsonObject root =
                new JsonObject();

        root.addProperty(
                "protocolVersion",
                PROTOCOL_VERSION
        );

        root.add(
                "dialogue",
                encodeDialogue(view)
        );

        return gson.toJson(root);
    }

    private JsonObject encodeDialogue(
            DialogueEditorView view
    ) {

        JsonObject dialogue =
                new JsonObject();

        dialogue.addProperty(
                "key",
                view.dialogueKey()
        );

        dialogue.addProperty(
                "name",
                view.name()
        );

        JsonArray triggers =
                new JsonArray();

        for (DialogueEditorTriggerView trigger : view.triggers()) {
            JsonObject triggerJson = new JsonObject();
            triggerJson.addProperty("id", trigger.id());
            triggerJson.addProperty("name", trigger.name());
            triggerJson.addProperty("type", trigger.type());
            triggerJson.addProperty("targetId", trigger.targetId());
            triggerJson.addProperty("displayTarget", trigger.displayTarget());
            triggers.add(triggerJson);
        }

        dialogue.add(
                "triggers",
                triggers
        );

        JsonArray quests =
                new JsonArray();

        for (DialogueEditorQuestView quest : view.quests()) {
            JsonObject questJson = new JsonObject();
            questJson.addProperty("id", quest.id());
            questJson.addProperty("name", quest.name());
            questJson.addProperty("description", quest.description());
            quests.add(questJson);
        }

        dialogue.add(
                "quests",
                quests
        );

        JsonArray elements =
                new JsonArray();

        for (
                DialogueEditorElementView element
                : view.elements()
        ) {

            elements.add(
                    encodeElement(element)
            );
        }

        dialogue.add(
                "elements",
                elements
        );

        JsonArray links =
                new JsonArray();

        for (
                DialogueEditorLinkView link
                : view.links()
        ) {

            links.add(
                    encodeLink(link)
            );
        }

        dialogue.add(
                "links",
                links
        );

        return dialogue;
    }

    private JsonObject encodeElement(
            DialogueEditorElementView element
    ) {

        return switch (element) {

            case DialogueEditorStartView start ->
                    encodeStart(start);

            case DialogueEditorReplyView reply ->
                    encodeReply(reply);

            case DialogueEditorBranchView branch ->
                    encodeBranch(branch);

            case DialogueEditorChoiceView choice ->
                    encodeChoice(choice);

            case DialogueEditorEndView end ->
                    encodeEnd(end);
        };
    }

    private JsonObject encodeStart(
            DialogueEditorStartView start
    ) {

        JsonObject json =
                baseElement(
                        start.key(),
                        "START"
                );

        return json;
    }

    private JsonObject encodeReply(
            DialogueEditorReplyView reply
    ) {

        JsonObject json =
                baseElement(
                        reply.key(),
                        "REPLY"
                );

        json.addProperty(
                "speaker",
                reply.speaker()
        );

        json.addProperty(
                "text",
                reply.text()
        );

        json.add(
                "conditions",
                encodeConditions(
                        reply.conditions()
                )
        );

        json.add(
                "actions",
                encodeActions(
                        reply.actions()
                )
        );

        return json;
    }

    private JsonObject encodeBranch(
            DialogueEditorBranchView branch
    ) {

        return baseElement(
                branch.key(),
                "BRANCH"
        );
    }

    private JsonObject encodeChoice(
            DialogueEditorChoiceView choice
    ) {

        JsonObject json =
                baseElement(
                        choice.key(),
                        "CHOICE"
                );

        json.addProperty(
                "text",
                choice.text()
        );

        json.addProperty(
                "position",
                choice.position()
        );

        json.add(
                "conditions",
                encodeConditions(
                        choice.conditions()
                )
        );

        json.add(
                "actions",
                encodeActions(
                        choice.actions()
                )
        );

        return json;
    }

    private JsonObject encodeEnd(
            DialogueEditorEndView end
    ) {

        return baseElement(
                end.key(),
                "END"
        );
    }

    private JsonObject baseElement(
            String key,
            String type
    ) {

        JsonObject json =
                new JsonObject();

        json.addProperty(
                "key",
                key
        );

        json.addProperty(
                "type",
                type
        );

        return json;
    }

    private JsonArray encodeConditions(
            Iterable<DialogueEditorConditionView> conditions
    ) {

        JsonArray json =
                new JsonArray();

        for (
                DialogueEditorConditionView condition
                : conditions
        ) {

            JsonObject entry =
                    new JsonObject();

            entry.addProperty(
                    "key",
                    condition.key()
            );

            entry.addProperty(
                    "provider",
                    condition.provider()
            );

            entry.addProperty(
                    "expression",
                    condition.expression()
            );

            json.add(entry);
        }

        return json;
    }

    private JsonArray encodeActions(
            Iterable<DialogueEditorActionView> actions
    ) {

        JsonArray json =
                new JsonArray();

        for (
                DialogueEditorActionView action
                : actions
        ) {

            JsonObject entry =
                    new JsonObject();

            entry.addProperty(
                    "key",
                    action.key()
            );

            entry.addProperty(
                    "provider",
                    action.provider()
            );

            entry.addProperty(
                    "expression",
                    action.expression()
            );

            entry.addProperty(
                    "position",
                    action.position()
            );

            json.add(entry);
        }

        return json;
    }

    private JsonObject encodeLink(
            DialogueEditorLinkView link
    ) {

        JsonObject json =
                new JsonObject();

        json.addProperty(
                "sourceKey",
                link.sourceKey()
        );

        json.addProperty(
                "targetKey",
                link.targetKey()
        );

        return json;
    }
}