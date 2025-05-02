package com.vnexos.sema.loader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

/**
 * Represents a file part of request for multipart content types.
 * 
 * @author Trần Việt Đăng Quang
 */
public class Part {
  private String fileName;
  private InputStream inputStream;
  private String name;
  private String contentType;
  private long size;

  /**
   * Constructs a Part by jakarta part.
   * 
   * @param part the jakarta Part get from request
   * @throws IOException if the input stream cannot be get
   */
  public Part(jakarta.servlet.http.Part part) throws IOException {
    this.fileName = Paths.get(part.getSubmittedFileName()).getFileName().toString();
    this.inputStream = part.getInputStream();
    this.contentType = part.getContentType();
    this.size = part.getSize();
    this.name = part.getName();
  }

  /**
   * Gets content type of the part.
   * 
   * @return the part's content type
   */
  public String getContentType() {
    return contentType;
  }

  /**
   * Gets the file name of the part.
   * 
   * @return name of file
   */
  public String getFileName() {
    return fileName;
  }

  /**
   * Gets the input stream from the part.
   * 
   * @return instance of input stream
   */
  public InputStream getInputStream() {
    return inputStream;
  }

  /**
   * Gets size of the part.
   * 
   * @return the part's size
   */
  public long getSize() {
    return size;
  }

  /**
   * Gets name of the part.
   * 
   * @return the part name
   */
  public String getName() {
    return name;
  }
}
