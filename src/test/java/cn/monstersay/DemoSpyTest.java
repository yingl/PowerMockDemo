package cn.monstersay;

import junit.framework.Assert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.InOrder;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;

@RunWith(PowerMockRunner.class)
@PrepareForTest({DemoSpy.class})
public class DemoSpyTest {
    // 被测对象
    DemoSpy demoSpy;
    // 辅助对象
    private String robin = "Robin Li";
    private String jack = "Jack Ma";
    private String pony = "Pony Ma";

    @Before
    public void setUp() throws Exception {
        /**
         * spy表示partial mock，创建一个真实的对象然后根据需要mock。
         * 不mock的时候你还是可以调用原来的方法获取希望的结果。
         */
        demoSpy = PowerMockito.spy(new DemoSpy("noir.zsk"));
    }

    @After
    public void tearDown() throws Exception {

    }

    // Mock构造函数
    // 同时展示参数匹配
    @Test
    public void mockConstructor() {
        DemoSpy demo;
        DemoSpy demoSpyRobin = PowerMockito.spy(new DemoSpy("R"));
        DemoSpy demoSpyJack = PowerMockito.spy(new DemoSpy("J"));
        DemoSpy demoSpyPony = PowerMockito.spy(new DemoSpy("P"));
        /**
         * 使用"PowerMockito.doReturn(robin).when(demoSpyBaidu.getName());"的写法会导致PowerMock抛出异常：
         * org.mockito.exceptions.misusing.UnfinishedStubbingException:
         * Unfinished stubbing detected here:
         * -> at org.powermock.api.mockito.internal.PowerMockitoCore.doAnswer(PowerMockitoCore.java:36)

         * E.g. thenReturn() may be missing.
         * Examples of correct stubbing:
         * when(mock.isOk()).thenReturn(true);
         * when(mock.isOk()).thenThrow(exception);
         * doThrow(exception).when(mock).someVoidMethod();
         * Hints:
         * 1. missing thenReturn()
         * 2. you are trying to stub a final method, you naughty developer!
         * 如果遇到此类问题请尝试修改调用顺序：
         * 1. doXXX(...).when(...);
         * 2. doXXX(...).when(instanceName).methodCall(...);
         * 3. when(...).thenXXX(...);
         * 基本上不外乎这么几种格式。Google上没有很官方的说法，我的理解是和表达式计算顺序有关。
         */
        PowerMockito.doReturn(robin).when(demoSpyRobin).getName();
        PowerMockito.doReturn(jack).when(demoSpyJack).getName();
        PowerMockito.doReturn(pony).when(demoSpyPony).getName();
        try {
            // 直接匹配和用eq匹配
            PowerMockito.whenNew(DemoSpy.class).withArguments("POny").thenReturn(demoSpyPony);
            PowerMockito.whenNew(DemoSpy.class).withArguments(Mockito.eq("PonY")).thenReturn(demoSpyPony);
            PowerMockito.whenNew(DemoSpy.class).withArguments(Mockito.startsWith("pony")).thenReturn(demoSpyPony);
            demo = new DemoSpy("POny");
            Assert.assertEquals(pony, demo.getName());
            demo = new DemoSpy("PonY");
            Assert.assertEquals(pony, demo.getName());
            demo = new DemoSpy("pony ma");
            Assert.assertEquals(pony, demo.getName());
            demo = new DemoSpy("Pony Ma");
            Assert.assertEquals(null, demo);
            // 自定义匹配器
            class IsJackStringMatcher extends ArgumentMatcher<String> {
                public boolean matches(Object string) {
                    return ((String)string).equalsIgnoreCase(jack);
                }
            }
            PowerMockito.whenNew(DemoSpy.class).withArguments(Mockito.argThat(new IsJackStringMatcher())).thenReturn(demoSpyJack);
            demo = new DemoSpy("JaCK ma");
            Assert.assertEquals(jack, demo.getName());
            demo = new DemoSpy("jAcK MA");
            Assert.assertEquals(jack, demo.getName());
            // 匹配String类型的任意值
            PowerMockito.whenNew(DemoSpy.class).withArguments(Mockito.anyString()).thenReturn(demoSpyRobin);
            demo = new DemoSpy("random string");
            Assert.assertEquals(robin, demo.getName());
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }
    }

    // Mock公有static函数
    @Test
    public void mockPublicStaticMethod() {
        DemoSpy.setBossName(pony);
        Assert.assertEquals(pony, DemoSpy.getBossName());
        PowerMockito.mockStatic(DemoSpy.class);
        PowerMockito.when(DemoSpy.getBossName()).thenReturn(jack);
        Assert.assertEquals(jack, DemoSpy.getBossName());
    }

    // Mock私有static函数
    @Test
    public void mockPrivateStaticMethod() {
        DemoSpy.setBossName(pony);
        Assert.assertEquals(pony, DemoSpy.getBossName());
        // 这里用spy可以保证getBossName会调用getBossNameInternal。
        PowerMockito.spy(DemoSpy.class);
        try {
            PowerMockito.doReturn(jack).when(DemoSpy.class, "getBossNameInternal");
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }
        Assert.assertEquals(jack, DemoSpy.getBossName());
    }

    // Mock公有方法
    @Test
    public void mockPublicMethod() {
        try {
            PowerMockito.doReturn("mocked feedback").when(demoSpy).doFeedback(Mockito.anyBoolean());
            Assert.assertEquals("mocked feedback", demoSpy.doFeedback(false));
        } catch(Exception e) {
            Assert.assertEquals(e.getMessage(), false);
        }
    }

    // Mock公有方法抛出异常
    @Test
    public void mockPublicMethodThrowException() {
        try {
            PowerMockito.doThrow(new IOException("...")).when(demoSpy).doFeedback(Mockito.anyBoolean());
            try {
                demoSpy.doFeedback(true);
                Assert.assertEquals("IOException is expected.", false);
            } catch (IOException e) {
                Assert.assertTrue(true);
            }
        } catch(Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }
    }

    // UDT类型处理
    @Test
    public void mockProcessUDTType() {
        UDT udt = new UDT();
        udt.setName(robin);
        demoSpy.setName(pony);
        demoSpy.processUDTType(udt);
        Assert.assertEquals(pony, udt.getName());
        // ArgumentCaptor
        ArgumentCaptor<UDT> argUDT = ArgumentCaptor.forClass(UDT.class);
        PowerMockito.doNothing().when(demoSpy).processUDTType(argUDT.capture());
        udt.setName(jack);
        demoSpy.processUDTType(udt);
        // 因为processUDTType是无返回值函数，通过mock直接拦截。
        Assert.assertEquals(jack, udt.getName());
    }

    // Mock函数体
    @Test
    public void mockMethodBody() {
        UDT udt = new UDT();
        udt.setName(pony);
        ArgumentCaptor<UDT> argUDT = ArgumentCaptor.forClass(UDT.class);
        PowerMockito.doAnswer(new Answer<Object>() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                UDT udt = (UDT)invocation.getArguments()[0];
                udt.setName(robin);
                // 因为是void函数，所以返回null就可以了。
                return null;
            }
        }).when(demoSpy).processUDTType(argUDT.capture());
        demoSpy.processUDTType(udt);
        Assert.assertEquals(robin, udt.getName());
    }

    // Mock私有方法
    @Test
    public void mockPrivateMethod() {
        try {
            demoSpy.processInput(pony);
            Assert.assertEquals(pony.toUpperCase(), demoSpy.doFeedback(true));
            PowerMockito.doReturn(jack).when(demoSpy, "doFeedbackInternal", true);
            Assert.assertEquals(jack, demoSpy.doFeedback(true));
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }
    }

    // Mock私有方法抛出异常
    @Test
    public void mockPrivateMethodThrowException() {
        try {
            PowerMockito.doThrow(new IOException("...")).when(demoSpy, "doFeedbackInternal", true);
            demoSpy.processInput(pony);
            Assert.assertEquals(pony, demoSpy.doFeedback(false));
            try {
                demoSpy.doFeedback(true);
                Assert.assertTrue("IOException is expected.", false);
            } catch (IOException e) {
                Assert.assertTrue(true);
            }
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }
    }

    /**
     * 检测函数调用顺序（只对公有方法有效）和次数
     */
    @Test
    public void checkCallSequenceAndTimes() {
        demoSpy.mixedOperations();
        InOrder inOrder = Mockito.inOrder(demoSpy);
        inOrder.verify(demoSpy, Mockito.times(2)).step001();
        inOrder.verify(demoSpy, Mockito.times(1)).step002();
        inOrder.verify(demoSpy, Mockito.times(2)).step003();
        inOrder.verify(demoSpy, Mockito.times(1)).step002();
        inOrder.verify(demoSpy, Mockito.times(1)).step003();
        try {
            PowerMockito.verifyPrivate(demoSpy, Mockito.times(1)).invoke("step004Internal");
        } catch (Exception e) {
            Assert.assertTrue(e.getMessage(), false);
        }
    }
}