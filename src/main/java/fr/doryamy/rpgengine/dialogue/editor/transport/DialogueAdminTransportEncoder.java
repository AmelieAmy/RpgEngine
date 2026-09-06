package fr.doryamy.rpgengine.dialogue.editor.transport;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueAdminEntryView;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueAdminTriggerView;
import fr.doryamy.rpgengine.dialogue.editor.view.DialogueAdminView;

import java.util.Objects;

/**
 * Encode la vue résumée de l'administration
 * des dialogues vers le protocole JSON plugin -> mod.
 */
public final class DialogueAdminTransportEncoder {

    public static final int PROTOCOL_VERSION = 2;

    private final Gson gson =
            new GsonBuilder()
                    .disableHtmlEscaping()
                    .create();

    public String encode(
            DialogueAdminView view
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

        JsonArray dialogues =
                new JsonArray();

        for (DialogueAdminEntryView entry
                : view.dialogues()) {

            JsonObject dialogue =
                    new JsonObject();

            dialogue.addProperty(
                    "key",
                    entry.key()
            );

            dialogue.addProperty(
                    "name",
                    entry.name()
            );

            JsonArray triggers =
                    new JsonArray();

            for (DialogueAdminTriggerView trigger
                    : entry.triggers()) {

                JsonObject triggerJson =
                        new JsonObject();

                triggerJson.addProperty(
                        "id",
                        trigger.id()
                );

                triggerJson.addProperty(
                        "name",
                        trigger.name()
                );

                triggerJson.addProperty(
                        "type",
                        trigger.type()
                );

                triggerJson.addProperty(
                        "targetId",
                        trigger.targetId()
                );

                triggerJson.addProperty(
                        "displayTarget",
                        trigger.displayTarget()
                );

                triggers.add(
                        triggerJson
                );
            }

            dialogue.add(
                    "triggers",
                    triggers
            );

            dialogues.add(
                    dialogue
            );
        }

        root.add(
                "dialogues",
                dialogues
        );

        return gson.toJson(
                root
        );
    }
}
