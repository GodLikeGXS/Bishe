import java.util.*;


class EventList
{
    private ArrayList<Event>el;
    ArrayList<Integer>shuffle;
    private HashMap<Event,Integer>mm_index;
    void setShuffle(ArrayList<Integer>shuffle)
    {
        this.shuffle=shuffle;
    }
    //返回event在el中的下标
    int getIndex(Event e)
    {
        return mm_index.get(e);
    }
    EventList()
    {
        el=new ArrayList<>();
        this.shuffle=null;
        mm_index=new HashMap<>();
    }
    void add(Event e)
    {
        el.add(e);
        mm_index.put(e, el.size()-1);
    }
    Event get(int i)
    {
        if(shuffle!=null)
        {
            return el.get(shuffle.get(i));
        }
        else
            return el.get(i);
    }
    int size() { return el.size(); }
}