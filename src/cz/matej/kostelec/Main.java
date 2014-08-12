package cz.matej.kostelec;

import java.nio.file.FileSystems;
import java.util.Date;
import java.util.UUID;

public final class Main {

  private static Delegate SYSOUT_DELEGATE = new Delegate() {

    @Override
    public void fileFound(final String absolutePath, final String fileName,
        final long size, final Date creationTime, final String hash) {
      System.out.println(absolutePath + ";;;;" + size + ";;;;" + creationTime
          + ";;;;" + hash);
    }
  };

  public static void main(final String[] args) {
    final DatabaseDelegate delegate = new DatabaseDelegate(UUID.randomUUID()
        .toString());
    delegate.init();
    new Searcher(FileSystems.getDefault().getPath("/Users/vektor/Pictures"),
        delegate).search();
    delegate.close();
  }

}