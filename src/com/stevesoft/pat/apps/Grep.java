package//
// This software is now distributed according to
// the Lesser Gnu Public License.  Please see
// http://www.gnu.org/copyleft/lesser.txt for
// the details.
//    -- Happy Computing!
//
com.stevesoft.pat.apps;
// Grep.java, Copyright 2001 by Steven R Brandt
// All rights reserved

import com.stevesoft.pat.*;
import java.io.*;
import java.util.*;

/** This provides the functionality of the UNIX utility grep.
    Options:
    <ol>
    <li> -i : ignore case
    <li> -p : paragraph based matching
    <li> -v : invert, print only lines that don't match
    </ol>
*/
public class Grep {

    boolean iflag=false, pflag=false,vflag=false,verbose=false;
    Regex re=null;
    Vector v=new Vector();
    public static void main(String[] args) throws Exception {
        Grep g = new Grep();
        g.doArgs(args);
    }

    // -- BEGIN OPTIONS -- //

    /** The ignore case flag */
    public boolean getIFlag() {
      return iflag;
    }
    /** The ignore case flag */
    public void setIFlag(boolean b) {
      iflag = b;
    }
    /** The paragraph mode flag */
    public boolean getPFlag() {
      return pflag;
    }
    /** The paragraph mode flag */
    public void setPFlag(boolean b) {
      pflag = b;
    }
    /** If the vflag is true, then only lines <em>not</em>
        matching the supplied pattern will be printed. */
    public boolean getVFlag() {
      return vflag;
    }
    /** If the vflag is true, then only lines <em>not</em>
        matching the supplied pattern will be printed. */
    public void setVFlag(boolean b) {
      vflag = b;
    }
    /** Determine if file and line number info is written. */
    public boolean getVerbose() {
      return verbose;
    }
    /** Determine if file and line number info is written. */
    public void setVerbose(boolean b) {
      verbose = b;
    }
    /** The pattern to be searched for */
    public void setRegex(Regex r) {
      re = r;
    }
    /** The pattern to be searched for */
    public Regex getRegex() {
      return re;
    }
    int lineno=0;
    /** Line number info reported by verbose */
    void setLineno(int n) {
        lineno=n;
    }
    /** Line number info reported by verbose */
    int getLineno() {
        return lineno;
    }
    /** Line number info reported by verbose */
    void incLineno() {
        lineno++;
    }
    String _file = "";
    /** File name info reported by verbose */
    String getFile() { return _file; }
    /** File name info reported by verbose */
    void setFile(String s) { _file=s; }

    // -- END OPTIONS -- //

    void doArgs(String[] args) throws Exception {
        // Process command line arguments
        for(int i=0;i<args.length;i++) {
            if(args[i].charAt(0)=='-') {
                for(int j=1;j<args[i].length();j++) {
                    char c = args[i].charAt(j);
                    switch(c) {
                    case 'i':
                        iflag = true;
                        break;
                    case 'p':
                        pflag = true;
                        break;
                    case 'v':
                        vflag = true;
                        break;
                    default:
                        throw new Error("Unknown flag: "+c);
                    }
                }

                // After flags comes the Regex
            } else if(re==null) {
                Regex r = new Regex( (iflag ? "(?i)" : "")+args[i]);
                r.optimize();
                setRegex(r);

                // After the Regex comes the file list
            } else {
                String[] files = FileRegex.list(args[i]);
                for(int j=0;j<files.length;j++)
                    v.addElement(files[j]);
            }
        }

        // Need to see file and line-number
        if(v.size()>1)
            setVerbose(true);

        // Process files
        if(v.size()==0)
            doInputStream(System.in);
        for(int i=0;i<v.size();i++)
            doFile((String)v.elementAt(i));
    }

    void doInputStream(InputStream is) throws Exception {
        InputStreamReader r = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(r);
        setFile("STDIN");
        setLineno(0);

        PrintWriter stdout = new PrintWriter(
          new OutputStreamWriter( System.out ));
        process(br,stdout);
        stdout.close();
    }

    void doFile(String file) throws Exception {
        // Prepare IO objects
        FileReader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);
        PrintWriter stdout = new PrintWriter(
          new OutputStreamWriter( System.out ));

        // Prepare Grep object. iflag, pflag, and vflag should
        // already be set or unset as appropriate.
        setFile(file);
        setLineno(0);

        process(br,stdout);
        br.close();
        stdout.close();
    }


    void process(BufferedReader br,PrintWriter pw) throws Exception {
        String s = nextLine(br);
        while(s != null) {
            if(re.search(s)^vflag) {
                if(verbose)
                    pw.print(getFile()+" "+getLineno()+": ");
                pw.println(s);
            }
            s = nextLine(br);
        }
    }

    String nextLine(BufferedReader br) throws Exception {
        if(pflag) {
            StringBuffer sb = new StringBuffer();
            String s=null;
            while(s != null && !s.trim().equals("")) {
                sb.append(s);
                s = br.readLine();
                incLineno();
            }
            return sb.toString();
        } else {
            String s = br.readLine();
            incLineno();
            return s;
        }
    }
}
