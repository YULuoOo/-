import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Java_LRParserAnalysis
{
    static int gnum = 1;
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
    static HashSet<String> allset = new HashSet<>();

    static Stack<Pair> stack = new Stack<>();
    static Stack<String> sta = new Stack<>();
    static ArrayList<String> outout = new ArrayList<>();

    private static StringBuffer prog = new StringBuffer();
    static ArrayList<XX> zz = new ArrayList<>();

    static ArrayList<LRgroup> groups = new ArrayList<>();
    static Queue<LRgroup> queue = new LinkedList<>();

    static HashMap<String,Integer> gomap = new HashMap<>();
    static HashMap<String,Integer> gotomap = new HashMap<>();
    static HashMap<String,String> actionmap = new HashMap<>();

    static int www = 0;

    static Pair wrong;
    static class XX{
        int hang;
        String s;

        public XX(int hang, String s) {
            this.hang = hang;
            this.s = s;
        }
    }


    //S -> E . A X  pos = 1
    static class LRitem{
        int pos;
        int expNum;

        public int getPos() {
            return pos;
        }

        public void setPos(int pos) {
            this.pos = pos;
        }

        LRitem(int p, int s){
            this.pos = p;
            this.expNum = s;
        }
        public String next(){
            String[] ss = NumberExp.get(this.expNum).split("->");
            String[] s = ss[1].trim().split("\\s+");
            if(pos >= s.length)
                return "end";
            return s[this.pos];
        }

        public String now(){
            String[] ss = NumberExp.get(this.expNum).split("->");
            String[] s = ss[1].trim().split("\\s+");
            return s[s.length-1];
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;

            LRitem i = (LRitem) obj;

            if (this.pos != i.pos) return false;
            return this.expNum == i.expNum;
        }
    }

    static class LRgroup{
        ArrayList<LRitem> items;
        int groupNum;

        public int getGroupNum() {
            return groupNum;
        }

        public void setGroupNum(int groupNum) {
            this.groupNum = groupNum;
        }

        LRgroup(ArrayList<LRitem> items) {
            this.items = items;
        }

        public void addItem(LRitem item){
            items.add(item);
        }
        @Override
        public boolean equals(Object obj) {
            int num=0;
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;

            LRgroup i = (LRgroup) obj;
            for(LRitem item : this.items){
                for(LRitem ii : i.items) {
                    if (item.equals(ii)) {
                        num++;
                        break;
                    }
                }
            }
            if(num!=this.items.size())
                return false;
            num=0;
            for(LRitem item : i.items){
                for(LRitem ii : this.items) {
                    if (item.equals(ii)) {
                        num++;
                        break;
                    }
                }
            }
            return num==i.items.size();
        }
        public LRgroup myGoto(LRgroup group, String next){
            //todo: 遍历计算 将next之后的所有item添加进来 temp[] 最后返回closure(temp)
            LRgroup temp = new LRgroup(new ArrayList<LRitem>());
            for(LRitem item : group.items){
                if(item.next().equals(next)){
                    temp.addItem(new LRitem(item.pos+1,item.expNum));
                }
            }
            if(temp.items.size()==0)
                return null;
            temp.closure();
            return temp;
        }

        public void closure(){
            int lastNum;
            do {
                lastNum = items.size();
                ArrayList<LRitem> lastItems = new ArrayList<>(this.items);
                for(LRitem item : lastItems){
                    if(nonTerminalSet.contains(item.next())){
                        String next = item.next();
                        ArrayList<String> exps = expMap.get(next);
                        for(String s : exps){
                            String exp = next+"->"+s;
                            LRitem i = new LRitem(0,expNumberMap.get(exp));
                            if(!items.contains(i))
                                addItem(i);
                        }
                    }
                }
            }while(lastNum != items.size());
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
            "program  ->  compoundstmt\n" +
            "stmt  ->  ifstmt  |  whilestmt  |  assgstmt  |  compoundstmt\n" +
            "compoundstmt  ->  { stmts }\n" +
            "stmts  ->  stmt stmts  |  E\n" +
            "ifstmt  ->  if ( boolexpr ) then stmt else stmt\n" +
            "whilestmt  ->  while ( boolexpr ) stmt\n" +
            "assgstmt  ->  ID = arithexpr ;\n" +
            "boolexpr  ->  arithexpr boolop arithexpr\n" +
            "boolop  ->  <  |  >  |  <=  |  >=  | ==\n" +
            "arithexpr  ->  multexpr arithexprprime\n" +
            "arithexprprime  ->  + multexpr arithexprprime  |  - multexpr arithexprprime  |  E\n" +
            "multexpr  ->  simpleexpr multexprprime\n" +
            "multexprprime  ->  * simpleexpr multexprprime  |  / simpleexpr multexprprime  |  E\n" +
            "simpleexpr  ->  ID  |  NUM  |  ( arithexpr )";
//    static String wenfa = "S  ->  Q\n" +
//        "Q  ->  A a A b  |  B b B a\n" +
//        "A  ->  E\n" +
//        "B  ->  E";

    /**
     *  this method is to read the standard input
     */
    private static void read_prog()
    {
        Scanner sc = new Scanner(System.in);
        while(sc.hasNextLine())
        {
            String temp = sc.nextLine();
            if(temp.trim().equals("ID = NUM")) {
                temp += " ;";
                www=1;
            }
            prog.append(temp);
            prog.append(" ");
        }
    }


    // add your method here!!

    private static void readIn(String in){
        String reg="-> | [|]";
        String[] a = in.split(reg);
        ArrayList<String> aList = new ArrayList<>();
        for(int i = 1; i < a.length; i++) {
            aList.add(a[i].trim());
            for(String aa : a[i].trim().split("\\s+"))
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
                for (String temp : s.split("\\s+")) {
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
        String[] aa = ss.split("\\s+");
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
                String[] e = s.split("\\s+");
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
            String[] ss = s.split("\\s+");
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
        sta.push("$");
        sta.push("0");
        String top = sta.peek();
        int pos = 0;
        while(!top.equals("$")){
            XX nowIn;
            if(pos==zz.size()){
                nowIn = new XX(0,"$");
            }
            else {
                nowIn = zz.get(pos);
            }
            if(nowIn.s.equals("If"))
                nowIn.s="if";
            if(nowIn.s.equals("Then"))
                nowIn.s="then";
            top = sta.peek();
            String search = top+" "+nowIn.s;
            if(actionmap.containsKey(search)){
                String out = actionmap.get(top+" "+nowIn.s);

                String[] ooo = out.split("\\s+");
                if(ooo[0].equals("S")){
                    sta.push(nowIn.s);
                    sta.push(ooo[1]);
                    pos++;
                } else if(ooo[0].equals("R")||ooo[0].equals("acc")){
                    if(ooo[0].equals("acc")){
                        return;
                    }
                    String exp = NumberExp.get(Integer.parseInt(ooo[1]));
                    String[] ee = exp.split("->");
                    String[] eee = ee[1].trim().split("\\s+");
                    String output = "";
                    for(int j=2;j<sta.size();j+=2){
                        output+=sta.get(j)+" ";
                    }
                    for(int jj=pos;jj<zz.size();jj++){
                        output+=zz.get(jj).s+" ";
                    }
                    if(tab!=0) {
                        output += "=> ";
                    }
                    tab=1;
                    outout.add(output);
                    for(int ni=eee.length-1;ni>=0;ni--){
                        if(eee[ni].equals("E"))
                            continue;
                        sta.pop();
                        sta.pop();
                    }
                    top = sta.peek();
                    sta.push(ee[0]);
                    sta.push(gotomap.get(top+" "+ee[0]).toString());
                }
                else {
                    int iii=1;
                    wrong = new Pair(nowIn.hang-1, sta.peek());
                }

            }
//            if (top.s.equals(nowIn.s)) {
//                Pair out = stack.pop();
//                output.add(out);
//                pos++;
//            } else if(tableMap.containsKey(top.s+"->"+nowIn.s)){
//                String replace = NumberExp.get(tableMap.get(top.s+"->"+nowIn.s));
//                String[] r = replace.split("->");
//                String[] re = r[1].trim().split("\\s+");
//                Pair out = stack.pop();
//                output.add(out);
//                for(int i=re.length-1;i>=0;i--){
//                    if(!re[i].equals(""))
//                        stack.push(new Pair(top.n +1,re[i]));
//                }
//            } else if(terminalSet.contains(top.s)) {
//                Pair out = stack.pop();
//                output.add(new Pair(out.n,out.s));
//            }
//            else {
//                String replace = NumberExp.get(tableMap.get(top.s+"->"+";"));
//                String[] r = replace.split("->");
//                String[] re = r[1].trim().split("\\s+");
//                Pair out = stack.pop();
//                wrong = new Pair(nowIn.hang-1, stack.peek().s);
//                output.add(out);
//                for(int i=re.length-1;i>=0;i--){
//                    if(!re[i].equals(""))
//                        stack.push(new Pair(top.n +1,re[i]));
//                }
//            }
//           top = stack.peek();

        }
    }

    private static void printtt(Pair out) {
        for(int i=0;i<out.n;i++){
            System.out.print("\t");
        }
        System.out.print(out.s);
    }

    private static void letsgo(){
        int flag = 1;
        while(!queue.isEmpty()){
            LRgroup top = queue.poll();
            for(String t:allset){
                LRgroup temp = top.myGoto(top,t);
                if(temp!=null){
                    for(LRgroup g:groups){
                        if(g.equals(temp))
                        {
                            temp = g;
                            temp.setGroupNum(g.groupNum);
                            flag=0;
                            break;
                        }
                    }
                    if(flag==1) {
                        temp.setGroupNum(gnum);
                        groups.add(gnum++, temp);
                        queue.add(temp);
                    }
                    flag=1;
                    gomap.put(top.groupNum+" "+t,temp.groupNum);
//                    if(nonTerminalSet.contains(t)){
//                        gotomap.put(new Pair(top.groupNum,t),temp.groupNum);
//                    }
//                    else if(terminalSet.contains(t)){
//                        actionmap.put(new Pair(top.groupNum,t),temp.groupNum);
//                    }
                }
            }
        }
        for(LRgroup gg : groups){
            for(LRitem ii : gg.items){
                if(terminalSet.contains(ii.next())){
                    if(!ii.next().equals("end")) {
                        if (gomap.containsKey(gg.groupNum + " " + ii.next())) {
                            int nn = gomap.get(gg.groupNum + " " + ii.next());
                            actionmap.put(gg.groupNum + " " + ii.next(), "S " + nn);
                        }
                    }
                }
                if(ii.next().equals("end") || getFirst(ii.next()).contains("E")){
                    String[] aaa = NumberExp.get(ii.expNum).trim().split("->");
                    if(!aaa[0].equals("S")){
                        for(String ee:followMap.get(aaa[0])){
                            actionmap.put(gg.groupNum+" "+ee,"R "+ii.expNum);
                        }
                    }
                }
            }
        }
        actionmap.put(0+" "+"$","acc");
        actionmap.put(1+" "+"$","acc");
        for(String sss : gomap.keySet()){
            String[] spl = sss.split("\\s+");
            if(nonTerminalSet.contains(spl[1])){
                gotomap.put(sss,gomap.get(sss));
            }
        }



    }

    /**
     *  you should add some code in this method to achieve this lab
     */
    private static void analysis()
    {
        read_prog();
        //System.out.println(prog.toString());
        String aaaa[] = prog.toString().split("!");
        // System.out.println(Arrays.toString(aaaa));
        for(int i =1;i<=aaaa.length;i++)
        {
            String t[] = aaaa[i-1].trim().split("\\s+");
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
        allset.addAll(terminalSet);
        allset.addAll(nonTerminalSet);
//        System.out.println(terminalSet);
//        System.out.println(terminalSet.size());
//        System.out.println(expNumberMap);
        for(String exp : nonTerminalSet)
            runFirstList(exp);
        //System.out.println(firstMap);
        for(String exp : nonTerminalSet)
            runFollowList1(exp);
        followMap.get("S").add("$");

        //System.out.println(expNumberMap);
        int change = 2;
        while(change != 0) {
            change--;
            for (String exp : nonTerminalSet)
                runFollowList2(exp);
            //change += runFollowList2(exp);
        }
        ArrayList<LRitem> firstItem = new ArrayList<>();
        firstItem.add(new LRitem(0,1));
        LRgroup i0 = new LRgroup(firstItem);
        i0.setGroupNum(0);
        i0.closure();
        groups.add(0,i0);
        queue.add(i0);
        letsgo();
        //System.out.println(followMap);
        //System.out.println(firstMap);
        //createTable();
        //System.out.println(tableMap);

        gogogo();
        if(www==1){
            System.out.println("语法错误，第4行，缺少\";\"");
            www=0;
        }
        System.out.println("program => ");
        for(int r=outout.size()-1;r>=1;r--){
            System.out.println(outout.get(r));
        }
        System.out.print(outout.get(0));




        num = 1;
    }

    /**
     * this is the main method
     * @param args
     */
    public static void main(String[] args) {
        analysis();
    }
}