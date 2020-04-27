import java.lang.Class;
//https://stackoverflow.com/questions/16179170/fluent-api-with-static-class-in-java
public class Assertion {
    static ObjSaver obj=null;
    static void setObjSaver(ObjSaver os){
        obj=os;
    }
    //object
    static Assertion assertThat(Object o) {
        Assertion.setObjSaver(new ObjSaver(o));
        return new Assertion();
    }
    public Assertion isNotNull() throws Exception {
        if(obj.data==null)
            throw new Exception();
        return this;
    }
    public Assertion isNull() throws Exception {
        if(obj.data!=null)
            throw new Exception();
        return this;
    }
    public Assertion isEqualTo(Object o) throws Exception {
        if(obj.data==null && o==null)
            ;
        else if(!obj.data.equals(o))
            throw new Exception();
        return this;
    }
    public Assertion isNotEqualTo(Object o) throws Exception {
        if(obj.data==null) {
            if(o==null)
                throw new Exception();
        }
        else if(obj.data.equals(o))
            throw new Exception();
        return this;
    }
    public Assertion isInstanceOf(Class c) throws Exception {
        if(!(c.isInstance(obj.data)))
            throw new Exception();
        return this;
    }
    //string
    static Assertion assertThat(String s) {
        Assertion.setObjSaver(new ObjSaver(s));
        return new Assertion();
    }
    public Assertion startsWith(String s) throws Exception {
        if(!(obj.data instanceof String))
            throw new Exception();
        else if(!obj.data.toString().startsWith(s))
            throw new Exception();
        return this;
    }
    public Assertion isEmpty() throws Exception {
        if(!(obj.data instanceof String))
            throw new Exception();
        else if(!obj.data.toString().isEmpty())
            throw new Exception();
        return this;
    }
    public Assertion contains(String s) throws Exception {
        if(!(obj.data instanceof String))
            throw new Exception();
        else if(!obj.data.toString().contains(s))
            throw new Exception();
        return this;
    }

    //boolean
    static Assertion assertThat(boolean b) {
        Assertion.setObjSaver(new ObjSaver(Boolean.valueOf(b)));
        return new Assertion();
    }
    public Assertion isEqualTo(boolean b) throws Exception {
        if(!(obj.data instanceof Boolean))
            throw new Exception();
        else if(b!=((Boolean) obj.data).booleanValue()){
            throw new Exception();
        }
        return this;
    }
    public Assertion isTrue() throws Exception {
        if(!(obj.data instanceof Boolean))
            throw new Exception();
        else if(((Boolean) obj.data).booleanValue()==false)
            throw new Exception();
        return this;
    }
    public Assertion isFalse() throws Exception {
        if(!(obj.data instanceof Boolean))
            throw new Exception();
        else if(((Boolean) obj.data).booleanValue()==true)
            throw new Exception();
        return this;
    }

    static Assertion assertThat(int i) {
        Assertion.setObjSaver(new ObjSaver(Integer.valueOf(i)));
        return new Assertion();
    }

    public Assertion isEqualTo(int i) throws Exception {
        if(!(obj.data instanceof Integer))
            throw new Exception();
        else if(i!=((Integer) obj.data).intValue())
            throw new Exception();
        return this;
    }
    public Assertion isNotEqualTo(int i) throws Exception {
        if(!(obj.data instanceof Integer))
            throw new Exception();
        else if(i==((Integer) obj.data).intValue())
            throw new Exception();
        return this;
    }
    public Assertion isGreaterThan(int i) throws Exception {
        if(!(obj.data instanceof Integer))
            throw new Exception();
        else if(i>=((Integer) obj.data).intValue())
            throw new Exception();
        return this;
    }
    public Assertion isLessThan(int i) throws Exception {
        if(!(obj.data instanceof Integer))
            throw new Exception();
        else if(i<=((Integer) obj.data).intValue())
            throw new Exception();
        return this;
    }

}