package cz.matej.kostelec.graphviz;

public final class Edge {

  private final String nodeFrom;

  private final String nodeTo;

  private final boolean dashed;

  private final int width;

  private final String label;

  public Edge(final String path, final String parent) {
    this.nodeFrom = path;
    this.nodeTo = parent;
    this.dashed = true;
    this.width = 1;
    this.label = null;
  }

  public Edge(final String nodeFrom, final String nodeTo, final int width,
      final String label) {
    this.nodeFrom = nodeFrom;
    this.nodeTo = nodeTo;
    this.dashed = false;
    this.width = width;
    this.label = label;
  }

  public String getNodeFrom() {
    return nodeFrom;
  }

  public String getNodeTo() {
    return nodeTo;
  }

  public boolean isDashed() {
    return dashed;
  }

  public int getWidth() {
    return width;
  }

  public String getLabel() {
    return label;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (dashed ? 1231 : 1237);
    result = prime * result + (nodeFrom == null ? 0 : nodeFrom.hashCode());
    result = prime * result + (nodeTo == null ? 0 : nodeTo.hashCode());
    result = prime * result + width;
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (!(obj instanceof Edge)) {
      return false;
    }
    final Edge other = (Edge) obj;
    if (dashed != other.dashed) {
      return false;
    }
    if (nodeFrom == null) {
      if (other.nodeFrom != null) {
        return false;
      }
    } else if (!nodeFrom.equals(other.nodeFrom)) {
      return false;
    }
    if (nodeTo == null) {
      if (other.nodeTo != null) {
        return false;
      }
    } else if (!nodeTo.equals(other.nodeTo)) {
      return false;
    }
    if (width != other.width) {
      return false;
    }
    return true;
  }

  public boolean touchesNode(final Node n) {
    return n.getId().equals(nodeFrom) || n.getId().equals(nodeTo);
  }

}
