import java.lang.reflect.*;
import java.util.*;
import java.lang.annotation.*;


public class Prop {
    private String name="";
    private boolean isSyntaxErr=false;
    private LinkedList<FuncInput> inputs=new LinkedList<>();
    private Method method;
    private Class cls;
    private LinkedList<LinkedList<Object>> parameterComb=new LinkedList<>();
    public Method getMethod() {
        return method;
    }
    public boolean getIsSyntaxErr(){
        return isSyntaxErr;
    }
    public int getExeCnt(){
        int cnt=1;
        if(inputs==null)
            return cnt;
        for(int i=0;i<inputs.size();++i){
            FuncInput f=inputs.get(i);
            cnt*=f.ant.getList().size();
        }
        return cnt;
    }
    private void PTree2PComb(PTreeNode root,LinkedList<Object> ret){
        LinkedList<PTreeNode> list=root.getChild();
        //the 1st layer
        if(root.getParentNode()==null) {
           //this root has no child
            if(list.size()==0) {
                return;
            }
            //this root has children
            else {
                PTree2PComb(list.get(0),null);
            }
        }
        //other layers
        else {
            if(ret==null){
                ret = new LinkedList<>();
            }
            ret.add(root.getSelf());
            PTreeNode tmp=null;
            if(ret.size()==method.getParameterCount()) {
                parameterComb.add(ret);
                root.getParentNode().removeChild(0);
                //traceback
                tmp=root;
                while (tmp.getParentNode()!=null) {
                    tmp = tmp.getParentNode();
                    if(tmp.getChild().size()==0 && tmp.getParentNode()!=null)
                        //remove self
                        tmp.getParentNode().removeChild(0);
                }
                if(tmp.getChild().size()>0)
                    tmp=tmp.getChildAt(0);
                else
                    return;
            }
            if(tmp==null) {
                if(list.size()>0)
                    PTree2PComb(list.get(0), ret);
                else
                    return;
            }
            else
                PTree2PComb(tmp,null);
        }

    }

    private void genPTree(PTreeNode root,LinkedList<FuncInput> inputs,int curIdx){
        if(curIdx==inputs.size()){
            return;
        }
        else {
            //root MUST be newed outside
            List<Object> tmp=(LinkedList)inputs.get(curIdx).ant.getList();
            for(int i=0;i<tmp.size();++i){
                PTreeNode n=new PTreeNode(root,tmp.get(i));
                root.addChild(n);
                genPTree(n,inputs,curIdx+1);
            }
        }
    }

    public Object[][] getParameterComb() {
        int tpc=method.getParameterCount();
        if(tpc==0){
            Object[][] obj=new Object[1][1];
            obj[0][0]=null;
            return obj;
        }
        //
        Object[][] ret=new Object[getExeCnt()][tpc];
        int[] funcEnum=new int[tpc];
        int cnt=1;

        if(tpc==1){
            FuncInput f = inputs.get(0);
            funcEnum[0]=f.ant.getList().size();
            for(int u=0;u<funcEnum[0];++u){
                ret[u][0]=f.ant.getList().get(u);
            }
        }
        else {
            PTreeNode root=new PTreeNode(null,null);
            genPTree(root,inputs,0);
            PTree2PComb(root,null);
            for(int m=0;m<parameterComb.size();++m)
                for(int n=0;n<parameterComb.get(m).size();++n) {
                    ret[m][n]=parameterComb.get(m).get(n);
                }
        }
        return ret;
    }

    public static Comparator comp= new Comparator<Prop>() {
        public int compare(Prop p1, Prop p2)
        {
            return p1.method.getName().compareTo(p2.method.getName());
        }
    };
    private FuncInput convertStringPar(Annotation ant) {
        StringSet ss = (StringSet)ant;
        FuncInput inp=null;
        int cnt=1;
        LinkedList<Object> list = new LinkedList<>();
        if (ss != null) {
            if(ss.strings().length > 0) {
                for (String s : ss.strings())
                    list.add(s);

            } else {
                list.add("");
            }
            Annot annotation = new Annot("String", list);
            inp = new FuncInput(annotation, "String");
            cnt *= list.size();
         } else throw new RuntimeException();
        return inp;
    }
    private FuncInput convertIntegerPar(Annotation ant) {
        IntRange ir = (IntRange) ant;
        int cnt=1;
        FuncInput inp=null;
        if (ir != null && ir.min() <= ir.max()) {
            LinkedList<Object> list = new LinkedList<>();
            for (int x = ir.min(); x <= ir.max(); ++x)
                list.add(Integer.valueOf(x));
            Annot annotation=new Annot("Integer", list);
            inp=new FuncInput(annotation, "Integer");
            cnt *= list.size();
        } else throw new RuntimeException();
        return inp;
    }
    private LinkedList<FuncInput> convertObjectPar(Annotation ant) {
        ForAll fa = (ForAll)ant;
        int cnt=1;
        LinkedList<FuncInput> inp=new LinkedList<>();
        Object tObj=null;
        try {
            tObj=(Object) cls.getDeclaredConstructor().newInstance();
            if (fa != null && fa.times() > 0) {
                LinkedList<Object> list = new LinkedList<>();
                for(int k=0;k<fa.times();++k) {
                    Method meth=cls.getMethod(fa.name());
                    if(meth==null) throw new RuntimeException();
                    Object r=null;
                    if(Modifier.isStatic(meth.getModifiers())){
                        r=meth.invoke(null);
                    }else{
                        r= meth.invoke(tObj,null);
                    }
                    if(r==null || !(r instanceof Object)) throw new RuntimeException();
                    else list.add(r);
                }
                Annot annotation = new Annot("Object", list);
                inp.add(new FuncInput(annotation, "Object"));
                cnt *= inp.size();
            } else throw new RuntimeException();
        } catch (Exception e){e.printStackTrace();}
        return inp;
    }
    private void convertAntList(AnnotatedType type, LinkedList<FuncInput> input) {
        for (Annotation annotation : type.getDeclaredAnnotations()) {
            if(annotation.toString().contains("@ListLength")) {
                ListLength listLength = (ListLength) annotation;
                if (listLength != null &&
                    listLength.max() >= 0 && listLength.min() >= 0 &&
                    listLength.max() >= listLength.min()) {

                    LinkedList<Integer> ll = new LinkedList<>();
                    for (int x = listLength.min(); x <= listLength.max(); ++x) {
                        ll.add(Integer.valueOf(x));
                    }
                    input.add(new FuncInput("List", ll));
                }else throw new RuntimeException();
            }
            else if(annotation.toString().contains("@StringSet"))
                input.add(convertStringPar(annotation));
            else if(annotation.toString().contains("@IntRange"))
                input.add(convertIntegerPar(annotation));
            else if(annotation.toString().contains("@ForAll"))
                input.addAll(convertObjectPar(annotation));
        }
        if (type instanceof AnnotatedParameterizedType) {
            AnnotatedParameterizedType pt = (AnnotatedParameterizedType) type;
            for (AnnotatedType typeArg : pt.getAnnotatedActualTypeArguments()) {
                convertAntList( typeArg,input);
            }
        }
    }
    private FuncInput convertListPar(Parameter p) {
        int cnt=1;
        FuncInput funcInput=null;
        LinkedList<FuncInput> inp=new LinkedList<>();
        convertAntList(p.getAnnotatedType(),inp );

        //start from len-2
        FuncInput last=inp.get(inp.size()-1);
        if(last.strType=="List") throw new RuntimeException();
        //handle the most inner part
        LinkedList<LinkedList<Object>> twoDList=null;
        List<Object> cand=last.ant.getList();
        //
        List<Object> manyLists=new LinkedList<>();
        List<Object> args=new LinkedList<>();
        for(int i=inp.size()-2;i>=0;i--){
            FuncInput fi=inp.get(i);
            if(cand.size()==0) {
                throw new RuntimeException();
            }
            //manyLists=>args
            if(cand.size() >=fi.listLen.get(0)){
                for (int t=0;t<fi.listLen.size();++t) {
                    if(cand.size()<fi.listLen.get(t)) break;
                    else if( fi.listLen.get(t)==0) {
                        args.add(new LinkedList<Object>());
                        continue;
                    }
                    else twoDList = getCombinations((LinkedList<Object>) cand, cand.size(), fi.listLen.get(t));
                    if(fi.strType=="List"){
                        if(twoDList.size()==0) args.add(new LinkedList<Object>());
                        else for(int r=0;r<twoDList.size();++r) {
                            args.add(twoDList.get(r));
                        }
                    }
                    else
                        throw new RuntimeException();
                }
            }else throw new RuntimeException();
            //args=>manyLists
            manyLists.clear();
            for (int k = 0; k < args.size(); ++k) {
                LinkedList<Object> tmp = new LinkedList<>();
                LinkedList<Object> o= (LinkedList<Object>) args.get(k);
                for(int n=0;n<o.size();++n) {
                    tmp.add(o.get(n));
                }
                manyLists.add(tmp);
            }
            args.clear();
            cand=manyLists;
        }
        Annot annot=new Annot("List",manyLists);
        funcInput=new FuncInput(annot,"List");
        return funcInput;
    }
    private void convertParameter(Parameter p) {
        String type = p.getParameterizedType().toString();
        Annotation[] ant = p.getAnnotations();
        LinkedList<FuncInput> fInput=new LinkedList<>();

        if(type.contains("java.util.List")){
            if(isForAllInvolved(p)) isSyntaxErr=true;
            inputs.add(convertListPar(p));
        }
        else if (type.contains("java.lang.String")) {
            if(isForAllInvolved(p)) isSyntaxErr=true;
            inputs.add(convertStringPar(p.getAnnotation(StringSet.class)));
        } else if (type.contains("java.lang.Integer")) {
            if(isForAllInvolved(p)) isSyntaxErr=true;
            inputs.add(convertIntegerPar(p.getAnnotation(IntRange.class)));
        }
        else if(type.contains("java.lang.Object")){
            inputs.addAll(convertObjectPar(p.getAnnotation(ForAll.class)));
        }
        else
            throw new RuntimeException();
    }
    private boolean isForAllInvolved(Parameter p) {
        LinkedList<FuncInput> inp=new LinkedList<>();
        convertAntList(p.getAnnotatedType(),inp );
        if(inp.get(0).strType.equals("Object"))
            return true;
        else
            return true;
    }
    public Prop( Class c,Method m) {
        method=m;
        cls=c;
        Parameter[] p=m.getParameters();
        if(p.length==0)
            inputs=null;
        for(int k=0;k<p.length;++k){
            convertParameter(p[k]);
        }
    }

    //realLen>0
    //it's impossible for cand to be empty because the spec of ForAll/ StringSet/ IntRange
    public LinkedList<LinkedList<Object>> getCombinations(LinkedList<Object> cand,int candLen, int realLen){
        LinkedList<Object> data=new LinkedList<>();
        LinkedList<LinkedList<Object>> list=new LinkedList<>();
        combUtil(cand, data,0,candLen-1,0,realLen,list);
        return list;
    }
    private static void combUtil(LinkedList<Object> arr, LinkedList<Object> data, int start, int end, int index, int r,LinkedList<LinkedList<Object>> finalList){
        if (index == r) {
            LinkedList<Object> tmp=new LinkedList<>();
            for(Object i:data)
                tmp.add(i);
            finalList.add(tmp);
            return;
        }
        for (int i=start; i<=end && end-i+1 >= r-index; i++)
        {   //data[index] = arr[i];
            if(data.size() >= index+1)
                data.remove(index);
            data.add(index,arr.get(i));
            combUtil(arr, data, i+1, end, index+1, r,finalList);
        }
    }
}
