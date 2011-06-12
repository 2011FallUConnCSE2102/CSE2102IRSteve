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

/* A helper class for the ColorText class.  This provides a single
line of text with various colored parts. */
public class ColorLine {
    Vector v = new Vector();
    public Object clone() {
        ColorLine cl = new ColorLine();
        cl.v = (Vector)v.clone();
        return cl;
    }
    int ColorLineWidth(FontMetrics fm) {
        int width = 0;
        int i;
        for(i=0;i<v.size();i++)
            if(v.elementAt(i) instanceof String) {
            String s = (String)v.elementAt(i);
            width += fm.stringWidth(s);
        }
        return width;
    }
    /** Add a string s with color c to this
        line of text. When you've built up a
        line of text, simply add it to the
        ColorText object. */
    public void add(Color c,String s) {
        v.addElement(c);
        v.addElement(s);
    }
    public Dimension getSize(FontMetrics fm) {
        int xs = 0;
        for(int i=0;i<v.size();i++) {
            Object o = v.elementAt(i);
            if(o instanceof String)
                xs += fm.stringWidth((String)o);
        }
        return new Dimension(xs,fm.getHeight());
    }
}
