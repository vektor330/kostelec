package cz.matej.kostelec;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public final class Main {

  public static void main(final String[] args) {
    final DatabaseDelegate delegate = new DatabaseDelegate(UUID.randomUUID()
        .toString());
    delegate.init();
    final FileSystem defaultFS = FileSystems.getDefault();
    final List<Path> exclusions = Arrays.asList(defaultFS
        .getPath("/Users/vektor/Devel/workspace-kepler"));
    new Searcher(defaultFS.getPath("/Users/vektor/Devel"), exclusions, delegate)
        .search();
    delegate.close();
  }
}