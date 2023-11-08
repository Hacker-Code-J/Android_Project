package com.cookandroid.class_14_server_client;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HandshakeCompletedListener;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {

    ServerThread mServerThread; // 아래에서 생성한 serverThread의 객체 인스턴스
    TextView mTextOutput; // main 스레드에서 출력할 문장 변수


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextOutput = (TextView)findViewById(R.id.textOutput);
    }

    // 현재 사용한 모든 포트 및 주소 정보를 없애기
    // 포트에 대한 정보를 제대로 처리하지 않으면 계속 가지고 있는다. 그래서 필요
    protected void onDestroy(){
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void mOnClick(View v){
        // mOnClick은 start와 Quit에서 모두 사용됨으로 어떤 버튼이 눌렸을지를 알아야 한다.
        switch (v.getId()){

            case R.id.btnStart:
                // start일 경우 서버 스레드를 구동 시켜주어야 한다.

                if(mServerThread == null){
                    // 서버 스레드가 없을 경우
                    mServerThread = new ServerThread(this, mMainHandler);
                    mServerThread.start();
                }
                break;
            case R.id.btnQuit:
                finish();
                break;
        }
    }

    // handler를 구현하여 메시지 큐에 쌓여 있는 메시지를 case에 따라 작동시킨다.
    private Handler mMainHandler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 1:
                    // 1일 때는 메지시를 덧붙여서 출력
                    mTextOutput.append((String)msg.obj);
                    break;
            }
        }
    };
}

// 서버 스레드 만들기

class ServerThread extends Thread {
    private Context mContext; // 메인 스레드에서의 context가 전달됨
    private Handler mMainHandler;
    // 메시지 큐에 들어있는 메시지를 다루는 변수
    // 생성자를 통해 메인 스레드의 핸들러를 받아 자신의 메시지를 메인 스레드의 핸들러로 전송

    static byte[] encrypted = new byte[32];
    static byte[] iv = new byte[16];
    static byte[] recovery = new byte[32];

    static PublicKey publicKey = null;
    static PrivateKey privateKey = null;
    static byte[] PublicKeyBytes = null;
    static byte[] PrivateKeyBytes = null;

    static byte[] encrypted_key = new byte[256];
    static byte[] recovery_key = null;

    static SecretKey secret_recover = null;

    static String name = null;
    static String passward = null;
    static String ID = null;
    static Integer age = null;

    // 서버 스레드 구현(생성자)
    public ServerThread(Context context, Handler mainHandler){
        mContext = context; // main 스레드의 context
        mMainHandler = mainHandler; // main 스레드의 handler
        // main 스레드에 메시지를 전달할 때 사용된다
    }

    // 서버 스레드 클래스의 메인 함수와 같은 역할
    public void run(){
        ServerSocket servSock = null; // 서버 소켓 변수 생성

        try{
            // 클라이언트에서 요청이 들어오면 포트를 9000번으로 하여 소켓 생성
            servSock = new ServerSocket(9000);
            WifiManager wifiMgr = (WifiManager)mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            // wifi를 관리할 수 있는 객체 인스턴스
            // wifi와 관련된 통신용 정보를 얻어옴

            WifiInfo wifiInfo = wifiMgr.getConnectionInfo();
            // wifi에 관한 정보를 관리할 수 있는 객체 인스턴스

            int serverip = wifiMgr.isWifiEnabled() ? wifiInfo.getIpAddress():0x0100007F;
            // wifiMgr을 통하여 ip 주소 얻어오기
            // 설정되어 있는 ip 주소를 얻어오는 것

            doPrintIn(">> 서버 시작!"+ ipv4ToString(serverip) + "/" + servSock.getLocalPort());
            // 서버 시작과 ip주소와 포트 번호를 메시지로 주어 main 스레드에서 출력할 수 있도록 한다.

            Keygenerate();
            // 공개키와 개인키 만들기

            // while문을 통하여 클라이언트로 부터 요청이 올 때까지 대기
            // 요청이 올때 까지는 while문의 코드가 실행되지 않는다.
            while(true){
                Socket sock = servSock.accept();
                // 요청 개수 만큼 sock이 생성된다.

                String ip = sock.getInetAddress().getHostAddress();
                // 소켓을 통하여 상대방의 ip주소를 얻어온다.

                int port = sock.getPort();
                // 상대방의 포트 번호 얻어오기

                doPrintIn(">> 클라이언트 접속 : " + ip + "/ " + port);
                // 클라이언트의 정보를 출력

                InputStream in = sock.getInputStream();
                final OutputStream out = sock.getOutputStream();
                // 클라이언트와 소통하기 위해 input, output stream을 만든다.

                Charset charset = Charset.forName("UTF-8");

                out.write(PublicKeyBytes);
                // 무한 루프를 돌면서 클라이언트에게서 온 메시지를 받는다.
                while(true){
                    // 입출력과 관련된 부분이라 try catch 문이 필요
                    try {
                        byte[] buf = new byte[1024];
                        // 클라이언트와 소통하기 위해 필요한 공간

                        int nbytes = in.read(buf);
                        // 메시지의 byte 수
                        // buf로 받은 데이터를 옮긴다.

                        if (nbytes > 0) {
                            // 클라이언트에게서 메시지가 옴

                            for(int i = 0; i < 256; i++){
                                encrypted_key[i] = buf[i];
                            }
                            for(int i = 256; i < 288; i++){
                                encrypted[i-256] = buf[i];
                            }
                            for(int i = 288; i < 304; i++){
                                iv[i-288] = buf[i];
                            }

//                            String Hex_key = changeHex(encrypted_key);
//                            String Hex_enc = changeHex(encrypted);
//                            String Hex_iv = changeHex(iv);
//
//                            doPrintIn("키" + Hex_key+"\n");
//                            doPrintIn("암호문" + Hex_enc+"\n");
//                            doPrintIn("초기값" + Hex_iv+"\n");

                            // 클라이언트로 부터 받은 암호화된 대칭키를 복호화
                            if(recovery_key == null){
                                Decrytped_key();
                            }

                            // 복호화한 대칭키로 암호화된 암호문 복호화 하기
                            Cipher();

                            String recovered = new String(recovery, charset);

                            String[] data = recovered.split("/");

                            if(data.length == 4){
                                name = data[0];
                                ID = data[1];
                                passward = data[2];
                                age = Integer.parseInt(data[3]);

                                doPrintIn("회원가입자 정보");
                                doPrintIn("이름 : " + name);
                                doPrintIn("아이디 : " + ID);
                                doPrintIn("비밀번호 : " + passward);
                                doPrintIn("나이 : " + age);

                                //<추가 구현> 데이터 베이스 파일로 정보 보내기
                                Response.Listener<String> responseListener = new Response.Listener<String>() {
                                    @Override
                                    public void onResponse(String response) {
                                        try{
                                            JSONObject jsonObject = new JSONObject(response);
                                            doPrintIn("Json 객체 생성");
                                            boolean success = jsonObject.getBoolean("success");
                                            doPrintIn("success boolean 생성");
                                            if(success){
                                                String msg = "회원가입을 축하드립니다.";
                                                //out.write(msg.getBytes());
                                                doPrintIn(msg);
                                            } else{
                                                String msg = "회원가입에 실패하였습니다.";
                                                //out.write(msg.getBytes());
                                                doPrintIn(msg);
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                };

                                doPrintIn("데이터 베이스로 값 넣기");
                                RegisterRequest registerRequest = new RegisterRequest(ID, passward, name, age, responseListener);
                                RequestQueue queue = Volley.newRequestQueue(mContext.getApplicationContext());
                                queue.add(registerRequest);

                                doPrintIn("종료하기");

                            }else if(data.length == 2){
                                ID = data[0];
                                passward = data[1];

                                doPrintIn("로그인자 정보");
                                doPrintIn("아이디 : " + ID);
                                doPrintIn("비밀번호 : " + passward);

                                // <추가 구현> 데이터 베이스 파일로 정보를 보내 확인 값 받기

                            }else{
                                String mes = "모든 정보를 입력해주세요.";
                                out.write(mes.getBytes());
                            }

                        } else {
                            // 클라이언트에게서 메시지가 끝남
                            doPrintIn(">> 클라이언트 종료: " + ip + "/" + port);
                            break;
                        }
                    }
                    catch (IOException e){
                        // 아예 클라이언트에게서 메시지가 오지 않음
                        doPrintIn(">> 클라이언트 종료: " + ip + "/" + port);
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // while문을 벗어 났다는 것은 대화가 끝났다는 것이니 sock을 닫아준다.
                sock.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 대화 뿐만 아니라 서버가 끝남
            try{
                // 서버가 있을 경우
                if(servSock != null){
                    servSock.close();
                }
                doPrintIn(">> 서버 종료!! ");
            }catch (IOException e){
                // 서버가 없을 경우
                doPrintIn(e.getMessage());
            }
        }
    }

    // 대칭키 복호화
    public static void Cipher() throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        if(secret_recover == null)
            secret_recover = new SecretKeySpec(recovery_key, "AES");

        try {
            cipher.init(Cipher.DECRYPT_MODE, secret_recover, new IvParameterSpec(iv));
        }catch (InvalidAlgorithmParameterException e){
            e.printStackTrace();
        }
        recovery = cipher.doFinal(encrypted);
    }

    // 하이브리드 암호에서 공개키와 개인키 생성하여 파일로 저장하기
    public static void Keygenerate() throws Exception {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
        generator.initialize(2048);

        KeyPair pair = generator.generateKeyPair();
        publicKey = pair.getPublic();
        privateKey = pair.getPrivate();

        PublicKeyBytes = publicKey.getEncoded();
        PrivateKeyBytes = privateKey.getEncoded();
    }

    // 클라이언트로 부터 받은 암호화된 대칭키를 개인키로 복호화 하기
    public static void Decrytped_key() throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        recovery_key = cipher.doFinal(encrypted_key);
    }

    // worker 스레드에서 바로 main 스레드의 UI를 건들이면 충돌이 발생하여 오류가 난다고 했기에
    // doPrinIn은 worker 스레드에서 따로 message를 생성하여 main 스레드에 보내준다.
    private void doPrintIn(String str){
        Message msg = Message.obtain(); // 메시지 객체 구성
        msg.what = 1; // 메시지의 타입 설정
        msg.obj = str + "\n";
        mMainHandler.sendMessage(msg);
        // main 스레드로 msg를 전달해주기
    }

    private String ipv4ToString(int ip){
        int a = (ip) & 0xFF, b = (ip >> 8) & 0xFF;
        int c = (ip >> 16) & 0xFF, d = (ip >> 24) & 0xFF;
        return Integer.toString(a) + "." + Integer.toString(b) + "." + Integer.toString(c) + "." +
                Integer.toString(d);
    }

//    public static String changeHex(byte[] arr){
//        StringBuilder sb = new StringBuilder();
//        for(final byte b: arr)
//            sb.append(String.format("%02x ", b&0xff));
//        return sb.toString();
//    }
}