package gigaherz.util.nbt.serialization.tests;

import gigaherz.util.nbt.serialization.ICustomNBTSerializable;
import gigaherz.util.nbt.serialization.NBTSerializer;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.commons.lang3.SerializationException;

import java.util.*;

public class NBTSerializerTests
{
    public static void main(String[] args)
    {
        if (runTests())
            System.out.println("All tests passed.");
        else
            System.out.println("At least one test failed. See printed stack trace");
    }

    public static boolean runTests()
    {
        try
        {
            testSerialize(null, "{type:\"null\",}");
            testSerialize(new TestSingleByte().prepare(), "{value1:10b,className:\"gigaherz.util.nbt.serialization.tests.NBTSerializerTests$TestSingleByte\",type:\"object\",}");
            testSerialize(new TestSingleShort().prepare(), "{value1:10s,className:\"gigaherz.util.nbt.serialization.tests.NBTSerializerTests$TestSingleShort\",type:\"object\",}");
            testSerialize(new TestSingleInt().prepare(), "{value1:10,className:\"gigaherz.util.nbt.serialization.tests.NBTSerializerTests$TestSingleInt\",type:\"object\",}");
            testSerialize(new TestSingleLong().prepare(), "{value1:10L,className:\"gigaherz.util.nbt.serialization.tests.NBTSerializerTests$TestSingleLong\",type:\"object\",}");
            testSerialize(new TestSingleFloat().prepare(), "{value1:10.0f,className:\"gigaherz.util.nbt.serialization.tests.NBTSerializerTests$TestSingleFloat\",type:\"object\",}");
            testSerialize(new TestSingleDouble().prepare(), "{value1:10.0d,className:\"gigaherz.util.nbt.serialization.tests.NBTSerializerTests$TestSingleDouble\",type:\"object\",}");
            testSerialize(new TestSingleBoolean().prepare(), "{value1:1b,className:\"gigaherz.util.nbt.serialization.tests.NBTSerializerTests$TestSingleBoolean\",type:\"object\",}");
            testSerialize(new TestString().prepare(), "{value1:\"Test8\",className:\"gigaherz.util.nbt.serialization.tests.NBTSerializerTests$TestString\",type:\"object\",}");

            testRoundTrip(new TestSingleByte().prepare());
            testRoundTrip(new TestSingleShort().prepare());
            testRoundTrip(new TestSingleInt().prepare());
            testRoundTrip(new TestSingleLong().prepare());
            testRoundTrip(new TestSingleFloat().prepare());
            testRoundTrip(new TestSingleDouble().prepare());
            testRoundTrip(new TestSingleBoolean().prepare());
            testRoundTrip(new TestString().prepare());
            testRoundTrip(new TestCustomSerializable().prepare());
            testRoundTrip(new TestArrayOfFloats().prepare());
            testRoundTrip(new TestArrayOfStrings().prepare());
            testRoundTrip(new TestListOfStrings().prepare());
            testRoundTrip(new TestSetOfStrings().prepare());
            testRoundTrip(new TestMapOfStrings().prepare());
            testRoundTrip(new TestMapOfLists().prepare());

            testRoundTrip(new TestListOfTests().prepare());
        }
        catch (TestException e)
        {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    private static void testSerialize(Object o, String expected) throws TestException
    {
        NBTTagCompound serialized;
        try
        {
            serialized = NBTSerializer.serialize(o);
        }
        catch (ReflectiveOperationException e)
        {
            throw new TestException("Test threw an exception", e);
        }
        catch (SerializationException e)
        {
            throw new TestException("Test threw an exception", e);
        }

        String result = serialized.toString();

        if (!expected.equals(result))
            throw new TestException("Result did not match expected: " + result);
    }

    private static void testRoundTrip(Object o) throws TestException
    {
        NBTTagCompound serialized;

        try
        {
            serialized = NBTSerializer.serialize(o);
        }
        catch (ReflectiveOperationException e)
        {
            throw new TestException("Test threw an exception during serialization", e);
        }
        catch (SerializationException e)
        {
            throw new TestException("Test threw an exception during serialization", e);
        }

        Object result;
        try
        {
            result = NBTSerializer.deserialize(o.getClass(), serialized);
        }
        catch (ReflectiveOperationException e)
        {
            throw new TestException("Test threw an exception during deserialization", e);
        }
        catch (SerializationException e)
        {
            throw new TestException("Test threw an exception during deserialization", e);
        }

        if (!o.equals(result))
            throw new TestException("Result did not match expected.");
    }

    private static abstract class AbstractTest
    {
        public AbstractTest prepare()
        {
            return this;
        }

        @Override
        public abstract boolean equals(Object obj);
    }

    public static class TestSingleByte extends AbstractTest
    {
        byte value1 = 10;

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof TestSingleByte))
                return false;
            TestSingleByte other = (TestSingleByte) obj;
            return value1 == other.value1;
        }
    }

    public static class TestSingleShort extends AbstractTest
    {
        short value1 = 10;

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof TestSingleShort))
                return false;
            TestSingleShort other = (TestSingleShort) obj;
            return value1 == other.value1;
        }
    }

    public static class TestSingleInt extends AbstractTest
    {
        int value1 = 10;

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof TestSingleInt))
                return false;
            TestSingleInt other = (TestSingleInt) obj;
            return value1 == other.value1;
        }
    }

    public static class TestSingleLong extends AbstractTest
    {
        long value1 = 10;

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof TestSingleLong))
                return false;
            TestSingleLong other = (TestSingleLong) obj;
            return value1 == other.value1;
        }
    }

    public static class TestSingleFloat extends AbstractTest
    {
        float value1 = 10;

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof TestSingleFloat))
                return false;
            TestSingleFloat other = (TestSingleFloat) obj;
            return value1 == other.value1;
        }
    }

    public static class TestSingleDouble extends AbstractTest
    {
        double value1 = 10;

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof TestSingleDouble))
                return false;
            TestSingleDouble other = (TestSingleDouble) obj;
            return value1 == other.value1;
        }
    }

    public static class TestSingleBoolean extends AbstractTest
    {
        boolean value1 = true;

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof TestSingleBoolean))
                return false;
            TestSingleBoolean other = (TestSingleBoolean) obj;
            return value1 == other.value1;
        }
    }

    public static class TestString extends AbstractTest
    {
        String value1 = "Test8";

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof TestString))
                return false;
            TestString other = (TestString) obj;
            return value1.equals(other.value1);
        }
    }

    public static class TestCustomSerializable extends AbstractTest implements ICustomNBTSerializable
    {
        String value1 = "Test8";

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof TestCustomSerializable))
                return false;
            TestCustomSerializable other = (TestCustomSerializable) obj;
            return value1.equals(other.value1);
        }

        @Override
        public void writeToNBT(NBTTagCompound tag)
        {
            tag.setString("custom1", value1);
        }

        @Override
        public void readFromNBT(NBTTagCompound tag)
        {
            value1 = tag.getString("custom1");
        }
    }

    public static class TestListOfStrings extends AbstractTest
    {
        List<String> value1 = new ArrayList<String>();

        @Override
        public AbstractTest prepare()
        {
            value1.add("Test1");
            value1.add("Test2");
            value1.add("Test3");
            value1.add("Test4");
            return this;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof TestListOfStrings))
                return false;
            TestListOfStrings other = (TestListOfStrings) obj;
            return value1.equals(other.value1);
        }
    }

    public static class TestArrayOfFloats extends AbstractTest
    {
        float[] value1;

        @Override
        public AbstractTest prepare()
        {
            value1 = new float[5];
            value1[0] = 1.0f;
            value1[1] = 1.1f;
            value1[2] = 1.2f;
            value1[3] = Float.POSITIVE_INFINITY;
            value1[4] = Float.NaN;
            return this;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof TestArrayOfFloats))
                return false;
            TestArrayOfFloats other = (TestArrayOfFloats) obj;

            return Arrays.equals(value1, other.value1);
        }
    }

    public static class TestArrayOfStrings extends AbstractTest
    {
        String[] value1;

        @Override
        public AbstractTest prepare()
        {
            value1 = new String[4];
            value1[0] = "Test1";
            value1[1] = "Test2";
            value1[2] = "Test3";
            value1[3] = null;
            return this;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof TestArrayOfStrings))
                return false;
            TestArrayOfStrings other = (TestArrayOfStrings) obj;

            return Arrays.equals(value1, other.value1);
        }
    }

    public static class TestSetOfStrings extends AbstractTest
    {
        Set<String> value1 = new HashSet<String>();

        @Override
        public AbstractTest prepare()
        {
            value1.add("Test1");
            value1.add("Test2");
            value1.add("Test3");
            value1.add(null);
            return this;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof TestSetOfStrings))
                return false;
            TestSetOfStrings other = (TestSetOfStrings) obj;
            return value1.equals(other.value1);
        }
    }

    public static class TestMapOfStrings extends AbstractTest
    {
        Map<String, String> value1 = new HashMap<String, String>();

        @Override
        public AbstractTest prepare()
        {
            value1.put("Key1", "Value1");
            value1.put("Key2", "Value2");
            value1.put("Key3", "Value3");
            value1.put("Key4", "Value4");
            value1.put("Key4", null);
            value1.put(null, "Value4");
            return this;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof TestMapOfStrings))
                return false;
            TestMapOfStrings other = (TestMapOfStrings) obj;
            return value1.equals(other.value1);
        }
    }

    public static class TestMapOfLists extends AbstractTest
    {
        Map<String, List> value1 = new HashMap<String, List>();

        @Override
        public AbstractTest prepare()
        {
            value1.put("Key1", makeListFrom("1", "2", "3"));
            value1.put("Key2", makeListFrom("a", "b", "c"));
            value1.put("Key3", makeListFrom(null, "", "\t"));
            return this;
        }

        private List makeListFrom(String... s)
        {
            List<String> list = new ArrayList<String>();
            list.addAll(Arrays.asList(s));
            return list;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof TestMapOfLists))
                return false;
            TestMapOfLists other = (TestMapOfLists) obj;
            return value1.equals(other.value1);
        }
    }

    public static class TestListOfTests extends AbstractTest
    {
        List<AbstractTest> value1 = new ArrayList<AbstractTest>();

        @Override
        public AbstractTest prepare()
        {
            value1.add(new TestSingleByte().prepare());
            value1.add(new TestSingleShort().prepare());
            value1.add(new TestSingleInt().prepare());
            value1.add(new TestSingleLong().prepare());
            value1.add(new TestSingleFloat().prepare());
            value1.add(new TestSingleDouble().prepare());
            value1.add(new TestSingleBoolean().prepare());
            value1.add(new TestString().prepare());
            value1.add(new TestArrayOfFloats().prepare());
            value1.add(new TestArrayOfStrings().prepare());
            value1.add(new TestListOfStrings().prepare());
            value1.add(new TestSetOfStrings().prepare());
            value1.add(new TestMapOfStrings().prepare());
            value1.add(new TestMapOfLists().prepare());
            return this;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (!(obj instanceof TestListOfTests))
                return false;
            TestListOfTests other = (TestListOfTests) obj;
            return value1.equals(other.value1);
        }
    }
}
