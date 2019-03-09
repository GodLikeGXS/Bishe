



class LinkedUnit
{
    char val;
    LinkedUnit next;
    LinkedUnit()
    {
        val='\0';
        next=null;
    }
    LinkedUnit(char c)
    {
        val=c;
        next=null;
    }
}
public class CList extends OTUnit
{

    private LinkedUnit head;
    private int size;

    CList()
    {
        head=new LinkedUnit();
        size=0;
    }
    CList(String str)
    {
        this();
        for(int i=0;i<str.length();i++)
            this.insert(str.charAt(i), this.size()+1);
    }
    public int size()
    {
        return this.size;
    }
    @Override
    public void doOperation(Operation o)
    {
       // System.out.println(o.getClass().getName());
        assert(o instanceof ListOperation);
        ListOperation op=(ListOperation)o;
        if(op instanceof InsertOp)
            this.insert(((InsertOp) op).val,((InsertOp) op).position );
        if(op instanceof DeleteOp)
            this.delete(((DeleteOp)op).position);
        if(op instanceof NoOp)
            return;
    }
    @Override
    public String toString()
    {
        String r=new String();
        LinkedUnit p=head.next;
        for(int i=0;i<size;i++)
        {
            r+=p.val;
            p=p.next;
        }
        return r;
    }

    /* this OT function make the assumption that the arguments of the Insert of Delete Operation is valid
        i.e >0&&<= lenth of the list;
     */
    @Override
    public Operation[] transform(OTUnit unit,Operation o1,Operation o2)
    {
        assert(o1 instanceof ListOperation && o2 instanceof ListOperation);

        if(o1 instanceof NoOp || o2 instanceof NoOp)
            return new Operation[]{o1,o2};

        if(o1 instanceof InsertOp && o2 instanceof InsertOp)
        {
            InsertOp op1=(InsertOp)o1,op2=(InsertOp)o2;
            if(op1.position<op2.position)
                return new Operation[]{o1,new InsertOp(op2.position+1,op2.priority,op2.val)};
            if(op1.position>op2.position)
                return new Operation[]{new InsertOp(op1.position+1,op1.priority,op1.val),o2};
            if(op1.position==op2.position)
            {
                if(op1.val==op2.val)
                    return new Operation[]{new NoOp(op1.priority),new NoOp(op2.priority)};
                else if(op1.priority>op2.priority)
                    return new Operation[]{new InsertOp(op1.position+1,op1.priority,op1.val),o2};
                else
                    return new Operation[]{o1,new InsertOp(op2.position+1,op2.priority,op2.val)};
            }
        }
        if(o1 instanceof InsertOp && o2 instanceof  DeleteOp)
        {
            InsertOp op1=(InsertOp)o1;
            DeleteOp op2=(DeleteOp)o2;
            if(op1.position<=op2.position)
                return new Operation[]{o1,new DeleteOp(op2.position+1,op2.priority)};
            else
                return new Operation[]{new InsertOp(op1.position-1,op1.priority,op1.val),o2};
        }
        if(o1 instanceof DeleteOp && o2 instanceof InsertOp)
        {
            Operation[] t=this.transform(unit,o2,o1);
            Operation r=t[0];
            t[0]=t[1];
            t[1]=r;
            return t;
        }
        if(o1 instanceof DeleteOp && o2 instanceof DeleteOp)
        {
            DeleteOp op1=(DeleteOp)o1,op2=(DeleteOp)o2;
            if(op1.position<op2.position)
                return new Operation[]{o1,new DeleteOp(op2.position-1,op2.priority)};
            if(op1.position>op2.position)
                return new Operation[]{new DeleteOp(op1.position-1,op1.priority),o2};
            if(op1.position==op2.position)
                return new Operation[]{new NoOp(op1.priority),new NoOp(op2.priority)};
        }
        assert(false);
        return null;
    }
    @Override
    public Operation[] transform(OTUnit unit,Operation o1,Operation[] oplist)
    {
        assert(unit==null || unit instanceof CList);
        Operation[] r=new Operation[1+oplist.length];
        Operation t=o1;
        try {
            for (int i = 0; i < oplist.length; i++) {
                Operation[] tem = transform(null,t,oplist[i]);
                r[i + 1] = tem[1];
                t = tem[0];
            }
            r[0] = t;
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return r;
    }
    @Override
    public Object clone() throws CloneNotSupportedException
    {
        //dont understand why
        //CList newList = (CList) super.clone();
        CList newList=new CList();

        LinkedUnit q=this.head.next;
        while(q!=null)
        {
            newList.insert(q.val, newList.size+1);
            q=q.next;
        }
        return newList;
    }

    private void insert(char val,int pos)
    {
        if(pos>size+1||pos<=0)
            return;
        LinkedUnit p=head;
        for(int i=1;i<pos;i++)
            p=p.next;
        LinkedUnit t=new LinkedUnit(val);
        t.next=p.next;
        p.next=t;
        this.size++;
    }
    private void delete(int pos)
    {
        if(pos>size||pos<=0)
            return;
        LinkedUnit p=head;
        for(int i=1;i<pos;i++)
            p=p.next;
        p.next=p.next.next;
        this.size--;
    }
    @Override
    public boolean equals(Object obj)
    {
        if(obj==null)
            return false;
        if(obj==this)
            return true;
        if(obj instanceof CList)
        {
            CList clist=(CList)obj;
            if(this.size()!=clist.size())
                return false;
            LinkedUnit p=this.head.next,q=clist.head.next;
            while(p!=null)
            {
                if(p.val!=q.val)
                    return false;
                p=p.next;
                q=q.next;
            }
            return true;
        }
        return false;
    }
    @Override
    public void show()
    {
        LinkedUnit p=head;
        while(p!=null)
        {
            System.out.print(p.val+" ");
            p=p.next;
        }
        System.out.println();
    }
    /*this is for test*/
    public static void main(String[] args)
    {

        CList cl=new CList("HELLO");
        cl.insert('G', 1);
        cl.show();
        cl.insert('X',5 );
        cl.show();
        CList t=null;
        try
        {
            t=(CList)cl.clone();
            System.out.print("aa");
            t.show();
            t.insert('O', t.size()+1);
            cl.insert('F', cl.size()+1);
            System.out.println(t.equals(cl));
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        cl.insert('P', 8);
        cl.show();
        cl.delete(1);
        cl.show();
        cl.delete(7);
        cl.show();
        t.show();


    }
}
