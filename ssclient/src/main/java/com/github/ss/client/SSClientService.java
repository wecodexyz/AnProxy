package com.github.ss.client;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.Process;
import android.text.TextUtils;

import com.github.ss.utils.AppUtils;
import com.github.ss.utils.Config;

import java.util.Random;
import java.util.logging.Logger;


public class SSClientService extends Service {

    private static final Logger logger = Logger.getLogger("SSClientService");

    private static final String TAG = SSClientService.class.getSimpleName();
    private NioLocalClient localClient;
    private Config config;

    public static String localAddress = "127.0.0.1";
    public static int localPort = 9999;

    public static final String METHOD = "method";
    public static final String PASSWORD = "password";
    public static final String SERVER = "server";
    public static final String SERVER_PORT = "server_port";

    private String method = "aes-256-cfb";
    private String password = "tbox888666";
    private String server = "52.199.25.74";
    private int serverPort = 9001;

    private Thread task;

    public SSClientService() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
//        setupConfig();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        getDataFromIntent(intent);
        setupConfig();
        runOnBackend();
        return super.onStartCommand(intent, flags, startId);
    }


    protected void runOnBackend() {
        if (task != null) {
            task.interrupt();
            task = null;
            logger.info("Reset SSClient to new server:" + server);
        }

        task = new Thread() {
            @Override
            public void run() {
                if (localClient != null) {
                    localClient.startClient(config);
                }
            }
        };
        task.start();
        logger.info("SSClient is running ：" + server);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        logger.info("SSClient is stop!");
        if (localClient != null) {
            localClient.stopLocalClient();
            localClient = null;
        }
        if (task != null) {
            task.interrupt();
            task = null;
        }
//        if (!AppUtils.isMainProcess(this)) {
//            Process.killProcess(Process.myPid());
//        }

    }

    private void getDataFromIntent(Intent intent) {
        if (intent != null) {
            if (intent.hasExtra(SERVER) && intent.getStringExtra(SERVER) != null) {
                String m = intent.getStringExtra(METHOD);
                if (!TextUtils.isEmpty(m)) {
                    method = m;
                }
                String p = intent.getStringExtra(PASSWORD);
                if (!TextUtils.isEmpty(p)) {
                    password = p;
                }

                String s = intent.getStringExtra(SERVER);
                if (!TextUtils.isEmpty(s)) {
                    server = s;
                }
                serverPort = intent.getIntExtra(SERVER_PORT, 9009);
            }

        }
    }

    private void setupConfig() {
        localClient = new NioLocalClient();
        config = new Config();
        config.setClientListenIp(localAddress);
        config.setClientListenPort(localPort);

        config.setEncryptMethod(method);
        config.setEncryptPassword(password);
        config.setProxyServerIp(server);
        config.setProxyServerPort(serverPort);
    }
}
