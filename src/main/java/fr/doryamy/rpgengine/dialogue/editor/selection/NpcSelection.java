package fr.doryamy.rpgengine.dialogue.editor.selection;

import java.util.Objects;

/**
 * Représente un PNJ Citizens sélectionné
 * dans un brouillon d'administration.
 *
 * <p>L'identifiant Citizens constitue l'identité
 * technique de la cible. Le nom est conservé uniquement
 * comme information d'affichage du brouillon.
 */
public record NpcSelection(
        String npcId,
        String npcName
) {

    public NpcSelection {

        Objects.requireNonNull(
                npcId,
                "npcId"
        );

        Objects.requireNonNull(
                npcName,
                "npcName"
        );

        npcId = npcId.trim();
        npcName = npcName.trim();

        if (npcId.isBlank()) {
            throw new IllegalArgumentException(
                    "L'identifiant du PNJ ne peut pas être vide."
            );
        }

        if (npcName.isBlank()) {
            throw new IllegalArgumentException(
                    "Le nom du PNJ ne peut pas être vide."
            );
        }
    }
}
