package cz.matej.kostelec.graphviz;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import cz.matej.kostelec.Configuration;

public final class GraphvizExporter {

  public static void main(final String[] args) {
    final Set<Node> nodes = new HashSet<>();
    final Set<Edge> edges = new HashSet<>();

    try (final Connection con = DriverManager
        .getConnection(Configuration.JDBC_URL);) {

      final Set<String> allPaths = new LinkedHashSet<>();
      try (final PreparedStatement ps = con
          .prepareStatement("SELECT path FROM duplicates_paths WHERE search_id = ?");) {
        ps.setString(1, Configuration.RESUME_ID);
        try (final ResultSet rs = ps.executeQuery();) {
          while (rs.next()) {
            String path = rs.getString(1);
            while (path.length() > 1) {
              allPaths.add(path);
              path = path.replaceFirst("/[^/]+$", "");
            }
          }
        }
      }

      for (final String path : allPaths) {
        final String normalizedPath = normalize(path);
        try (final PreparedStatement ps = con
            .prepareStatement("SELECT COUNT(*), SUM(size) FROM files WHERE search_id = ? AND fullpath LIKE ?");) {
          ps.setString(1, Configuration.RESUME_ID);
          ps.setString(2, path + "%");
          try (final ResultSet rs = ps.executeQuery();) {
            while (rs.next()) {
              final int totalFiles = rs.getInt(1);
              final long totalSize = rs.getLong(2);
              nodes.add(new Node(normalizedPath, getFolderFromEnd(path, 1)
                  + " " + totalFiles + " "
                  + humanReadableByteCount(totalSize, false)));
            }
          }
        }
        final String parent = path.replaceFirst("/[^/]+$", "");
        if (parent.length() > 0) {
          edges.add(new Edge(normalizedPath, normalize(parent)));
        }
      }

      try (final PreparedStatement ps = con
          .prepareStatement("SELECT "
              + "regexp_replace(d1.fullpath, '/[^/]+$', '') path1, regexp_replace(d2.fullpath, '/[^/]+$', '') path2, COUNT(*) cnt "
              + "FROM duplicates d1 "
              + "JOIN duplicates d2 ON d1.hash = d2.hash AND d1.search_id = d2.search_id "
              + "AND d1.fullpath < d2.fullpath WHERE d1.search_id = ? GROUP BY path1, path2;");) {
        ps.setString(1, Configuration.RESUME_ID);
        try (final ResultSet rs = ps.executeQuery();) {
          while (rs.next()) {
            final String path1 = rs.getString(1);
            final String path2 = rs.getString(2);
            final int count = rs.getInt(3);
            final int weight = (int) Math.round(Math.log(count) / Math.log(10)) + 1;
            if (weight > 1) {
              edges.add(new Edge(normalize(path1), normalize(path2), weight,
                  String.valueOf(count)));
            }
          }
        }
      }
    } catch (final SQLException e) {
      e.printStackTrace();
    }

    final GraphvizRepresentation trimmed = trim(new GraphvizRepresentation(
        nodes, edges));

    try (final PrintWriter out = new PrintWriter(Configuration.OUTPUT_FILE);) {
      out.print(trimmed.render());
    } catch (final FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  private static GraphvizRepresentation trim(GraphvizRepresentation input) {
    while (true) {
      // find a node to remove
      final Node toRemove = findANodeToRemove(input);
      if (toRemove == null) {
        return input;
      }
      // remove the node and all the edges that touch it
      final Set<Node> keepingNodes = new HashSet<>(input.getNodes());
      keepingNodes.remove(toRemove);
      final Set<Edge> keepingEdges = new HashSet<>();
      for (final Edge e : input.getEdges()) {
        if (!e.touchesNode(toRemove)) {
          keepingEdges.add(e);
        }
      }
      input = new GraphvizRepresentation(keepingNodes, keepingEdges);
    }
  }

  private static Node findANodeToRemove(final GraphvizRepresentation input) {
    for (final Node n : input.getNodes()) {
      int edges = 0;
      for (final Edge e : input.getEdges()) {
        if (e.touchesNode(n)) {
          edges++;
        }
      }
      if (edges == 1) {
        return n;
      }
    }
    return null;
  }

  private static String getFolderFromEnd(final String path, final int i) {
    final String[] split = path.split("/");
    return split[split.length - i];
  }

  private static String normalize(final String s) {
    return s.replaceAll("[^A-Za-z0-9]", "_");
  }

  private static String humanReadableByteCount(final long bytes,
      final boolean si) {
    final int unit = si ? 1000 : 1024;
    if (bytes < unit) {
      return bytes + " B";
    }
    final int exp = (int) (Math.log(bytes) / Math.log(unit));
    final String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1)
        + (si ? "" : "i");
    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
  }

}
