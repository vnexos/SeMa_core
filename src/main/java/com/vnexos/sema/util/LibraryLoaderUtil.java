package com.vnexos.sema.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import com.vnexos.sema.Constants;
import com.vnexos.sema.context.ServerContext;
import com.vnexos.sema.loader.Loader;

/**
 * Get Loader with libraries
 * 
 * @author Trần Việt Đăng Quang
 */
public class LibraryLoaderUtil {
  private static ServerContext context = Constants.context;

  private LibraryLoaderUtil() {
  }

  /**
   * Get all files in library directory
   * 
   * @param libFoler directory to get files
   * @return the list of all files in directory tree
   */
  private static List<File> getFileTree(File libFoler) {
    List<File> files = new ArrayList<>();

    for (File f : libFoler.listFiles()) {
      if (f.isDirectory()) {
        files.addAll(getFileTree(f));
      } else {
        files.add(f);
      }
    }

    return files;
  }

  /**
   * Get URLs of all modules and libraries
   * 
   * @param subFiles list of files in module folder
   * @return the list of JAR file URL
   */
  private static URL[] getLibUrls(File[] subFiles) {
    List<URL> urls = new ArrayList<>();
    File file = new File(context.joinPath("libs"));

    if (!file.exists())
      file.mkdirs();

    List<File> libFiles = getFileTree(file);
    File[] filesToCatch = Stream.concat(libFiles.stream(), Arrays.stream(subFiles)).toArray(File[]::new);

    for (File f : filesToCatch) {
      if (f.getName().toLowerCase().endsWith(".jar")) {
        try {
          urls.add(f.toURI().toURL());
        } catch (MalformedURLException e) {
          context.log(e);
        }
      }
    }

    return urls.toArray(new URL[0]);
  }

  /**
   * Get common URLClassLoader from modules.
   * 
   * @param subFiles module files
   * @return the class loader
   */
  public static URLClassLoader getCommonLoader(File[] subFiles) {
    return new URLClassLoader(
        getLibUrls(subFiles),
        Loader.class.getClassLoader());
  }
}
