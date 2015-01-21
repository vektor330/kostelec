package cz.matej.kostelec;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public final class DatabaseDelegate implements Delegate {

  private final String searchId;
  private final List<FoundFile> cache = new ArrayList<FoundFile>(
      Configuration.CACHE_SIZE);
  private Connection con;
  private long startTime;
  private Boolean isResuming = null;
  private long hashedThisRun = 0;
  private long hashedBytesThisRun = 0;

  public DatabaseDelegate(final String searchId) {
    this.searchId = searchId;
  }

  public void init() {
    startTime = System.nanoTime();
    try {
      con = DriverManager.getConnection(Configuration.JDBC_URL);
    } catch (final SQLException e) {
      e.printStackTrace();
    }
  }

  @Override
  public boolean found(final FoundFile file) {
    cache.add(file);
    if (cache.size() < Configuration.CACHE_SIZE) {
      return false;
    }
    flush();
    final long milliSecondsTaken = milliSecondsTaken();
    System.out.println(countSearch() + " (" + hashedThisRun + " this run) / "
        + Configuration.EXPECTED_FILE_COUNT + " done in "
        + formatTime(milliSecondsTaken / 1000) + " (~" + hashedBytesThisRun
        / (1024 * milliSecondsTaken / 1000) + " kB/s)");
    return true;
  }

  private long milliSecondsTaken() {
    return (System.nanoTime() - startTime) / 1000000l;
  }

  private static String formatTime(final long seconds) {
    if (seconds < 120) {
      return seconds + " s";
    }
    final long minutes = seconds / 60;
    final long remainingSeconds = seconds % 60;
    if (minutes < 120) {
      return minutes + " m " + remainingSeconds + " s";
    }
    final long hours = minutes / 60;
    final long remainingMinutes = minutes % 60;
    return hours + " h " + remainingMinutes + " m";
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
        if (file.getCreationTime() != null) {
          ps.setTimestamp(5, new Timestamp(file.getCreationTime().getTime()));
        } else {
          ps.setNull(5, Types.TIMESTAMP);
        }
        if (file.getModificationTime() != null) {
          ps.setTimestamp(6,
              new Timestamp(file.getModificationTime().getTime()));
        } else {
          ps.setNull(6, Types.TIMESTAMP);
        }
        ps.setString(7, file.getHash());
        ps.addBatch();
      }
      ps.executeBatch();
      System.out.println("Added " + cache.size() + " found files.");
      ps.close();
      cache.clear();
    } catch (final SQLException e) {
      e.printStackTrace();
      e.getNextException().printStackTrace();
    }
  }

  public boolean checkExists(final FoundFile file) {
    try (final PreparedStatement ps = con
        .prepareStatement("SELECT * FROM files "
            + "WHERE search_id = ? AND fullpath = ?")) {
      ps.setString(1, searchId);
      ps.setString(2, file.getAbsolutePath());
      final boolean ret = ps.executeQuery().isBeforeFirst();
      if (!ret) {
        hashedThisRun++;
        hashedBytesThisRun += file.getSize();
      }
      if (isResuming == null) {
        if (!ret) {
          // nothing, we are not resuming
        } else {
          System.out.print("Will resume the previous state...");
          isResuming = Boolean.TRUE;
        }
      } else if (isResuming.booleanValue() && !ret) {
        System.out.println("resumed in "
            + formatTime(milliSecondsTaken() / 1000));
        isResuming = Boolean.FALSE;
      }
      return ret;
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

  public long countSearch() {
    try (final PreparedStatement ps = con
        .prepareStatement("SELECT COUNT(*) cnt FROM files WHERE search_id = ?;")) {
      ps.setString(1, searchId);
      final ResultSet rs = ps.executeQuery();
      while (rs.next()) {
        return rs.getInt("cnt");
      }
    } catch (final SQLException e) {
      e.printStackTrace();
    }
    return 0;
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
