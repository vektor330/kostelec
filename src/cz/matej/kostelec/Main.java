package cz.matej.kostelec;

import java.io.IOException;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.UUID;

public final class Main {

  private static final String RESUME_ID = "68f4cd51-39c0-4450-a2ab-b39e1c24e96b";
  private static final long MIN_SIZE = 1000000l;
  private static final boolean TRUNCATE = false;

  public static void main(final String[] args) {
    final String searchId;
    if (RESUME_ID == null) {
      searchId = UUID.randomUUID().toString();
      System.out.println("Starting a new search ID " + searchId);
    } else {
      searchId = RESUME_ID;
      System.out.println("Resuming search ID " + searchId);
    }
    final DatabaseDelegate delegate = new DatabaseDelegate(searchId);
    delegate.init();
    if (TRUNCATE) {
      delegate.truncateSearch();
      return;
    }

    Runtime.getRuntime().addShutdownHook(new Thread() {

      @Override
      public void run() {
        delegate.close();
      }
    });

    final FileSystem defaultFS = FileSystems.getDefault();
    new Searcher(defaultFS.getPath("/Users/vektor/Devel"), new Filter<Path>() {

      @Override
      public boolean accept(final Path entry) throws IOException {
        if (Files.isDirectory(entry, LinkOption.NOFOLLOW_LINKS)) {
          return true;
        }
        final BasicFileAttributes attributes = Files.readAttributes(entry,
            BasicFileAttributes.class);
        if (attributes.size() < MIN_SIZE) {
          return false;
        }
        if (RESUME_ID == null) {
          return true;
        }
        return !delegate.checkExists(Main.pathToFound(entry, null));
      }

    }, delegate).search();
    delegate.close();
  }

  public static FoundFile pathToFound(final Path path, final String hash) {
    BasicFileAttributes attributes;
    try {
      attributes = Files.readAttributes(path, BasicFileAttributes.class);
      final FoundFile found = new FoundFile(path.toAbsolutePath().toString(),
          path.getFileName().toString(), attributes.size(), new Date(attributes
              .creationTime().toMillis()), new Date(attributes
              .lastModifiedTime().toMillis()), hash);
      return found;
    } catch (final IOException e) {
      e.printStackTrace();
    }
    throw new RuntimeException();
  }
}