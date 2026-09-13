package fr.doryamy.rpgengine.dialogue.character.portrait;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/** Stockage filesystem des portraits normalisés en PNG. */
public final class FileSystemCharacterPortraitAssetStore
        implements CharacterPortraitAssetStore {

    private static final Pattern ASSET_ID_PATTERN =
            Pattern.compile("[0-9a-f]{64}");

    private final Path rootDirectory;

    public FileSystemCharacterPortraitAssetStore(Path rootDirectory) {
        this.rootDirectory = Objects.requireNonNull(rootDirectory, "rootDirectory")
                .toAbsolutePath()
                .normalize();
    }

    @Override
    public boolean exists(String assetId) {
        return Files.isRegularFile(resolveAssetPath(assetId));
    }

    @Override
    public void putIfAbsent(String assetId, byte[] pngBytes) {
        Objects.requireNonNull(pngBytes, "pngBytes");
        if (pngBytes.length == 0) {
            throw new IllegalArgumentException("Un portrait vide ne peut pas être stocké.");
        }

        Path target = resolveAssetPath(assetId);
        if (Files.isRegularFile(target)) {
            return;
        }

        try {
            Files.createDirectories(rootDirectory);

            Path temporary = rootDirectory.resolve(
                    assetId + "." + UUID.randomUUID() + ".tmp"
            );

            try {
                Files.write(
                        temporary,
                        pngBytes,
                        StandardOpenOption.CREATE_NEW,
                        StandardOpenOption.WRITE
                );

                if (Files.isRegularFile(target)) {
                    return;
                }

                try {
                    Files.move(
                            temporary,
                            target,
                            StandardCopyOption.ATOMIC_MOVE
                    );
                } catch (AtomicMoveNotSupportedException e) {
                    if (!Files.isRegularFile(target)) {
                        Files.move(
                                temporary,
                                target,
                                StandardCopyOption.REPLACE_EXISTING
                        );
                    }
                }
            } finally {
                Files.deleteIfExists(temporary);
            }
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Impossible de stocker le portrait " + assetId + ".",
                    e
            );
        }
    }

    @Override
    public byte[] read(String assetId) {
        Path path = resolveAssetPath(assetId);

        if (!Files.isRegularFile(path)) {
            throw new IllegalArgumentException(
                    "Le portrait " + assetId + " n'existe pas."
            );
        }

        try {
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Impossible de lire le portrait " + assetId + ".",
                    e
            );
        }
    }

    @Override
    public void delete(String assetId) {
        try {
            Files.deleteIfExists(resolveAssetPath(assetId));
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Impossible de supprimer le portrait " + assetId + ".",
                    e
            );
        }
    }

    @Override
    public Set<String> listAssetIds() {
        if (!Files.isDirectory(rootDirectory)) {
            return Set.of();
        }

        Set<String> result = new HashSet<>();

        try (var stream = Files.list(rootDirectory)) {
            stream.filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .filter(name -> name.endsWith(".png"))
                    .map(name -> name.substring(0, name.length() - 4))
                    .filter(name -> ASSET_ID_PATTERN.matcher(name).matches())
                    .forEach(result::add);
        } catch (IOException e) {
            throw new IllegalStateException(
                    "Impossible de parcourir le stockage des portraits.",
                    e
            );
        }

        return Set.copyOf(result);
    }

    private Path resolveAssetPath(String assetId) {
        String normalized = normalizeAssetId(assetId);
        Path path = rootDirectory.resolve(normalized + ".png").normalize();

        if (!path.getParent().equals(rootDirectory)) {
            throw new IllegalArgumentException("Identifiant de portrait invalide.");
        }

        return path;
    }

    private String normalizeAssetId(String assetId) {
        Objects.requireNonNull(assetId, "assetId");
        String normalized = assetId.trim().toLowerCase();

        if (!ASSET_ID_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                    "Identifiant de portrait invalide : " + assetId
            );
        }

        return normalized;
    }
}
