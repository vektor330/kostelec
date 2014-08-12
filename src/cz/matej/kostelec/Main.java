package cz.matej.kostelec;

import java.nio.file.FileSystems;
import java.util.UUID;

public final class Main {

  public static void main(final String[] args) {
    final DatabaseDelegate delegate = new DatabaseDelegate(UUID.randomUUID()
        .toString());
    delegate.init();
    new Searcher(FileSystems.getDefault().getPath("/Users/vektor/Devel"),
        delegate).search();
    delegate.close();
  }

}