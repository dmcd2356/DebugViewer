/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package debugviewer;

import java.awt.Graphics;
import java.util.HashMap;
import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.WindowConstants;
import javax.swing.text.AttributeSet;

/**
 *
 * @author dan
 */
public class DebugMessage {
    
  public static final boolean PRINT_TO_STDOUT = false; // set to false to create a panel for msgs

  /**
   * these are the types of messages that can be displayed
   */
  public enum DebugType {
    // these are for local use only
    Normal,
    Hexdata,
    Addr,
    Ascii,
    Tstamp,
    // these are for external use
    Error,    // error messages
    Warn,     // warnings
    Info,     // general information
    Entry,    // entry to an Executor command
    Exit,     // (not currently used)
    Stack,    // stack push/pop (concrete value)
    StackS,   // stack push/pop (symbolic value)
    StackI,   // stack new/restore (indicates entry/exit from a method call)
    Local,    // local parameter load/store (concrete value)
    LocalS,   // local parameter load/store (symbolic value)
    Solve;    // solver output
  }
    
  // used in formatting printArray
  private final static char[] HEXARRAY = "0123456789ABCDEF".toCharArray();
    
  private static final String NEWLINE = System.getProperty("line.separator");

  private static JTextPane debugTextPane;
  private static JFrame    debugFrame;
  private static long      startTime = System.currentTimeMillis(); // get the start time
  private static boolean   showTime  = false;
  private static boolean   showHours = false;
  private static boolean   showType  = false;
  private static HashMap<String, FontInfo> messageTypeTbl = new HashMap<>();

  public DebugMessage (boolean bGuiPanel) {
    if (bGuiPanel) {
      createDebugPanel();
      setColors();
      showTime = true;
      showType = true;
    }
    else {
      debugTextPane = null; // dump messages to stdout
    }
  }
  
  public DebugMessage (JTextPane textpane) {
    debugTextPane = textpane;
      
    // if not, create one and place it in a frame for display
    if (textpane == null) {
      createDebugPanel();
    }
      
    // setup message type colors
    setColors();
      
    // enable display of time and type
    showTime = true;
    showType = true;
  }

  private void createDebugPanel() {
    debugTextPane = new javax.swing.JTextPane();
    javax.swing.JScrollPane scrollPane = new javax.swing.JScrollPane(debugTextPane);

    debugFrame = new javax.swing.JFrame("danalyzer");
    debugFrame.add(scrollPane);
    debugFrame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    debugFrame.setSize(new java.awt.Dimension(800, 500));
    debugFrame.setLocationRelativeTo(null);
    debugFrame.setVisible(true);
  }
  
  private void setColors () {
    // set some default text colors for local use
    setTypeColor (DebugType.Normal.toString(),  DebugUtil.TextColor.Black, DebugUtil.FontType.Normal);
    setTypeColor (DebugType.Tstamp.toString(),  DebugUtil.TextColor.Brown, DebugUtil.FontType.Normal);
    setTypeColor (DebugType.Hexdata.toString(), DebugUtil.TextColor.DkVio, DebugUtil.FontType.Normal);
    setTypeColor (DebugType.Addr.toString(),    DebugUtil.TextColor.Brown, DebugUtil.FontType.Italic);
    setTypeColor (DebugType.Ascii.toString(),   DebugUtil.TextColor.Green, DebugUtil.FontType.Normal);

    setTypeColor (DebugType.Error.toString(),  DebugUtil.TextColor.Red,   DebugUtil.FontType.Bold);
    setTypeColor (DebugType.Warn.toString(),   DebugUtil.TextColor.DkRed, DebugUtil.FontType.Bold);
    setTypeColor (DebugType.Info.toString(),   DebugUtil.TextColor.Black, DebugUtil.FontType.Normal);
    setTypeColor (DebugType.Entry.toString(),  DebugUtil.TextColor.Brown, DebugUtil.FontType.Normal);
    setTypeColor (DebugType.Exit.toString(),   DebugUtil.TextColor.Brown, DebugUtil.FontType.Normal);
    setTypeColor (DebugType.Stack.toString(),  DebugUtil.TextColor.Blue,  DebugUtil.FontType.Normal);
    setTypeColor (DebugType.StackS.toString(), DebugUtil.TextColor.Blue,  DebugUtil.FontType.Italic);
    setTypeColor (DebugType.StackI.toString(), DebugUtil.TextColor.Blue,  DebugUtil.FontType.Bold);
    setTypeColor (DebugType.Local.toString(),  DebugUtil.TextColor.Green, DebugUtil.FontType.Normal);
    setTypeColor (DebugType.LocalS.toString(), DebugUtil.TextColor.Green, DebugUtil.FontType.Italic);
    setTypeColor (DebugType.Solve.toString(),  DebugUtil.TextColor.DkVio, DebugUtil.FontType.Bold);
  }
  
  /**
   * returns the elapsed time in seconds.
   * The format of the String is: "HH:MM:SS"
   * 
   * @return a String of the formatted time
   */
  private static String getElapsedTime () {
    // get the elapsed time in secs
    long currentTime = System.currentTimeMillis();
    long elapsedTime = currentTime - startTime;
    if (elapsedTime < 0) {
      elapsedTime = 0;
    }
        
    // split value into hours, min and secs
    Long msecs = elapsedTime % 1000;
    Long secs = (elapsedTime / 1000);
    Long hours = 0L;
    secs += msecs >= 500 ? 1 : 0;
    if (showHours) {
      hours = secs / 3600;
    }
    secs %= 3600;
    Long mins = secs / 60;
    secs %= 60;

    // now stringify it
    String elapsed = "";
    if (showHours) {
      if (hours < 10) {
        elapsed = "0";
      }
      elapsed += hours.toString();
      elapsed += ":";
    }
    elapsed += (mins < 10) ? "0" + mins.toString() : mins.toString();
    elapsed += ":";
    elapsed += (secs < 10) ? "0" + secs.toString() : secs.toString();
    return elapsed;
  }

  /**
   * sets the specified input string to a fixed length all uppercase version
   * 
   * @param value - the string value
   * @return the properly formatted version
   */
  private static String formatStringLength (String value) {
    value += "      ";
    return value.substring(0, 6).toUpperCase();
  }
    
  /**
   * A generic function for appending formatted text to a JTextPane.
   * 
   * @param tp    - the TextPane to append to
   * @param msg   - message contents to write
   * @param color - color of text
   * @param font  - the font selection
   * @param size  - the font point size
   * @param ftype - type of font style
   */
  private static void appendToPane(String msg, DebugUtil.TextColor color, String font, int size,
                            DebugUtil.FontType ftype) {
    if (debugTextPane == null) {
      return;
    }
        
    AttributeSet aset = DebugUtil.setTextAttr(color, font, size, ftype);
    int len = debugTextPane.getDocument().getLength();
    debugTextPane.setCaretPosition(len);
    debugTextPane.setCharacterAttributes(aset, false);
    debugTextPane.replaceSelection(msg);
  }

  /**
   * A generic function for appending formatted text to a JTextPane.
   * 
   * @param tp    - the TextPane to append to
   * @param msg   - message contents to write
   * @param color - color of text
   * @param ftype - type of font style
   */
  private static void appendToPane(String msg, DebugUtil.TextColor color, DebugUtil.FontType ftype) {
    appendToPane(msg, color, "Courier", 11, ftype);
  }

  /**
   * checks if the specified type is valid
   * 
   * @param type - the type as specified in the message
   * @return the specified valid type (null if not valid)
   */
  public static String getValidType(String type) {
      if (type.length() >= 7 && type.charAt(6) == ':') {
      type = type.substring(0, 6).trim();
      	switch (type) {
      	case "ERROR":
      	case "WARN":
      	case "INFO":
      	case "ENTRY":
      	case "EXIT":
      	case "STACK":
      	case "STACKS":
      	case "STACKI":
      	case "LOCAL":
      	case "LOCALS":
      	case "SOLVE":
          return type;
        default:
          break;
      }
    }
    return null;
  }
  
  /**
   * closes the panel (if one was open)
   */
  public static void close () {
    debugTextPane = null;   // make sure the panel will no longer be accessed
    if (debugFrame != null) {
      debugFrame.dispose();
    }
  }
  
  /**
   * enables/disables the display of time when using the print command
   * @param enable - true to enable display of time preceeding message
   */
  public static final void enableTime(boolean enable) {
    showTime = enable;
  }
    
  /**
   * enables/disables the display of hours in time display when using the print command
   * @param enable - true to enable display of hours preceeding message
   */
  public static final void enableHours(boolean enable) {
    showHours = enable;
  }
    
  /**
   * enables/disables the display of message type when using the print command
   * @param enable - true to enable display of message type preceeding message
   */
  public static final void enableType(boolean enable) {
    showType = enable;
  }
    
  /**
   * resets the start time
   */
  public static final void resetTime() {
    startTime = System.currentTimeMillis(); // get the start time
  }
    
  /**
   * clears the display.
   */
  public static final void clear() {
    if (debugTextPane != null) {
      debugTextPane.setText("");
    }
  }

  /**
   * updates the display immediately
   */
  public static final void updateDisplay () {
    if (debugTextPane != null) {
      Graphics graphics = debugTextPane.getGraphics();
      if (graphics != null) {
        debugTextPane.update(graphics);
      }
    }
  }
    
  /**
   * sets the association between a type of message and the characteristics
   * in which to print the message.
   * 
   * @param type  - the type to associate with the font characteristics
   * @param color - the color to assign to the type
   * @param ftype - the font attributes to associate with the type
   */
  public void setTypeColor (String type, DebugUtil.TextColor color, DebugUtil.FontType ftype) {
    // limit the type to a 5-char length (pad with spaces if necessary)
    type = formatStringLength(type);
        
    FontInfo fontinfo = new FontInfo(color, ftype);
    if (messageTypeTbl.containsKey(type)) {
      messageTypeTbl.replace(type, fontinfo);
    }
    else {
      messageTypeTbl.put(type, fontinfo);
    }
  }
    
  /**
   * same as above, but lets user select font family and size as well.
   * 
   * @param type  - the type to associate with the font characteristics
   * @param color - the color to assign to the type
   * @param ftype - the font attributes to associate with the type
   * @param size  - the size of the font
   * @param font  - the font family (e.g. Courier, Ariel, etc.)
   */
  public final void setTypeColor (String type, DebugUtil.TextColor color, DebugUtil.FontType ftype,
                                  int size, String font) {
    // limit the type to a 5-char length (pad with spaces if necessary)
    type = formatStringLength(type);
        
    FontInfo fontinfo = new FontInfo(color, ftype, size, font);
    if (messageTypeTbl.containsKey(type)) {
      messageTypeTbl.replace(type, fontinfo);
    }
    else {
      messageTypeTbl.put(type, fontinfo);
    }
  }
    
  /**
   * outputs the timestamp info to the debug window.
   */
  public static final void printTimestamp() {
    String tstamp = "[" + getElapsedTime() + "] ";
    printRaw(DebugType.Tstamp.toString(), tstamp);
  }
    
  /**
   * outputs the message type to the debug window.
   * 
   * @param type - the message type to display
   */
  public static final void printType(String type) {
    type = formatStringLength(type);
    printRaw(type, type + ": ");
  }
    
  /**
   * outputs the header info (timestamp and message type) to the debug window
   * if they are enabled.
   * 
   * @param type - the message type to display
   */
  public static final void printHeader(String type) {
    if (showTime)
      printTimestamp();
    if (showType) {
      printType(type);
    }
  }
    
  /**
   * outputs a termination char to the debug window
   */
  public static final void printTerm() {
    if (debugTextPane == null) {
      System.out.println();
      return;
    }
    appendToPane(NEWLINE, DebugUtil.TextColor.Black, "Courier", 11, DebugUtil.FontType.Normal);
  }
    
  /**
   * displays a message in the debug window (no termination).
   * 
   * @param type  - the type of message to display
   * @param message - message contents to display
   */
  public static final void printRaw(String type, String message) {
    if (message != null && !message.isEmpty()) {
      if (debugTextPane == null) {
        System.out.print(message);
        return;
      }
        
      // set default values (if type was not found)
      DebugUtil.TextColor color = DebugUtil.TextColor.Black;
      DebugUtil.FontType ftype = DebugUtil.FontType.Normal;
      String font = "Courier";
      int size = 11;

      // get the color and font for the specified type
      FontInfo fontinfo = messageTypeTbl.get(formatStringLength(type));
      if (fontinfo != null) {
        color = fontinfo.color;
        ftype = fontinfo.fonttype;
        font  = fontinfo.font;
        size  = fontinfo.size;
      }

      appendToPane(message, color, font, size, ftype);
    }
  }

  /**
   * outputs the various types of messages to the status display.
   * all messages will guarantee the previous line was terminated with a newline,
   * and will preceed the message with a timestamp value and terminate with a newline.
   * 
   * @param type    - the type of message
   * @param message - the message to display
   */
  public static final void print(String type, String message) {
    // limit the type to a fixed length (pad with spaces if necessary)
    type = formatStringLength(type);
        
    if (message != null && !message.isEmpty()) {
      if (debugTextPane == null) {
        System.out.println(type + ": " + message);
        return;
      }
        
      if (!message.contains(NEWLINE)) {
        // print the header info if enabled (timestamp, message type)
        printHeader(type);

        // now print the message with a terminator
        printRaw(type, message + NEWLINE);
      }
      else {
        // seperate into lines and print each independantly
        String[] msgarray = message.split(NEWLINE);
        for (String msg : msgarray) {
          printHeader(type);
          printRaw(type, msg + NEWLINE);
        }
      }
    }
  }

  /**
   * prints the array of bytes to the status display.
   * 
   * @param array - the data to display
   * @param showAscii - true if also display the ASCII char representation to the right
   */
  public static final void printByteArray(byte[] array, boolean showAscii) {

    final int bytesperline = 32; // the number of bytes to print per line
        
    print(DebugType.Normal.toString(), "Size of array = " + array.length + " bytes");

    // print line at a time
    for (int offset = 0; offset < array.length; offset += bytesperline) {
      char[] hexChars = new char[bytesperline * 3];
      char[] ascChars = new char[bytesperline];
      for (int ix = 0; ix < bytesperline; ix++) {
        if (ix + offset >= array.length) {
          hexChars[ix * 3 + 0] = ' ';
          hexChars[ix * 3 + 1] = ' ';
          hexChars[ix * 3 + 2] = ' ';
        }
        else {
          // make an array of displayable chars
          int v = array[ix + offset] & 0xFF;
          if (v >= 32 && v <= 126) {
            ascChars[ix] = (char) v;
          }
          else {
            ascChars[ix] = '.';
          }

          // make the array of hex values
          hexChars[ix * 3 + 0] = HEXARRAY[v >>> 4];
          hexChars[ix * 3 + 1] = HEXARRAY[v & 0x0F];
          hexChars[ix * 3 + 2] = ' ';
        }
      }
      String hexdata = new String(hexChars);
      String ascdata = new String(ascChars);

      // generate the address value
      String address = "000000" + Integer.toHexString(offset);
      address = address.substring(address.length() - 6);
            
      // display the data
      printHeader(DebugType.Hexdata.toString());
      printRaw(DebugType.Addr.toString()   , address + ": ");
      printRaw(DebugType.Hexdata.toString(), hexdata + "  ");
      if (showAscii) {
        printRaw(DebugType.Ascii.toString(), ascdata + NEWLINE);
      }
    }
  }
    
  /**
   * a simple test of the colors
   */
  public final void testColors () {
    if (debugTextPane == null) {
      return;
    }
    appendToPane("-----------------------------------------" + NEWLINE,
                DebugUtil.TextColor.Black, DebugUtil.FontType.Normal);
    for (DebugUtil.TextColor color : DebugUtil.TextColor.values()) {
      appendToPane("This is a sample of the color: " + color + NEWLINE,
                color, DebugUtil.FontType.Bold);
    }
    appendToPane("-----------------------------------------" + NEWLINE,
                DebugUtil.TextColor.Black, DebugUtil.FontType.Normal);
  }
    
  public class FontInfo {
    DebugUtil.TextColor  color;      // the font color
    DebugUtil.FontType   fonttype;   // the font attributes (e.g. Italics, Bold,..)
    String          font;       // the font family (e.g. Courier)
    int             size;       // the font size
        
    FontInfo (DebugUtil.TextColor col, DebugUtil.FontType type) {
      color = col;
      fonttype = type;
      font = "Courier";
      size = 11;
    }
        
    FontInfo (DebugUtil.TextColor col, DebugUtil.FontType type, int fsize, String fontname) {
      color = col;
      fonttype = type;
      font = fontname;
      size = fsize;
    }
  }
}
