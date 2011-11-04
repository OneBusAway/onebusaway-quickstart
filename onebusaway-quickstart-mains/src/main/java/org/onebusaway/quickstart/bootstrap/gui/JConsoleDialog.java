package org.onebusaway.quickstart.bootstrap.gui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EtchedBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.JScrollPane;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class JConsoleDialog extends JDialog {

  private static final long serialVersionUID = 1L;

  private final JPanel contentPanel = new JPanel();

  private final JTextPane _consoleTextPane = new JTextPane();

  /**
   * Launch the application.
   */
  public static void main(String[] args) {
    try {
      JConsoleDialog dialog = new JConsoleDialog();
      dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
      dialog.setVisible(true);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Create the dialog.
   */
  public JConsoleDialog() {
    setBounds(100, 100, 450, 300);
    getContentPane().setLayout(new BorderLayout());
    contentPanel.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
    getContentPane().add(contentPanel, BorderLayout.CENTER);
    contentPanel.setLayout(new BorderLayout(0, 0));
    {
      JScrollPane scrollPane = new JScrollPane();
      contentPanel.add(scrollPane, BorderLayout.CENTER);
      {
        _consoleTextPane.setEditable(false);
        scrollPane.setViewportView(_consoleTextPane);
      }
    }
    {
      JPanel buttonPane = new JPanel();
      buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
      getContentPane().add(buttonPane, BorderLayout.SOUTH);
      {
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent e) {
            // TODO: Maybe we should do a bit more cleanup?
            System.exit(0);
          }
        });
        cancelButton.setActionCommand("Cancel");
        buttonPane.add(cancelButton);
      }
    }
    System.setOut(new PrintStream(new ConsoleOutputStream(System.out)));
    System.setErr(new PrintStream(new ConsoleOutputStream(System.err)));
  }

  private void updateTextPane(final String value) {
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        Document doc = _consoleTextPane.getDocument();

        try {
          doc.insertString(doc.getLength(), value, null);
        } catch (BadLocationException ex) {
          throw new IllegalStateException(ex);
        }
      }
    });
  }

  private class ConsoleOutputStream extends OutputStream {

    private PrintStream _original;

    public ConsoleOutputStream(PrintStream out) {
      _original = out;
    }

    @Override
    public void write(int b) throws IOException {
      _original.write(b);
      updateTextPane(String.valueOf((char) b));
    }

    @Override
    public void write(byte[] b) throws IOException {
      _original.write(b);
      updateTextPane(new String(b));
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
      _original.write(b, off, len);
      updateTextPane(new String(b, off, len));
    }
  }
}
