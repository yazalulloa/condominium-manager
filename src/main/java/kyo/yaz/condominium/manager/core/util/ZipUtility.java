package kyo.yaz.condominium.manager.core.util;


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorOutputStream;
import org.apache.commons.compress.utils.IOUtils;

/**
 * This utility compresses a list of files to standard ZIP format file. It is able to compress all sub files and sub
 * directories, recursively.
 *
 * @author www.codejava.net
 */
public class ZipUtility {

  /**
   * A constants for buffer size used to read/write data
   */
  private static final int BUFFER_SIZE = 4096;

  private ZipUtility() {
    super();
  }

  public static void zipDirectory(File file, String destZipFile) throws IOException {
    if (file.isDirectory()) {
      final var listFiles = file.listFiles();
      if (listFiles != null && listFiles.length > 0) {
        final var list = Arrays.stream(listFiles).toList();
        zip(list, destZipFile);
        return;
      }
    }

    throw new IllegalArgumentException();

  }

  /**
   * Compresses a list of files to a destination zip file
   *
   * @param listFiles   A collection of files and directories
   * @param destZipFile The path of the destination zip file
   * @throws FileNotFoundException
   * @throws IOException
   */
  public static void zip(List<File> listFiles, String destZipFile) throws FileNotFoundException,
      IOException {
    ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(destZipFile));
    for (File file : listFiles) {
      if (file.isDirectory()) {
        zipDirectory(file, file.getName(), zos);
      } else {
        zipFile(file, zos);
      }
    }
    zos.flush();
    zos.close();
  }

  /**
   * Compresses files represented in an array of paths
   *
   * @param files       a String array containing file paths
   * @param destZipFile The path of the destination zip file
   * @throws FileNotFoundException
   * @throws IOException
   */
  public static void zip(String destZipFile, String... files) throws FileNotFoundException, IOException {
    List<File> listFiles = new ArrayList<>();
    for (String file : files) {
      listFiles.add(new File(file));
    }
    zip(listFiles, destZipFile);
  }

  /**
   * Adds a directory to the current zip output stream
   *
   * @param folder       the directory to be  added
   * @param parentFolder the path of parent directory
   * @param zos          the current zip output stream
   * @throws FileNotFoundException
   * @throws IOException
   */
  private static void zipDirectory(File folder, String parentFolder, ZipOutputStream zos)
      throws FileNotFoundException, IOException {
    for (File file : folder.listFiles()) {
      if (file.isDirectory()) {
        zipDirectory(file, parentFolder + "/" + file.getName(), zos);
        continue;
      }
      zos.putNextEntry(new ZipEntry(parentFolder + "/" + file.getName()));
      BufferedInputStream bis = new BufferedInputStream(
          new FileInputStream(file));
      long bytesRead = 0;
      byte[] bytesIn = new byte[BUFFER_SIZE];
      int read = 0;
      while ((read = bis.read(bytesIn)) != -1) {
        zos.write(bytesIn, 0, read);
        bytesRead += read;
      }
      zos.closeEntry();
    }
  }

  /**
   * Adds a file to the current zip output stream
   *
   * @param file the file to be added
   * @param zos  the current zip output stream
   * @throws FileNotFoundException
   * @throws IOException
   */
  private static void zipFile(File file, ZipOutputStream zos)
      throws FileNotFoundException, IOException {
    zos.putNextEntry(new ZipEntry(file.getName()));
    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(
        file));
    long bytesRead = 0;
    byte[] bytesIn = new byte[BUFFER_SIZE];
    int read = 0;
    while ((read = bis.read(bytesIn)) != -1) {
      zos.write(bytesIn, 0, read);
      bytesRead += read;
    }
    zos.closeEntry();
  }

  public static void gzip(String tarGzPath, String... directoryPath) throws IOException {

    try (FileOutputStream fOut = new FileOutputStream(new File(tarGzPath))) {
      try (BufferedOutputStream bOut = new BufferedOutputStream(fOut)) {
        try (GzipCompressorOutputStream gzOut = new GzipCompressorOutputStream(bOut)) {
          try (TarArchiveOutputStream tOut = new TarArchiveOutputStream(gzOut)) {

            for (String path : directoryPath) {
              gzip(tOut, path, "");
            }

            tOut.finish();
          }
        }
      }
    }

  }

  private static void gzip(TarArchiveOutputStream tOut, String path, String base) throws IOException {
    final var f = new File(path);
    final var entryName = base + f.getName();
    final var tarEntry = new TarArchiveEntry(f, entryName);

    tOut.putArchiveEntry(tarEntry);

    if (f.isFile()) {
      IOUtils.copy(new FileInputStream(f), tOut);

      tOut.closeArchiveEntry();
    } else {
      tOut.closeArchiveEntry();

      final var children = f.listFiles();

      if (children != null) {
        for (File child : children) {
          gzip(tOut, child.getAbsolutePath(), entryName + "/");
        }
      }
    }
  }
}

