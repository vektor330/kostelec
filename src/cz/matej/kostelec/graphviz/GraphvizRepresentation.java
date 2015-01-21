package cz.matej.kostelec.graphviz;

import java.util.Set;

public final class GraphvizRepresentation {

  private final Set<Node> nodes;

  private final Set<Edge> edges;

  public GraphvizRepresentation(final Set<Node> nodes, final Set<Edge> edges) {
    this.nodes = nodes;
    this.edges = edges;
  }

  public Set<Node> getNodes() {
    return nodes;
  }

  public Set<Edge> getEdges() {
    return edges;
  }

  public String render() {
    final StringBuilder ret = new StringBuilder();
    ret.append("graph {").append("\n\tnode [shape = folder,fontsize=9];");
    for (final Node node : nodes) {
      ret.append("\n\t").append(node.getId()).append(" [label = \"")
          .append(node.getLabel()).append("\"];");
    }
    for (final Edge edge : edges) {
      if (edge.isDashed()) {
        ret.append("\n\t").append(edge.getNodeFrom()).append(" -- ")
            .append(edge.getNodeTo()).append(" [style = dashed];");
      } else {
        ret.append("\n\t").append(edge.getNodeFrom()).append(" -- ")
            .append(edge.getNodeTo()).append(" [weight = ")
            .append(edge.getWidth()).append(", penwidth = ")
            .append(edge.getWidth()).append(", label = \"")
            .append(edge.getLabel()).append("\"];");
      }
    }
    ret.append("\n}\n");
    return ret.toString();
  }

}
