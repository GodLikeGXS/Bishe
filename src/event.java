import java.util.ArrayList;
import java.util.Comparator;

class Event
{
    int id,sorder,corder;//sorder 为全局顺序  corder为局部顺序
    Operation op;
    Event(int id,int corder,Operation op)
    {
        this.id=id;
        this.corder=corder;
        this.op=op;
        this.sorder=-1;// -1 表示尚未排序
    }
    void setSorder(int sorder)
    {
        this.sorder=sorder;
    }
}
//id为客户id corder表示客户的第几个事件
class Local_EVENT extends Event
{
    Server_EVENT se;
    Local_EVENT(int id,int corder,Operation op)
    {
        super(id,corder,op);
        se=null;
    }
    void setServerEvent(Server_EVENT se)
    {
        this.se=se;
    }
    @Override
    public String toString()
    {
        return new String("Local Event Client: "+this.id+" SEQ: "+this.corder+" "+op);
    }
}

class Server_EVENT extends Event
{
    ArrayList<Remote_Event>rel;
    Server_EVENT(int id,int corder,Operation op)
    {
        super(id,corder,op);
        rel= new ArrayList<>();
    }
    void addRe(Remote_Event re)
    {
        rel.add(re);
    }
    @Override
    public String toString()
    {
        return new String("Server Event Client: "+this.id+" SEQ: "+this.corder+" "+op);
    }
}
class Remote_Event extends Event
{
    ArrayList<Operation> path;
    int executor;
    Remote_Event(int id,int corder,int executor,Operation op,ArrayList<Operation>path)
    {
        super(id,corder,op);
        this.executor=executor;
        this.path=path;
    }
    void setOp(Operation op) { this.op=op; }
    void setPath(ArrayList<Operation>path){ this.path=path;}
    @Override
    public String toString()
    {
        return new String("Remoto Event Client: "+this.id+" SEQ: "+this.corder+" Executor: "+this.executor);
    }
}
class WrapperComparator implements Comparator<Integer>
{
    Comparator<Event> ec;
    EventList el;
    WrapperComparator(Comparator<Event> ec,EventList el)
    {
        this.ec=ec;
        this.el=el;
    }
    @Override
    public int compare(Integer i1,Integer i2)
    {
        if(i1==0)
            return -1;
        if(i2==0)
            return 1;
        /*由于排列器排列元素1-n 而事件下表为0-(n-1) 这里需要减一*/
        Event e1=el.get(i1.intValue()-1),e2=el.get(i2.intValue()-1);
        return ec.compare(e1,e2);
    }
}
//for test//
class TestComparator implements Comparator<Event>
{
    static final int LARGER=1;
    static final int EQUAL=2;
    static final int SMALLER=-1;
    static final int UNDEF=0;

    @Override
    public int compare(Event e1,Event e2)
    {
        assert(e1.sorder!=-1&&e2.sorder!=-1);
        if(!(e1 instanceof Remote_Event)&&!(e2 instanceof Remote_Event))
        {
            if(e1.sorder<e2.sorder)
                return SMALLER;
            if(e1.sorder>e2.sorder)
                return LARGER;
            assert(false);
        }
        if(!(e1 instanceof  Remote_Event) && e2 instanceof Remote_Event)
        {
            if(e1.id==e2.id&&e1.corder<=e2.corder)
                return SMALLER;
            return UNDEF;

        }
        if(e1 instanceof Remote_Event && e2 instanceof Remote_Event)
        {
            if(e1.sorder<e2.sorder)
                return SMALLER;
            if(e1.sorder>e2.sorder)
                return LARGER;
            if(((Remote_Event) e1).executor<((Remote_Event) e2).executor)
                return SMALLER;
            return LARGER;
        }
        return 0-compare(e2, e1);
    }
}
class EventComparator implements Comparator<Event>
{
    static final int LARGER=1;
    static final int EQUAL=2;
    static final int SMALLER=-1;
    static final int UNDEF=0;

    boolean simplify;
    EventComparator(boolean simplify)
    {
        this.simplify=simplify;
    }

    @Override
    public int compare(Event e1,Event e2)
    {
        assert((e1.sorder==-1&&e2.sorder==-1)||(e1.sorder!=-1&&e2.sorder!=-1));
        assert(e1!=e2);
        if(e1==e2)//注意 ： 这样做假定了每一个事件仅存在一个拷贝
            return SMALLER;//return EQUAL; 按照算法应该是SMALLER
        if(e1 instanceof Local_EVENT && e2 instanceof  Local_EVENT)
        {
            if(e1.id==e2.id)
            {
                if (e1.corder > e2.corder)
                    return LARGER;
                return SMALLER;
            }
            if(e1.sorder!=-1)
            {
                if(e1.sorder<e2.sorder)
                    return SMALLER;
                return LARGER;
            }
            return UNDEF;
        }
        if(e1 instanceof Local_EVENT && e2 instanceof Server_EVENT)
        {
            if(e1.id==e2.id && e1.corder<=e2.corder)
                return SMALLER;
            if(e1.sorder!=-1)
            {
                if(e1.sorder<e2.sorder)
                    return SMALLER;
                return LARGER;
            }
            return UNDEF;
        }
        if(e1 instanceof Server_EVENT && e2 instanceof Local_EVENT)
        {
            int r=compare(e2,e1);
            return 0-r;
        }
        if( e1 instanceof Server_EVENT && e2 instanceof Server_EVENT)
        {
            if(e1.id!=e2.id)
            {
                if(e1.sorder!=-1 )
                {
                    if(e1.sorder<e2.sorder)
                        return SMALLER;
                    else
                        return LARGER;
                }
                return UNDEF;
            }
            else
            {
                if(e1.corder<e2.corder)
                    return SMALLER;
                else
                    return LARGER;
            }
        }
        if(e1 instanceof Local_EVENT && e2 instanceof  Remote_Event)
        {
            if(e1.sorder<e2.sorder)
                return SMALLER;
            return UNDEF;
        }
        if(e1 instanceof Remote_Event && e2 instanceof  Local_EVENT)
        {
            return 0-compare(e2,e1);
        }
        if(e1 instanceof Server_EVENT && e2 instanceof Remote_Event)
        {
            if(e1.sorder<=e2.sorder)
                return SMALLER;
            return UNDEF;
        }
        if(e1 instanceof Remote_Event && e2 instanceof Server_EVENT)
        {
            return 0-compare(e2,e1);
        }
        if(e1 instanceof Remote_Event && e2 instanceof Remote_Event)
        {
            assert(e1.sorder!=-1&&e2.sorder!=-1);
            if(((Remote_Event) e1).executor==((Remote_Event) e2).executor)
            {
                if (e1.sorder < e2.sorder)
                    return SMALLER;
                if (e1.sorder > e2.sorder)
                    return LARGER;
                assert(false);
            }
            if(!simplify)
                return UNDEF;
            else
            {
                if(e1.sorder<e2.sorder)
                    return SMALLER;
                else if(e1.sorder>e2.sorder)
                    return LARGER;
                else if(((Remote_Event) e1).executor<((Remote_Event) e2).executor)
                    return SMALLER;
                else
                    return LARGER;
            }
        }
        assert(false);
        return UNDEF;
    }
}