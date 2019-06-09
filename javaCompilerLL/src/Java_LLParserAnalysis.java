import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Java_LLParserAnalysis
{
    static int num = 1;
    static HashMap<String,ArrayList<String>> expMap = new HashMap<>();
    static HashMap<String,HashSet<String>> firstMap = new HashMap<>();
    static HashMap<String,HashSet<String>> firstMap2 = new HashMap<>();
    static HashMap<String,HashSet<String>> followMap = new HashMap<>();

    static HashMap<String,Integer> expNumberMap = new HashMap<>();
    static HashMap<Integer,String> NumberExp = new HashMap<>();
    static HashMap<String,Integer> tableMap = new HashMap<>();

    static HashSet<String> nonTerminalSet = new HashSet<>();
    static HashSet<String> terminalSet = new HashSet<>();

    static Stack<Pair> stack = new Stack<>();
    static Queue<Pair> output = new LinkedList<>();

    private static StringBuffer prog = new StringBuffer();
    static ArrayList<XX> zz = new ArrayList<>();
    static Pair wrong;

    static class XX{
        int hang;
        String s;

        public XX(int hang, String s) {
            this.hang = hang;
            this.s = s;
        }
    }

    static class Pair{
        int n;
        String s;
        Pair(int n,String s){
            this.n = n;
            this.s = s;
        }
    }


    static String wenfa = "S -> program\n" +
            "program -> compoundstmt\n" +
            "stmt ->  ifstmt  |  whilestmt  |  assgstmt  |  compoundstmt\n" +
            "compoundstmt ->  { stmts }\n" +
            "stmts ->  stmt stmts   |   E\n" +
            "ifstmt ->  if ( boolexpr ) then stmt else stmt\n" +
            "whilestmt ->  while ( boolexpr ) stmt\n" +
            "assgstmt ->  ID = arithexpr ;\n" +
            "boolexpr  ->  arithexpr boolop arithexpr\n" +
            "boolop ->   <  |  >  |  <=  |  >=  | ==\n" +
            "arithexpr  ->  multexpr arithexprprime\n" +
            "arithexprprime ->  + multexpr arithexprprime  |  - multexpr arithexprprime  |   E\n" +
            "multexpr ->  simpleexpr  multexprprime\n" +
            "multexprprime ->  * simpleexpr multexprprime  |  / simpleexpr multexprprime  |   E\n" +
            "simpleexpr ->  ID  |  NUM  |  ( arithexpr )";

    /**
     *  this method is to read the standard input
     */
    private static void read_prog()
    {
        Scanner sc = new Scanner(System.in);
        while(sc.hasNextLine())
        {
            prog.append(sc.nextLine());
            prog.append("!");
        }
    }


    // add your method here!!

    private static void readIn(String in){
        String reg="-> | [|]";
        String[] a = in.split(reg);
        ArrayList<String> aList = new ArrayList<>();
        for(int i = 1; i < a.length; i++) {
            aList.add(a[i].trim());
            for(String aa : a[i].trim().split(" "))
                if(!aa.equals(""))
                    terminalSet.add(aa.trim());
            expNumberMap.put(a[0].trim()+"->"+a[i].trim(), num);
            NumberExp.put(num++,a[0].trim()+"->"+a[i].trim());
        }
        expMap.put(a[0].trim(), aList);
        nonTerminalSet.add(a[0].trim());
        terminalSet.add(a[0].trim());
    }

    private static void runFirstList(String exp){
        if (firstMap.containsKey(exp))
            return;
        HashSet<String> list = new HashSet<>();
        if (terminalSet.contains(exp)) {
            list.add(exp);
            firstMap.put(exp, list);
            return;
        }
        for (String s : expMap.get(exp)) {
            if ("E".equals(exp)) {
                list.add("E");
            } else {
                for (String temp : s.split(" ")) {
                    if (!firstMap.containsKey(temp))
                        runFirstList(temp);
                    list.addAll(firstMap.get(temp));
                    if (!firstMap.get(temp).contains("E")) {
                        list.remove("E");
                        break;
                    }
                }
            }
        }
        firstMap.put(exp, list);
    }

    private static HashSet<String> getFirst(String ss){
        if(ss.equals("") || ss.equals(" ") )
            return null;
        HashSet<String> set = new HashSet<>();
        if (firstMap2.containsKey(ss))
            return new HashSet<String>(firstMap2.get(ss));
        String[] aa = ss.split(" ");
        for(int i = 0; i < aa.length; i++){
            if(!aa[i].equals("")){
                if (!firstMap.containsKey(aa[i]))
                    runFirstList(aa[i]);
                HashSet<String> temp = firstMap.get(aa[i]);
                set.addAll(temp);
                if (temp.contains("E")) {
                    i++;
                }
                else
                    break;
                if (i == aa.length) {
                    set.add("E");
                }
            }
        }
        firstMap2.put(ss,set);
        return set;
    }

    private static void runFollowList1(String exp){
        HashSet<String> followBegin = followMap.containsKey(exp) ? followMap.get(exp) : new HashSet<>();
        for (String ss : nonTerminalSet) {
            for (String s : expMap.get(ss)) {
                String[] e = s.split(" ");
                for (int i = 0; i < e.length - 1; i++) {
                    if(exp.equals(e[i])){
                        StringBuilder temp = new StringBuilder();
                        for(int j=i+1;j<e.length;j++)
                            temp.append(e[j]+" ");
                        HashSet<String> tt = getFirst(temp.toString().trim());
                        if(tt!=null&&!tt.contains("E"))
                            followBegin.addAll(tt);
                    }
                }
            }
        }
        followMap.put(exp, followBegin);
    }

    private static void runFollowList2(String exp){
        //int change = 0;
        ArrayList<String> exps = expMap.get(exp);
        for(String s : exps){
            String[] ss = s.split(" ");
            for(int i=ss.length-1;i>=0;i--){
                if(nonTerminalSet.contains(ss[i])&& i < ss.length - 1){
                    HashSet<String> follow = new HashSet<>();
                    StringBuilder temp = new StringBuilder();
                    for(int j=i+1;j<ss.length;j++)
                        temp.append(ss[j]+" ");
                    HashSet<String> first = getFirst(temp.toString().trim());
                    if(first.contains("E")){
                        first.remove("E");
                        follow.addAll(first);
                        follow.addAll(followMap.get(exp));
                        follow.addAll(followMap.get(ss[i]));
                        followMap.put(ss[i],follow);
                    }
                }
                else if(nonTerminalSet.contains(ss[i])&& i == ss.length - 1){
                    HashSet<String> follow = new HashSet<>();
                    follow.addAll(followMap.get(exp));
                    follow.addAll(followMap.get(ss[i]));
                    followMap.put(ss[i],follow);
                }

            }
        }

        //return change;
    }

    private static void createTable(){
        for(String nonT : nonTerminalSet){
            for(String ss : expMap.get(nonT)){
                HashSet<String> first = getFirst(ss);
                for(String s : first){
                    tableMap.put(nonT+"->"+s,expNumberMap.get(nonT+"->"+ss));
                }
                if(first.contains("E")){
                    for(String s : followMap.get(nonT)){
                        tableMap.put(nonT+"->"+s,expNumberMap.get(nonT+"->E"));
                    }
                }
            }
        }
    }


    private static void gogogo() {
        int tab = 0;
        stack.push(new Pair(0,"$"));
        stack.push(new Pair(0,"program"));
        Pair top = stack.peek();
        int pos = 0;
        while(!top.s.equals("$")){
            XX nowIn = zz.get(pos);
            if (top.s.equals(nowIn.s)) {
                Pair out = stack.pop();
                output.add(out);
                pos++;
            } else if(tableMap.containsKey(top.s+"->"+nowIn.s)){
                String replace = NumberExp.get(tableMap.get(top.s+"->"+nowIn.s));
                String[] r = replace.split("->");
                String[] re = r[1].trim().split(" ");
                Pair out = stack.pop();
                output.add(out);
                for(int i=re.length-1;i>=0;i--){
                    if(!re[i].equals(""))
                        stack.push(new Pair(top.n +1,re[i]));
                }
            } else if(terminalSet.contains(top.s)) {
                Pair out = stack.pop();
                output.add(new Pair(out.n,out.s));
            }
            else {
                String replace = NumberExp.get(tableMap.get(top.s+"->"+";"));
                String[] r = replace.split("->");
                String[] re = r[1].trim().split(" ");
                Pair out = stack.pop();
                wrong = new Pair(nowIn.hang-1, stack.peek().s);
                output.add(out);
                for(int i=re.length-1;i>=0;i--){
                    if(!re[i].equals(""))
                        stack.push(new Pair(top.n +1,re[i]));
                }
            }
            top = stack.peek();

        }
    }

    private static void printtt(Pair out) {
        for(int i=0;i<out.n;i++){
            System.out.print("\t");
        }
        System.out.print(out.s);
    }

    /**
     *  you should add some code in this method to achieve this lab
     */
    private static void analysis()
    {
        read_prog();
        //System.out.println(prog.toString());
//        Pattern p = Pattern.compile("\\s+|\t|\r|\n");
//        Matcher m = p.matcher(prog.toString());
//        String aaaa = m.replaceAll(" ");
        String aaaa[] = prog.toString().split("!");
        // System.out.println(Arrays.toString(aaaa));
        for(int i =1;i<=aaaa.length;i++)
        {
            String t[] = aaaa[i-1].trim().split(" ");
            for(String s : t){
                zz.add(new XX(i,s));
            }
        }


        String[] in = wenfa.split("\n");
        //System.out.println(in);

        for(String a : in) {
            readIn(a);
        }

        //System.out.println(expMap);
        for(String nonT : nonTerminalSet){
            terminalSet.remove(nonT);
        }
        terminalSet.add("E");
//        System.out.println(terminalSet);
//        System.out.println(terminalSet.size());
//        System.out.println(expNumberMap);
        for(String exp : nonTerminalSet)
            runFirstList(exp);
        //System.out.println(firstMap);
        for(String exp : nonTerminalSet)
            runFollowList1(exp);
        followMap.get("S").add("$");

        //System.out.println(followMap);
        int change = 2;
        while(change != 0) {
            change--;
            for (String exp : nonTerminalSet)
                runFollowList2(exp);
            //change += runFollowList2(exp);
        }
        //System.out.println(followMap);
        //System.out.println(firstMap);
        createTable();
        //System.out.println(tableMap);

        gogogo();
        if(wrong!=null){
            System.out.println("语法错误,第"+wrong.n+"行,缺少\""+wrong.s+"\"");
        }
        while(output.size()>1){
            printtt(output.poll());
            System.out.print("\n");
        }
        printtt(output.poll());


        //num = 1;
    }

    /**
     * this is the main method
     * @param args
     */
    public static void main(String[] args) {
        analysis();
    }
}
