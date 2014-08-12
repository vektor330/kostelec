package cz.matej.kostelec;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class DatabaseDelegate implements Delegate {

  private final static int CACHE_SIZE = 1024;

  private final String searchId;
  private final List<FoundFile> cache = new ArrayList<FoundFile>(CACHE_SIZE);
  private Connection con;

  public DatabaseDelegate(final String searchId) {
    this.searchId = searchId;
  }

  public void init() {
    try {
      con = DriverManager
          .getConnection("jdbc:postgresql:kostelec?user=postgres&password=postgres");
    } catch (final SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public void found(final FoundFile file) {
    cache.add(file);
    if (cache.size() < CACHE_SIZE) {
      return;
    }
    flush();
  }

  private void flush() {
    try (final PreparedStatement ps = con.prepareStatement("INSERT INTO files "
        + "(search_id, fullpath, name, size, created, hash) "
        + "VALUES (?, ?, ?, ?, ?, ?);")) {
      for (final FoundFile file_ : cache) {
        ps.setString(1, searchId);
        ps.setString(2, file_.getAbsolutePath());
        ps.setString(3, file_.getFileName());
        ps.setLong(4, file_.getSize());
        ps.setTimestamp(5, new Timestamp(file_.getCreationTime().getTime()));
        ps.setString(6, file_.getHash());
        ps.addBatch();
      }
      ps.executeBatch();
      System.out.println("Added " + CACHE_SIZE + " found files.");
      ps.close();
      cache.clear();
    } catch (final SQLException e) {
      e.printStackTrace();
    }
  }

  public void close() {
    try {
      if (cache.size() > 0) {
        flush();
      }
      con.close();
    } catch (final SQLException e) {
      e.printStackTrace();
    }
  }
}
