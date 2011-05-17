package org.onebusaway.quickstart.bootstrap;

import java.net.URL;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.Parser;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.bio.SocketConnector;
import org.eclipse.jetty.webapp.WebAppContext;

public class WebappBootstrapMain {

  public static void run(URL warUrl, String[] args) throws Exception {

    Options options = createOptions();
    Parser parser = new GnuParser();

    CommandLine cli = parser.parse(options, args);
    args = cli.getArgs();

    if (args.length != 1) {
      usage();
      System.exit(-1);
    }

    String bundlePath = args[0];

    System.setProperty("bundlePath", bundlePath);

    Server server = new Server();
    SocketConnector connector = new SocketConnector();

    // Set some timeout options to make debugging easier.
    connector.setMaxIdleTime(1000 * 60 * 60);
    connector.setSoLingerTime(-1);
    connector.setPort(8080);
    server.setConnectors(new Connector[] {connector});

    WebAppContext context = new WebAppContext();
    context.setServer(server);
    context.setContextPath("/");

    context.setWar(warUrl.toExternalForm());

    // server.addHandler(context);
    server.setHandler(context);

    try {
      server.start();

      System.err.println("=============================================================");
      System.err.println("=");
      System.err.println("= Your OneBusAway instance has started.  Browse to:");
      System.err.println("=");
      System.err.println("= http://localhost:8080/");
      System.err.println("=");
      System.err.println("= to see your instance in action.");
      System.err.println("=");
      System.err.println("= When you are finished, press return to exit gracefully...");
      System.err.println("=============================================================");

      System.in.read();
      server.stop();
      server.join();
    } catch (Exception e) {
      e.printStackTrace();
      System.exit(100);
    }
  }

  private static void usage() {
    System.err.println("webapp usage: [opts] bundlePath");
  }

  private static Options createOptions() {
    Options options = new Options();
    return options;
  }
}
