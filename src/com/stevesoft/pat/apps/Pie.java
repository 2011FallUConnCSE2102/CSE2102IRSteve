//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat.apps;

import java.awt.*;
import java.io.*;
import java.util.*;
import java.awt.event.*;
import com.stevesoft.pat.*;

/** This class is used internally by Pie.
    @see com.stevesoft.pat.apps.Pie
*/
class LabelField extends Panel {

  Label l = null;
  TextField tf = null;
  public void setEnabled(boolean b) {
    tf.setEnabled(b);
  }
  LabelField(String s) {
    l = new Label(s,Label.LEFT);
    tf = new TextField();

    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    setLayout(gbl);

    gbc.weighty = 1.0;
    gbc.weightx = 0.0;
    gbc.fill = GridBagConstraints.BOTH;
    gbc.gridx = 0; gbc.gridy = 0;
    add(l);
    gbl.setConstraints(l,gbc);

    add(tf);
    gbc.weightx = 1.0;
    gbc.gridx = 1;
    gbl.setConstraints(tf,gbc);
  }
}

/** For use internally by com.stevesoft.pat.apps.Pie */
class Buttons extends Panel {
  GridBagLayout gbl = new GridBagLayout();
  GridBagConstraints gbc = new GridBagConstraints();
  Buttons() {
    setLayout(gbl);
    gbc.fill = gbc.BOTH;
    gbc.gridy =  0;
    gbc.gridx = -1;
    gbc.weightx = 1.0;
    gbc.weighty = 1.0;
  }
  Vector bv = new Vector();
  void add(Button b) {
    super.add(b);
    bv.addElement(b);
    gbc.gridx++;
    gbl.setConstraints(b,gbc);
  }
  public void setEnabled(boolean b) {
    for(int i=0;i<bv.size();i++)
      ((Button)bv.elementAt(i)).setEnabled(b);
  }
}

/** The name of this utility was inspired
by the option flags given to perl to modifiy a
file in place (e.g. perl -pi -e 's/foo/bar' file).

To use this command, simply type
  java com.stevesoft.pat.apps.Pie

When the window appears, type your regular expression pattern
where it says "pattern" the text you want to replace the matched
region with where it says "replace" and the files that you want
the replacement to operate on where it says files (i.e. *.java to
get all java files).
<p>
When you are ready to go, hit the "replace" button and Pie will
look for the first instance of your pattern it can find.  When it
finds it, it will display a few of the lines of text before and after
the place where the match was along with the file name (in blue) and
the line numbers before the lines (also in blue).  The replaced region
of text will be in red, and on the line on which the replacement is to
be performed is shown immediately afterward with the replaced text in
green.
<p>
If you are happy with the replacement, hit the replace button again
and Pie will look for another match.  If you don't want that replacement
operation to occur, hit skip.  If you want all the rest of the replacements
in all the remaining files to proceed, hit "Replace All."
<p>
In any event, Pie will make a backup copy of each of
your files in a file with ".bak" appended to the name.
*/
public class Pie extends Frame {
  // This is the number of lines above and below the current
  // line to display during the replacement process.
  final static int BUFFERLINES = 2;

  public Pie(String s) { super(s); }
  /** @serial */
  ColorText view = new ColorText();
  /** @serial */
  LabelField pattern = new LabelField("Pattern");
  /** @serial */
  LabelField replace = new LabelField("Replace");
  /** @serial */
  LabelField files = new LabelField("Files");
  /** @serial */
  Buttons buttons = new Buttons();
  /** @serial */
  int count = 0;

  public void setEnabled(boolean b) {
    buttons.setEnabled(b);
    pattern.setEnabled(b);
    replace.setEnabled(b);
    files.setEnabled(b);
  }

  public static void main(String[] args) {
    Pie p = new Pie("Pie");
    p.addWindowListener(new ShutDown());
    p.packNShow();
  }

  void message(String[] lines) {
    final Dialog d = new Dialog(this,"Message",true);
    d.setLayout(new GridLayout(lines.length+1,1));
    for(int i=0;i<lines.length;i++)
      d.add(new Label(lines[i],Label.LEFT));
    Button b = new Button("OK");
    b.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        d.dispose();
      }
    });
    d.add(b);
    d.pack();
    d.show();
  }

  /** @serial */
  Regex curRegex = null;
  /** @serial */
  String[] curFiles = null;
  /** @serial */
  int curFile = 0;

  /** @serial */
  boolean replaceAll = false;
  void doit() {
    setEnabled(false);
    if(curRegex == null) {
      String patternText = pattern.tf.getText();
      String filePattern = files.tf.getText();
      String replacementText = replace.tf.getText();

      Regex r = new Regex(patternText,replacementText);
      r.optimize();
      curRegex = r;

      String[] files = NonDirFileRegex.list(filePattern);
      curFiles = files;
      curFile = 0;
      count = 0;
    }
    while(curFile<curFiles.length)
      if(dofile(curFiles[curFile],curRegex) && !replaceAll) {
        buttons.setEnabled(true);
        return;
      }
    message(new String[]{
      "Transformation Complete",
      ""+count+" substitutions in",
      "in "+curFiles.length+" files."
    });
    view.clear();
    curRegex = null;
    curFiles = null;
    setEnabled(true);
  }

  /** @serial */
  Vector contents = null;
  /** @serial */
  int index = 0, index2 = 0;
  /** @serial */
  RegRes oldres = null;
  boolean dofile(String fname,Regex r) {
    try {
      File f = new File(fname);
      File fb = new File(fname+".bak");
      if(contents == null) {
        FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);
        String s = br.readLine();
        Vector v = new Vector();
        contents = v;
        while(s != null) {
          v.addElement(s);
          s = br.readLine();
        }
        br.close();
      }

      for(int i=index;i<contents.size();i++) {
        while(index2 >= 0) {
          String s = (String)contents.elementAt(i);
          String ns = r.replaceFirstFrom(s,index2);
          if(ns.equals(s))
            index2 = -1;
          else {
            count++;
            int vmin = i-BUFFERLINES < 0 ? 0 : i-BUFFERLINES;
            int vmax = i+BUFFERLINES > contents.size()-1 ?
              contents.size()-1 : i+BUFFERLINES;
            view.clear();
            ColorLine cl = new ColorLine();
            cl.add(Color.blue,"File: "+fname);
            view.addColorLine(cl);
            for(int j=vmin;j<i;j++) {
              cl = new ColorLine();
              cl.add(Color.blue,""+(j+1)+": ");
              cl.add(Color.black,(String)contents.elementAt(j));
              view.addColorLine(cl);
            }
        
            cl = new ColorLine();
            cl.add(Color.blue,""+(i+1)+": ");
            cl.add(Color.black,r.left());
            cl.add(Color.red,r.stringMatched());
            cl.add(Color.black,r.right());
            view.addColorLine(cl);

            cl = new ColorLine();
            cl.add(Color.blue,""+(i+1)+": ");
            cl.add(Color.black,r.left());
            cl.add(Color.green,ns.substring(r.matchedFrom(),
              r.matchedTo()+ns.length()-s.length()));
            cl.add(Color.black,r.right()); 
            view.addColorLine(cl);

            for(int j=i+1;j<=vmax;j++) {
              cl = new ColorLine();
              cl.add(Color.blue,""+(j+1)+": ");
              cl.add(Color.black,(String)contents.elementAt(j));
              view.addColorLine(cl);
            }
            index = i;
            contents.setElementAt(ns,i);
            oldres = r.result();
            index2 = r.matchedTo()+ns.length()-s.length();
            if(!replaceAll) return true;
          }
        }
        index2 = 0;
      }

      fb.delete();
      f.renameTo(fb);
      FileWriter fw = new FileWriter(f);
      BufferedWriter bw = new BufferedWriter(fw);
      PrintWriter pw = new PrintWriter(bw);
      for(int i=0;i<contents.size();i++)
        pw.println((String)contents.elementAt(i));
      pw.close();
      contents = null;
      index = 0;
      curFile++;

    } catch(Exception e) {
      e.printStackTrace();
      System.exit(255);
    }
    return false;
  }

  public void packNShow() {
    GridBagLayout gbl = new GridBagLayout();
    GridBagConstraints gbc = new GridBagConstraints();
    setLayout(gbl);

    add(pattern);
    add(replace);
    add(files);
    add(buttons);
    ScrollPane sp = new ScrollPane();
    sp.add(view);
    add(sp);

    Button go = new Button("Replace");
    go.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        replaceAll = false;
        doit();
      }
    });
    buttons.add(go);

    Button rpAll = new Button("Replace All");
    rpAll.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        replaceAll = true;
        doit();
      }
    });
    buttons.add(rpAll);
    final Frame pieframe = this;

    Button sk = new Button("Skip");
    sk.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        if(oldres == null || index <= 0 || index-1>=contents.size()) {
          message(new String[]{"Inappropriate Action"});
          return;
        }
        contents.setElementAt(oldres.getString(),index);
        count--;
        doit();
      }
    });
    buttons.add(sk);

    Button alt = new Button("Alternate Text");
    alt.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        if(oldres == null || index <= 0 || index-1>=contents.size()) {
          message(new String[]{"Inappropriate Action"});
          return;
        }
        final Dialog d = new Dialog(pieframe,"Alternate Text Dialog",true);
        d.setLayout(new GridLayout(2,1));
        LabelField lf = new LabelField("Alternate Text");
        d.add(lf);
        Button b = new Button("Done");
        d.add(b);
        b.addActionListener(new ActionListener() {
          public void actionPerformed(ActionEvent ae) {
            d.dispose();
          }
        });
        d.setResizable(true);
        d.pack();
        d.show();
        String s = oldres.left()+lf.tf.getText()+oldres.right();
        contents.setElementAt(s,index);
        doit();
      }
    });
    buttons.add(alt);

    Button quit = new Button("Quit");
    quit.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        dispose();
        if(contents != null) try {
          String fname = curFiles[curFile];
          File f = new File(fname);
          File fb = new File(fname+".bak");
          fb.delete();
          f.renameTo(fb);
          FileWriter fw = new FileWriter(f);
          BufferedWriter bw = new BufferedWriter(fw);
          PrintWriter pw = new PrintWriter(bw);
          for(int i=0;i<contents.size();i++)
            pw.println((String)contents.elementAt(i));
          pw.close();
        } catch(Exception e) {
          e.printStackTrace();
          System.exit(255);
        }
        System.exit(0);
      }
    });
    buttons.add(quit);

    Button about = new Button("About");
    about.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent ae) {
        message(new String[]{
          "Pie -- a utility for transforming",
          "the text of a group of files.",
          "",
          "Written By Steven R. Brandt",
          "http://javaregex.com"});
      }
    });
    buttons.add(about);

    gbc.gridwidth = GridBagConstraints.REMAINDER;
    gbc.weightx = 1.0;
    gbc.weighty = 0.0;
    gbc.fill = GridBagConstraints.BOTH;
    gbl.setConstraints(pattern,gbc);
    gbl.setConstraints(replace,gbc);
    gbl.setConstraints(files,gbc);
    gbl.setConstraints(buttons,gbc);
    gbc.weighty = 1.0;
    gbl.setConstraints(sp,gbc);

    pack();
    show();
  }
}
