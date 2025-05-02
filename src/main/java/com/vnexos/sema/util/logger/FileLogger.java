package com.vnexos.sema.util.logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.vnexos.sema.Constants;
import com.vnexos.sema.util.format.Formatter;

/**
 * Handles log to file.
 * 
 * @author Trần Việt Đăng Quang
 */
public class FileLogger {
  private BufferedWriter writer;
  private File file;

  /**
   * Constructs a file logger.
   */
  public FileLogger() {
    String folderPath = Constants.getString("log.folder");
    String filePath = Constants.getString("log.file");
    // Create log folder if not exist
    File logFolder = new File(Constants.context.joinPath(folderPath));
    if (!logFolder.exists())
      logFolder.mkdirs();

    // Create a logging file if not exist
    file = new File(Constants.context.joinPath(folderPath, filePath));
    if (!file.exists())
      try {
        if (file.createNewFile()) {
          // do nothing
        }
      } catch (IOException e) {
        e.printStackTrace();
      }

    // Set file logging writer
    try {
      writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    }
  }

  /**
   * Logs message to file
   * 
   * @param message the message to log
   */
  public void log(String message) {
    try {
      writer.write(Formatter.clearFormat(message));
      writer.newLine();
      writer.flush();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * Closes the file and rename the log file.
   */
  public void close() {
    LocalDateTime time = LocalDateTime.now();
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-hh-mm-ss");
    String timeString = time.format(formatter);
    String fileName = file.getAbsolutePath();
    String fileExt = fileName.substring(fileName.lastIndexOf('.'));
    try {
      writer.close();
      if (!file.renameTo(new File(fileName.replace(fileExt, "-") + timeString + fileExt))) {
        // do nothing
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
}
