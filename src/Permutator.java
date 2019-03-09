import java.util.ArrayList;
import java.util.Comparator;

class Permutator
{
    int nTotal;
    int[] list;
    int[] reverse_list;
    int cnt;
    Permutator(int nTotal)
    {
        this.nTotal=nTotal;
        this.cnt=1;
        list=new int[nTotal+1];
        reverse_list=new int[nTotal+1];
        for(int i=0;i<=nTotal;i++)
        {
            list[i] = i;
            reverse_list[i] = i;
        }
    }
   // ArrayList<ArrayList<Integer>> getAllPermutations(){  return allPermutations;   }
    ArrayList<ArrayList<Integer>> doPermutation(Comparator<Integer> cmp)
    {
        ArrayList<ArrayList<Integer>>allPermutations=new ArrayList<>();

        int k=nTotal,l,j;
        ArrayList<Integer>t=new ArrayList();// 改为写文件
        for(int i=1;i<=nTotal;i++)
            t.add(list[i]);
        allPermutations.add(t);
        cnt++;
        while(true)
        {
            j=reverse_list[k];
            assert(j>=1);
            l = list[j - 1];
            if(cmp.compare(l,k)==-1)
            {
                while(j<k)
                {
                    l=list[j+1];
                    list[j]=l;
                    reverse_list[l]=j;
                    j++;
                }
                reverse_list[k]=k;
                list[k]=k;
                k--;
                if(k<=0)
                    break;
            }
            else
            {
                list[j-1]=k;
                list[j]=l;
                reverse_list[k]=j-1;
                reverse_list[l]=j;
                cnt++;
                t=new ArrayList();
                for(int i=1;i<=nTotal;i++)
                    t.add(list[i]);
                allPermutations.add(t);
                cnt++;
                k=nTotal;
            }
        }
        return allPermutations;
    }
}