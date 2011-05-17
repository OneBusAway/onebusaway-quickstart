package org.onebusaway.quickstart.bootstrap;

import java.io.IOException;

import org.onebusaway.transit_data_federation.bundle.FederatedTransitDataBundleCreatorMain;

public class BuildBootstrapMain {
  public static void main(String[] args) throws IOException,
      ClassNotFoundException {
    FederatedTransitDataBundleCreatorMain.main(args);
  }
}
