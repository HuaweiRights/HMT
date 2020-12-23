package com.example.hmtest.hook;

import android.app.Activity;
import android.app.Instrumentation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;


public class HookHelper {
    public static void replaceActivityInstrumentation(Activity activity) {
        try {
            Field field = Activity.class.getDeclaredField("mInstrumentation");
            field.setAccessible(true);
            Instrumentation instrumentation = (Instrumentation) field.get(activity);
            Instrumentation instrumentationProxy = new InstrumentationProxy(instrumentation);
            field.set(activity, instrumentationProxy);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //此方法在鸿蒙中不能用
    public static void replaceContextInstrumentation() {
        try {
            Class<?> activityThreadClazz = Class.forName("android.app.ActivityThread");
            Field activityThreadField = activityThreadClazz.getDeclaredField("sCurrentActivityThread");
            activityThreadField.setAccessible(true);
            Object currentActivityThread = activityThreadField.get(null);
            Field mInstrumentationField = activityThreadClazz.getDeclaredField("mInstrumentation");
            mInstrumentationField.setAccessible(true);
            Instrumentation mInstrumentation = (Instrumentation) mInstrumentationField.get(currentActivityThread);
            Instrumentation mInstrumentationProxy = new InstrumentationProxy(mInstrumentation);
            mInstrumentationField.set(currentActivityThread, mInstrumentationProxy);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
