package cz.matej.kostelec;

import java.util.Date;

public final class FoundFile {

  private final String absolutePath;
  private final String fileName;
  private final long size;
  private final Date creationTime;
  private final Date modificationTime;
  private final String hash;

  public FoundFile(final String absolutePath, final String fileName,
      final long size, final String hash) {
    this(absolutePath, fileName, size, null, null, hash);
  }

  public FoundFile(final String absolutePath, final String fileName,
      final long size, final Date creationTime, final Date modificationTime,
      final String hash) {
    this.absolutePath = absolutePath;
    this.fileName = fileName;
    this.size = size;
    this.creationTime = creationTime;
    this.modificationTime = modificationTime;
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

  public Date getModificationTime() {
    return modificationTime;
  }

  public String getHash() {
    return hash;
  }

}