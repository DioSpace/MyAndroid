package com.myself.learnservice;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import static java.lang.Thread.sleep;

/*
 * Service 是一个后台线程
 * */
public class MyService extends Service {

    private boolean serviceRunnig = false;

    public MyService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return new Binder();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        System.out.println("MyService -- onStartCommand");
        return super.onStartCommand(intent, flags, startId);
    }

    //创建服务（一个服务只会创建一次）
    @Override
    public void onCreate() {
        super.onCreate();
        serviceRunnig = true;
        System.out.println("Service create");
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (serviceRunnig) {
                    System.out.println("服务正在运行...");
                    try {
                        sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread.start();
    }

    //销毁服务
    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("Service destory");
        serviceRunnig = false;
    }
}
