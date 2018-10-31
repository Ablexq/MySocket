package com.dch.mysocket;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/*
* 服务端
* */
public class MainActivity extends AppCompatActivity {

    @BindView(R.id.tv_ip)
    TextView tvIp;
    @BindView(R.id.rv_msg)
    RecyclerView recyclerView;
    @BindView(R.id.et_msg)
    EditText etMsg;
    @BindView(R.id.et_ip)
    EditText etIp;
    @BindView(R.id.btn_send)
    Button btnSend;

    public static final int SERVER_MSG = 1;
    public static final int CLIENT_MSG = 2;
    private String serverIp;
    private ArrayList<MyBean> msgLists = new ArrayList<>();

    @SuppressLint("HandlerLeak")
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SERVER_MSG://接收
                    String sendMsg = (String) msg.obj;
                    System.out.println("==========接收了信息================" + sendMsg);
                    MyBean myBean1 = new MyBean(sendMsg, SERVER_MSG);
                    msgLists.add(myBean1);
                    break;

                case CLIENT_MSG://发送
                    String receiveMsg = (String) msg.obj;
                    System.out.println("发送了信息================" + receiveMsg);
                    MyBean myBean2 = new MyBean(receiveMsg, CLIENT_MSG);
                    msgLists.add(myBean2);
                    break;
            }
            myAdapter.notifyDataSetChanged();
            recyclerView.scrollToPosition(msgLists.size() - 1);
        }
    };
    private MyAdapter myAdapter;
    private String localIP;
    public static final int SERVER_PORT = 8888;
    //客户端发送
    private OutputStream outputStream;
    private Socket sendSocket;
    //服务端接收
    private ServerSocket serverSocket;
    private InputStream inputStream;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        initTitle();
        initRv();
        receiveData();
    }

    private void initRv() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        layoutManager.setOrientation(OrientationHelper.VERTICAL);
        myAdapter = new MyAdapter(this, msgLists);
        recyclerView.setAdapter(myAdapter);
    }


    private void initTitle() {
        localIP = getLocalIp();
        String title = "我的ip是" + localIP;
        tvIp.setText(title);
    }

    private String getLocalIp() {
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            int ipAddress = wifiInfo.getIpAddress();
            if (ipAddress == 0) return null;
            return ((ipAddress & 0xff) + "." + (ipAddress >> 8 & 0xff) + "."
                    + (ipAddress >> 16 & 0xff) + "." + (ipAddress >> 24 & 0xff));
        }
        return null;
    }


    /*========================================客户端连接和发送==========================================*/
    @OnClick({R.id.btn_send})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_send://发送
                serverIp = etIp.getText().toString().trim();

                if (TextUtils.isEmpty(serverIp)) {
                    Toast.makeText(MainActivity.this, "请先输入需要连接的ip", Toast.LENGTH_LONG).show();
                    return;
                }

                final String send_msg = etMsg.getText().toString().trim();
                if (TextUtils.isEmpty(send_msg)) {
                    Toast.makeText(MainActivity.this, "请先输入需要发送的信息", Toast.LENGTH_LONG).show();
                    return;
                }

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            sendSocket = new Socket(serverIp, SERVER_PORT);
                            outputStream = sendSocket.getOutputStream();
                            outputStream.write(send_msg.getBytes());
                        } catch (IOException e) {
                            e.printStackTrace();
                        } finally {
                            Message message = new Message();
                            message.what = CLIENT_MSG;
                            message.obj = send_msg;
                            handler.sendMessage(message);

                            try {
                                outputStream.close();
                                sendSocket.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }).start();
                break;
        }
    }

    /*=========================================服务端接收======================================*/
    public void receiveData() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    //1.创建一个服务器端Socket，即ServerSocket，指定绑定的端口，并监听此端口
                    ServerSocket serverSocket = new ServerSocket(SERVER_PORT);

                    //记录客户端的数量
                    int count = 0;
                    System.out.println("=======服务器即将启动，等待客户端的连接===========");

                    Socket socket;
                    //循环监听等待客户端的连接
                    while (true) {
                        //2、调用accept()方法开始监听，等待客户端的连接
                        socket = serverSocket.accept();

                        //创建一个新的线程
                        ServerThread serverThread = new ServerThread(socket);
                        //启动线程
                        serverThread.start();

                        count++;//统计客户端的数量
                        System.out.println("客户端的数量==========" + count);
                        InetAddress address = socket.getInetAddress();
                        System.out.println("当前客户端的IP============" + address.getHostAddress());
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }).start();
    }

    class ServerThread extends Thread {
        Socket socket = null;

        public ServerThread(Socket socket) {
            this.socket = socket;
        }

        //线程执行的操作，响应客户端的请求
        public void run() {
            InputStream stream = null;
            try {
                //3、获取输入流，并读取客户端信息
                stream = socket.getInputStream();

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                while (-1 != (len = stream.read(buffer))) {
                    baos.write(buffer, 0, len);
                    baos.flush();
                }
                String string = baos.toString("utf-8");
                System.out.println("====服务器接收的数据========================" + string);
                socket.shutdownInput();//关闭输入流

                if (!TextUtils.isEmpty(string)) {
                    Message message = new Message();
                    message.what = SERVER_MSG;
                    message.obj = string;
                    handler.sendMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                //关闭资源
                try {
                    if (stream != null)
                        stream.close();
                    if (socket != null)
                        socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        try {
            serverSocket.close();
            outputStream.close();
            sendSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
