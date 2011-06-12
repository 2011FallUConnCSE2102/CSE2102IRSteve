//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat.apps;
import java.awt.*;
import java.util.*;

/** A simply class for making a message box with arbitrary
 text inside it. */
public class Message extends Frame {
    /** @serial */
    public Vector v = new Vector();
    /** @serial */
    public Component c=null;
    /** Open the window.  When any of the buttons on the
	window are clicked, the associated message will be delivered to
	component c. */
    public void ask(Component c) {
        this.c = c;
        packNShow();
    }
    public void packNShow() {
        int i;
        GridBagLayout gb = new GridBagLayout();
        setLayout(gb);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.fill = gc.HORIZONTAL;
        int xpos = 0,ypos = 0;
        Label lab;
        for(i=0;i<v.size();i++)
            if(v.elementAt(i) instanceof Label) {
            lab = (Label)v.elementAt(i);
            add(lab);
            gc.gridy = ypos++;
            gb.setConstraints(lab,gc);
        }
        gc.gridy = ypos;
        for(i=0;i<v.size();i++)
            if(v.elementAt(i) instanceof Button) {
            Button but = (Button)v.elementAt(i);
            add(but);
            gc.gridx = xpos++;
            gb.setConstraints(but,gc);
        }
        pack();
        resize(gb.preferredLayoutSize(this));
        show();
    }
    /** @serial */
    String btxt = null;
    /** Add a button to the message with text b. */
    public void addButton(Button b) {
        v.addElement(b);
    }
    /** Add a centered line of text to the message. */
    public void addCentered(String lb) {
        v.addElement(new Label(lb,Label.CENTER));
    }
    /** Add a left justified line of text to the message. */
    public void addLeft(String lb) {
        v.addElement(new Label(lb,Label.LEFT));
    }
    /** Add a right justified line of text to the message. */
    public void addRight(String lb) {
        v.addElement(new Label(lb,Label.RIGHT));
    }
    /** This action will dispose of this graphics object and
	deliver whatever action event it receives to the component
	object supplied by the ask method.
        @see com.stevesoft.pat.apps.Message#ask(java.awt.Component)
	*/
    public boolean action(Event e,Object o) {
        if(c == null) return true;
        dispose();
        btxt = ((Button)e.target).getLabel();
        e.target = this;
        c.postEvent(e);
        return true;
    }
}
