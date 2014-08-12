package cz.matej.kostelec;

import java.util.Date;

public class FoundFile {

  private final String absolutePath;
  private final String fileName;
  private final long size;
  private final Date creationTime;
  private final String hash;

  public FoundFile(final String absolutePath, final String fileName,
      final long size, final Date creationTime, final String hash) {
    this.absolutePath = absolutePath;
    this.fileName = fileName;
    this.size = size;
    this.creationTime = creationTime;
    this.hash = hash;
  }

  public String getAbsolutePath() {
    return absolutePath;
  }

  public String getFileName() {
    return fileName;
  }

  public long getSize() {
    return size;
  }

  public Date getCreationTime() {
    return creationTime;
  }

  public String getHash() {
    return hash;
  }

}