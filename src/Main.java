import java.util.*;

public class Main {

    private static List<Object> SET=Arrays.asList("s1","s2","s3","s4","s5","s6","s7","s8","s9","s10");
    int index=0;
    public Object genIntSet(){
        Object o=SET.get(index);
        ++index;
        return o;
    }
    @Property
    public boolean cherry( @ForAll(name="genIntSet", times=8) Object o) throws Exception{
        System.out.println("cherry");
        return true;
    }
    @Property
    public boolean park(@ListLength(min=0, max=3) List<@ListLength(min=0, max=5) List<@IntRange (min=0, max=2) Integer>> l, @StringSet(strings={"s1", "s2"})String s,@IntRange (min=0, max=1) Integer i,@ForAll(name="genIntSet", times=10) Object o){
        System.out.println("park");
        return true;
    }
    @Property
    public Object animal(@ListLength(min=0, max=2) List<@ListLength(min=0, max=3) List<@IntRange (min=0, max=2) Integer>> l){System.out.println("animal");return true;}
    @Property
    public boolean cat(@ListLength(min=0, max=2) List<@ListLength(min=0, max=3) List<@ForAll (name="genIntSet", times=10) Object >> l){System.out.println("cat");return true;}
    @Property
    static public boolean apple(@ListLength(min=1, max=2) List<@ListLength(min=0, max=3) List<@StringSet(strings={"s1", "s2"}) String>> l){ System.out.println("apple");return false;}
    @Property
    static public boolean orange(@StringSet(strings={"s1","s2","s3"}) String s, @IntRange (min=-3, max=-1) Integer i, @IntRange (min=1, max=3) Integer ii,@ForAll (name="genIntSet", times=10)Object o){ return true;}
    @Property
    static public void blueberry(){System.out.println("blueberry");}

    public static void main(String[] args) {
        try {
            HashMap<String, Object[]> test=Unit.quickCheckClass("Main");
        }catch (Throwable e){
            e.printStackTrace();
        }



    }

}
