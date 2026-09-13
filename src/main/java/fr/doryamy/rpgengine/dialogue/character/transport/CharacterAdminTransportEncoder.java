package fr.doryamy.rpgengine.dialogue.character.transport;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.doryamy.rpgengine.dialogue.character.view.CharacterAdminEntryView;
import fr.doryamy.rpgengine.dialogue.character.view.CharacterAdminTriggerView;
import fr.doryamy.rpgengine.dialogue.character.view.CharacterAdminView;

import java.util.Objects;

/** Encode la projection de bibliothèque de personnages pour le client NeoForge. */
public final class CharacterAdminTransportEncoder {

    public static final int PROTOCOL_VERSION = 2;

    public String encode(CharacterAdminView view) {
        Objects.requireNonNull(view, "view");

        JsonObject root = new JsonObject();
        root.addProperty("protocolVersion", PROTOCOL_VERSION);

        JsonArray characters = new JsonArray();
        for (CharacterAdminEntryView character : view.characters()) {
            JsonObject json = new JsonObject();
            json.addProperty("key", character.key());
            json.addProperty("name", character.name());
            if (character.portraitResource() == null) {
                json.add("portraitResource", null);
            } else {
                json.addProperty("portraitResource", character.portraitResource());
            }
            if (character.citizensNpcId() == null) {
                json.add("citizensNpcId", null);
                json.add("citizensNpcName", null);
            } else {
                json.addProperty("citizensNpcId", character.citizensNpcId());
                json.addProperty("citizensNpcName", character.citizensNpcName());
            }
            characters.add(json);
        }
        root.add("characters", characters);

        JsonArray triggers = new JsonArray();
        for (CharacterAdminTriggerView trigger : view.triggers()) {
            JsonObject json = new JsonObject();
            json.addProperty("id", trigger.id());
            json.addProperty("name", trigger.name());
            json.addProperty("type", trigger.type());
            json.addProperty("targetId", trigger.targetId());
            triggers.add(json);
        }
        root.add("triggers", triggers);

        return root.toString();
    }
}
