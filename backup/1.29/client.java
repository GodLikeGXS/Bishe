import java.util.ArrayList;


abstract class UFSUnit //implements simulable
{
    int id;
    abstract void executeEvent(Event e);
    abstract  void registerEvent(EventList el);
    abstract void traceToOrigin();
}
class Client extends UFSUnit
{
    ClientUnit unit;
    ArrayList<Operation>localOp;
    int ecnt;//执行计数

    UFSet inital;//初始状态

    Client(UFSet ufset)
    {
        unit=new ClientUnit(ufset);
        localOp=new ArrayList<>();
        this.inital=ufset;
        this.ecnt=0;
    }
    void register(Server server)
    {
        this.id=server.addClient(this);
    }
    void addLocalOp(Operation op) { localOp.add(op); }
    int getLocalOpNum(){ return localOp.size(); }
    void registerEvent(EventList el)
    {
        /*
        for(int i=0;i<localOp.size();i++)
        {
            el.add(new Local_EVENT(this.id, i, localOp.get(i)));
            el.add(new Server_EVENT(this.id,i,localOp.get(i)));
        }
        */
    }
    @Override
    void executeEvent(Event e)
    {
        if(e instanceof  Local_EVENT)
            assert(e.id==this.id);
        if(e instanceof Remote_Event)
            assert(((Remote_Event) e).executor==this.id);
        if(e instanceof Local_EVENT)
        {
            assert(ecnt==e.corder);
            ecnt++;
            unit.addLocalOperation(e.op);
        }
        if(e instanceof Remote_Event)
        {
            unit.addGlobalOperation(e.op, ((Remote_Event) e).path);
        }
    }
    @Override
    void traceToOrigin()
    {
        this.unit=new ClientUnit(inital);
        this.ecnt=0;
    }
    UFSet getCurrentState()
    {
        return unit.currentNode.ufset;
    }
    void showCurrent()
    {
        System.out.print(unit.currentNode.ufset);
    }

    static void show(Client c)
    {
        System.out.println("CLIENT "+c.id);
        ClientUnit.show(c.unit);
    }

    //用于测试
    public static void main(String[] args)
    {
        UFSet initSet=new UFSet(4);
        initSet.init(new equClass[]{new equClass(new int[]{1,2}),
                new equClass(new int[]{3}),new equClass(new int[]{4})});

        Client c1=new Client(initSet),c2=new Client(initSet);
        Server s=new Server(initSet);
        c1.register(s);
        c2.register(s);

        Operation o1=new UnionOp(1,3);
        Operation o2=new UnionOp(1,4);
        Operation o3=new SplitOp(new int[]{1});

        c1.addLocalOp(o1);
        c1.addLocalOp(o3);
        c2.addLocalOp(o2);

        EventList el=new EventList();
        s.registerEvent(el);

        for(int i=0;i<el.size();i++)
        {
            Event e=el.get(i);
            assert(e.id==1||e.id==2);
            if(e instanceof Local_EVENT)
            {
                if(e.id==1)
                    c1.executeEvent(e);
                else
                    c2.executeEvent(e);
            }
            else if(e instanceof Server_EVENT)
                s.executeEvent(e);
            else
            {
                if(((Remote_Event)e).executor==1)
                    c1.executeEvent(e);
                else
                    c2.executeEvent(e);
            }
        }
        Client.show(c1);
        System.out.println();
        Client.show(c2);
        System.out.println();
        Server.show(s);
    }
}
class Server extends UFSUnit
{
    ArrayList<ServerUnit>all_unit;
    ArrayList<Client>all_client;

    UFSet initial;

    Server(UFSet ufset)
    {
        all_unit=new ArrayList<>();
        all_client=new ArrayList<>();
        all_unit.add(null);
        all_client.add(null);
        this.id=0;
        this.initial=ufset;
    }
    int addClient(Client c)
    {
        all_client.add(c);
        all_unit.add(new ServerUnit(initial));
        return all_client.size()-1;
    }
    ArrayList<Client> getAllClients(){ return all_client;   }

    @Override
    void registerEvent(EventList el)
    {
        ArrayList<Remote_Event>rel=new ArrayList<>();
        for(int i=1;i<all_client.size();i++)
        {
            Client c=all_client.get(i);
            for(int j=0;j<c.localOp.size();j++)
            {
                Local_EVENT l=new Local_EVENT(c.id,j,c.localOp.get(j));
                Server_EVENT s=new Server_EVENT(c.id,j,c.localOp.get(j));
                for(int k=1;k<all_client.size();k++)
                {
                    assert(all_client.get(k).id==k);
                    if(all_client.get(k).id!=c.id)
                    {
                        /*rel 按照executor有序*/
                        Remote_Event re=new Remote_Event(c.id, j, all_client.get(k).id, null, null);
                        s.addRe(re);
                        rel.add(re);
                    }
                }
                l.setServerEvent(s);
                el.add(l);
                el.add(s);
            }
        }
        for(int i=0;i<rel.size();i++)
            el.add(rel.get(i));
        /*
        for(int i=0;i<all_client.size();i++)
            all_client.get(i).registerEvent(el);
        for(int i=0;i<all_client.size();i++)
        {
            Client c=all_client.get(i);
            for(int j=0;j<c.getLocalOpNum();j++)
                el.add(new Remote_Event(c.id,j,null,null));
        }
        */
    }
    @Override
    void executeEvent(Event e)
    {
        assert(e instanceof Server_EVENT);
        Operation op=all_unit.get(e.id).addLocalOperation(e.op);
        for(int i=1;i<all_unit.size();i++)
        {
            if(i!=e.id)
            {
                ArrayList<Operation>path=all_unit.get(i).addGlobalOperation(op);
                for(int j=0;j<((Server_EVENT) e).rel.size();j++)
                {
                    Remote_Event re=((Server_EVENT) e).rel.get(j);
                    if(re.executor==i)
                    {
                        re.setOp(op);
                        re.setPath(path);
                        break;
                    }
                }
            }
        }

    }
    @Override
    void traceToOrigin()
    {
        all_unit=new ArrayList<>();
        all_unit.add(null);
        for(int i=1;i<all_client.size();i++)
            all_unit.add(new ServerUnit(this.initial));
    }

    static void show(Server s)
    {
        for(int i=1;i<s.all_unit.size();i++)
        {
            System.out.println("SERVER " +s.all_client.get(i).id);
            ServerUnit.show(s.all_unit.get(i));
        }
    }

}