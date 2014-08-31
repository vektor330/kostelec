package cz.matej.kostelec;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.DirectoryIteratorException;
import java.nio.file.DirectoryStream;
import java.nio.file.DirectoryStream.Filter;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public final class Searcher {

  private final Path root;
  private final Filter<Path> filter;
  private final Delegate delegate;

  public Searcher(final Path root, final Filter<Path> filter,
      final Delegate delegate) {
    this.root = root;
    this.filter = filter;
    this.delegate = delegate;
  }

  public void search() {
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(root, filter)) {
      for (final Path file : stream) {
        if (Files.isRegularFile(file, LinkOption.NOFOLLOW_LINKS)) {
          delegate.found(Main.pathToFound(file, getHash(file)));
        }
        if (Files.isDirectory(file, LinkOption.NOFOLLOW_LINKS)) {
          new Searcher(file, filter, delegate).search();
        }
      }
    } catch (IOException | DirectoryIteratorException x) {
      System.err.println(x);
    }
  }

  private static String getHash(final Path path) {
    System.out.println("Hashing " + path);

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
    } catch (final IOException e) {
      e.printStackTrace();
      return null;
    }
  }
}
