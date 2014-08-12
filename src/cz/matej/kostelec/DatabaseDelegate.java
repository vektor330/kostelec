package cz.matej.kostelec;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

public class DatabaseDelegate implements Delegate {

  private final String searchId;
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
  public void fileFound(final String absolutePath, final String fileName,
      final long size, final Date creationTime, final String hash) {
    try {
      final PreparedStatement ps = con
          .prepareStatement("INSERT INTO files (search_id, fullpath, name, size, created, hash) "
              + "VALUES (?, ?, ?, ?, ?, ?);");
      ps.setString(1, searchId);
      ps.setString(2, absolutePath);
      ps.setString(3, fileName);
      ps.setLong(4, size);
      ps.setTimestamp(5, new Timestamp(creationTime.getTime()));
      ps.setString(6, hash);
      final int inserted = ps.executeUpdate();
      if (inserted != 1) {
        throw new RuntimeException("Expected to insert 1 row, inserted "
            + inserted);
      }
      ps.close();
    } catch (final SQLException e) {
      e.printStackTrace();
    }
  }

  public void close() {
    try {
      con.close();
    } catch (final SQLException e) {
      e.printStackTrace();
    }
  }
}
