package com.cookandroid.class_14_myclient;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.Charset;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class MainActivity extends AppCompatActivity {

    private ClientThread mClientThread;
    private EditText mEditIp, mEditname, mEditid, mEditpassword, mEditage;
    private Button mBtnConnect, mBtnSend, mBtnlogin, mBtnwrite;
    private TextView mTextOutput;

    static byte[] encryptedData = null; // 대칭키로 암호화한 암호문
    static byte[] ivData = null; // 초기값
    static byte[] keyData = null; // 대칭키
    static byte[] encrypted_key = null; // 공개키로 암호화한 대칭키

    static byte[] publickeyBytes = null; // 공개키

    static SecretKey key = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mEditIp = (EditText)findViewById(R.id.editIp);
        mEditname = (EditText)findViewById(R.id.editname);
        mEditid = (EditText)findViewById(R.id.editid);
        mEditpassword = (EditText)findViewById(R.id.editpassword);
        mEditage = (EditText)findViewById(R.id.editage);

        mBtnConnect = (Button) findViewById(R.id.btnConnect);
        mBtnSend = (Button) findViewById(R.id.btnSend);
        mBtnlogin = (Button)findViewById(R.id.btnlogin);
        mBtnwrite = (Button)findViewById(R.id.btnwrite);

        mTextOutput = (TextView)findViewById(R.id.textOutput);

    }

    protected void onDestroy(){
        super.onDestroy();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void mOnClick(View v) throws Exception {
        String name = null;
        String password = null;
        String ID = null;
        String age = null;

       switch (v.getId()){
           case R.id.btnConnect:

               symkeygenerate();
               if(mClientThread == null){
                   String str = mEditIp.getText().toString();
                   if(str.length() != 0){
                       mClientThread = new ClientThread(str, mMainHandler);
                       mClientThread.start();
                       mBtnConnect.setEnabled(false);
                       mBtnSend.setEnabled(true);
                       mBtnlogin.setEnabled(true);
                   }
               }
               break;
           case  R.id.btnQuit:
               finish();
               break;

           case R.id.btnSend:

               name = mEditname.getText().toString();
               ID = mEditid.getText().toString();
               password = mEditpassword.getText().toString();
               age = mEditage.getText().toString();

               if(name.isEmpty() || ID.isEmpty() || password.isEmpty() || age.isEmpty()) {
                   mTextOutput.append("모든 정보를 입력해주세요.\n");
               }else{

                    String text = name + "/" + ID + "/" + password + "/" + age;
                    Cipher(text);

                    if(SendThread.mHandler != null) {

                        StringBuilder sc = new StringBuilder();

                        Message msg = Message.obtain(); // 키 + 암호문 + 초기값

                        byte[] b = new byte[304];

                        String Hex_key = changeHex(encrypted_key);
                        String Hex_enc = changeHex(encryptedData);
                        String Hex_iv = changeHex(ivData);

                        mTextOutput.append("정보 합치기");

                        for (int i = 0; i < 256; i++) {
                            b[i] = encrypted_key[i];
                        }
                        for (int i = 256; i < 288; i++) {
                            b[i] = encryptedData[i - 256];
                        }
                        for (int i = 288; i < 304; i++) {
                            b[i] = ivData[i - 288];
                        }

                        String total = changeHex(b);
//
//                      mTextOutput.append("키" + Hex_key + "\n");
//
//                      mTextOutput.append("암호문" + Hex_enc + "\n");
//
//                      mTextOutput.append("초기값" + Hex_iv + "\n");
//
//                      mTextOutput.append("\n합친 바이트 배열" + total + "\n");

                        msg.what = 1;

                        msg.obj = b;
                        SendThread.mHandler.sendMessage(msg);
                    }
               }
               break;

           case R.id.btnlogin:

               ID = mEditid.getText().toString();
               password = mEditpassword.getText().toString();

               if(ID.isEmpty() || password.isEmpty()){
                   mTextOutput.append("모든 정보를 입력해주세요.\n");
               }
               else{

                   String login_text = ID + "/" + password;
                   Cipher(login_text);
                   if(SendThread.mHandler != null){

                       StringBuilder sc = new StringBuilder();

                       Message msg = Message.obtain(); // 키 + 암호문 + 초기값

                       byte[] b = new byte[304];

                       String Hex_key = changeHex(encrypted_key);
                       String Hex_enc = changeHex(encryptedData);
                       String Hex_iv = changeHex(ivData);

                       for(int i = 0; i < 256; i++){
                           b[i] = encrypted_key[i];
                       }
                       for(int i = 256; i < 288; i++){
                           b[i] = encryptedData[i-256];
                       }
                       for(int i = 288; i < 304; i++){
                           b[i] = ivData[i-288];
                       }

                       String total = changeHex(b);

//                   mTextOutput.append("키" + Hex_key + "\n");
//
//                   mTextOutput.append("암호문" + Hex_enc + "\n");
//
//                   mTextOutput.append("초기값" + Hex_iv + "\n");
//
//                   mTextOutput.append("\n합친 바이트 배열" + total + "\n");

                       msg.what = 1;

                       msg.obj = b;
                       SendThread.mHandler.sendMessage(msg);
                   }
               }
               break;
       }
    }

    private Handler mMainHandler = new Handler(){
        public void handleMessage(Message msg){
            switch (msg.what){
                case 1:
                    mTextOutput.append((String)msg.obj);
                    break;
                case 3:
                    publickeyBytes = (byte[])msg.obj;

                    try {
                        Cipher_sysmetrickey();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };

    // 공개키를 기반으로 대칭키 암호화하기
    public static void Cipher_sysmetrickey() throws Exception {
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(new X509EncodedKeySpec(publickeyBytes));

        Cipher cipher = Cipher.getInstance("RSA/ECB/OAEPWithSHA-256AndMGF1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        encrypted_key = cipher.doFinal(keyData);
    }


    // 대칭키를 기반으로 암호화하여 키 먼저 생성하기
    // 서버로 부터 받은 공개키로 먼저 암호화 하기 위해
    public static void symkeygenerate() throws Exception {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        keyGenerator.init(128);
        key = keyGenerator.generateKey();

        keyData = key.getEncoded();
    }

    // 뒤늦게 받은 평문을 암호화하기
    public static void Cipher(String text) throws Exception {
        Charset charset = Charset.forName("UTF-8");

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        ivData = new SecureRandom().getSeed(16);

        String plaintext = text;
        try{
            cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(ivData));
        }catch (InvalidAlgorithmParameterException e){
            e.printStackTrace();
        }

        encryptedData = cipher.doFinal(plaintext.getBytes(charset));
    }

    public static String changeHex(byte[] arr){
        StringBuilder sb = new StringBuilder();
        for(final byte b: arr)
            sb.append(String.format("%02x ", b&0xff));
        return sb.toString();
    }
}

class ClientThread extends Thread{
    private String mServAdd; // 서버와의 연결을 위한 변수
    private Handler mMainHandler; // 메인 함수의 문장을 출력하기 위한 핸들러

    public ClientThread(String servAdd, Handler mainHandler){
        mServAdd = servAdd;
        mMainHandler = mainHandler;
    }

    public void run(){
        Socket sock = null;
        try{
            sock = new Socket(mServAdd, 9000);
            doPrintIn(">> 서버와 연결 성공!");
            SendThread sendThread = new SendThread(this, sock.getOutputStream());
            RecvThread recvThread = new RecvThread(this, sock.getInputStream());
            sendThread.start();
            recvThread.start();
            try {
                sendThread.join(); // sendThread의 작업이 끝날 때 까지 대기
                recvThread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            doPrintIn(e.getMessage());
        } finally {
            try{
                if(sock != null){
                    sock.close();
                    doPrintIn("서버와 연결 종료!");
                }
            } catch (IOException e) {
                doPrintIn(e.getMessage());
            }
        }
    }

    // 공개키를 제외한 평문일 때는
    public void doPrintIn(String str){
        Message msg = Message.obtain(); // 메시지 객체 구성
        msg.what = 1; // 메시지의 타입 설정
        msg.obj = str + "\n";
        mMainHandler.sendMessage(msg);
        // main 스레드로 msg를 전달해주기
    }

    // 공개키 일때 바로 전달해주기
    public void doPrintIn_direct(byte[] arr){
        Message msg = Message.obtain();
        msg.what = 3;
        msg.obj = arr;
        mMainHandler.sendMessage(msg);
    }
}

// 클라이언트가 서버와 연결되면 클라이언트에서는 sendThread와 recvThread가 생성된다.
class SendThread extends Thread{
    private ClientThread mClientThread;
    // 지금의 sendThread를 만든 클라이언트의 주체
    private OutputStream mOutStream;
    // 메시지 전송을 위해 output 스트림을 생성
    public static Handler mHandler;
    // 메인 함수의 핸들러를 받아와서 메인과 소통하기 위해 생성
    // static으로 선언되었기에 SendThread의 객체를 생성하지 않아도 사용 가능

    public SendThread(ClientThread clientThread, OutputStream outStream){
        mClientThread = clientThread;
        mOutStream = outStream;
    }

    public void run(){
        Looper.prepare();
        // 메인 메시지를 받은 것을 서버로 전달해 주기 위해 루퍼를 생성
        // 메인과의 통신이 아니면 무조건 루퍼를 생성해 줘어야 한다

        mHandler = new Handler(){
            public void handleMessage(Message msg){
                switch (msg.what){
                    case 1:
                        try{
                            byte[] s = (byte[]) msg.obj;
                            mOutStream.write(s);
                            mClientThread.doPrintIn("[보낸 데이터]" + s);
                        } catch (IOException e) {
                            mClientThread.doPrintIn(e.getMessage());
                        }
                        break;

                    case 2:
                        getLooper().quit();
                        break;
                }
            }
        };

        Looper.loop();
    }
}

class RecvThread extends Thread{
    private ClientThread mClientThread;
    // 지금의 sendThread를 만든 클라이언트의 주체
    private InputStream mInStream;
    // 메시지 전송을 위해 output 스트림을 생성

    public RecvThread(ClientThread clientThread, InputStream inStream){
        mClientThread = clientThread;
        mInStream = inStream;
    }

    public void run(){

        byte[] buf = new byte[1024];
        while(true){
            try{
                int nbytes = mInStream.read(buf);
                if(nbytes > 0){

                    StringBuilder sc = new StringBuilder();
                    for(final  byte b : buf)
                        sc.append(String.format("%02x ", b&0xff));
                    String s = sc.toString();
                    String check = s.substring(0,5);
                    //mClientThread.doPrintIn("[받은 데이터]" + check);

                    // 서버에서 온 값이 공개키 일 때
                    if(check.equals("30 82")){
                        mClientThread.doPrintIn_direct(buf);
                    }else{
                        String plain = new String(buf, 0, nbytes);
                        mClientThread.doPrintIn("[받은 데이터]" + plain);
                    }
//                    // Hex 값으로 바꾸기
//                    StringBuilder sc = new StringBuilder();
//                    for(final  byte b : buf)
//                        sc.append(String.format("%02x ", b&0xff));
//                    String s = sc.toString();

                } else{
                    mClientThread.doPrintIn(">> 서버가 연결 끊음!");
                    if(SendThread.mHandler != null){
                        Message msg = Message.obtain();
                        msg.what = 2;
                        SendThread.mHandler.sendMessage(msg);
                    }
                    break;
                }
            }catch (Exception e){
                mClientThread.doPrintIn(e.getMessage());
            }
        }
    }
}

