import java.lang.reflect.*;
import java.util.*;
import java.lang.annotation.*;


public class Unit {
    private static Comparator comp= new Comparator<String>() {
        public int compare(String s1, String s2)
        {
            return s1.compareTo(s2.toString());
        }
    };
    private static void exeMethod(Class cls,List<String> mName)  {
        for(int x=0;x<mName.size();x++) {
            Method mtd= null;
            try {
                mtd = cls.getMethod(mName.get(x));
                if(Modifier.isStatic(mtd.getModifiers()))
                    mtd.invoke(null);
                else {
                    mtd.invoke((Object) cls.getDeclaredConstructor().newInstance(),(Object[]) null);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //the values are either the exception or error thrown by a test case (indicating that test case failed) or null for test cases that passed.
    private static void getTestExeResults(Class cls,HashMap<String, Throwable> tester,String methodName )  {
        Method mtd=null;
        try {
            mtd = cls.getMethod(methodName);
            if(Modifier.isStatic(mtd.getModifiers()))
                mtd.invoke(null);
            else {
                mtd.invoke((Object) cls.getDeclaredConstructor().newInstance(),(Object[]) null);
            }
            //pass
            tester.put(methodName, null);
        } catch (Throwable e) {
            //thrown exceptions, errors
            tester.put(methodName, e.getCause());
        }
    }

    public static HashMap<String, Throwable> testClass(String name)  {
        HashMap<String, Throwable> tester = new HashMap<>();
        try {
            Class cls = Class.forName(name);
            LinkedList<String> bfClass = new LinkedList<>();
            LinkedList<String> aftClass = new LinkedList<>();
            LinkedList<String> bf = new LinkedList<>();
            LinkedList<String> aft = new LinkedList<>();
            LinkedList<String> test = new LinkedList<>();
            //get all methods of the class
            Method m[] = cls.getDeclaredMethods();
            for (int i = 0; i < m.length; i++) {
                Annotation[] mant = m[i].getAnnotations();
                if (mant.length > 1) throw new RuntimeException();
                else if (mant.length == 0) continue;
                //handle only when mant length==1
                Method curMethod = m[i];
                Annotation curAnt = mant[0];

                boolean isDecided = false;
                if (curAnt.toString().contains("@BeforeClass")) {
                    if (Modifier.isStatic(curMethod.getModifiers())) {
                        bfClass.add(curMethod.getName());
                        isDecided = true;
                    } else
                        throw new RuntimeException();
                }
                if (curAnt.toString().contains("@AfterClass")) {
                    if (isDecided) throw new RuntimeException();
                    else isDecided = true;
                    if (Modifier.isStatic(curMethod.getModifiers()))
                        aftClass.add(curMethod.getName());
                    else
                        throw new RuntimeException();
                }
                if (curAnt.toString().contains("@Test")) {
                    if (isDecided) throw new RuntimeException();
                    else isDecided = true;
                    test.add(curMethod.getName());
                }
                if (curAnt.toString().contains("@Before") && !curAnt.toString().contains("@BeforeClass")) {
                    if (isDecided) throw new RuntimeException();
                    else isDecided = true;
                    bf.add(curMethod.getName());
                }
                if (curAnt.toString().contains("@After") && !curAnt.toString().contains("@AfterClass")) {
                    if (isDecided) throw new RuntimeException();
                    else isDecided = true;
                    aft.add(curMethod.getName());
                }
                if (!isDecided) throw new RuntimeException();
            }
            Collections.sort(bfClass, comp);
            Collections.sort(aftClass, comp);
            Collections.sort(bf, comp);
            Collections.sort(aft, comp);
            Collections.sort(test, comp);
            //The @BeforeClass and @AfterClass annotated methods should be run even if there are no @Test methods in the class. (But not @Before or @After methods.)
            if (test.size() == 0) {
                try {
                    exeMethod(cls, bfClass);
                    exeMethod(cls, aftClass);
                    return tester;
                } catch (Exception e) {
                    throw e;
                }
            }
            //
            exeMethod(cls, bfClass);
            for (int x = 0; x < test.size(); ++x) {
                exeMethod(cls, bf);
                getTestExeResults(cls, tester, test.get(x));
                exeMethod(cls, aft);
            }
            exeMethod(cls, aftClass);

        }catch (Exception e){
            e.printStackTrace();
        }
        return tester;
    }

    public static HashMap<String, Object[]> quickCheckClass(String name)  {
        HashMap<String, Object[]> ret = new HashMap<>();
        try {
            Class cls = Class.forName(name);
            if (cls == null) throw new RuntimeException();
            Method m[] = cls.getMethods();
            LinkedList<Prop> properties = new LinkedList<>();

            Object tObj = (Object) cls.getDeclaredConstructor().newInstance();
            for (int i = 0; i < m.length; i++) {
                Annotation[] ant = m[i].getAnnotations();
                if (ant.length > 1) throw new RuntimeException();
                else if (ant.length == 0) continue;
                if (ant[0].toString().contains("Property")) {
                    properties.add(new Prop(cls, m[i]));
                }
            }
            Collections.sort(properties, Prop.comp);
            if (properties.size() == 0) return ret;

            //call https://docs.oracle.com/javase/tutorial/reflect/member/methodInvocation.html
            int quickCheck_totalExeCnt = 0;
            for (int w = 0; w < properties.size(); ++w) {
                Prop p = properties.get(w);
                Method mtd = p.getMethod();
                Object[][] comb = p.getParameterComb();
                //no arguments
                boolean b = false;
                if (mtd.getParameterCount() == 0) {
                    try {
                        if (Modifier.isStatic(mtd.getModifiers()))
                            b = ((Boolean) mtd.invoke(null)).booleanValue();
                        else
                            b = ((Boolean) mtd.invoke(tObj, null)).booleanValue();
                        ++quickCheck_totalExeCnt;
                        if (!b) {
                            ret.put(mtd.getName(), null);
                        } else
                            ret.put(mtd.getName(), null);
                    } catch (Throwable t) {
                        ret.put(mtd.getName(), null);
                    }
                }
                //with arguments
                else {
                    for (int q = 0; q < comb.length; ++q) {
                        try {
                            if (quickCheck_totalExeCnt == 100) return ret;
                            if (Modifier.isStatic(mtd.getModifiers()))
                                b = ((Boolean) mtd.invoke(null, comb[q])).booleanValue();
                            else
                                b = ((Boolean) mtd.invoke(tObj, comb[q])).booleanValue();
                            ++quickCheck_totalExeCnt;
                            if (!b) {
                                ret.put(mtd.getName(), comb[q]);
                                break;
                            } else
                                ret.put(mtd.getName(), null);
                        } catch (Throwable t) {
                            ret.put(mtd.getName(), comb[q]);
                            break;
                        }
                    }
                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return ret;
    }

}