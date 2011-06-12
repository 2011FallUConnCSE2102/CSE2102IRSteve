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
import com.stevesoft.pat.*;

/** This class is used internally by ReGame */
public class TestGroup extends Panel {
    /** @serial */
    Label lab;
    final static Color darkgreen = new Color(0x00,0x77,0x00);
    final static Color darkblue  = new Color(0x00,0x00,0x77);
    final static Color darkred   = new Color(0x77,0x00,0x00);
    /** @serial */
    public TextField txt;
    /** @serial */
    public ColorText ctxt;
    public void repaint() {
      ctxt.repaint();
    }
    public Object clone() {
      TestGroup t = new TestGroup(lab.getText(),txt.isEditable());
      t.txt = new TextField();
      t.txt.setText(txt.getText());
      t.ctxt = (ColorText)ctxt.clone();
      return t;
    }
    public TestGroup(String s,boolean b) {
        lab = new Label(s);
        lab.setAlignment(Label.LEFT);
        txt = new TextField();
        txt.setEditable(b);
        ctxt = new ColorText();
        GridBagLayout gb = new GridBagLayout();
        setLayout(gb);
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = gc.HORIZONTAL;
        gc.gridx = 0; gc.gridy = 0;
        gc.weightx = 0.0; gc.weighty = 0.0;
        gb.setConstraints(lab,gc);
        gc.weightx = 1.0;
        gc.gridx = 1;
        gb.setConstraints(txt,gc);
        gc.fill = gc.BOTH;
        gc.weighty = 1.0;
        gc.gridwidth = 2;
        gc.gridx = 0; gc.gridy = 1;
        gb.setConstraints(ctxt,gc);
        add(lab);
        add(txt);
        add(ctxt);
    }
    public void ShowRes(Regex r) {
        ctxt.clear();
        if(r.didMatch())
            ShowSuccess(r);
        else
            ShowFail();
    }
    public void ShowFail() {
        ColorLine ln = new ColorLine();
        ln.add(darkred,"Match failed!");
        ctxt.addColorLine(ln);
        ctxt.repaint();
    }
    public void ShowError(RegSyntax rs) {
        ColorLine ln = new ColorLine();
        ln.add(darkred,"RegSyntax: "+rs.getMessage());
        ctxt.addColorLine(ln);
        ctxt.repaint();
    }
    public void ShowSuccess(Regex r) {
        ColorLine ln = new ColorLine();
        ln.add(darkgreen,"==>");
        ln.add(Color.black,r.left());
        ln.add(darkgreen,"|");
        ln.add(darkred,r.substring());
        ln.add(darkgreen,"|");
        ln.add(Color.black,r.right());
        ln.add(darkgreen,"<==");
        ctxt.addColorLine(ln);
        ctxt.addColorLine(new ColorLine());
        if(r.numSubs() > 0) {
            ln = new ColorLine();
            ln.add(darkblue,"Backreferences:");
            ctxt.addColorLine(ln);
        }
        int i;
        for(i=1;i<=r.numSubs();i++) {
            ln = new ColorLine();
            ln.add(darkblue,"("+i+") : ");
            if(r.left(i)==null) ln.add(darkblue,"[null]");
            else {
                ln.add(Color.black,r.left(i));
                ln.add(darkgreen,"|");
                ln.add(darkred,r.substring(i));
                ln.add(darkgreen,"|");
                ln.add(Color.black,r.right(i));
            }
            ctxt.addColorLine(ln);
        }
        ctxt.repaint();
    }
}
