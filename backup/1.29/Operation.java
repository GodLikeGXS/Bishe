import jdk.nashorn.internal.runtime.OptimisticReturnFilters;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.util.ArrayList;
import java.util.List;


class Operation
{
    String name;
    int nParameters;
    List<Integer> parameterList;
    Operation()
    {
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

class UnionOp extends Operation
{
    UnionOp(int e1,int e2)
    {
        super();
        this.name="UNION";
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

class SplitOp extends Operation
{
    SplitOp(int[] el)
    {
        super();
        this.name="SPLIT";
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