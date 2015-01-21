package cz.matej.kostelec;

import java.io.IOException;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.UUID;

// TODO list of inclusion folders, list of exclusion folders
// TODO check root exists on start (unmounted drive)
// TODO "interested only in duplicates in this folder"
public final class Main {

  public static void main(final String[] args) {
    final String searchId;
    if (Configuration.RESUME_ID == null) {
      searchId = UUID.randomUUID().toString();
      System.out.println("Starting a new search ID " + searchId);
    } else {
      searchId = Configuration.RESUME_ID;
      System.out.println("Resuming search ID " + searchId);
    }
    final DatabaseDelegate delegate = new DatabaseDelegate(searchId);
    delegate.init();
    if (Configuration.TRUNCATE) {
      delegate.truncateSearch();
      return;
    }

    Runtime.getRuntime().addShutdownHook(new Thread() {

      @Override
      public void run() {
        delegate.close();
      }
    });

    new Searcher(FileSystems.getDefault().getPath(Configuration.SEARCH_ROOT),
        new Filter<Path>() {

          @Override
          public boolean accept(final Path entry) throws IOException {
            if (Files.isDirectory(entry, LinkOption.NOFOLLOW_LINKS)) {
              return true;
            }
            final BasicFileAttributes attributes = Files.readAttributes(entry,
                BasicFileAttributes.class);
            if (attributes.size() < Configuration.MIN_SIZE) {
              return false;
            }
            if (Configuration.RESUME_ID == null) {
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