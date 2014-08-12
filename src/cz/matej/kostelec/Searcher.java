package cz.matej.kostelec;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public final class Searcher {

  private final Path root;
  private final List<Path> excluded;
  private final Delegate delegate;

  public Searcher(final Path root, final Delegate delegate) {
    this(root, Collections.<Path> emptyList(), delegate);
  }

  public Searcher(final Path root, final List<Path> excluded,
      final Delegate delegate) {
    this.root = root;
    this.excluded = excluded;
    this.delegate = delegate;
  }

  public void search() {
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(root)) {
      for (final Path file : stream) {
        if (Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
          examine(file);
        }
        if (Files.isDirectory(file, LinkOption.NOFOLLOW_LINKS)) {
          new Searcher(file, excluded, delegate).search();
        }
      }
    } catch (IOException | DirectoryIteratorException x) {
      System.err.println(x);
    }
  }

  private void examine(final Path path) {
    try {
      final BasicFileAttributes attributes = Files.readAttributes(path,
          BasicFileAttributes.class);
      final FileTime creationTime = attributes.creationTime();

      delegate.found(new FoundFile(path.toAbsolutePath().toString(), path
          .getFileName().toString(), attributes.size(), new Date(creationTime
          .toMillis()), getHash(path)));
    } catch (final IOException e) {
      e.printStackTrace();
    }
  }

  private static String getHash(final Path path) throws IOException {

    MessageDigest md = null;
    ByteBuffer bbf = null;
    StringBuilder hexString = null;

    try (final FileChannel fc = FileChannel.open(path, StandardOpenOption.READ)) {
      md = MessageDigest.getInstance("MD5");
      bbf = ByteBuffer.allocateDirect(8192);

      int b;

      b = fc.read(bbf);

      while (b != -1 && b != 0) {
        bbf.flip();

        final byte[] bytes = new byte[b];
        bbf.get(bytes);

        md.update(bytes, 0, b);

        bbf.clear();
        b = fc.read(bbf);
      }

      final byte[] mdbytes = md.digest();

      hexString = new StringBuilder();

      for (int i = 0; i < mdbytes.length; i++) {
        hexString.append(Integer.toHexString(0xFF & mdbytes[i]));
      }

      return hexString.toString();
    } catch (final NoSuchAlgorithmException e) {
      return null;
    }
  }
}
