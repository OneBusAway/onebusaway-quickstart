package org.onebusaway.quickstart.bootstrap;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

/**
 * This is the bootstrap entry point for the onebusaway-quickstart WAR that
 * provides an executable war that can be used to quickly build a transit data
 * bundle and run a simple webapp on top of that bundle.
 * 
 * We have functionality to bootstrap a Java classpath from JARs contained
 * within and then execute targets within those JARs.
 * 
 * @author bdferris
 * 
 */
public class BootstrapMain {
  
  public static void main(String[] args) throws Exception {

    if (args.length == 0) {
      usage();
      System.exit(-1);
    }
    
    /**
     * This bit of code locates the URL of the WAR from where we're being
     * executed
     */
    ProtectionDomain protectionDomain = BootstrapMain.class.getProtectionDomain();
    URL warUrl = protectionDomain.getCodeSource().getLocation();

    /**
     * We create a temporary directory to extract JARs out of our parent WAR.
     * The URLClassLoader unfortunately can't point to them directly within the
     * WAR.
     */
    File tmpDir = getTempDirectory();

    String firstArg = args[0];

    String[] subArgs = new String[args.length - 1];
    System.arraycopy(args, 1, subArgs, 0, subArgs.length);

    if ("-build".equals(firstArg)) {

      /**
       * Bootstrap the classpath by extracting the JARs in our WAR into the
       * temporary directory and then creating a special classloader on top of
       * those JARs. Note that we include the WEB-INF/lib JARs since they have
       * the onebusaway-transit-data-federation.jar that we care about.
       */
      URLClassLoader classloader = bootstrapClasspath(warUrl, tmpDir, true);

      Class<?> c = classloader.loadClass("org.onebusaway.quickstart.bootstrap.BuildBootstrapMain");
      Method method = c.getMethod("main", String[].class);
      invokeWithProperClassloader(classloader, method, (Object) subArgs);
    
    } else if ("-webapp".equals(firstArg)) {
      
      String tempPath = System.getProperty("java.io.tmpdir");

      /**
       * A fix to handle the crazy path the Mac JVM returns that cause problems for embedded Jetty
       */
      if (tempPath.startsWith("/var/folders/"))
        System.setProperty("java.io.tmpdir", "/tmp");

      /**
       * Bootstrap the classpath by extracting the JARs in our WAR into the
       * temporary directory and then creating a special classloader on top of
       * those JARs. Note that we exclude the WEB-INF/lib JARs, since those will
       * be loaded by the webapp container.
       */
      URLClassLoader classloader = bootstrapClasspath(warUrl, tmpDir, false);

      Class<?> c = classloader.loadClass("org.onebusaway.quickstart.bootstrap.WebappBootstrapMain");
      Method method = c.getMethod("run", URL.class, String[].class);
      invokeWithProperClassloader(classloader, method, warUrl, subArgs);
    
    } else {
      System.err.println("unexpected first arg: " + firstArg);
      usage();
      System.exit(-1);
    }
  }

  private static void invokeWithProperClassloader(URLClassLoader classloader,
      final Method method, final Object... args) throws InterruptedException {

    Runnable r = new Runnable() {
      @Override
      public void run() {
        try {
          method.invoke(null, args);
        } catch (Exception ex) {
          ex.printStackTrace();
          System.exit(-1);
        }
      }
    };

    Thread thread = new Thread(r);
    thread.setContextClassLoader(classloader);
    thread.start();

    thread.join();
  }

  private static File getTempDirectory() throws IOException {
    File tmpDir = File.createTempFile(BootstrapMain.class.getName() + "-",
        "-jars");
    tmpDir.delete();
    tmpDir.mkdirs();

    DeleteTempDirectoryOnExitRunnable r = new DeleteTempDirectoryOnExitRunnable(
        tmpDir);
    Runtime.getRuntime().addShutdownHook(new Thread(r));
    return tmpDir;
  }

  private static void usage() {
    System.err.println("usage:");
    System.err.println("  -build ...");
    System.err.println("  -webapp ...");
    System.err.println("  -buildAndWebapp ...");
  }

  private static URLClassLoader bootstrapClasspath(URL warUrl, File tmpDir,
      boolean includeWarLibs) {

    System.err.println("Extracting the bootstrap classpath.  This may take a few moments...");
    
    try {

      List<URL> urls = new ArrayList<URL>();

      exploreJar(warUrl, urls, tmpDir, includeWarLibs);

      URL[] array = urls.toArray(new URL[urls.size()]);
      return URLClassLoader.newInstance(array);

    } catch (Exception ex) {
      throw new IllegalStateException(ex);
    }
  }

  private static void exploreJar(URL location, List<URL> urls, File tmpDir,
      boolean includeWarLibs) throws IOException, MalformedURLException,
      URISyntaxException {

    JarInputStream jar = new JarInputStream(location.openStream());
    JarEntry entry = null;

    while ((entry = jar.getNextJarEntry()) != null) {

      String name = entry.getName();

      /**
       * The bulk of the JARs we care about are already in the WEB-INF/lib
       * directory of the parent WAR. However, we've put a few additional
       * dependencies in META-INF/bootstrap-lib to help with running the
       * embedded webapp.
       */
      if ((name.startsWith("META-INF/bootstrap-lib") || (includeWarLibs && name.startsWith("WEB-INF/lib")))
          && name.endsWith(".jar")) {
        
        name = name.replace("META-INF/", "");

        File outputJar = new File(tmpDir, name);
        outputJar.deleteOnExit();

        outputJar.getParentFile().mkdirs();
        OutputStream out = new BufferedOutputStream(new FileOutputStream(
            outputJar));
        byte[] buffer = new byte[1024];
        while (true) {
          int rc = jar.read(buffer);
          if (rc == -1)
            break;
          out.write(buffer, 0, rc);
        }
        out.close();
        urls.add(outputJar.toURI().toURL());
      }
    }
    jar.close();
  }

  /**
   * This runner should clean up all the extracted wars from the filesystem when
   * we finish up.
   * 
   * @author bdferris
   * 
   */
  private static class DeleteTempDirectoryOnExitRunnable implements Runnable {

    private File _path;

    public DeleteTempDirectoryOnExitRunnable(File path) {
      _path = path;
    }

    @Override
    public void run() {
      deleteFiles(_path);
    }

    private void deleteFiles(File file) {
      if (file.isDirectory()) {
        for (File child : file.listFiles())
          deleteFiles(child);
      }
      file.delete();
    }
  };

}
