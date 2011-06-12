//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat.apps;

import java.awt.*;
import java.awt.event.*;

/** Just a little class to listen to the windowClosing event
    and call System.exit(0); */
public class ShutDown implements WindowListener {
  public void windowActivated(WindowEvent we) {}
  public void windowDeactivated(WindowEvent we) {}
  public void windowIconified(WindowEvent we) {}
  public void windowDeiconified(WindowEvent we) {}
  public void windowClosing(WindowEvent we) { System.exit(0); }
  public void windowClosed(WindowEvent we) {}
  public void windowOpened(WindowEvent we) {}
}
