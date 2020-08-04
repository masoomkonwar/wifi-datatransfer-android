package com.jec.file_transfer2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.TypedArrayUtils;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MainActivity extends AppCompatActivity {
    EditText IPText,data;
    Button sndbtn,connectbtn,listenbtn;
    String IP_Address;
    String ConnectionIP;
    boolean isListening , isSender;
    public final File directory = Environment.getExternalStorageDirectory();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA}, PackageManager.PERMISSION_GRANTED);
        IPText = (EditText) findViewById(R.id.IP);
        data = (EditText) findViewById(R.id.data);
        sndbtn = (Button) findViewById(R.id.sndbtn);
        connectbtn = (Button) findViewById(R.id.connect);
        final Thread myThread = new Thread(new myServer());
        final  Thread myThread2  = new Thread(new mySendServer());
        isListening = false;
        isSender = false;
        listenbtn = (Button) findViewById(R.id.listen);
        listenbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isListening)
                myThread.start();
                isListening = true;
                isSender = true;
                connectbtn.setVisibility(View.INVISIBLE);
                myThread2.start();
            }
        });
        sndbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackgroundTask b = new BackgroundTask();
                if(!isSender)
                    ConnectionIP = "192.168.43.1";
                IPText.setText(ConnectionIP);
                b.execute(IPText.getText().toString(),data.getText().toString());
            }
        });
        WifiManager  wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipInt = wifiInfo.getIpAddress();
        try {
            IP_Address = InetAddress.getByAddress(ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(ipInt).array()).getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        connectbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isListening) {
                    myThread.start();
                    isListening = true;
                }
                isSender=false;
                listenbtn.setVisibility(View.INVISIBLE);
                BackgroundTask b1 = new BackgroundTask();
                b1.execute("192.168.43.1",IP_Address);
            }
        });
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }



    class BackgroundTask extends AsyncTask<String,Void,String>
    {   Socket socket;
        DataOutputStream dos;
        String ip,message;
        @Override
        protected String doInBackground(String... strings) {
            ip = strings[0];
            message = strings[1];
            try {
                socket = new Socket(ip, 9060);
                dos = new DataOutputStream(socket.getOutputStream());
                dos.writeUTF(message);
                dos.close();
            }catch (IOException ex)
            {
                ex.printStackTrace();
            }
            return null;
        }
    }
    class BackgroundTask2 extends AsyncTask<String,Void,String>
    {   Socket socket;

        OutputStream dos;
        String ip,message;
        @Override
        protected String doInBackground(String... strings) {
            ip = strings[0];
            message = strings[1];
            try {
                byte[] b = new byte[20000];
                socket = new Socket(ip, 9061);
                dos = socket.getOutputStream();
                FileInputStream fileInputStream = new FileInputStream(Environment.getExternalStorageDirectory().getAbsolutePath()+"/ASHROY2/1.txt");
                fileInputStream.read(b,0,b.length);
                dos.write(b,0,b.length);
                fileInputStream.close();
                dos.close();
            }catch (IOException ex)
            {
                ex.printStackTrace();
            }
            return null;
        }
    }
    class myServer implements Runnable
    {
        ServerSocket serverSocket;
        Socket socket;
        DataInputStream dis;
        String message;
        Handler handler = new Handler();
        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(9060);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(),"waiting for client",Toast.LENGTH_SHORT).show();
                    }
                });
                while (true)
                {
                    socket = serverSocket.accept();
                    dis = new DataInputStream(socket.getInputStream());
                    message = dis.readUTF();
                    if(message.contains("192.168"))
                    {
                        ConnectionIP = message;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(),"connection successful",Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                      else {  handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "message :" + message, Toast.LENGTH_LONG).show();
                            }
                        }); }
                }


            }catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }

    }
    class mySendServer implements Runnable{
        ServerSocket serverSocket;
        Socket socket;
        InputStream dis;
        @Override
        public void run() {
           // Handler handler = new Handler();
            try {
                serverSocket = new ServerSocket(9061);
                byte[] b = new byte[20000];

                while (true)
                {
                    socket = serverSocket.accept();
                    dis = socket.getInputStream();
                     FileOutputStream fos = new FileOutputStream(Environment.getExternalStorageDirectory().getAbsolutePath()+"/ASHROY2/2.txt");
                     dis.read(b,0,b.length);
                     fos.write(b,0,b.length);
                     fos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }
    public void sendFile(View view)
    {
        System.out.println("SendFile");
        File dir = new File(directory.getAbsolutePath()+"/FileSender/sent/");

        dir.mkdir();
        File dir2 = new File(directory.getAbsolutePath()+"/FileSender/received/");
        dir2.mkdir();
        File newFile = new File(dir,"1.txt");
        System.out.println("SendFile2");
        //BackgroundTask2 b2 = new BackgroundTask2();
        //if(!isSender)
         //   ConnectionIP = "192.168.43.1";
        //IPText.setText(ConnectionIP);
        //b2.execute(IPText.getText().toString(),data.getText().toString());
    }
    public void CameraButton(View view)
    {
        //index++;
        File dir = new File(directory.getAbsolutePath()+"/ASHROY2/");
        dir.mkdir();
        File newFile = new File(dir,"1.jpg");
        try {
            newFile.createNewFile();
        }catch (Exception ex)
        {
            ex.printStackTrace();
        }
        Uri outputfileuri = Uri.fromFile(newFile);
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,outputfileuri);
        startActivity(cameraIntent);
        //imageUri = outputfileuri;
        //img.setImageURI(imageUri);
        BackgroundTask2 b2 = new BackgroundTask2();
        if(!isSender)
           ConnectionIP = "192.168.43.1";
        IPText.setText(ConnectionIP);
        b2.execute(IPText.getText().toString(),data.getText().toString());
    }
}
