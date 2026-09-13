package fr.doryamy.rpgengine.dialogue.character.portrait;

import fr.doryamy.rpgengine.dialogue.DialogueCharacterProfile;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service d'assets des portraits Character.
 *
 * <p>Les octets envoyés par un client ne sont jamais considérés comme fiables.
 * Ils sont inspectés, décodés, bornés puis normalisés en PNG avant stockage.
 */
public final class CharacterPortraitAssetService {

    public static final int MAX_UPLOAD_BYTES = 2 * 1024 * 1024;
    public static final int MAX_SOURCE_DIMENSION = 4096;
    public static final int MAX_STORED_DIMENSION = 1024;

    private static final String RESOURCE_PREFIX = "portrait:";
    private static final Pattern MANAGED_RESOURCE_PATTERN =
            Pattern.compile("^portrait:([0-9a-f]{64})$");

    private final CharacterPortraitAssetStore store;

    public CharacterPortraitAssetService(CharacterPortraitAssetStore store) {
        this.store = Objects.requireNonNull(store, "store");
    }

    /**
     * Valide et importe un portrait. La ressource retournée est stable et
     * déterministe : deux images normalisées identiques partagent le même asset.
     */
    public String importPortrait(byte[] sourceBytes) {
        Objects.requireNonNull(sourceBytes, "sourceBytes");

        if (sourceBytes.length == 0) {
            throw new IllegalArgumentException("Le portrait envoyé est vide.");
        }
        if (sourceBytes.length > MAX_UPLOAD_BYTES) {
            throw new IllegalArgumentException(
                    "Le portrait dépasse la taille maximale de 2 Mio."
            );
        }

        BufferedImage decoded = decodeValidatedImage(sourceBytes);
        BufferedImage normalizedImage = resizeIfNecessary(decoded);
        byte[] pngBytes = encodePng(normalizedImage);
        String assetId = sha256Hex(pngBytes);

        store.putIfAbsent(assetId, pngBytes);
        return RESOURCE_PREFIX + assetId;
    }

    /**
     * Vérifie uniquement les références gérées par ce service. Les références
     * historiques/non gérées restent compatibles avec les données existantes.
     */
    public void requireAvailableManagedReference(String resourceKey) {
        String assetId = managedAssetId(resourceKey);
        if (assetId != null && !store.exists(assetId)) {
            throw new IllegalArgumentException(
                    "Le portrait RPGEngine référencé n'existe pas : " + resourceKey
            );
        }
    }

    /**
     * Charge les octets PNG d'une référence gérée par RPGEngine.
     *
     * <p>Cette méthode n'accepte volontairement aucune référence historique
     * ou chemin arbitraire : le transport runtime ne peut lire que les assets
     * content-addressed produits par ce service.
     */
    public byte[] loadManagedPortrait(String resourceKey) {
        String assetId = managedAssetId(resourceKey);
        if (assetId == null) {
            throw new IllegalArgumentException(
                    "Référence de portrait RPGEngine invalide : " + resourceKey
            );
        }

        if (!store.exists(assetId)) {
            throw new IllegalArgumentException(
                    "Le portrait RPGEngine référencé n'existe pas : " + resourceKey
            );
        }

        return store.read(assetId);
    }

    /** Supprime un asset seulement lorsqu'aucun Character ne le référence. */
    public void deleteIfUnreferenced(
            String resourceKey,
            List<DialogueCharacterProfile> profiles
    ) {
        String assetId = managedAssetId(resourceKey);
        if (assetId == null) {
            return;
        }

        boolean referenced = Objects.requireNonNull(profiles, "profiles")
                .stream()
                .map(DialogueCharacterProfile::portraitResource)
                .anyMatch(resourceKey::equals);

        if (!referenced) {
            store.delete(assetId);
        }
    }

    /**
     * Nettoyage de démarrage : retire les imports abandonnés qui ne sont liés à
     * aucun Character. Cela couvre notamment un import suivi d'une fermeture du
     * formulaire avant validation.
     */
    public void cleanupUnreferenced(List<DialogueCharacterProfile> profiles) {
        Objects.requireNonNull(profiles, "profiles");

        Set<String> referencedAssetIds = new HashSet<>();
        for (DialogueCharacterProfile profile : profiles) {
            String assetId = managedAssetId(profile.portraitResource());
            if (assetId != null) {
                referencedAssetIds.add(assetId);
            }
        }

        for (String storedAssetId : store.listAssetIds()) {
            if (!referencedAssetIds.contains(storedAssetId)) {
                store.delete(storedAssetId);
            }
        }
    }

    private BufferedImage decodeValidatedImage(byte[] sourceBytes) {
        try (
                ByteArrayInputStream input = new ByteArrayInputStream(sourceBytes);
                ImageInputStream imageInput = ImageIO.createImageInputStream(input)
        ) {
            if (imageInput == null) {
                throw new IllegalArgumentException("Format de portrait invalide.");
            }

            Iterator<ImageReader> readers = ImageIO.getImageReaders(imageInput);
            if (!readers.hasNext()) {
                throw new IllegalArgumentException(
                        "Le portrait doit être une image PNG ou JPEG valide."
                );
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(imageInput, true, true);

                String format = reader.getFormatName().toLowerCase(Locale.ROOT);
                if (!format.equals("png")
                        && !format.equals("jpeg")
                        && !format.equals("jpg")) {
                    throw new IllegalArgumentException(
                            "Seuls les portraits PNG et JPEG sont acceptés."
                    );
                }

                int width = reader.getWidth(0);
                int height = reader.getHeight(0);

                if (width <= 0 || height <= 0) {
                    throw new IllegalArgumentException(
                            "Les dimensions du portrait sont invalides."
                    );
                }

                if (width > MAX_SOURCE_DIMENSION || height > MAX_SOURCE_DIMENSION) {
                    throw new IllegalArgumentException(
                            "Le portrait ne peut pas dépasser "
                                    + MAX_SOURCE_DIMENSION
                                    + " x "
                                    + MAX_SOURCE_DIMENSION
                                    + " pixels."
                    );
                }

                BufferedImage image = reader.read(0);
                if (image == null) {
                    throw new IllegalArgumentException(
                            "Impossible de décoder le portrait."
                    );
                }
                return image;
            } finally {
                reader.dispose();
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Impossible de lire le portrait envoyé.",
                    e
            );
        }
    }

    private BufferedImage resizeIfNecessary(BufferedImage source) {
        int width = source.getWidth();
        int height = source.getHeight();
        int largest = Math.max(width, height);

        if (largest <= MAX_STORED_DIMENSION) {
            return ensureArgb(source);
        }

        double scale = (double) MAX_STORED_DIMENSION / largest;
        int targetWidth = Math.max(1, (int) Math.round(width * scale));
        int targetHeight = Math.max(1, (int) Math.round(height * scale));

        BufferedImage target = new BufferedImage(
                targetWidth,
                targetHeight,
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D graphics = target.createGraphics();
        try {
            graphics.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BICUBIC
            );
            graphics.setRenderingHint(
                    RenderingHints.KEY_RENDERING,
                    RenderingHints.VALUE_RENDER_QUALITY
            );
            graphics.drawImage(
                    source,
                    0,
                    0,
                    targetWidth,
                    targetHeight,
                    null
            );
        } finally {
            graphics.dispose();
        }

        return target;
    }

    private BufferedImage ensureArgb(BufferedImage source) {
        if (source.getType() == BufferedImage.TYPE_INT_ARGB) {
            return source;
        }

        BufferedImage target = new BufferedImage(
                source.getWidth(),
                source.getHeight(),
                BufferedImage.TYPE_INT_ARGB
        );

        Graphics2D graphics = target.createGraphics();
        try {
            graphics.drawImage(source, 0, 0, null);
        } finally {
            graphics.dispose();
        }
        return target;
    }

    private byte[] encodePng(BufferedImage image) {
        try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            if (!ImageIO.write(image, "png", output)) {
                throw new IllegalStateException(
                        "Aucun encodeur PNG n'est disponible sur le serveur."
                );
            }
            return output.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Impossible de normaliser le portrait en PNG.",
                    e
            );
        }
    }

    private String sha256Hex(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return java.util.HexFormat.of().formatHex(digest.digest(bytes));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 n'est pas disponible.", e);
        }
    }

    private String managedAssetId(String resourceKey) {
        if (resourceKey == null) {
            return null;
        }

        Matcher matcher = MANAGED_RESOURCE_PATTERN.matcher(resourceKey.trim());
        return matcher.matches() ? matcher.group(1) : null;
    }
}
