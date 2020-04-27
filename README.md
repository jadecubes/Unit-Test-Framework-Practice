# Unit Test Framework Design Practice
This practice includes two parts, a JUnit test engine component and QuickCheck-style random test generator.

## Part 1. a JUnit Test Engine

```java
    public static HashMap<String, Throwable> testClass(String name);
```
This method runs all the test cases in that class. The return value is a map where the keys of the map are the test case names, and the values are either the exception or error thrown by a test case (indicating that test case failed) or null for test cases that passed.

* Test cases are those methods annotated with __@Test__.
* Test cases are executed in alphabetical order (as defined by String's compareTomethod).
* If there are any methods annotated as __@BeforeClass__, they are executed once before any tests in the class are run. If there are multiple __@BeforeClass methods__, they are executed in alphabetical order.
* If there are any methods annotated as __@Before__, they are run before each execution of a test method. Multiple __@Before__ methods are run in alphabetical order.
* Methods annotated __@AfterClass__ and __@After__ are analogous to __@BeforeClass__ and __@Before__, except they are run after the test methods.
* The __@BeforeClass__ and __@AfterClass__ annotated methods are run even if there are no __@Test__ methods in the class. (But not __@Before__ or __@After__ methods.)
* __@BeforeClass__ and __@AfterClass__ can only appear on static methods.
* A method can have only one annotation among __{@Test, @BeforeClass, @Before, @AfterClass, @After}__. If a method has more than one such annotation, testClass throws an exception (any exception).
* testClass catches all throwables from invoking test methods and return them in its result. However, it doesn't catch any throwables raised in methods annotated as __@BeforeClass, @Before, @AfterClass, or @After__. (Such exceptions can be caught and then rethrown, wrapped in a runtime exception.)
* Limitation: annotated with __@Test, @Before, and @After__ are public instance methods that take no arguments. __@BeforeClass__ and __@AfterClass__ only appear on public methods.

## Part 2. QuickCheck for JUnit

QuickCheck is an automated program testing technique developed originally for Haskell. The idea of QuickCheck is that the programmer specifies a test case (called a property) that takes some parameters. The QuickCheck infrastructure runs that test case repeatedly with random choices of parameters.

```java
    public static HashMap<String, Object[]> quickCheckClass(String name);
```
This method runs all all test cases of all methods with annotation @Property in that class. The return value is illustrated in the example below.

```java
@Property
boolean absNonNeg(@IntRange(min=-10, max=10) Integer i) {
  return Math.abs(i.intValue()) >= 0;
}
```

quickCheckClass calls absNonNeg with many different input integers ranging from -10 to 10, inclusive. For the first one for which absNonNeg returns false, quickCheckClass adds a mapping from the method name ("absNonNeg") to the array of arguments for which the method returned false or threw a Throwable. Otherwise, if the property runs until termination with only true return values, the "absNonNeg" is mapped to null. Then quickCheckClass runs the next property in the class.

* It runs all the __@Property__ methods in class name, in alphabetical order. This is completely separate from test cases: no method is annotated as both @Test and @Property.
* A property is deemed to have failed if it returns false or throws any Throwable.
* Unlike testClass, the quickCheckClass method returns a map that has the arguments that lead to the failure, but does not indicate what the failure was (i.e., it doesn't include a Throwable in the map).
* The first failing/throwing arguments to the property are stored in the map. After a failing/throwing argument is found, the property is not executed again.
* All properties are run at most 100 times. They may run fewer times depending on limits of the search, as described below.
* All property argument types must be annotated in some way, as follows.
    * __Integer__ arguments must be annotated with __@IntRange__(min=i1, max=i2), indicating the minimum integer value and the maximum integer value (both inclusive) for the argument.
    * __String__ arguments must be annotated with __@StringSet__(strings={"s1", "s2", ...}), indicating the set of strings for the argument. 
    * __List__ arguments must be annotated with __@ListLength__(min=i1, max=i2), indicating the minimum and maximum (inclusive) list lengths for the argument. The type Tmust itself be annotated appropriately for its range. For example, @ListLength(min=0, max=2) List<@IntRange(min=5, max=7) Integer> indicates an argument with lists of length 0 to 2 containing integers from 5 to 7, e.g., [], [5], [6], [7], [5,5], [5,6], [5,7], [6,5], [6,6], [6,7], [7,5], [7,6], [7,7].
    * __Object__ arguments must be annotated with __@ForAll__(name="method", times=i), where method is the name of the (public, no argument, instance) method in the property's class that will be called to generate i values for the argument. For example:
    ```java
     @Property boolean testFoo(@ForAll(name="genIntSet", times=10) Object o) {
         Set s=(Set) o;
         return s.add("Foo").contains("Foo");
     }
     
     int count = 0;
     Object genIntSet(){
         Set s = new HashSet();
         for(int i=0;i<count;i++){ s.add(i);}
         count++;
         return s;
     }
    ``` 
* No other argument types for properties are allowed.
