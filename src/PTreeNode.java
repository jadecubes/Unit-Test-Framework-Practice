import java.util.*;
public class PTreeNode {
    private Object self;
    private PTreeNode parentNode;
    private LinkedList<PTreeNode> cNode;
    public PTreeNode(PTreeNode p, Object s){
        self=s;
        parentNode=p;
        cNode=new LinkedList<PTreeNode>();
    }
    public void addChild(PTreeNode child){
        cNode.add(child);
    }
    public LinkedList<PTreeNode> getChild() {
        return cNode;
    }
    public PTreeNode getParentNode(){
        return parentNode;
    }
    public Object getSelf(){
        return self;
    }
    public void removeChild(int idx){
        cNode.remove(idx);
    }
    public PTreeNode getChildAt(int idx){
        return cNode.get(idx);
    }
}
