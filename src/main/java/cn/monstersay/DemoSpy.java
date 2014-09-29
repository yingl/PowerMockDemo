package cn.monstersay;

import java.io.IOException;

/**
 * Created by linying on 14-9-29.
 */
public class DemoSpy {
    static private String bossName;
    private String name;
    private String input;

    // 构造函数
    public DemoSpy(String name) {
        this.name = name;
    }

    // 公有方法
    static public void setBossName(String bossName) {
        DemoSpy.bossName = bossName;
    }

    static public String getBossName() {
        return getBossNameInternal();
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void processInput(String input) {
        this.input = input;
    }

    public String doFeedback(boolean needUpperCase) throws IOException {
        return doFeedbackInternal(needUpperCase);
    }

    public void processUDTType(UDT udt) {
        if (udt.getName() != name) {
            udt.setName(name);
        }
    }

    public void mixedOperations() {
        step001();
        step001();
        step002();
        step003();
        step003();
        step002();
        step003();
        step004Internal();
    }

    public void step001() {}
    public void step002() {}
    public void step003() {}

    // 私有方法
    static private String getBossNameInternal() {
        return bossName;
    }

    private String doFeedbackInternal(boolean needUpperCase) throws IOException {
        if ((input == null) || (input.isEmpty())) {
            throw new IOException("...");
        }
        return needUpperCase ? input.toUpperCase() : input;
    }

    private void step004Internal() {}
}
