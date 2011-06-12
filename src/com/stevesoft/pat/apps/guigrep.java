//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat.apps;

import com.stevesoft.pat.*;
import java.io.*;
import java.awt.*;
import java.awt.event.*;

/** A graphical-interfaced program to search the contents of files
with path names based on the current directory. */

public class guigrep {
    static class grepFrame extends Frame implements ActionListener {
        TextField t1,t2;
        ColorText t3;
        ScrollPane p3;
        Regex spat,dpat;
        Button gobutton = new Button("Go!");
        grepFrame() {

            // A very basic menu....
            MenuBar mb = new MenuBar();
            Menu mf = new Menu("Menu");
            MenuItem exit = new MenuItem("Exit",
                new MenuShortcut('x'));
            exit.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    System.exit(0);
                }
            });
            mf.add(exit);
            mb.add(mf);
            MenuItem about = new MenuItem("About",
                new MenuShortcut('a'));
            about.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent ae) {
                    final Message m = new Message();
                    m.setTitle("About");
                    m.addCentered("guigrep");
                    m.addCentered("A file search utility");
                    m.addCentered("by Steven R. Brandt");
                    m.addCentered("Home page at");
                    m.addCentered("http://javaregex.com");
                    Button ok = new Button("OK");
                    m.addButton(ok);
                    m.packNShow();
                    ok.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent ae2) {
                            m.dispose();
                        }
                    });
                }
            });

            mf.add(about);
            setMenuBar(mb);

            // do the layout...
            GridBagLayout gb = new GridBagLayout();
            setLayout(gb);
            GridBagConstraints gc = new GridBagConstraints();

            Label srchpat = new Label("search pattern");
            gc.gridwidth = gc.gridheight = 1;
            gc.fill = gc.HORIZONTAL;
            gc.weightx = 0.0; gc.weighty = 0.0;
            gc.gridx = 0; gc.gridy = 0;
            gb.setConstraints(srchpat,gc);
            add(srchpat);

            t1 = new TextField("");
            t1.addActionListener(this);
            gc.weightx = 1.0;
            gc.gridx = 1; gc.gridy = 0;
            gb.setConstraints(t1,gc);
            add(t1);

            Label dirpat = new Label("file pattern");
            gc.gridwidth = gc.gridheight = 1;
            gc.fill = gc.HORIZONTAL;
            gc.weightx = 0.0; gc.weighty = 0.0;
            gc.gridx = 0; gc.gridy = 1;
            gb.setConstraints(dirpat,gc);
            add(dirpat);

            t2 = new TextField("");
            t2.addActionListener(this);
            gc.weightx = 1.0;
            gc.gridx = 1; gc.gridy = 1;
            gb.setConstraints(t2,gc);
            add(t2);

            gobutton.addActionListener(this);
            gc.gridx = 2; gc.gridy = 0;
            gc.gridwidth = 1;
            gc.gridheight = 2;
            gc.weightx = 0.0;
            gc.fill = gc.BOTH;
            gb.setConstraints(gobutton,gc);
            add(gobutton);

            t3 = new ColorText();
            p3 = new ScrollPane();
            gc.fill = gc.BOTH;
            gc.gridheight = 1;
            gc.gridwidth = 3;
            gc.weighty = 1.0;
            gc.weightx = 1.0;
            gc.gridx = 0; gc.gridy = 2;
            gb.setConstraints(p3,gc);
            add(p3);
            p3.add(t3);

            pack();
            setSize(500,350);
            addWindowListener(new ShutDown());
            show();
        }
        public void actionPerformed(ActionEvent ae) {
          gobutton.setEnabled(false);
          t1.setEnabled(false);
          t2.setEnabled(true);
          try {
            spat = new Regex(t1.getText());
            spat.optimize();
            String dpat = t2.getText();
            t3.clear();
            String[] files = NonDirFileRegex.list(dpat);
            for(int i=0;i<files.length;i++) try {
                System.out.println("Searching file "+files[i]);
                File d = new File(files[i]);
                FileReader ff=new FileReader(files[i]);
                BufferedReader bf=new BufferedReader(ff);
                String s = bf.readLine();
                int lno = 1;
                while(s != null) {
                    if(spat.search(s)) {
                        RegRes r = spat.result();
                        ColorLine cl=new ColorLine();
                        cl.add(Color.blue,files[i]+" "+lno+
                            ": ");
                        cl.add(Color.black,
                            r.left().replace('\t',' '));
                        cl.add(Color.red,
                            r.substring().replace('\t',' '));
                        cl.add(Color.black,
                            r.right().replace('\t',' '));
                        t3.addColorLine(cl);
                        repaint();
                    }
                    lno++;
                    s = bf.readLine();
                }
                bf.close();
            }catch(Exception t) {
                t.printStackTrace();
                System.exit(255);
            }
            p3.doLayout();
            System.out.println("Done");
          } finally {
            gobutton.setEnabled(true);
            t1.setEnabled(true);
            t2.setEnabled(true);
          }
       }
    }
    static Regex pat = null;

    public static void main(String[] unused) {
        grepFrame gf=new grepFrame();
    }
}
