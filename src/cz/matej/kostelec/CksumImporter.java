package cz.matej.kostelec;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public final class CksumImporter {

  public static void main(final String[] args) {
    final DatabaseDelegate delegate = new DatabaseDelegate(
        Configuration.RESUME_ID);
    delegate.init();
    try (BufferedReader br = new BufferedReader(new FileReader(
        Configuration.INPUT_CKSUMS_FILE))) {

      String line;
      while ((line = br.readLine()) != null) {
        final int pos1 = line.indexOf(' ');
        final String hash = line.substring(0, pos1);
        final String rest = line.substring(pos1 + 1);
        final int pos2 = rest.indexOf(' ');
        final long size = Long.valueOf(rest.substring(0, pos2)).longValue();
        final String fullPath = rest.substring(pos2 + 1);
        final String fileName = fullPath
            .substring(fullPath.lastIndexOf('/') + 1);
        delegate.found(new FoundFile(fullPath, fileName, size, hash));
      }
    } catch (final IOException e) {
    }
    delegate.close();
  }

}
