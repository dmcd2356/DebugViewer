/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package debugviewer;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;

/**
 *
 * @author dmcd2356
 */
public class DebugViewer {

  final static private int GAPSIZE = 4; // gap size to place on each side of each widget
  
  private enum Orient { NONE, LEFT, RIGHT, CENTER }

  private static JFrame       mainFrame;
  private static JFileChooser fileSelector;
  
  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    // create the frame
    Dimension framesize = new Dimension(500, 300);
    mainFrame = new JFrame("DebugViewer");
    mainFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    mainFrame.setSize(framesize);
    mainFrame.setMinimumSize(framesize);

    // setup the layout for the frame
    GridBagLayout gbag = new GridBagLayout();
    mainFrame.setFont(new Font("SansSerif", Font.PLAIN, 14));
    mainFrame.setLayout(gbag);

    // we need a filechooser for the Load/Save buttons
    fileSelector = new JFileChooser();
    
    // create the controls
    JButton loadButton = makeButton(mainFrame, gbag, Orient.LEFT, true, "Load");
    JTextPane textPanel = makeScrollText(mainFrame, gbag, "");

    // setup the text port
    DebugMessage debug = new DebugMessage(textPanel);
    
    // setup the control actions
    loadButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(java.awt.event.ActionEvent evt) {
        loadButtonActionPerformed(evt);
      }
    });

    // display the frame
    mainFrame.pack();
    mainFrame.setLocationRelativeTo(null);
    mainFrame.setVisible(true);
  }
  
  /**
   * This creates a JButton and places it in the container.
   * 
   * @param container - the container to place the component in (e.g. JFrame or JPanel)
   * @param gridbag - the layout info
   * @param pos     - orientatition on the line: LEFT, RIGHT or CENTER
   * @param end     - true if this is last widget in the line
   * @param name    - the name to display as a label preceeding the widget
   * @return the button widget
   */
  private static JButton makeButton(Container container, GridBagLayout gridbag, Orient pos, boolean end,
                             String name) {
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(GAPSIZE, GAPSIZE, GAPSIZE, GAPSIZE);

    switch(pos) {
      case LEFT:
        c.anchor = GridBagConstraints.BASELINE_LEADING;
        break;
      case RIGHT:
        c.anchor = GridBagConstraints.BASELINE_TRAILING;
        break;
      case CENTER:
        c.anchor = GridBagConstraints.CENTER;
        break;
      case NONE:
        break;
    }
    c.fill = GridBagConstraints.NONE;
    c.gridheight = 1;
    if (end) {
      c.gridwidth = GridBagConstraints.REMAINDER; //end row
    }

    JButton button = new JButton(name);
    gridbag.setConstraints(button, c);
    container.add(button);
    return button;
  }

  /**
   * This creates a JScrollPane containing a JTextPane for text and places it in the container.
   * 
   * @param container - the container to place the component in (e.g. JFrame or JPanel)
   * @param gridbag - the layout info
   * @param name    - the name to display as a label preceeding the widget
   * @return the text pane widget
   */
  private static JTextPane makeScrollText(Container container, GridBagLayout gridbag, String name) {
    GridBagConstraints c = new GridBagConstraints();
    c.insets = new Insets(GAPSIZE, GAPSIZE, GAPSIZE, GAPSIZE);

    c.fill = GridBagConstraints.BOTH;
    c.gridwidth  = GridBagConstraints.REMAINDER;
    // since only 1 component, these both have to be non-zero for grid bag to work
    c.weightx = 1.0;
    c.weighty = 1.0;

    // create a text panel component
    JTextPane panel = new JTextPane();

    // create the scroll panel and apply constraints
    JScrollPane spanel = new JScrollPane(panel);
    spanel.setBorder(BorderFactory.createTitledBorder(name));
    gridbag.setConstraints(spanel, c);
    container.add(spanel);
    return panel;
  }

  private static void loadButtonActionPerformed(java.awt.event.ActionEvent evt) {
    fileSelector.setApproveButtonText("Open");
    fileSelector.setMultiSelectionEnabled(false);
    int retVal = fileSelector.showOpenDialog(mainFrame);
    if (retVal == JFileChooser.APPROVE_OPTION) {
      // init the text contents
      DebugUtil.clear();
      // read entries from file (ignore comments and blank lines)
      File file = fileSelector.getSelectedFile();
      BufferedReader in;
      try {
        in = new BufferedReader(new FileReader(file));
      } catch (FileNotFoundException ex) {
        return;
      }
      try {
        String line;
        while ((line = in.readLine()) != null) {
          DebugUtil.printLine(line);
        }
      } catch (IOException ex) {
        System.err.println(ex.getMessage());
        System.exit(0);
      }
    }
  }
  
}
