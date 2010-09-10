package org.onebusaway.quickstart;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.onebusaway.transit_data_federation.bundle.FederatedTransitDataBundleCreator;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundle;
import org.onebusaway.transit_data_federation.bundle.model.GtfsBundles;
import org.opentripplanner.graph_builder.impl.osm.FileBasedOpenStreetMapProviderImpl;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;

public class QuickStartGraphBuilderMain {
  public static void main(String[] args) throws Exception {

    if (args.length != 2) {
      System.err.println("usage: bundle_defintion.xml bundle_output_path");
      System.exit(-1);
    }

    File outputPath = new File(args[1]);
    File checkFile = new File(outputPath, "graph_bundle_complete");
    if (checkFile.exists()) {
      System.err.println("Graph bundle check file " + checkFile
          + " already exists.  Skipping graph buidling...");
      return;
    }

    FederatedTransitDataBundleCreator creator = new FederatedTransitDataBundleCreator();

    creator.setOutputPath(outputPath);

    List<File> contextPaths = new ArrayList<File>();
    contextPaths.add(new File(args[0]));

    String bundleXmlPath = System.getProperty("bundle.xml.path");
    if (isDefined(bundleXmlPath))
      contextPaths.add(new File(bundleXmlPath));
    creator.setContextPaths(contextPaths);

    Map<String, BeanDefinition> beans = new HashMap<String, BeanDefinition>();

    String gtfsPath = System.getProperty("gtfs.path");
    if (isDefined(gtfsPath)) {

      GtfsBundle gtfsBundle = new GtfsBundle();
      gtfsBundle.setPath(new File(gtfsPath));

      BeanDefinitionBuilder gtfsBundles = BeanDefinitionBuilder.genericBeanDefinition(GtfsBundles.class);
      gtfsBundles.addPropertyValue("bundles", Arrays.asList(gtfsBundle));
      beans.put("gtfs-bundles", gtfsBundles.getBeanDefinition());
    }

    String osmPath = System.getProperty("osm.path");
    if (isDefined(osmPath)) {
      BeanDefinitionBuilder osmProvider = BeanDefinitionBuilder.genericBeanDefinition(FileBasedOpenStreetMapProviderImpl.class);
      osmProvider.addPropertyValue("path", osmPath);
      beans.put("osmProvider", osmProvider.getBeanDefinition());
    }

    creator.setContextBeans(beans);

    creator.run();

    // Touch the check file
    PrintWriter writer = new PrintWriter(new FileWriter(checkFile));
    writer.println();
    writer.close();
  }

  private static boolean isDefined(String value) {
    return value != null && value.length() > 0;
  }
}
