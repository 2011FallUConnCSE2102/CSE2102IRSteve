//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat.apps;
import com.stevesoft.pat.*;
import java.awt.*;
import java.util.*;

/** This is the test applet in home page.
    You can run it standalone by typing,
    java com.stevesoft.pat.apps.ReGap.
    */
public class ReGap extends java.applet.Applet {
    public static void main(String[] unused) {
      Frame f = new Frame("Regex Tester");
      //f.addWindowListener(new ShutDown());
      f.resize(400,170);
      ReGap r = new ReGap();
      f.add(r);
      r.init();
      r.start();
      f.show();
    }
    /** @serial */
    Label pat_msg = new Label("Pattern");
    /** @serial */
    public TextField pat = new TextField();
    /** @serial */
    public Button bn = new Button("Go!");
    /** @serial */
    public TestGroup tg = null;
    public void init() {
        tg = new TestGroup("Text",true);
        add(pat_msg);
        add(pat);
        add(tg);
        add(bn);

        // layout info
        GridBagLayout gb = new GridBagLayout();
        setLayout(gb);
        GridBagConstraints gc = new GridBagConstraints();

        gc.gridwidth = gc.gridheight = 1;
        gc.fill = gc.HORIZONTAL;
        gc.weightx = 0.0; gc.weighty = 0.0;
        gc.gridx = 0; gc.gridy = 0;
        gb.setConstraints(pat_msg,gc);

        gc.weightx = 1.0;
        gc.gridx = 1; gc.gridy = 0;
        gb.setConstraints(pat,gc);

        gc.weightx = 0.0;
        gc.gridx = 2; gc.gridy = 0;
        gc.fill = gc.BOTH;
        gb.setConstraints(bn,gc);

        gc.gridwidth = 3;
        gc.gridheight = 1;
        gc.weighty = 1.0;
        gc.gridx = 0; gc.gridy = 1;
        gb.setConstraints(tg,gc);
    }
    public boolean action(Event e,Object o) {
        tg.ctxt.clear();
        String p = pat.getText();
        String t = tg.txt.getText();
        if(p == null||t == null) return true;
        if(p.equals("")||t.equals("")) return true;
        Regex r = new Regex();
        try {
          r.compile(p);
	  System.out.println("compile=["+p+"]");
          r.search(t);
          tg.ShowRes(r);
        } catch(RegSyntax rs) {
          tg.ShowError(rs);
        }
        return true;
    }
}
