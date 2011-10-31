/**
 * Copyright (C) 2011 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.onebusaway.quickstart.bootstrap.gui;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;
import javax.swing.JTextPane;
import javax.swing.UIManager;

public class RunPanel extends JPanel {

  private static final long serialVersionUID = 1L;

  /**
   * Create the panel.
   */
  public RunPanel() {
    setLayout(new MigLayout("", "[grow]", "[grow]"));

    JTextPane txtpnWereReadyTo = new JTextPane();
    txtpnWereReadyTo.setBackground(UIManager.getColor("control"));
    txtpnWereReadyTo.setText("We're ready to go!");
    add(txtpnWereReadyTo, "cell 0 0,grow");

  }

}
