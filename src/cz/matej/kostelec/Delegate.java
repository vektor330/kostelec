package cz.matej.kostelec;

import java.util.Date;

public interface Delegate {

  void fileFound(final String absolutePath, final String fileName,
      final long size, final Date creationTime, final String hash);

}
