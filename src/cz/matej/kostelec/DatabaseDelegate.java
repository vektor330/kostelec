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
  public boolean found(final FoundFile file) {
    cache.add(file);
    if (cache.size() < CACHE_SIZE) {
      return false;
    }
    flush();
    return true;
  }

  private void flush() {
    try (final PreparedStatement ps = con.prepareStatement("INSERT INTO files "
        + "(search_id, fullpath, name, size, created, modified, hash) "
        + "VALUES (?, ?, ?, ?, ?, ?, ?);")) {
      for (final FoundFile file : cache) {
        ps.setString(1, searchId);
        ps.setString(2, file.getAbsolutePath());
        ps.setString(3, file.getFileName());
        ps.setLong(4, file.getSize());
        ps.setTimestamp(5, new Timestamp(file.getCreationTime().getTime()));
        ps.setTimestamp(6, new Timestamp(file.getModificationTime().getTime()));
        ps.setString(7, file.getHash());
        ps.addBatch();
      }
      ps.executeBatch();
      System.out.println("Added " + cache.size() + " found files.");
      ps.close();
      cache.clear();
    } catch (final SQLException e) {
      e.printStackTrace();
    }
  }

  public boolean checkExists(final FoundFile file) {
    try (final PreparedStatement ps = con
        .prepareStatement("SELECT * FROM files "
            + "WHERE search_id = ? AND fullpath = ? AND size = ? AND created = ? AND modified = ?")) {
      ps.setString(1, searchId);
      ps.setString(2, file.getAbsolutePath());
      ps.setLong(3, file.getSize());
      ps.setTimestamp(4, new Timestamp(file.getCreationTime().getTime()));
      ps.setTimestamp(5, new Timestamp(file.getModificationTime().getTime()));
      return ps.executeQuery().isBeforeFirst();
    } catch (final SQLException e) {
      e.printStackTrace();
    }
    throw new RuntimeException();
  }

  public void truncateSearch() {
    try (final PreparedStatement ps = con
        .prepareStatement("DELETE FROM files WHERE search_id = ?;")) {
      ps.setString(1, searchId);
      ps.executeUpdate();
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
