package cz.matej.kostelec;

import java.nio.file.FileSystems;
import java.nio.file.Path;

public final class Main {

  public static void main(final String[] args) {
    for (final Path path : FileSystems.getDefault().getRootDirectories()) {
      new Searcher(path).search();
    }
  }

}