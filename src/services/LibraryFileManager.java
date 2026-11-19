package services;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.ArrayList;

import models.Book;
import models.FinePayment;
import models.LibraryData;
import models.LibraryMember;
import models.Loan;

public class LibraryFileManager {

    private final Path dataDirectory = Paths.get(
            System.getProperty("libracore.dataDir", "data"));

    public LibraryFileManager() {
        try {
            Files.createDirectories(dataDirectory);
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "Failed to create data directory: " + dataDirectory, exception);
        }
    }

    public LibraryData loadData() throws IOException {
        Path snapshot = dataDirectory.resolve("library.dat");
        if (Files.exists(snapshot)) {
            return loadSnapshot(snapshot);
        }

        String[] legacyFiles = { "books.dat", "members.dat", "loans.dat" };
        int legacyFileCount = 0;
        for (String fileName : legacyFiles) {
            if (Files.exists(dataDirectory.resolve(fileName))) {
                legacyFileCount++;
            }
        }
        if (legacyFileCount > 0 && legacyFileCount < legacyFiles.length) {
            throw new IOException(
                    "The legacy library data set is incomplete in "
                            + dataDirectory
                            + ". Preserve the remaining files and restore "
                            + "the missing files before restarting.");
        }

        return new LibraryData(
                loadLegacyList("books.dat", Book.class),
                loadLegacyList("members.dat", LibraryMember.class),
                loadLegacyList("loans.dat", Loan.class),
                new ArrayList<FinePayment>());
    }

    public void saveData(LibraryData libraryData) throws IOException {
        if (libraryData == null) {
            throw new IllegalArgumentException("Library data cannot be null.");
        }

        Path snapshot = dataDirectory.resolve("library.dat");
        Path backup = dataDirectory.resolve("library.dat.bak");
        Path temporary = Files.createTempFile(
                dataDirectory, "library-", ".tmp");

        try {
            try (FileOutputStream fileOutput =
                    new FileOutputStream(temporary.toFile());
                    ObjectOutputStream output =
                            new ObjectOutputStream(fileOutput)) {
                output.writeObject(libraryData);
                output.flush();
                fileOutput.getChannel().force(true);
            }

            if (Files.exists(snapshot)) {
                // Preserve the last known-good snapshot before replacing it.
                Files.copy(
                        snapshot,
                        backup,
                        StandardCopyOption.REPLACE_EXISTING);
            }

            try {
                Files.move(
                        temporary,
                        snapshot,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(
                        temporary,
                        snapshot,
                        StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    public void restoreBackup() throws IOException {
        Path snapshot = dataDirectory.resolve("library.dat");
        Path backup = dataDirectory.resolve("library.dat.bak");
        if (!Files.exists(backup)) {
            throw new IOException("No library.dat.bak recovery file exists.");
        }

        loadSnapshot(backup);
        Path temporary = Files.createTempFile(
                dataDirectory, "library-recovery-", ".tmp");
        try {
            Files.copy(backup, temporary, StandardCopyOption.REPLACE_EXISTING);

            if (Files.exists(snapshot)) {
                // Keep the invalid snapshot for diagnosis instead of overwriting it.
                String timestamp = LocalDateTime.now().format(
                        DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss-SSS"));
                Path preserved = dataDirectory.resolve(
                        "library.dat.corrupt-" + timestamp);
                int suffix = 1;
                while (Files.exists(preserved)) {
                    preserved = dataDirectory.resolve(
                            "library.dat.corrupt-" + timestamp + "-" + suffix++);
                }
                moveReplacing(snapshot, preserved);
            }

            moveReplacing(temporary, snapshot);
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private void moveReplacing(Path source, Path target) throws IOException {
        try {
            Files.move(
                    source,
                    target,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException exception) {
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private LibraryData loadSnapshot(Path file) throws IOException {
        try (FileInputStream fileInput = new FileInputStream(file.toFile())) {
            try (ObjectInputStream input = new ObjectInputStream(fileInput)) {
                Object value = input.readObject();
                if (!(value instanceof LibraryData)) {
                    throw new IOException(
                            "The saved library snapshot has an unexpected format.");
                }

                LibraryData snapshotData = (LibraryData) value;
                if (snapshotData.getVersion() != 1) {
                    throw new IOException(
                            "Unsupported library snapshot version: "
                                    + snapshotData.getVersion());
                }

                return new LibraryData(
                        validateItems(snapshotData.getBooks(), Book.class, file),
                        validateItems(
                                snapshotData.getMembers(), LibraryMember.class, file),
                        validateItems(snapshotData.getLoans(), Loan.class, file),
                        validateItems(
                                snapshotData.getPayments(), FinePayment.class, file));
            }
        } catch (ClassNotFoundException | RuntimeException exception) {
            throw corruptedData(file, exception);
        } catch (IOException exception) {
            throw corruptedData(file, exception);
        }
    }

    private <T> ArrayList<T> loadLegacyList(
            String fileName,
            Class<T> itemType) throws IOException {
        Path file = dataDirectory.resolve(fileName);
        if (!Files.exists(file)) {
            return new ArrayList<>();
        }

        try (FileInputStream fileInput = new FileInputStream(file.toFile())) {
            try (ObjectInputStream input = new ObjectInputStream(fileInput)) {
                Object value = input.readObject();
                if (!(value instanceof ArrayList<?>)) {
                    throw new IOException(
                            "The legacy data file has an unexpected format.");
                }

                return validateItems((ArrayList<?>) value, itemType, file);
            }
        } catch (ClassNotFoundException | RuntimeException exception) {
            throw corruptedData(file, exception);
        } catch (IOException exception) {
            throw corruptedData(file, exception);
        }
    }

    private <T> ArrayList<T> validateItems(
            ArrayList<?> items,
            Class<T> itemType,
            Path file) throws IOException {
        ArrayList<T> result = new ArrayList<>();
        for (Object item : items) {
            if (!itemType.isInstance(item)) {
                throw new IOException(
                        "Unexpected record type in " + file.getFileName());
            }
            result.add(itemType.cast(item));
        }
        return result;
    }

    private IOException corruptedData(Path file, Exception cause) {
        return new IOException(
                "Unable to read library data from " + file
                        + ". The file was left unchanged. Preserve it and "
                        + "the library.dat.bak backup, then restore a known "
                        + "good copy or contact the project maintainer.",
                cause);
    }
}
