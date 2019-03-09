import java.sql.Clob;
import java.sql.SQLSyntaxErrorException;
import java.util.*;



abstract class OTUnit implements Cloneable
{
    abstract public void doOperation(Operation op);
    abstract public Operation[] transform(OTUnit unit,Operation o1,Operation o2);
    abstract public Operation[] transform(OTUnit unit,Operation op,Operation[] oplist);
    abstract public void show();
    public Object clone() throws CloneNotSupportedException
    {
        OTUnit newUnit=(OTUnit)super.clone();
        return newUnit;
    }
}

class equClass implements Cloneable
{
   HashSet<Integer> set;
   equClass(int[] arr)
   {
       set=new HashSet<Integer>();
       for(int ele:arr)
           set.add(ele);
   }
    boolean subset(equClass ec)
    {
        if(ec==null)
            return true;
        Set set2=ec.set;
        Iterator it=set2.iterator();
        while(it.hasNext())
        {
            if(!this.set.contains(it.next()))
                return false;
        }
        return true;
    }

   boolean equal(equClass ec)
   {
       Set set2=ec.set;
       if(set==null&&set2==null)
           return true;
       if( set==null || set2==null || set.size()!=set2.size())
           return false;
       Iterator it=set.iterator();
       while(it.hasNext())
       {
           if(!set2.contains(it.next()))
               return false;
       }
       return true;
   }

   void merge(equClass ec)
   {
       this.set.addAll(ec.set);
   }

   boolean remove(int[] arr) throws Exception
   {
       for(int ele:arr)
       {
           if(!set.remove(ele))
               throw new Exception("element "+ele+ "not found in the set");
       }
       return true;
   }

   Set<Integer> getSet()
   {
       return  set;
   }
   int getSize()
   {
       return set.size();
   }

    @Override
    public String toString()
    {
       String s=new String();
       for(int e:set)
           s=s+' '+e;
       return s;
    }

    public Object clone() throws CloneNotSupportedException
    {
        equClass nc=(equClass)super.clone();
        nc.set=new HashSet<>();
        for(int i:this.set)
            nc.set.add(i);
        return nc;
    }
}

class UFSet extends OTUnit
 {
     /*元素个数 编号为1,2,3...n*/
     int nElements;
     /*所有等价类*/
     List<equClass> equClassList;
     /*元素-等价类映射*/
     equClass[] belongTo;
     UFSet(int nElements)
     {
         this.nElements=nElements;
         equClassList=new ArrayList<>();
         belongTo=new equClass[this.nElements+1];
         for(int i=0;i<this.nElements;i++)
             belongTo[i]=null;
     }
     void init(equClass[] equ)
     {
         for(equClass e:equ)
         {
             equClassList.add(e);
             for(int i:e.getSet())
                 belongTo[i]=e;
         }
     }
     int size()
     {
        return this.nElements;
     }
     private void Union(int e1,int e2)
     {
         if(belongTo[e1]==belongTo[e2])
             return;
         equClassList.remove(belongTo[e2]);
         belongTo[e1].merge(belongTo[e2]);
         for(int e:belongTo[e2].getSet())
             belongTo[e]=belongTo[e1];
     }
     private void Split(int[] ele)
     {
         if(ele.length==0)
             return;
         equClass c=Find(ele[0]);

         for(int i=1;i<ele.length;i++)
             assert(c==Find(ele[i]));
         try {
             c.remove(ele);
         }catch (Exception e)
         {
             e.printStackTrace();
         }
         if(c.getSize()==0)
             equClassList.remove(c);
         equClass nc=new equClass(ele);
         equClassList.add(nc);
         for(int i:ele)
             belongTo[i]=nc;
     }
     private equClass Find(int ele)
     {
         assert(ele>=1&&ele<=nElements);
         return belongTo[ele];
     }

     @Override
     public void doOperation(Operation o)
     {
         assert(o instanceof UFSOperation);
         UFSOperation op=(UFSOperation)o;

         if(op instanceof UnionOp)
             this.Union(op.parameterList.get(0), op.parameterList.get(1));
         if(op instanceof SplitOp)
         {
             Integer[] el=op.parameterList.toArray(new Integer[op.nParameters]);
             int[] r=new int[op.nParameters];
             for(int i=0;i<op.nParameters;i++)
                r[i]=el[i].intValue();
             this.Split(r);
         }
     }

     @Override
     public  Operation[] transform(OTUnit unit,Operation o1, Operation o2)
     {
         assert(o1 instanceof UFSOperation && o2 instanceof UFSOperation);
         UFSOperation op1=(UFSOperation)o1,op2=(UFSOperation)o2;
         UFSet ufSet=(UFSet)unit;

         if(op1 instanceof UnionOp && op2 instanceof UnionOp)
            return new Operation[]{op1,op2};
         if(op1 instanceof SplitOp && op2 instanceof SplitOp)
             return new Operation[]{op1,op2};
         if(op1 instanceof SplitOp && op2 instanceof UnionOp)
         {
             Operation[] r=transform(ufSet,op2,op1);
             Operation t=r[0];
             r[0]=r[1];
             r[1]=t;
             return r;
         }
         if(op1 instanceof UnionOp && op2 instanceof SplitOp)
         {
             //assert(op2.nParameters==1);
             if(ufSet.belongTo[op1.parameterList.get(0)]==ufSet.belongTo[op1.parameterList.get(1)])
                 return new Operation[]{op2,op2};
             if(op2.nParameters==0)
                 return new Operation[]{op1,op2};
             if(op2.nParameters>1)
             {
                 //System.out.println("oooops so many to split!!");
                 int a=op1.parameterList.get(0),b=op1.parameterList.get(1);
                 int[] c=new int[op2.nParameters];
                 for(int i=0;i<op2.nParameters;i++)
                     c[i] = op2.parameterList.get(i);
                 for(int i=0;i<op2.nParameters-1;i++)
                     assert(ufSet.belongTo[op2.parameterList.get(i)]==ufSet.belongTo[op2.parameterList.get(i+1)]);

                 equClass e1=ufSet.belongTo[a],e2=ufSet.belongTo[b],e3=new equClass(c);
                 if(e1!=e2)
                 {
                     equClass e = null;
                     if (e1.subset(e3)&&op2.parameterList.contains(a))
                         e = e1;
                     if (e2.subset(e3)&&op2.parameterList.contains(b))
                         e = e2;
                     if (e == null)
                         return new Operation[]{op1, op2};
                     Set r = new HashSet<Integer>();
                     r.addAll(e.set);
                     r.removeAll(e3.set);
                     assert (r.size() == e.getSize() - e3.getSize());
                     Integer[] sbJava = new Integer[r.size()];
                     r.toArray(sbJava);
                     int[] z = new int[r.size()];
                     for (int i = 0; i < r.size(); i++)
                         z[i] = sbJava[i];
                     return new Operation[]{op1, new SplitOp(z)};
                 }
                 else
                     assert(false);
             }
             int a=op1.parameterList.get(0),b=op1.parameterList.get(1),c=op2.parameterList.get(0),x,y;
             int[] z;
             if(a!=b)
             {
                 if(c!=a&&c!=b)
                     z=new int[]{c};
                 else
                 {
                     equClass e=null;
                     if (a == c)
                         e = ufSet.belongTo[a];
                     if (b == c)
                         e = ufSet.belongTo[b];
                     assert(e.set.contains(c));
                     List<Integer>el=new ArrayList<>(e.set);
                     z=new int[el.size()-1];
                     int i=0;
                     for(int j:el)
                         if(j!=c)
                             z[i++]=j;
                 }
             }
             else
                 z=new int[]{c};
             x=a;y=b;
             return new Operation[]{new UnionOp(x,y),new SplitOp(z)};
         }
         assert(false);
         return null;
     }
     @Override
     public Operation[] transform(OTUnit unit,Operation op,Operation[] oplist)
     {
         assert(unit instanceof UFSet);
         UFSet ufSet=(UFSet)unit;

         Operation[] r=new Operation[1+oplist.length];
         Operation t=op;
         try {
             UFSet u = (UFSet)(ufSet.clone());
             for (int i = 0; i < oplist.length; i++) {
                 Operation[] tem = transform(u,t,oplist[i]);
                 u.doOperation(oplist[i]);
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
     public String toString()
     {
         String s="";
         for(int i=0;i<equClassList.size();i++)
         {
             s+=equClassList.get(i);
             if(i!=equClassList.size()-1)
                 s+="|";
         }
         return s;
     }

     @Override
     public void show()
     {
         for(int i=0;i<equClassList.size();i++)
         {
             System.out.print(equClassList.get(i));
             if (i != equClassList.size() - 1)
                 System.out.print("|");
         }
         System.out.println();
     }

    @Override
    public Object clone() throws CloneNotSupportedException
    {
        UFSet newSet=(UFSet)super.clone();
        newSet.equClassList=new ArrayList<>();
        newSet.belongTo=new equClass[newSet.nElements+1];
        for(equClass e:equClassList)
        {
            equClass ne=(equClass)e.clone();
            newSet.equClassList.add(ne);
            for(int i: ne.getSet())
                newSet.belongTo[i]=ne;
        }
        return newSet;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj==this)
            return true;
        if(obj==null)
            return false;
        if(obj instanceof  UFSet)
        {
            UFSet us = (UFSet) obj;
            if(this.nElements!=us.nElements)
                return false;
            boolean[] ab=new boolean[this.nElements+1];
            for(int i=1;i<=this.nElements;i++)
                ab[i]=false;
            for(int i=1;i<=this.nElements;i++)
            {
                if(ab[i])
                    continue;
                equClass ec1=this.belongTo[i],ec2=us.belongTo[i];
                if(!ec1.equal(ec2))
                    return false;
                for(int e:ec1.getSet())
                    ab[e]=true;
            }
            return true;
        }
        return false;
    }

     public static void main(String[] args)
     {

         UFSet ufset=new UFSet(10);
         ufset.init(new equClass[]{
                 new equClass(new int[]{1,5,10}),
         new equClass(new int[]{2}),
         new equClass(new int[]{3,4,9}),
         new equClass(new int[]{6,7,8})
         });
         ufset.show();

         try {
             UFSet newSet = (UFSet) ufset.clone();
             //System.out.print(ufset.equals(newSet));
             System.out.println();
             ufset.Union(1, 5);
             ufset.Union(2, 6);
             ufset.Split(new int[]{3, 9});
             ufset.show();
             System.out.println();
             newSet.show();
         }catch (Exception e)
         {
             e.printStackTrace();
         }
         /*
         for(int e=1;e<ufset.size();e++)
             System.out.println(ufset.Find(e));
             */
     }
 }
