

import java.io.FileWriter;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Date;
import java.text.SimpleDateFormat;

class Simulator
{
    EventList el;
    ArrayList<UFSUnit>allUnit;
    Simulator()
    {
        el = new EventList();
        allUnit = new ArrayList<>();
    }
    void registerFromServer(Server server)
    {
        allUnit.add(server);
        ArrayList<Client>clients=server.getAllClients();
        for(int i=1;i<clients.size();i++)
            allUnit.add(clients.get(i));
    }
    void registerAllEvents()
    {
        allUnit.get(0).registerEvent(el);
    }
    void traceToOrigin()
    {
        for(int i=0;i<allUnit.size();i++)
            allUnit.get(i).traceToOrigin();
    }
    boolean checker()
    {
        for(int i=1;i<allUnit.size()-1;i++)
        {
            Client c1=(Client)(allUnit.get(i)),c2=(Client)(allUnit.get(i+1));
            if(!c1.getCurrentState().equals(c2.getCurrentState()))
                return false;
        }
        return true;
    }
    ArrayList<ArrayList<Integer>> doPermutation(boolean simplify,PrintWriter spw)
    {
        int n1=0;
        for(int i=1;i<allUnit.size();i++)
            n1+=((Client)(allUnit.get(i))).localOp.size();
        n1*=2;


        ArrayList<ArrayList<Integer>>allPermutations=new ArrayList<>();

        ArrayList<ArrayList<Integer>>p1=new Permutator(n1).doPermutation(new WrapperComparator(
                new EventComparator(simplify),el)
        );

       // System.out.println(p1.size());
        for(int i=0;i<p1.size();i++)
        {
            ArrayList<Integer>per=p1.get(i);

            ArrayList<Integer>shuffle=new ArrayList<Integer>();
            ArrayList<Integer>stem=new ArrayList<>();

            for(int j=0;j<per.size();j++)
            {
                int t=per.get(j).intValue();

                assert(t>=1 && t<=n1);
                assert(! (el.get(t-1) instanceof Remote_Event));

                shuffle.add(t-1);

                el.get(t-1).sorder=j;

                /*remote_event的sorder与相对应的server_event的sorder相等*/
                if(el.get(t-1) instanceof Server_EVENT)
                {
                    ArrayList<Remote_Event>rel=((Server_EVENT)(el.get(t-1))).rel;
                    for(Remote_Event re : rel)
                    {
                        re.setSorder(j);
                        stem.add(el.getIndex(re));
                    }
                }
            }
            shuffle.addAll(stem);
            el.setShuffle(shuffle);
            assert(shuffle.size()==el.size());


            for(int j=1;j<n1;j++)
            {
                assert(new WrapperComparator(new EventComparator(simplify),el).compare(j, j+1)==-1);
                /*
                System.out.println(new WrapperComparator(new EventComparator(simplify),el).compare(j, j+1));
                System.out.println(el.get(j-1)+" "+el.get(j-1).sorder);
                System.out.println(el.get(j)+" "+el.get(j).sorder);
                System.out.println();
                */
            }


            ArrayList<ArrayList<Integer>>p2=new Permutator(el.size()).doPermutation(new WrapperComparator(
                    new EventComparator(simplify),el)
            );
            /*
            ArrayList<ArrayList<Integer>>p2=new Permutator(el.size()).doPermutation(new WrapperComparator(
                    new TestComparator(),el)
            );
            */
            ArrayList<ArrayList<Integer>>p3=new ArrayList<>();
            for(ArrayList<Integer> p:p2)
            {
                ArrayList<Integer>q=new ArrayList<>();
                for(int j=0;j<p.size();j++)
                    q.add(shuffle.get(p.get(j)-1));
                p3.add(q);
            }

            allPermutations.addAll(p3);


            for(int j=0;j<shuffle.size();j++)
                spw.println(el.get(j));
            spw.println(p3.size());
            spw.println();

            el.setShuffle(null);

        }
        return allPermutations;
    }
    void execute(boolean simplify,PrintWriter pw)
    {
        try {

            PrintWriter spw=new PrintWriter(new FileWriter("shuffle data.txt"));

            ArrayList<ArrayList<Integer>> allPermutations = doPermutation(simplify,spw);
            spw.close();

            for (int i = 0; i < allPermutations.size(); i++)
            {

                pw.println(" 第" + i + "个排列执行：");
                ArrayList<Integer> permutation = allPermutations.get(i);

                System.out.println(" 第" + i + "个排列执行：");
                showEventPermutation(permutation);
                System.out.println();

                writeEventPermutation(permutation,pw);

                for (int j = 0; j < permutation.size(); j++)
                {
                    int event = permutation.get(j) ;
                    int executor;
                    if (el.get(event) instanceof Remote_Event)
                        executor = ((Remote_Event) (el.get(event))).executor;
                    else if (el.get(event) instanceof Local_EVENT)
                        executor = el.get(event).id;
                    else
                        executor = 0;
                    allUnit.get(executor).executeEvent(el.get(event));
                }
                if (checker())
                {
                    //System.out.println("TRUE");
                    pw.println("TRUE");
                    //((Client) (allUnit.get(1))).unit.currentNode.ufset.show();
                    pw.println(((Client) (allUnit.get(1))).unit.currentNode.ufset);
                }
                else
                {
                    //System.out.println("FALSE");
                    pw.println("FALSE");

                //    for (int j = 1; j < allUnit.size(); j++)
                //        ((Client) (allUnit.get(j))).unit.currentNode.ufset.show();
                    for (int j = 1; j < allUnit.size(); j++)
                        pw.print(((Client) (allUnit.get(j))).unit.currentNode.ufset);
                }
                traceToOrigin();
            }
            pw.close();
        }catch (Exception e)
        {
            e.printStackTrace();
        }

    }
    void simulate(Server server,boolean simplify)
    {
        this.registerFromServer(server);
        this.registerAllEvents();

        String filePath = new String("r_");
        SimpleDateFormat df=new SimpleDateFormat("yyyy_MM_dd HH_mm_ss");
        filePath = filePath + df.format(new Date())+".txt";
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(filePath));
            pw.print("INITIAL STATE: ");
            pw.println(((Client)allUnit.get(1)).inital);
            pw.println("USER NUM:"+(allUnit.size()-1));
            for(int i=1;i<allUnit.size();i++)
            {
                pw.print("USER "+i+" :");
                ArrayList<Operation>opl=((Client)(allUnit.get(i))).localOp;
                for(int j=0;j<opl.size();j++)
                    pw.print(opl.get(j)+" ");
                pw.println();
            }
            pw.println("SIMPLIFY:"+simplify);
            this.execute(simplify,pw);
            pw.close();
        } catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    void showEventPermutation(ArrayList<Integer>permutation)
    {
        for(int i=0;i<permutation.size();i++)
        {
            int j=permutation.get(i);
            System.out.println(el.get(j));
        }
    }
    void writeEventPermutation(ArrayList<Integer>permutation,PrintWriter pw)
    {
        for(int i=0;i<permutation.size();i++)
        {
            int j=permutation.get(i);
            pw.println(el.get(j));
        }
    }
    public static void main(String[] args)
    {
        Simulator sim=new Simulator();

        UFSet initSet=new UFSet(5);
        initSet.init(new equClass[]{new equClass(new int[]{1,2}),
                new equClass(new int[]{4}),new equClass(new int[]{3,5})});

        Client c1=new Client(initSet),c2=new Client(initSet),c3=new Client(initSet),c4=new Client(initSet);
        Server s=new Server(initSet);
        c1.register(s);
        c2.register(s);
        c3.register(s);
        //c4.register(s);

        Operation o1=new UnionOp(1,5);
        Operation o2=new UnionOp(4,5);
        //Operation o2=new SplitOp(new int[]{4});
        Operation o3=new SplitOp(new int[]{1});
        //Operation o3=new UnionOp(2,4);
        Operation o4=new UnionOp(2,5);

        c1.addLocalOp(o1);
        c2.addLocalOp(o2);
        c3.addLocalOp(o3);
       // c3.addLocalOp(o4);
       // c4.addLocalOp(o4);


        sim.simulate(s,true);



    }
    /*
    Server server;
    ArrayList<Client> allClient;

    void setServer(Server se){ this.server = se; }
    int addClient(Client c)
    {
        allClient.add(c);
        assert(c==allClient.get(allClient.size()-1));
        return allClient.size()-1;
    }
    */


}
