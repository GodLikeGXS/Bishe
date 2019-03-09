import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;

import java.util.ArrayList;
import java.util.List;

class ServerUnit
{
    UFSet currentState;
    UFSet lastState;
    ArrayList<Operation>localList;
    ArrayList<Operation>globalList;
    ServerUnit(UFSet initState)
    {
        try {
            this.currentState = (UFSet)initState.clone();
        }catch(Exception e)
        {
            e.printStackTrace();
        }
        this.lastState=currentState;
        localList=new ArrayList<>();
        globalList=new ArrayList<>();
    }
    // the method should return the transformed operation
    Operation addLocalOperation(Operation op)
    {
        if(globalList.size()==0)
        {
            assert(currentState==lastState);
            this.currentState.doOperation(op);
            localList.add(op);
            return op;
        }
        else
        {
            assert(currentState!=lastState);
            Operation[] transOp=UFSet.transform(lastState,op, globalList.toArray(new Operation[globalList.size()]));
            currentState.doOperation(transOp[0]);
            lastState.doOperation(op);
            localList.add(op);
            assert(transOp.length==globalList.size()+1);
            globalList.clear();
            for(int i=1;i<transOp.length;i++)
                globalList.add(transOp[i]);
            return transOp[0];
        }
    }
    // the method should return the operation context of op
    ArrayList<Operation> addGlobalOperation(Operation op)
    {
        ArrayList<Operation>path=new ArrayList<>();
        path.addAll(localList);
        path.addAll(globalList);
        if(globalList.size()==0)
        {
            assert(currentState==lastState);
            try {
                currentState = (UFSet) lastState.clone();
            }catch (Exception e)
            {
               e.printStackTrace();
            }
            currentState.doOperation(op);
            globalList.add(op);
        }
        else
        {
            currentState.doOperation(op);
            globalList.add(op);
        }
        return path;
    }
    //用于简单调试
    static void show(ServerUnit su)
    {
        System.out.println("current state:");
        su.currentState.show();
        System.out.println();

        System.out.println("last state:");
        su.lastState.show();
        System.out.println();

        System.out.println("local op list:");
        for(int i=0;i<su.localList.size();i++)
            System.out.println(su.localList.get(i));

        System.out.println("global op list:");
        for(int i=0;i<su.globalList.size();i++)
            System.out.println(su.globalList.get(i));

        System.out.println();

    }
}

class StateNode
{
    UFSet ufset;
    Operation localOp;
    StateNode NextLocalState;
    Operation globalOp;
    StateNode NextGlobalState;
   // boolean transform; // indicate whether the node has transformed yet
    void setLocalOp(Operation op)
    {
        this.localOp=op;
    }
    void setGlobalOp(Operation op)
    {
        this.globalOp=op;
    }
    void setNextLocalState(StateNode node)
    {
        this.NextLocalState=node;
    }
    void setNextGlobalState(StateNode node)
    {
        this.NextGlobalState=node;
    }
    void doOperation(Operation op)
    {
        ufset.doOperation(op);
    }
    StateNode()
    {
        localOp=null;
        NextGlobalState=null;
        NextLocalState=null;
        globalOp=null;
    //    transform=false;
    }
    StateNode(UFSet ufset)
    {
        this();
    //    transform=false;

        try {
            this.ufset = (UFSet) ufset.clone();
        }catch (Exception e)
        {
            System.out.println(e);
        }
    }
    static void transform(StateNode node)
    {
        if(node==null)
            return;
        if(node.NextLocalState==null||node.NextGlobalState==null)
            return;
        assert(node.NextGlobalState.NextLocalState==node.NextLocalState.NextGlobalState);
        if(node.NextLocalState.NextGlobalState!=null)
            return;
        //Operation[] transOp=node.ufset.transform(node.localOp,node.globalOp);
        Operation[] transOp=UFSet.transform(node.ufset,node.localOp,node.globalOp);

        StateNode sn=new StateNode(node.NextGlobalState.ufset);
        sn.doOperation(transOp[0]);

        /*this is for debugging*/
        StateNode st=new StateNode(node.NextLocalState.ufset);
        st.doOperation(transOp[1]);
        assert(st.ufset.equals(sn.ufset));


        node.NextGlobalState.setLocalOp(transOp[0]);
        node.NextGlobalState.setNextLocalState(sn);
        node.NextLocalState.setGlobalOp(transOp[1]);
        node.NextLocalState.setNextGlobalState(sn);
        transform(node.NextLocalState);
        transform(node.NextGlobalState);
    }

    static void printBlanks(int n)
    {
        for(int i=0;i<n;i++)
            System.out.print(" ");
    }
    // 用于简单的调试
    static void show(int nBlanks,StateNode sn)
    {
        printBlanks(nBlanks);

        if(sn==null)
        {
            System.out.print("null");
            return;
        }

        sn.ufset.show();
        System.out.println();
        printBlanks(nBlanks);

        System.out.print("global op:");
        Operation.show(sn.globalOp);
        System.out.println();

        show(nBlanks+10,sn.NextGlobalState);
        System.out.println();
        printBlanks(nBlanks);

        System.out.print("local op:");
        Operation.show(sn.localOp);
        System.out.println();

        show(nBlanks+10,sn.NextLocalState);
    }
}

class ClientUnit
{
    StateNode initNode;
    StateNode lastNode;
    StateNode currentNode;

    ClientUnit(UFSet ufSet)
    {
        initNode=new StateNode(ufSet);
        lastNode=initNode;
        currentNode=initNode;
    }
    void addLocalOperation(Operation op)
    {
        StateNode sn=new StateNode(lastNode.ufset);
        sn.doOperation(op);
        lastNode.setLocalOp(op);
        lastNode.setNextLocalState(sn);
        if(currentNode==lastNode)
        {
            lastNode=sn;
            currentNode=sn;
        }
        else
        {
            StateNode.transform(lastNode);
            lastNode=sn;
            StateNode p=lastNode;
            while(p.NextGlobalState!=null)
                p=p.NextGlobalState;
            assert(currentNode.NextLocalState==p);
            currentNode=p;
        }
    }
    void addGlobalOperation(Operation op,ArrayList<Operation>path)
    {
        StateNode p=initNode;


        for(int i=0;i<path.size();i++)
        {
            if(p.localOp!=null && p.localOp.equals(path.get(i)))
                p=p.NextLocalState;
            else if(p.globalOp!=null && p.globalOp.equals(path.get(i)))
                p=p.NextGlobalState;
            else
                assert(false);
        }
        assert(p.globalOp==null);
        p.setGlobalOp(op);
        StateNode q=new StateNode(p.ufset);
        q.doOperation(op);
        p.setNextGlobalState(q);
        StateNode.transform(p);

        while(currentNode.NextGlobalState!=null)
            currentNode=currentNode.NextGlobalState;
    }
    //用于简单调试
    static void show(ClientUnit cu)
    {
        StateNode.show(0,cu.initNode);
    }
}

// 用于测试
public class statespace
{
    static void f1(ClientUnit c1,ClientUnit c2,ServerUnit s1,ServerUnit s2)
    {
        System.out.println("c1:");
        ClientUnit.show(c1);
        System.out.println();
        System.out.println("c2:");
        ClientUnit.show(c2);
        System.out.println();
        System.out.println("s1");
        ServerUnit.show(s1);
        System.out.println();
        System.out.println("s2");
        ServerUnit.show(s2);
    }
    public static void main(String[] args)
    {
        UFSet initSet=new UFSet(4);
        initSet.init(new equClass[]{new equClass(new int[]{1,2}),
                new equClass(new int[]{3}),new equClass(new int[]{4})});

        ServerUnit s1=new ServerUnit(initSet),s2=new ServerUnit(initSet);
        ClientUnit c1=new ClientUnit(initSet),c2=new ClientUnit(initSet);

        Operation o1=new UnionOp(1,3);
        Operation o2=new UnionOp(1,4);
        Operation o3=new SplitOp(new int[]{1});

        c1.addLocalOperation(o1);
        Operation t=s1.addLocalOperation(o1);
        ArrayList<Operation>path=s2.addGlobalOperation(t);
        c2.addGlobalOperation(t,path);
       // f1(c1,c2,s1,s2);


        /**********************************/

        c2.addLocalOperation(o2);
        t=s2.addLocalOperation(o2);
        path=s1.addGlobalOperation(t);
        c1.addGlobalOperation(t,path);
        //f1(c1,c2,s1,s2);

        /************************************/

        c1.addLocalOperation(o3);
        t=s1.addLocalOperation(o3);
        path=s2.addGlobalOperation(t);
        c2.addGlobalOperation(t, path);
        f1(c1,c2,s1,s2);

    }
}