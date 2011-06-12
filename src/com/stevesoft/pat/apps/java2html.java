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

/** java2html makes your code an in-color web page --
    to use it just type
    <pre>
    java com.stevesoft.pat.apps.java2html file_patterns
    </pre>
    The program creates files with ".html" appended to them
    that are suitable for viewing with web browsers.
    <p>
    The file names provided to java2html will be interpreted as
    either source or destination files depending on the name
    (anything ending in ".html" is a destination file, ".nocolor_html",
    ".nocolor_asp", ".java", ".jas" are source files).
    <p>
    Normally, java2html will not overwrite a file if the file it is
    colorizing is newer than its target file.  However, if the -f
    option is supplied java2html will not check modification time.
    <p>
    <a href="../htm/java2html.html">Click here</a> for more info.
    */
public class java2html {
    // Basic colors
    static String PrimitiveColor = "ff00dd";
    static String QuoteColor = "0000ff";
    static String CommentColor = "000088";
    static String KeywordColor = "0000aa";
    static String DocumentBackgroundColor = "ffffdd";
    static String java_lang_Color = "dd00ff";

    // Jasmine colors
    static String DirectiveColor = "aaaa00";
    static String LabelColor = "ff00dd";

    // build up a list of rules for color changing
    static Regex DQuotes = (new Regex("\"(?:\\\\.|[^\"])*\"",
       "<font color="+QuoteColor+">$&</font>"));
    static Regex SQuotes = (new Regex("'(?:\\\\.|[^'])*'",
       "<font color="+QuoteColor+">$&</font>"));

    static Regex Comment1 = (new Regex("//.*",
       "<font color="+CommentColor+">$&</font>"));
    static Regex Comment2 = (new Regex("/\\*.*?\\*/",
       "<font color="+CommentColor+">$&</font>"));
    final static boolean[] readme = new boolean[1];


    static Regex Comment3 = null;

    static Regex PrimitiveTypes = new Regex(
       "\\b(?:boolean|char|byte|short|int|long|float|double)\\b",
       "<font color="+PrimitiveColor+">$&</font>");

    static Regex Keywords = (new Regex(
        "\\b(?:abstract|break|byvalue|case|cast|catch|"+
        "class|const|continue|default|do|else|extends|"+
        "false|final|finally|for|future|generic|goto|if|"+
        "implements|import|inner|instanceof|interface|"+
        "native|new|null|operator|outer|package|private|"+
        "protected|public|rest|return|static|super|switch|"+
        "synchronized|this|throw|throws|transient|true|try|"+
        "var|volatile|while)\\b",
        "<font color="+KeywordColor+"><b>$&</b></font>"));

    static Regex java_lang = new Regex(
        "\\b(?:Boolean|Byte|Character|Class|ClassLoader|Cloneable|Compiler|"+
        "Double|Float|Integer|Long|Math|Number|Object|Process|"+
        "Runnable|Runtime|SecurityManager|Short|String|StringBuffer|"+
        "System|Thread|ThreadGroup|Void)\\b",
        "<font color="+java_lang_Color+">$&</font>");

    static Regex oper = new Regex("(?:[\\+\\*\\^\\$\\-\\{\\}\\[\\]"+
        "\\=\\.\\(\\)\\,\\:/]|&(lt|gt|amp);)",
        "<b>$&</b>");

    static Transformer colorize = new Transformer(true);
    static Replacer java_replacer = colorize.getReplacer();
    static Replacer colorizer = null;
    static Replacer html_replacer = null;
    static Replacer pretran_html = null;
    static Replacer pretran_java = null;
    static Replacer pretran = null;
    static boolean jasmine_enabled = true;

    static void init() {
        // This is a rule that can apply itself across multiple
        // lines.  The rule named "end_comment" only looks for
        // the */ sequence, and pop's itself off the rule stack
        // when it finds it.
        ReplaceRule.define("endcomment",
           new Regex("\\*/","*/${POP}</font>"));
        Comment3 = (new Regex("/\\*","<font color="+
           CommentColor+">/*${+endcomment}"));

        // Jasmine stuff
        Regex.define("JasmineEnabled","",new Validator() {
          public int validate(String src,int begin,int end) {
             return jasmine_enabled ? end : -1;
          }
        });
        colorize.add(
          "s{(??JasmineEnabled)^\\s*;\\s*&gt;&gt;.*}"+
          "{<HR><H3>$&</H3>}");
        colorize.add(
          "s{(??JasmineEnabled)(?:^|\\s)\\s*;.*}"+
          "{<font color="+CommentColor+">$&</font>}");
        colorize.add(
          "s{(??JasmineEnabled)\\b(?:catch|class|end|field|"+
          "implements|interface|limit|line|method|source|super|"+
          "throws|var|stack|locals)\\b}{<font color="+
          DirectiveColor+"><b>$&</b></font>}");
        colorize.add(
          "s{(??JasmineEnabled)^\\w+:}{<font color="+
          LabelColor+"><b>$&</b></font>}");

        // stick all replacement rules into the Transformer
        colorize.add(DQuotes);
        colorize.add(SQuotes);
        colorize.add(Comment1);
        colorize.add(Comment2);
        colorize.add(Comment3);
        colorize.add(PrimitiveTypes);
        colorize.add(Keywords);
        colorize.add(java_lang);
        colorize.add(oper);
        colorize.add(Regex.perlCode(
          "s'\\w*(Error|Exception|Throwable)\\b'<font color=red>$&</font>'"));

        ReplaceRule.define("colorize",colorize);

        ReplaceRule.define("jascode",new ReplaceRule() {
          public void apply(StringBufferLike sb,RegRes rr) {
            String s1 = rr.stringMatched(1);
            if(s1 != null && s1.equals("jas"))
               jasmine_enabled = true;
          }
        });

        Regex r = new Regex("(?i)<(java|jas)code([^>]*?)>\\s*",
            "<!-- made by java2html, "+
            "see http://javaregex.com -->"+
            "<table $2 ><tr><td bgcolor="+
            DocumentBackgroundColor+
            "><pre>${jascode}${+colorize}");
        r.optimize();

        colorize.add(new Regex("(?i)\\s*</(?:java|jas)code>",
                "</pre></td></tr></table>${POP}"));

        html_replacer = r.getReplacer();

        Transformer DoPre = new Transformer(true);
        DoPre.add("s'(?i)\\s*</(?:jav|jas)acode>'$&$POP'");
        DoPre.add("s'<'&lt;'");
        DoPre.add("s'>'&gt;'");
        DoPre.add("s'&'&amp;'");
        ReplaceRule.define("DOPRE",DoPre);
        pretran_html = new Regex("(?i)<javacode[^>]*>","$&${+DOPRE}").getReplacer();
        pretran_java = DoPre.getReplacer();
    }

    static String trline(String s) {
        return s==null ? null : pretran.replaceAll(s+"\n");
    }
    public static void doFile(String FromFile) {
        String ToFile = null;
        System.out.print("("+FromFile+") ");
        try {
            boolean source_file = true;
            if(FromFile.endsWith(".java.html")) {
              ToFile = FromFile;
              String s = FromFile.substring(0,FromFile.length()-".java.html".length());
              FromFile = s+".java";
            } else if(FromFile.endsWith(".jas.html")) {
              ToFile = FromFile;
              String s = FromFile.substring(0,FromFile.length()-".jas.html".length());
              FromFile = s+".jas";
            } else if(FromFile.endsWith(".java")) {
              ToFile = FromFile+".html";
            } else if(FromFile.endsWith(".jas")) {
              ToFile = FromFile+".html";
            } else if(FromFile.endsWith(".html")) {
              ToFile = FromFile;
              String s = FromFile.substring(0,FromFile.length()-".html".length());
              FromFile = s+".nocolor_html";
              source_file = false;
            } else if(FromFile.endsWith(".asp")) {
              ToFile = FromFile;
              String s = FromFile.substring(0,FromFile.length()-".asp".length());
              FromFile = s+".nocolor_asp";
              source_file = false;
            } else if(FromFile.endsWith(".nocolor_html")) {
              ToFile = FromFile.substring(0,FromFile.length()-".nocolor_html".length())+".html";
              source_file = false;
            } else if(FromFile.endsWith(".nocolor_asp")) {
              ToFile = FromFile.substring(0,FromFile.length()-".nocolor_asp".length())+".asp";
              source_file = false;
            } else {
              System.out.println("Don't know what to do with "+FromFile);
            }
            // If someone modified to the ToFile by mistake, don't
            // wipe out their work.  Just print a message and skip.
            if(!force && (new File(FromFile)).lastModified() <
               (new File(ToFile)).lastModified()) {
               System.out.println(ToFile+" is newer than "+FromFile+
                 ".  Skipping.");
               return;
            }

            System.out.println("Translating "+
              (source_file ? "source file" : "html file")+
              " "+FromFile+" -> "+ToFile);
            if(!(new File(FromFile)).exists()) {
              System.out.println("  "+FromFile+" does not exist.  Skipping.");
              return;
            }

            // open files...
            FileReader fr = new FileReader(FromFile);
            BufferedReader br = new BufferedReader(fr);

            boolean code_flag;
            if(source_file) {
                jasmine_enabled = FromFile.endsWith(".jas");
                colorizer = (Replacer)java_replacer.clone();
                code_flag = false;
                pretran = (Replacer)pretran_java.clone();
            } else {
                jasmine_enabled = false;
                colorizer = (Replacer)html_replacer.clone();
                code_flag = true;
                pretran = (Replacer)pretran_html.clone();
            }
            FileWriter fw = new FileWriter(ToFile);
            BufferedWriter bw = new BufferedWriter(fw);
            PrintWriter pw = new PrintWriter(bw);

            // print header
            if(!code_flag) {
              pw.println("<html>");
              pw.println("<head><title>"+FromFile+"</title></head>");
              pw.println("<body bgcolor="+DocumentBackgroundColor+">");
              pw.println("<!-- Made by java2html -->");
              pw.println("<!-- See http://javaregex.com -->");
              pw.println("<pre>");
            }

            File f = new File(FromFile);
            if(!code_flag) {
              pw.println("<font color="+CommentColor+
                ">// Uncolored, plain source file:  <a href="+f.getName()+
                ">"+f.getName()+"</a></font>");
            }

            String s = null;

            s = trline(br.readLine());

            // main loop, do all colorizing and replacing
            while(s != null) {
                // this does everything...
                pw.print(colorizer.replaceAll(s));
                s = trline(br.readLine());
            }

            if(!code_flag) pw.println("</pre></body></html>");
            pw.close();
            br.close();

        } catch(Exception e) {
            e.printStackTrace();
            System.exit(255);
        }
    }

    static boolean force = false;
    public static void main(String[] args) {
        init();
        for(int i=0;i<args.length;i++) {
            if(args[i].equals("-f")) {
              force = true;
              continue;
            }
            String[] files = NonDirFileRegex.list(args[i]);
            if(files != null)
              for(int k=0;k<files.length;k++)
                doFile(files[k]);
        }
    }
}
