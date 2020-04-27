import java.util.LinkedList;

public class FuncInput {
    Annot ant=null;
    LinkedList<Integer> listLen=null;
    String strType="";
    public FuncInput(Annot a,String str){
        ant=a;
        strType=str;
    }
    public FuncInput( String s, LinkedList<Integer> len){
        strType=s;
        listLen=len;
    }
}
