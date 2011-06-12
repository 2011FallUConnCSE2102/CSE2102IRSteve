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

/** This is a simple class whose only purpose is to display
text with colors. */
public class ColorText extends Canvas {
    /** @serial */
    Vector v = new Vector();
    public Object clone() {
      ColorText ct = new ColorText();
      ct.v = (Vector)v.clone();
      return ct;
    }
    public ColorText() { setBackground(Color.white); }
    /** Add a colored line of text to the ColorText object. */
    public void addColorLine(ColorLine ln) {
      synchronized(this) {
        v.addElement(ln);
      }
    }
    /** Remove all lines of text from ColorText object. */
    public void clear() {
      synchronized (this) {
        v = new Vector();
        repaint();
      }
    }
    /** Draw the text. */
    public void paint(Graphics g) {
      synchronized(this) {
        FontMetrics fm = getFontMetrics(getFont());
        if(fm==null) return;
        int i;
        // y = fm.getAscent()+yi*fm.getHeight()+y_margin;
        Rectangle r = g.getClipRect();
        int ymin = r.y;
        int ymax = r.y+r.height;
        int imax = (ymax-y_margin-fm.getAscent())/fm.getHeight()+2;
        int imin = (ymin-y_margin-fm.getAscent())/fm.getHeight()-2;
        if(imin < 0) imin = 0;
        if(imax > v.size()) imax = v.size();
        for(i=imin;i<imax;i++)
        //for(i=0;i<v.size();i++)
            drawColorLine(g,fm,(ColorLine)v.elementAt(i),i);
        g.setColor(Color.lightGray);
        g.drawRect(1,1,size().width-2,size().height-2);
      }
    }
    static final int x_margin = 10, y_margin = 10;
    public Dimension getMinimumSize() { return getPreferredSize(); }
    public Dimension getPreferredSize() {
        FontMetrics fm = getFontMetrics(getFont());
        if(fm == null) return new Dimension(0,0);
        int i;
        int xs=0,ys=0;
        for(i=0;i<v.size();i++) {
            ColorLine cl = (ColorLine)v.elementAt(i);
            Dimension d = cl.getSize(fm);
            ys += d.height;
            xs = xs > d.width ? xs : d.width;
        }
        ys += fm.getAscent();
        Dimension d = new Dimension(xs,ys);
        return d;
    }
    final void
        drawColorLine(Graphics g,FontMetrics fm,ColorLine ln,int yi) {
        int i;
        int x = x_margin;
        int y = fm.getAscent()+yi*fm.getHeight()+y_margin;
        for(i=0;i<ln.v.size();i++) {
            Object o = ln.v.elementAt(i);
            if(o instanceof Color) {
                g.setColor((Color)o);
            } else {
                g.drawString((String)o,x,y);
                x += fm.stringWidth((String)o);
            }
        }
    }
}
