package org.onedatashare.transfer.model.core;

import lombok.Data;

import java.util.Collection;
import java.util.List;

@Data
public class Stat {
  /**
   * The id of the resource (Used in Google Drive SDK for now)
   */
  private String id;

  /**
   * The name of the resource.
   */
  private String name;

  /**
   * The size of the resource in bytes.
   */
  private long size;

  /**
   * The modification time of the resource in Unix time.
   */
  private long time;

  /**
   * Whether or not the resource is a directory.
   */
  private FileType type;

  /**
   * If the resource is a link, the link target.
   */
  private String link;

  /**
   * An implementation-specific permissions string.
   */
  private String permissions;

  /**
   * An array of subresources, if known.
   */
  private Stat[] files;

  /**
   * List of files in the Stat
   * Holds relative paths of all the children in case of a folder
   */
  private List<Stat> filesList;

  private transient long total_size = -1;
  private transient long total_num = 0;

  /**
   * Create a new {@code Stat} with no name.
   */
  public Stat() {
    this(null);
  }

  /**
   * Create a new {@code Stat} with the given name.
   */
  public Stat(String name) {
    this.name = name;
  }

  /**
   * Get the total size of the tree.
   */
  public long size() {
    if (total_size >= 0) {
      return total_size;
    }
    if (type == FileType.FOLDER) {
      long s = size;
      if (files != null) for (Stat f : files)
        s += f.type == FileType.FOLDER ? 0 : f.size();
      return total_size = s;
    }
    return total_size = size;
  }

  /**
   * Copy the data from the passed file tree into this one.
   */
  public Stat copy(Stat ft) {
    name = ft.name;
    size = ft.size;
    time = ft.time;
    type = ft.type;
    link = ft.link;
    permissions = ft.permissions;
    return this;
  }

  /**
   * Get the total number of items under this tree.
   */
  public long count() {
    if (total_num > 0) {
      return total_num;
    }
    if (type == FileType.FOLDER) {
      long n = 1;
      if (files != null) for (Stat f : files)
        n += f.count();
      return total_num = n;
    }
    return total_num = 1;
  }

  /**
   * Return a path up to the parent.
   */
  public String path() {
    return name;
  }

  /**
   * Set the files underneath this tree and reset cached values.
   */
  public Stat setFiles(Collection<Stat> fs) {
    return setFiles(fs.toArray(new Stat[fs.size()]));
  }

  public void setFilesList(List<Stat> fs){
    this.filesList = fs;
  }

  @Override
  public String toString(){
    return name;
  }


  public List<Stat> getFilesList(){
    return this.filesList;
  }

  /**
   * Set the files underneath this tree and reset cached values.
   */
  public Stat setFileNames(Collection<String> fs) {
    return setFiles(fs.toArray(new String[fs.size()]));
  }

  /**
   * Set the files underneath this tree and reset cached values.
   */
  public Stat setFiles(Stat[] fs) {
    files = fs;
    total_size = -1;
    total_num = 0;

    return this;
  }

  /**
   * Set the files underneath this tree given only their names.
   */
  public Stat setFiles(String[] names) {
    if (names == null) {
      files = null;
    } else {
      Stat[] stats = new Stat[names.length];
      for (int i = 0; i < stats.length; i++)
        stats[i] = new Stat(names[i]);
      files = stats;
      total_size = -1;
      total_num = 0;
    }

    return this;
  }
}
