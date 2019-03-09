

import java.util.ArrayList;
import java.util.List;


class Operation
{
    String name;
    Operation(String name)
    {
        this.name=name;
    }
    static void show(Operation op)
    {
        if(op==null)
            System.out.print("null");
        else
            System.out.print(op);
    }
}

class UFSOperation extends Operation
{
    int nParameters;
    List<Integer> parameterList;
    UFSOperation(String name)
    {
        super(name);
        parameterList=new ArrayList<Integer>();
    }
    static void show(Operation op)
    {
        if(op==null)
            System.out.print("null");
        else
            System.out.print(op);
    }
}

class UnionOp extends UFSOperation
{
    UnionOp(int e1,int e2)
    {
        super("UNION");
        this.nParameters=2;
        parameterList.add(e1);
        parameterList.add(e2);
        parameterList.sort(null);
    }
    @Override
    public boolean equals(Object obj)
    {
        if(obj==null)
            return false;
        if(obj instanceof UnionOp)
        {
            UnionOp op=(UnionOp)obj;
            return this.parameterList.get(0)==op.parameterList.get(0)
                    &&this.parameterList.get(1)==op.parameterList.get(1);
        }
        return false;
    }
    @Override
    public String toString()
    {
        return new String("UNION("+parameterList.get(0)+","+parameterList.get(1)+")");
    }
}

class SplitOp extends UFSOperation
{
    SplitOp(int[] el)
    {
        super("SPLIT");
        this.nParameters=el.length;
        for(int i=0;i<el.length;i++)
            parameterList.add(el[i]);
        parameterList.sort(null);
    }
    @Override
    public boolean equals(Object obj)
    {
        if(obj==null)
            return false;
        if(obj instanceof SplitOp)
        {
            SplitOp op=(SplitOp)obj;
            if(this.nParameters!=op.nParameters)
                return false;
            for(int i=0;i<nParameters;i++)
                if(this.parameterList.get(i)!=op.parameterList.get(i))
                    return false;
            return true;
        }
        return false;
    }
    @Override
    public String toString()
    {
        String s=new String("SPLIT(");
        for(int i=0;i<nParameters-1;i++)
            s=s+parameterList.get(i)+",";
        s=s+parameterList.get(nParameters-1)+')';
        return s;
    }
}

class ListOperation extends Operation
{
    int priority;
    ListOperation(String name,int priority)
    {
        super(name);
        this.priority=priority;
    }
}
class InsertOp extends ListOperation
{
    int position;
    char val;

    InsertOp(int position,int priority,char val)
    {
        super("InsertOp",priority);
        this.position=position;
        this.val=val;
    }
    @Override
    public boolean equals(Object obj)
    {
        if(obj==null)
            return false;
        if(obj instanceof InsertOp)
        {
            InsertOp op=(InsertOp)obj;
            return this.position==op.position && this.val==op.val && this.priority==op.priority;
        }
        return false;
    }
    public String toString()
    {
        return new String("INSERT("+this.val+","+this.position+")");
    }
}
class DeleteOp extends ListOperation
{
    int position;

    DeleteOp(int position,int priority)
    {
        super("DELETE",priority);
        this.position=position;
    }
    @Override
    public boolean equals(Object obj)
    {
        if(obj==null)
            return false;
        if(obj instanceof DeleteOp)
        {
            DeleteOp op=(DeleteOp)obj;
            return this.position==op.position && this.priority==op.priority;
        }
        return false;
    }
    public String toString()
    {
        return new String("DELETE("+this.position+")");
    }
}
class NoOp extends ListOperation
{
    NoOp(int priority)
    {
        super("NOP",1);
    }
}