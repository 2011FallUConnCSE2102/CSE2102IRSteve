//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
package com.stevesoft.pat.apps;
import com.stevesoft.pat.*;
import java.applet.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.net.*;
import java.io.*;

/** This is the thing used to design the parameters for a regular
expression game.  To use it, type
<pre>
java com.stevesoft.pat.apps.ReGameDes file.html
</pre>
At your command prompt.  ReGameDes will find the applet tag, and
read and edit the fields inside it.  If you do not supply a file
name, it will attempt to load a file named "ReGame.html" if there is
one.  If there is not, you can either select "New" or "Load" from
the file menu to give the designer a file to edit.
<p>
Creating a new game:
By default, ReGameDes makes a game with three text fields,
but this can be set to a different
number such as two, four, or ten (if you are really tough). You
are prompted for this number when you create a new file.
<p>
NQuizes: This numeric field is displayed at the top of your editor.
It tells you the number of pattern/text sets you have in the current
file.
<p>
NDiscards: There is a numeric value, displayed at the top of the
editor called NDiscards.  This is the number of questions that
are randomly discarded when the game starts up.  Thus, if NQuizes
is 10 and NDiscards is 5 then a player will be asked 5 questions
in the course of a game.  NDiscards can be incremented or decremented
through the options menu.
<p>
quizno: This numeric field, with values 0 to NQuizes, tells you the
number of the current quiz.
*/
public class ReGameDes extends Frame {
    /** @serial */
    final Frame for_dialog;
    /** @serial */
    Label nlabel = new Label();
    /** @serial */
    TestGroup[] tg = null;
    /** @serial */
    TextField InputPattern = null;
    /** @serial */
    GridBagLayout gb = new GridBagLayout();
    /** @serial */
    GridBagConstraints gc = new GridBagConstraints();
    /** @serial */
    int quizno = 0;
    /** @serial */
    Hashtable QuizDataTable = new Hashtable();
    /** @serial */
    String CurrentFile = null;
    /** @serial */
    Menu GotoQuestionMenu = null;
    /** @serial */
    boolean modified = false;

    // This routine sets the pattern menu,
    // it allows us to go directly to a previously
    // created pattern.
    void SetGotoQuestionMenu() {
        Menu m = GotoQuestionMenu;
        while(GotoQuestionMenu.getItemCount() > 0)
            GotoQuestionMenu.remove(0);

        int i;
        MenuItem mui = null;
        for(i=0;QuizDataTable.get("pat"+i) != null;i++) {
            m.add(mui = new MenuItem((String)(
                QuizDataTable.get("pat"+i))));

            class MenuNum implements ActionListener {
                int num;
                MenuNum(int n) { num = n; }
                public void actionPerformed(ActionEvent ae) {
                    UpdateCurrentQuestion();
                    quizno=num;
                    LoadQuestion();
                    setnLabel();
                }
            }
            mui.addActionListener(new MenuNum(i));

            if(i % 10==9) {
                Menu nm = new Menu("More");
                m.add(nm);
                m = nm;
            }
        }
    }
    void LoadQuestion() {
        try {
            InputPattern.setText((String)QuizDataTable.get("pat"+quizno));
            int i;
            for(i=0;i<tg.length;i++)
                tg[i].txt.setText((String)QuizDataTable.get("txt"+(i+1)+
                "-"+quizno));
            RunRegexOnTxtFields();
        } catch(Throwable t_) {}
    }
    void UpdateCurrentQuestion() {
        String inText = InputPattern.getText();
        if(inText == null || inText.equals("")) return;
        String patname = "pat"+quizno;
        String oldpat = (String)QuizDataTable.get(patname);
        if(oldpat == null
            || !oldpat.equals(InputPattern.getText())) modified = true;
        QuizDataTable.put(patname,InputPattern.getText());
        int i;
        for(i=0;i<tg.length;i++) {
            String txtname = "txt"+(i+1)+"-"+quizno;
            String oldtxt = (String)QuizDataTable.get(txtname);
            if(oldtxt == null
                || !oldtxt.equals(tg[i].txt.getText())) modified = true;
            QuizDataTable.put(txtname,tg[i].txt.getText());
        }
        if(modified) SetGotoQuestionMenu();
    }
    void setnLabel() {
        String nds = (String)QuizDataTable.get("NDiscards");
        String nqs = (String)QuizDataTable.get("NQuizes");
        int ndi = 0, nqi = 0;
        if(nds != null) try {
            ndi = (new Integer(nds)).intValue();
        } catch(Throwable ndt) {}
        if(nqs != null) try {
            nqi = (new Integer(nqs)).intValue();
        } catch(Throwable nqt) {}
        ndi = nqi-1 < ndi ? nqi-1 : ndi;
        ndi = ndi >= 0 ? ndi : 0;
        nlabel.setText("NQuizes="+nqi+", NDiscards="+ndi+
            ", quizno="+quizno);
    }
    void addc(Component c) {
        gb.setConstraints(c,gc);
        add(c);
    }
    public ReGameDes(String f) {
        for_dialog = this;

        CurrentFile = f;
        ReadAndProcessFile(f);

        setTitle("ReGame Designer");

        Object o = QuizDataTable.get("NGroups");
        int n = 3;
        if(o != null) try {
            n = (new Integer((String)o)).intValue();
        } catch(Throwable _t) {
            System.out.println("Badly formatted NGroups...");
        }

        tg = new TestGroup[n];
        setLayout(gb);

        // attach menubar
        MenuItem mui = null;
        MenuBar mb = new MenuBar();
        setMenuBar(mb);
        Menu m = new Menu("File");
        Menu pa = new Menu("Patterns");
        Menu op = new Menu("Options");
        GotoQuestionMenu = pa;
        mb.add(m);
        mb.add(pa);
        mb.add(op);
        pa.add(mui=new MenuItem("New Pattern",new MenuShortcut('W')));
        mui.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                UpdateCurrentQuestion();
                AddNewQuestion();
            }
        });

        pa.add(mui=new MenuItem("Next Pattern",new MenuShortcut('N')));
        mui.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                UpdateCurrentQuestion();
                quizno++;
                if(QuizDataTable.get("pat"+quizno)==null)
                    quizno--;
                else LoadQuestion();
                setnLabel();
            }
        });

        pa.add(mui=new MenuItem("Prev Pattern",new MenuShortcut('P')));
        mui.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                UpdateCurrentQuestion();
                quizno--;
                if(quizno < 0) quizno = 0;
                LoadQuestion();
                setnLabel();
            }
        });

        pa.add(mui=new MenuItem("Delete Pattern",new MenuShortcut('D')));
        mui.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                int nn=1;
                int i=quizno;
                while(QuizDataTable.get("pat"+i) != null) {
                    nn=i;
                    i++;
                }
                QuizDataTable.put("NQuizes",""+nn);
                if(nn <= 1) return;

                int d = 0;
                Object o2 = QuizDataTable.get("NDiscards");
                if(o2 != null) try {
                    d = (new Integer((String)o2)).intValue();
                } catch(Throwable t__) {}
                d = d > nn-1 ? nn-1 : d;
                QuizDataTable.put("NDiscards",""+d);
                setnLabel();

                if(nn != quizno) {
                    QuizDataTable.put("pat"+quizno,
                        QuizDataTable.get("pat"+nn));
                    for(i=0;i<tg.length;i++)
                        QuizDataTable.put("txt"+(i+1)+"-"+quizno,
                        QuizDataTable.get("txt"+(i+1)+"-"+nn));
                }
                QuizDataTable.remove("pat"+nn);
                for(i=0;i<tg.length;i++)
                    QuizDataTable.remove("txt"+(i+1)+"-"+nn);
                if(nn == quizno)
                    quizno--;
                LoadQuestion();
            }
        });

        pa.add(GotoQuestionMenu = new Menu("List Pattern"));

        op.add(mui=new MenuItem("Increment NDiscards",
            new MenuShortcut('I')));
        mui.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String ns = (String)QuizDataTable.get("NDiscards");
                int nn = 0;
                if(ns != null) try {
                    nn = (new Integer(ns)).intValue();
                } catch(Throwable _t) {}
                nn++;
                QuizDataTable.put("NDiscards",""+nn);
                setnLabel();
            }
        });

        op.add(mui=new MenuItem("Decrement NDiscards",
            new MenuShortcut('D')));
        mui.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String ns = (String)QuizDataTable.get("NDiscards");
                int nn = 0;
                if(ns != null) try {
                    nn = (new Integer(ns)).intValue();
                } catch(Throwable _t) {}
                nn--;
                if(nn<0) nn = 0;
                QuizDataTable.put("NDiscards",""+nn);
                setnLabel();
            }
        });

        m.add(mui=new MenuItem("New",new MenuShortcut('W')));
        mui.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                newfile(null);
            }
        });

        m.add(mui=new MenuItem("Load",new MenuShortcut('L')));
        mui.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if(!outfile()) return;
                FileDialog fd = new FileDialog(for_dialog,"Load",
                    FileDialog.LOAD);
                Regex html = new Regex("\\.html?$");
                fd.setFile("*.html");
                fd.setFilenameFilter(html);
                fd.show();
                String fstr = fd.getDirectory()+File.separator+
                    fd.getFile();
                //(new ReGameDes(fstr)).SizeAndShow();
                //dispose();
                if(!(new File(fstr)).exists()) {
                    System.out.println("No such file: "+fstr);
                    return;
                } else System.out.println("Loading: "+fstr);
                ReadAndProcessFile(fstr);
                System.out.println("File Loaded: "+fstr);
                SetGotoQuestionMenu();
                quizno = 0;
                LoadQuestion();
                modified = false;
            }
        });

        m.add(mui=new MenuItem("Save",new MenuShortcut('S')));
        mui.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                savefile();
            }
        });

        m.add(mui=new MenuItem("Save As...",
            new MenuShortcut('A')));
        mui.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                FileDialog fd = new FileDialog(for_dialog,"Save",
                    FileDialog.SAVE);
                Regex html = new Regex("\\.html?$");
                fd.setFile("*.html");
                fd.setFilenameFilter(html);
                fd.show();
                String NewFile = fd.getDirectory()+File.separator+
                    fd.getFile();
                File from = new File(CurrentFile);
                File to = new File(NewFile);
                if(!to.exists()) newfile(NewFile);
                copy(from,to);
                CurrentFile = NewFile;
                savefile();
            }
        });

        m.add(mui=new MenuItem("Quit",new MenuShortcut('Q')));
        mui.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                if(outfile()) System.exit(0);
            }
        });

        gc.gridx = 0; gc.gridy = 1;
        gc.fill = gc.NONE;
        addc(new Label("pattern"));

        gc.gridx = 1; gc.gridy = 1;
        gc.fill = gc.HORIZONTAL;
        gc.weightx = 1.0;
        InputPattern = new TextField();
        addc(InputPattern);

        gc.gridx = 0; gc.gridwidth = 2;
        gc.gridy = 0;
        setnLabel();
        addc(nlabel);

        gc.fill = gc.BOTH; gc.weighty = 1.0;
        int i;
        for(i=0;i<n;i++) {
            gc.gridy = i+2;
            addc(tg[i] = new TestGroup("txt"+(i+1),true));
        }

        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                try {
                    int ii;
                    String s = ae.paramString();
                    UpdateCurrentQuestion();
                    for(ii=0;QuizDataTable.get("pat"+ii) != null;ii++)
                        if(s.equals(QuizDataTable.get("pat"+ii))) {
                        quizno = ii;
                        LoadQuestion();
                        return;
                    }
                    RunRegexOnTxtFields();
                } catch(Exception ee) {
                    ee.printStackTrace();
                    System.exit(255);
                }
            }
        };
        InputPattern.addActionListener(al);
        for(i=0;i<tg.length;i++)
            tg[i].txt.addActionListener(al);

        LoadQuestion();
        SetGotoQuestionMenu();
    }
    public static void main(String[] args) {
        System.out.println(Regex.version());
        if(Regex.version().indexOf("shareware") >= 0) {
          System.out.println("ReGameDes does not work ");
          System.out.println("with the unregistered version.");
          System.out.println("If you are using a registered ");
          System.out.println("user and are seeing this message, ");
          System.out.println("please invoke this command as follows: ");
          System.out.println("java com.stevesoft.pat.apps.ReGameDes YourKey");
          System.exit(255);
        }
        ReGameDes g=null;
        if(args.length < 2)
            g = new ReGameDes("ReGame.html");
        else g = new ReGameDes(args[1]);
        int n=0;
        try {
            n = (new Integer((String)g.QuizDataTable.get("NQuizes")))
                .intValue();
        } catch(Throwable t__) {}
        if(n==0) g.AddNewQuestion();
        g.SizeAndShow(); 
    }
    public void SizeAndShow() {
        pack();
        Dimension d=getPreferredSize();
        setSize(d.width+100,d.height+200);
        modified = false;
        show();
    }

    void AddNewQuestion() {
        while(QuizDataTable.get("pat"+quizno) != null)
            quizno++;
        InputPattern.setText("");
        int i;
        for(i=0;i<tg.length;i++) {
            tg[i].txt.setText("");
            tg[i].ctxt.clear();
        }
        QuizDataTable.put("NQuizes",""+(quizno+1));
        setnLabel();
    }
    /** Not serializable.
        @serial */
    Regex begin_app = new Regex("(?i)<\\s*applet");
    void read_to_applet(BufferedReader b,PrintWriter w) {
        try {
            for(String s=b.readLine();s != null;s=b.readLine()) {
                if(s != null && begin_app.search(s)) {
                    while(s.indexOf(">") < 0)
                        s = s+" "+b.readLine();
                    if(w != null) w.println(s);
                    if(s.indexOf("ReGame.class")>=0) return;
                } else if(w != null) w.println(s);
            }
        } catch(Throwable t) { t.printStackTrace(); }
    }
    /** Not serial.
        @serial */
    Regex end_app = new Regex("(?i)<\\s*/\\s*applet\\s*>");

    public void ReadAndProcessFile(String ifile) {
        try {
            setTitle("ReGameDes: "+(new File(ifile)).getName());
            QuizDataTable = new Hashtable();
            FileReader in = new FileReader(ifile);
            CurrentFile = ifile;
            BufferedReader din = new BufferedReader(in);
            String quote = "\"(.*?[^\"\\\\])\"";
            Regex r = new Regex("(?i)name\\s*=\\s*"+quote+"\\s*"+
                "value\\s*=\\s*"+quote);
            String s = din.readLine();
            read_to_applet(din,null);
            for(;s != null;s = din.readLine())
                if(s != null && r.search(s)) {
                String nm = unescme(r.stringMatched(1));
                String va = unescme(r.stringMatched(2));
                QuizDataTable.put(nm,va);
            } else if(s != null && end_app.search(s))
            break;
            din.close();
            modified = false;
        } catch(Throwable t) {}
    }
    void copy(File from,File to) {
        try {
            FileReader ffrom=new FileReader(from);
            FileWriter ffto = new FileWriter(to);
            PrintWriter pout = new PrintWriter(ffto);
            BufferedReader din = new BufferedReader(ffrom);
            String s = din.readLine();
            for(;s != null;s = din.readLine())
                pout.println(s);
            pout.close();
            din.close();
        } catch(Throwable t_) {
            t_.printStackTrace();
        }
    }
    boolean outfile() {
        UpdateCurrentQuestion();
        if(!modified) return true;
        final Dialog d = new Dialog(this,true);
        d.setLayout(new GridLayout(4,1));
        d.setTitle("Abandon");
        d.add(new Label("You have unsaved"));
        d.add(new Label("changes, do you really"));
        d.add(new Label("want to abandon them?"));
        Panel p = new Panel();
        d.add(p);
        p.setLayout(new GridLayout(1,2));
        final boolean[] b = new boolean[1];
        b[0] = false;
        Button yes = new Button("Yes"),no = new Button("No");
        yes.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                b[0] = true;
                d.dispose();
            }
        });
        no.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                b[0] = false;
                d.dispose();
            }
        });
        p.add(yes);
        p.add(no);
        d.pack();
        Dimension dn=
            d.getLayout().preferredLayoutSize(d);
        d.setSize(dn);
        d.show();
        return b[0];
    }
    boolean newfile(String nfile) {
        if(!outfile()) return true;
        TextField tf = null;
        Choice c = null;
        try {
            if(nfile == null) {
                File f = null;
                int num=0;
                for(num=0;true;num++) {
                    File ff = new File("ReGame"+num+".html");
                    if(!ff.exists())
                        break;
                }
                do {
                    final Dialog d = new Dialog(this,true);
                    d.setLayout(new GridLayout(3,2));
                    d.setTitle("Creating new file...");
                    d.add(new Label("File Name"));
                    tf = new TextField("ReGame"+num+".html");
                    d.add(tf);
                    d.add(new Label("Number of Text Fields"));
                    c = new Choice();
                    for(int i=2;i<=10;i++)
                        c.addItem(""+i);
                    d.add(c);
                    c.select(1);
                    Button b = new Button("Done");
                    b.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent ae) {
                            d.dispose();
                        }
                    });
                    final boolean[] breakout = new boolean[1];
                    breakout[0] = false;
                    Button b2 = new Button("Cancel");
                    b2.addActionListener(new ActionListener() {
                        public void actionPerformed(ActionEvent ae) {
                            breakout[0] = true;
                            d.dispose();
                        }
                    });
                    d.add(b);
                    d.add(b2);
                    d.pack();
                    Dimension dn=
                        d.getLayout().preferredLayoutSize(d);
                    d.setSize(dn);
                    d.show();
                    if(breakout[0]) return true;
                    f = new File(tf.getText());
                } while(f.exists());
            }
            String fstr = nfile == null ? tf.getText() : nfile;

            FileWriter fout = new FileWriter(fstr);
            BufferedWriter bo=new BufferedWriter(fout);
            PrintWriter pout = new PrintWriter(bo);
            pout.println("<html><body>");
            pout.println("<applet "+
                "code=\"com.stevesoft.pat.apps.ReGame.class\""+
                " width=410 height=450>");
            pout.println("<param name=\"NGroups\" value=\""+
                (c==null ? tg.length : c.getSelectedIndex()+2)+"\">");
            pout.println("</applet>");
            pout.println("</body></html>");
            pout.close();
            ReadAndProcessFile(fstr);
            AddNewQuestion();
            SetGotoQuestionMenu();
            quizno = 0;
            LoadQuestion();
            modified = false;
        } catch(IOException ioe) {
            System.err.println("Error creating new File");
            ioe.printStackTrace();
        }
        return true;
    }
    void savefile() {
        try {
            System.out.println("Saving as "+CurrentFile);
            File fbak = new File(CurrentFile+".bak");
            File ff = new File(CurrentFile);
            fbak.delete();
            copy(ff,fbak);
            FileReader fi = new FileReader(fbak);
            BufferedReader din = new BufferedReader(fi);
            FileWriter fout = new FileWriter(CurrentFile);
            PrintWriter fo = new PrintWriter(fout);
            String s = null;
            boolean pr = true;

            read_to_applet(din,fo);

            Enumeration e = QuizDataTable.keys();
            while(e.hasMoreElements()) {
                String nm = (String)e.nextElement();
                String val = (String)QuizDataTable.get(nm);
                nm = escme(nm);
                val = escme(val);
                fo.println("<param name=\""+nm+"\" value=\""+
                    val+"\">");
            }
            s = din.readLine();
            while(s!=null) {
                s=din.readLine();
                if(s != null && end_app.search(s)) break;
            }
            while(s!=null) {
                fo.println(s);
                s=din.readLine();
            }
            fo.close();
            din.close();
            modified = false;
            System.out.println("Write complete.");
        } catch(Throwable t_) { t_.printStackTrace(); }
    }
    String unescme(String in) {
        StringBuffer sb = new StringBuffer();
        int i;
        for(i=0;i<in.length();i++) {
            if(in.charAt(i)=='_') {
                char c = in.charAt(++i);
                if(c == 'q') sb.append('"');
                else if(c == 'a') sb.append('&');
                else if(c == 'l') sb.append('<');
                else if(c == 'r') sb.append('>');
                else if(c == 'b') sb.append('\\');
                else sb.append('_');
            } else sb.append(in.charAt(i));
        }
        return sb.toString();
    }
    String escme(String in) {
        StringBuffer sb = new StringBuffer();
        int i;
        for(i=0;i<in.length();i++) {
            if(in.charAt(i)=='"')
                sb.append("_q");
            else if(in.charAt(i)=='_')
                sb.append("__");
            else if(in.charAt(i)=='<')
                sb.append("_l");
            else if(in.charAt(i)=='>')
                sb.append("_r");
            else if(in.charAt(i)=='&')
                sb.append("_a");
            else if(in.charAt(i)=='\\')
                sb.append("_b");
            else
                sb.append(in.charAt(i));
        }
        return sb.toString();
    }

    void RunRegexOnTxtFields() {
        String pat = InputPattern.getText();
        if(pat == null) return;
        Regex r = new Regex();
        try {
            r.compile(pat);
        } catch(RegSyntax rs) {
            rs.printStackTrace();
            return;
        } catch(Exception e) {
            e.printStackTrace();
            return;
        }
        int i;
        for(i=0;i<tg.length;i++) {
            tg[i].ctxt.clear();
            if(tg[i].txt.getText() != null)
                r.search(tg[i].txt.getText());
            tg[i].ShowRes(r);
        }
    }
}
