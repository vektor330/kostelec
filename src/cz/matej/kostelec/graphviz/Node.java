package cz.matej.kostelec.graphviz;

public final class Node {

  private final String id;

  private final String label;

  public Node(final String id, final String label) {
    this.id = id;
    this.label = label;
  }

  public String getId() {
    return id;
  }

  public String getLabel() {
    return label;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (id == null ? 0 : id.hashCode());
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
    if (!(obj instanceof Node)) {
      return false;
    }
    final Node other = (Node) obj;
    if (id == null) {
      if (other.id != null) {
        return false;
      }
    } else if (!id.equals(other.id)) {
      return false;
    }
    return true;
  }

}
