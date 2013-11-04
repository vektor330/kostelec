package cz.matej.kostelec;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

public final class Main {

  public static void main(final String[] args) {
    final ArrayList<File> roots = new ArrayList<File>();
    roots.addAll(Arrays.asList(File.listRoots()));

    for (final File file : roots) {
      new Searcher(file.toString().replace('\\', '/')).search();
    }
  }

}