package com.vnexos.sema;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;

import com.vnexos.sema.context.ServerContext;
import com.vnexos.sema.database.Database;
import com.vnexos.sema.loader.Loader;
import com.vnexos.sema.util.PrivateServiceConstructor;
import com.vnexos.sema.util.logger.LoggerBuilder;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

/**
 * Handles the begining and ending of the server.
 * 
 * @author Trần Việt Đăng Quang
 */
public class AppInitializer implements ServletContextListener {
  /**
   * Builds prefix of log message.
   * 
   * @return formatted prefix
   */
  private String buildPrefix() {
    StringBuilder sb = new StringBuilder();
    if (Constants.getBoolean("log.show.brand")) {
      sb.append("$reset()[$bold()$bg(1)$fg(0)Se$bg(11)Ma$reset()]");
      sb.append(' ');
    }
    if (Constants.getBoolean("log.show.time")) {
      sb.append("[$fg(10)$time()$reset()]");
      sb.append(' ');
    }
    if (Constants.getBoolean("log.show.type")) {
      sb.append("[$bold()$type()$reset()]");
      sb.append(' ');
    }
    if (Constants.getBoolean("log.show.module"))
      sb.append("$module()");
    sb.append("$fg(250)");
    return sb.toString();
  }

  /**
   * Builds postfix of log message.
   * 
   * @return formatted postfix
   */
  private String buildPostfix() {
    int logClassPath = Constants.getInteger("log.classpath");
    if (logClassPath == 0)
      return "";
    else
      return " $reset()[$fg(240)$classPath()$reset()]";
  }

  /**
   * Initializes the context.
   * 
   * @param servletContext context of the servlet
   */
  public static void initContext(ServletContext servletContext) throws NoSuchMethodException, SecurityException,
      InstantiationException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
    Constants.context = PrivateServiceConstructor.createInstance(
        ServerContext.class,
        PrivateServiceConstructor.createClassTypes(),
        PrivateServiceConstructor.createObjects());
    File file = new File(servletContext.getRealPath(""));
    Constants.context.setPath(file.getParent());
  }

  /**
   * Loads properties from config file.
   * 
   * @throws IOException if an error occurs while loading config file
   */
  private void loadProps() throws IOException {
    File configFile = new File(Constants.context.joinPath("config.properties"));
    if (!configFile.exists()) {
      InputStream input = Constants.class.getClassLoader().getResourceAsStream("config.properties");
      if (!configFile.createNewFile())
        throw new IOException("File cannot be created");
      BufferedReader br = new BufferedReader(new InputStreamReader(input));
      BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(configFile)));

      String line;
      while ((line = br.readLine()) != null) { // Loop until end of file
        bw.write(line);
        bw.newLine();
      }

      bw.close();
      br.close();
    }

    FileInputStream fis = new FileInputStream(configFile);
    Constants.props.load(fis);
    fis.close();
  }

  /**
   * Begining point of the server.
   */
  @Override
  public void contextInitialized(ServletContextEvent sce) {
    try {
      initContext(sce.getServletContext());
      loadProps();
      Constants.context.setLogger(
          LoggerBuilder
              .init()
              .setPrefix(buildPrefix())
              .setPostfix(buildPostfix())
              .build());

      PrivateServiceConstructor.invokeFunction(
          Database.class,
          "init", null,
          PrivateServiceConstructor.createClassTypes(),
          PrivateServiceConstructor.createObjects());

      Loader.init();
      Loader.loadPlugins();
      Constants.context.info("Server started successfully!");
    } catch (Exception e) {
      Constants.context.log(e);
    }
  }

  /**
   * Ending point of the server.
   */
  @Override
  public void contextDestroyed(ServletContextEvent sce) {
    Loader.unloadPlugins();
    Database.shutdown();
    Constants.context.info("Server stopped!");
    Constants.context.getLogger().close();
  }
}
