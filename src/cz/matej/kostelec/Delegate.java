package cz.matej.kostelec;

public interface Delegate {

  /**
   * Returns true if the data has just been flushed.
   */
  boolean found(FoundFile found);

}
