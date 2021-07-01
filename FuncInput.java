import java.util.List;

public class FuncInput {
    Annot ant=null;
    List<Integer> listLen=null;
    String strType="";
    public FuncInput(Annot a,String str){
        ant=a;
        strType=str;
    }
    public FuncInput( String s, List<Integer> len){
        strType=s;
        listLen=len;
    }
}
