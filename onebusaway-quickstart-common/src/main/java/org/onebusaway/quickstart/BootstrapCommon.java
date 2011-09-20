package org.onebusaway.quickstart;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class BootstrapCommon {
  public static void printUsage(Class<?> resourceClass, String resourceName) {
    InputStream is = resourceClass.getResourceAsStream(resourceName);
    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
    String line = null;
    try {
      while ((line = reader.readLine()) != null) {
        System.err.println(line);
      }
    } catch (IOException ex) {

    } finally {
      if (reader != null) {
        try {
          reader.close();
        } catch (IOException ex) {

        }
      }
    }
  }
}
