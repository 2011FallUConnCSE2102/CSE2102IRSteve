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
import java.net.*;

/** This is the applet for my ReGame page.*/
public class ReGame extends java.applet.Applet {
    static final String hide_ans = "????";
    /** @serial */
    Label pat_msg,ans_msg;
    /** @serial */
    public TextField pat, ans_txt;
    /** @serial */
    public TestGroup[] tgroup,tgroup2;
    /** @serial */
    public RegRes[] answers;
    /** @serial */
    int score;
    /** @serial */
    Label unreg;
    /** @serial */
    public TextField score_txt;
    /** @serial */
    public Button home_btn, redraw;
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
    void setScore(int s) {
        score = s;
        String st = "score: "+s;
        if(score_txt == null) {
            score_txt = new TextField(st);
            score_txt.setEditable(false);
        } else score_txt.setText(st);
    }
    void addScore(int s) {
        setScore(score + s);
    }
    /** @serial */
    boolean inited = false;
    public void init() {
        Panel user = new Panel();
        Panel ans  = new Panel();
        Panel header = new Panel();
        Panel body = new Panel();

        user.setBackground(Color.red);
        ans.setBackground(Color.green);
        body.setBackground(Color.blue);
        header.setBackground(Color.white);

        add(header);
        add(body);
        body.setLayout(new GridLayout(1,2));
        body.add(user);
        body.add(ans);

        pat_msg = new Label("Pattern");
        pat = new TextField();
        ans_msg = new Label("Answer");
        ans_txt = new TextField();
        ans_txt.setEditable(false);
        ans_txt.setText(hide_ans);

        // set title
        String umsg = null;
        umsg = getParameter("Title");
        if(umsg == null) umsg = "ReGame";
        unreg = new Label(umsg);

        unreg.setAlignment(unreg.CENTER);
        setScore(0);
        home_btn = new Button("About");
        redraw = new Button("Redraw");
        header.add(unreg);
        user.add(score_txt);
        score_txt.setEditable(false);
        ans.add(home_btn);
        ans.add(redraw);
        Panel p = new Panel();
        p.add(pat_msg);
        p.add(pat);
        user.add(p);
        Panel p2 = new Panel();
        p2.add(ans_msg);
        p2.add(ans_txt);
        ans.add(p2);

        // determine # of text groups
        int ngroups =
            (new Integer(getParameter("NGroups"))).intValue();
        tgroup = new TestGroup[ngroups];
        tgroup2 = new TestGroup[ngroups];
        answers = new RegRes[ngroups];
        int i;
        for(i=0;i<ngroups;i++) {
            tgroup[i] = new TestGroup("text"+(i+1),false);
            tgroup2[i] = new TestGroup("text"+(i+1),false);
            user.add(tgroup[i]);
            ans.add(tgroup2[i]);
        }

        // layout info
        GridBagLayout gb = new GridBagLayout();
        user.setLayout(gb);
        ans.setLayout(gb);
        header.setLayout(gb);
        setLayout(gb);
        GridBagConstraints gc = new GridBagConstraints();

        gc.gridy = 0; gc.gridx = 0;
        gc.gridwidth = 2; gc.gridheight = 1;
        gc.fill = gc.HORIZONTAL;
        gc.weightx = 0.0; gc.weighty = 0.0;
        gb.setConstraints(unreg,gc);

        gc.gridwidth = 1;
        gc.weightx = 1.0;
        gb.setConstraints(header,gc);

        gc.gridy = 1;
        gc.fill = gc.BOTH;
        gc.weighty = 1.0;
        gb.setConstraints(body,gc);

        gc.fill = gc.HORIZONTAL;
        gc.gridx = 0; gc.gridy = 0;
        gc.weightx = 1.0; gc.weighty = 0.0;
        gb.setConstraints(home_btn,gc);
        gc.gridx = 1;
        gb.setConstraints(redraw,gc);

        gc.gridx = 0; gc.gridy = 0;
        gc.weightx = 1.0; gc.weighty = 0.0;
        gb.setConstraints(score_txt,gc);

        gc.gridx = 0; gc.gridy=1;
        gc.gridwidth = 2; gc.weightx = 0.0;
        p.setLayout(gb);
        p2.setLayout(gb);
        gb.setConstraints(p,gc);
        gb.setConstraints(p2,gc);

        gc.weightx = 1.0; gc.weighty = 1.0;
        gc.gridx = 0; gc.gridwidth = 2;
        gc.fill = gc.BOTH; gc.gridx = 0;
        for(i=0;i<ngroups;i++) {
            gc.gridy=i+2;
            gb.setConstraints(tgroup[i],gc);
            gb.setConstraints(tgroup2[i],gc);
        }

        gc.gridx = 0; gc.gridy = 0;
        gc.gridwidth = 1;
        gc.fill = gc.HORIZONTAL;
        gc.weightx = 0.0;
        gb.setConstraints(pat_msg,gc);
        gb.setConstraints(ans_msg,gc);
        gc.gridx = 1; gc.weightx = 1.0;
        gb.setConstraints(pat,gc);
        gb.setConstraints(ans_txt,gc);
    }
    /** @serial */
    boolean started = false;
    public void start() {
        if(started) return;
        started = true;
        int nmax =
            (new Integer(getParameter("NQuizes"))).intValue();
        //bset = new BitSet(nmax);
        quizes = new Deck(nmax);
        int d;
        try {
            d = (new Integer(getParameter("NDiscards"))).intValue();
        } catch(Throwable t_) {
            d = 0;
        }
        while(d > 0) {
            d--;
            quizes.draw();
        }
        setScore(0);
        getquiz();
        game_over = false;
    }
    /** @serial */
    public Random qrand = new Random();
    /** @serial */
    int quizno = 0,p_len = 0;
    /** @serial */
    boolean game_over = false;
    /** @serial */
    public Deck quizes = null;
    Regex getquiz() {
        if(quizes.ncards() == 0) {
            score_txt.setText("Game Over: Score "+score+" of "+max_score);
            game_over = true;
            return null;
        }
        quizno = quizes.draw();

        pat.setText("");
        ans_txt.setText(hide_ans);

        String ps = unescme(getParameter("pat"+quizno));
        p_len = ps.length();
        Regex r = new Regex();
        RegSyntax rs = null;
        try {
            r.compile(ps);
        } catch(RegSyntax rst) {
            rs = rst;
        }
        //pat.setText(ps);
        for(int i=0;i<tgroup.length;i++) {
            String t = "txt"+(i+1)+"-"+quizno;
            t = unescme(getParameter(t));
            tgroup[i].txt.setText(t);
            tgroup2[i].txt.setText(t);
            if(rs == null) {
              r.search(t);
              tgroup2[i].ShowRes(r);
              tgroup[i].ctxt.clear();
              answers[i] = r.result();
            } else {
              tgroup2[i].ShowError(rs);
              tgroup[i].ctxt.clear();
              answers[i] = new RegRes();
            }
        }
        return r;
    }
    /** @serial */
    int max_score = 0;
    /** @serial */
    public Message mes = null;
    public boolean action(Event e,Object o) {
        repaint();
        if(e.target instanceof Button) {
           if(o.equals("About")) {
              Message m = new Message();
              m.setTitle("About");
              m.addCentered("ReGame");
              m.addCentered("A regular expression game");
              m.addCentered("by Steven R. Brandt");
              m.addCentered("Home page at");
              m.addCentered("http://javaregex.com");
              m.addButton(new Button("OK"));
              m.ask(this);
              return true;
           } else {
              for(int i=0;i<tgroup.length;i++)
                tgroup[i].repaint();
              for(int i=0;i<tgroup2.length;i++)
                tgroup2[i].repaint();
           }
        }
        if(e.target instanceof Message) {
            Message m = (Message)e.target;
            if(m.getTitle().equals("About")) return true;
            for(int i=0;i<tgroup.length;i++) {
                tgroup[i].ctxt.clear();
                tgroup[i].ctxt.clear();
            }
            getquiz();
            mes = null;
            return true;
        } else if(e.target instanceof TextField && mes == null) {
            if(game_over) return true;
            String p = pat.getText();
            Regex r = new Regex(p);
            int i;
            mes = new Message();
            mes.setTitle("Score");
            ans_txt.setText( unescme(getParameter("pat"+quizno)) );
            Regex ansr = new Regex(ans_txt.getText());
            for(i=0;i<tgroup.length;i++) {
                r.search(tgroup[i].txt.getText());
                ansr.search(tgroup2[i].txt.getText());
                RegRes res = r.result();
                tgroup2[i].ShowRes(ansr);
                tgroup[i].ShowRes(r);
                if(res.equals(answers[i])) {
                    String sc;
                    if(p.length() > p_len) {
                        sc = "Long Match: 5 pts";
                        addScore(5);
                    } else if(p.length() == p_len) {
                        sc = "Match: 10 pts";
                        addScore(10);
                    } else {
                        sc = "Short Match: 12 pts";
                        addScore(12);
                    }
                    mes.v.addElement(new Label(sc));
                } else {
                    System.out.println("user: "+res);
                    System.out.println("answ: "+answers[i]);
                    mes.v.addElement(new Label("No Match"));
                }
                max_score += 10;
            }
            //mes.v.addElement(new Label("answer: "+
                //unescme(getParameter("pat"+quizno))));
            mes.v.addElement(new Button("OK"));
            mes.ask(this);
            return true;
        }
        return false;
    }
}
