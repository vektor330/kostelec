package cz.matej.kostelec;

import java.io.File;

public final class Searcher {

  private final String root;

  public Searcher(final String root) {
    this.root = root;
  }

  public void search() {
    System.out.println(root);
    final File folder = new File(root);
    final File[] listOfFiles = folder.listFiles();
    if (listOfFiles == null) {
      return; // Added condition check
    }
    for (final File file : listOfFiles) {
      final String path = file.getPath().replace('\\', '/');
      System.out.println(path);
      if (file.isDirectory()) {
        new Searcher(path + "/").search();
      }
    }
  }
}
