package com.example.hmtest;

import android.app.Activity;
import android.view.Gravity;
import android.widget.FrameLayout;
import android.widget.Toast;
import com.example.hmtest.hook.HookHelper;
import com.example.hmtest.slice.MainAbilitySlice;
import ohos.aafwk.ability.Ability;
import ohos.aafwk.content.Intent;
import ohos.aafwk.content.Operation;
import ohos.agp.components.Button;
import ohos.agp.components.Text;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

import java.lang.reflect.Field;
import java.util.Map;

public class MainAbility extends Ability {

    // 定义日志标签
    private static final HiLogLabel label = new HiLogLabel(HiLog.LOG_APP, 0x00201, "MY_TAG");

    private int clickTime = 0;

    @Override
    public void onStart(Intent intent) {
        super.onStart(intent);
//        super.setMainRoute(MainAbilitySlice.class.getName());
        setUIContent(ResourceTable.Layout_ability_main);
        Text hello = (Text) findComponentById(ResourceTable.Id_text_helloworld);
        Button click = (Button) findComponentById(ResourceTable.Id_bt);
        click.setClickedListener(component -> {
            new android.os.Handler().postDelayed(() -> {
                click.setText("在下尹志平" + clickTime);
            }, 2000);
            hello.setText(String.valueOf(++clickTime));
            manipulateAndroidActivity();
        });
        HiLog.debug(label, "HelloWorld2020");

        findComponentById(ResourceTable.Id_hook).setClickedListener(component -> {
//            HookHelper.replaceContextInstrumentation();
            HookHelper.replaceActivityInstrumentation(getCurrentActivity());
        });

        Button bt_next = (Button) findComponentById(ResourceTable.Id_next);
        bt_next.setClickedListener(component -> {
            HiLog.debug(label, "next start");
            Intent next = new Intent();

            // 根据Ability的全称启动应用
            // 通过Intent中的OperationBuilder类构造operation对象
            Operation operation = new Intent.OperationBuilder()
                    .withDeviceId("")                                       //指定设备标识（空串表示当前设备）
                    .withBundleName("com.example.hmtest")                   //应用包名
                    .withAbilityName("com.example.hmtest.SecondAbility")    //Ability名称
                    .build();
            next.setOperation(operation);
            // 通过AbilitySlice的startAbility接口实现启动另一个页面
            startAbility(next);
            HiLog.debug(label, "next end");
        });
    }

    // 通过Android API，向鸿蒙应用界面中添加控件
    private void manipulateAndroidActivity() {
        Activity activity = getCurrentActivity();
        if (activity == null) return;
        HiLog.debug(label, "" + activity.getLocalClassName());
        Toast.makeText(activity, "单击次数" + clickTime, Toast.LENGTH_SHORT).show();
        FrameLayout decorView = (FrameLayout) activity.getWindow().getDecorView();
        android.widget.TextView someText = new android.widget.TextView(activity);
        someText.setText("添加一个Android TextView");
        someText.setTextSize(20);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        someText.setLayoutParams(params);
        decorView.addView(someText);
    }


    //反射获取Android的Activity
    public static Activity getCurrentActivity() {
        try {
            Class activityThreadClass = Class.forName("android.app.ActivityThread");
            Object activityThread = activityThreadClass.getMethod("currentActivityThread").invoke(
                    null);
            Field activitiesField = activityThreadClass.getDeclaredField("mActivities");
            activitiesField.setAccessible(true);
            Map activities = (Map) activitiesField.get(activityThread);
            for (Object activityRecord : activities.values()) {
                Class activityRecordClass = activityRecord.getClass();
                Field pausedField = activityRecordClass.getDeclaredField("paused");
                pausedField.setAccessible(true);
                if (!pausedField.getBoolean(activityRecord)) {
                    Field activityField = activityRecordClass.getDeclaredField("activity");
                    activityField.setAccessible(true);
                    return (Activity) activityField.get(activityRecord);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
