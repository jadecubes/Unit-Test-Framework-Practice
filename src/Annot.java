import java.util.List;

public class Annot {
    private String type=null;
    private List<Object> inputList=null;
    public Annot(String t,List<Object> l){
        type=t;
        inputList=l;
    }
    public List<Object> getList(){
        return inputList;
    }

}
