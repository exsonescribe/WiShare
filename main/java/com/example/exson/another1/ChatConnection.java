package com.example.exson.another1;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class ChatConnection {

    private Handler mUpdateHandler;
    private ChatServer mChatServer;
    private ChatClient mChatClient;

    private static final String TAG = "ChatServer";

    private Socket mSocket;
    private int mPort = -1;

    public ChatConnection(Handler handler) {
        mUpdateHandler = handler;
        mChatServer = new ChatServer(handler);
    }

    public void tearDown() {
        mChatServer.tearDown();
        mChatClient.tearDown();
    }

    public void connectToServer(InetAddress address, int port) {
        Log.d(TAG, String.valueOf(address));
        Log.d(TAG, String.valueOf(port));
        mChatClient = new ChatClient(address, port);
    }

    public int getLocalPort() {
        return mPort;
    }

    public void setLocalPort(int port) {
        mPort = port;
    }

    public void sendMessage(File fileToSend) {
        if (mChatClient != null) {
            mChatClient.sendMessage(fileToSend);
        }
    }

    private synchronized void setSocket(Socket socket) {
        Log.d(TAG, "setSocket being called.");
        if (socket == null) {
            Log.d(TAG, "Setting a null socket.");
        }
        else if (mSocket != null) {
            if (mSocket.isConnected()) {
                try {
                    mSocket.close();
                } catch (IOException e) {
                    // TODO(alexlucas): Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        mSocket = socket;
    }

    private Socket getSocket() {
        return mSocket;
    }

    private class ChatServer {
        ServerSocket mServerSocket = null;
        Thread mThread = null;

        public ChatServer(Handler handler) {
            mThread = new Thread(new ServerThread());
            mThread.start();
        }

        public void tearDown() {
            mThread.interrupt();
            try {
                mServerSocket.close();
            } catch (IOException ioe) {
                Log.e(TAG, "Error when closing server socket.");
            }
        }

        class ServerThread implements Runnable {

            @Override
            public void run() {

                try {
                    // Since discovery will happen via Nsd, we don't need to care which port is
                    // used.  Just grab an available one  and advertise it via Nsd.
                    mServerSocket = new ServerSocket(0);
                    setLocalPort(mServerSocket.getLocalPort());

                    while (!Thread.currentThread().isInterrupted()) {
                        Log.d(TAG, "ServerSocket Created, awaiting connection");
                        setSocket(mServerSocket.accept());
                        Log.d(TAG, "Connected.");
                        if (mChatClient == null) {
                            int port = mSocket.getPort();
                            InetAddress address = mSocket.getInetAddress();
                            connectToServer(address, port);
                        }
                    }
                } catch (IOException e) {
                    Log.e(TAG, "Error creating ServerSocket: ", e);
                    e.printStackTrace();
                }
            }
        }
    }

    private class ChatClient {

        private InetAddress mAddress;
        private int PORT;

        private final String CLIENT_TAG = "ChatClient";

        private Thread mSendThread;
        private Thread mRecThread;

        public ChatClient(InetAddress address, int port) {

            Log.d(CLIENT_TAG, "Creating chatClient");
            this.mAddress = address;
            this.PORT = port;

            mSendThread = new Thread(new SendingThread());
            mSendThread.start();
        }

        class SendingThread implements Runnable {

            BlockingQueue<File> mMessageQueue;
            private int QUEUE_CAPACITY = 10;

            public SendingThread() {
                mMessageQueue = new ArrayBlockingQueue<File>(QUEUE_CAPACITY);
            }

            @Override
            public void run() {
                try {
                    if (getSocket() == null) {
                        setSocket(new Socket(mAddress, PORT));
                        Log.d(CLIENT_TAG, "Client-side socket initialized.");

                    } else {
                        Log.d(CLIENT_TAG, "Socket already initialized. skipping!");
                    }

                    mRecThread = new Thread(new ReceivingThread());
                    mRecThread.start();

                } catch (UnknownHostException e) {
                    Log.d(CLIENT_TAG, "Initializing socket failed, UHE", e);
                } catch (IOException e) {
                    Log.d(CLIENT_TAG, "Initializing socket failed, IOE.", e);
                }

                while (true) {
                    try {
                        File file = mMessageQueue.take();
                        sendMessage(file);
                    } catch (InterruptedException ie) {
                        Log.d(CLIENT_TAG, "Message sending loop interrupted, exiting");
                    }
                }
            }
        }

        class ReceivingThread implements Runnable {

            int bytesRead;
        /*    int current = 0;

            FileOutputStream fos;
            BufferedOutputStream bos;
            OutputStream output;
            DataOutputStream dos;
            int len;
            int smblen;
            InputStream in;
            DataInputStream clientData;
            BufferedInputStream clientBuff;
            boolean flag=true; */

            @Override
            public void run() {

                try {
                    InputStream in = mSocket.getInputStream();
                    DataInputStream clientData = new DataInputStream(in);
                    Log.d(TAG, "About to start handshake");
                    OutputStream output = null;

                    while(true) {

                        String fileName = clientData.readUTF();

                            output = new FileOutputStream("mnt/sdcard/" + fileName);
                            long size = clientData.readLong();
                            byte[] buffer = new byte[1024];
                            while (size > 0 && (bytesRead = clientData.read(buffer, 0, (int) Math.min(buffer.length, size))) != -1) {
                                output.write(buffer, 0, bytesRead);
                                size -= bytesRead;

                            Log.d(TAG, "File Transfer Complete");

                        }

                        // Closing the FileOutputStream handle
                        in.close();
                        clientData.close();
                        output.close();

                    }

                }
                catch (IOException e) {
                    Log.e(CLIENT_TAG, "Server loop error: ", e);
                }
            }




    /* BATCH SENDING
                        while(flag){

                            in = socket.getInputStream(); //used
                            clientData = new DataInputStream(in); //use
                            clientBuff = new BufferedInputStream(in); //use

                            Log.d(TAG, "Starting.....");

                            int fileSize = clientData.read();

                            ArrayList<File> files=new ArrayList<File>(fileSize); //store list of filename from client directory
                            ArrayList<Integer>sizes = new ArrayList<Integer>(fileSize); //store file size from client
                            //Start to accept those filename from server
                            for (int count=0;count < fileSize;count ++){
                                File ff=new File(clientData.readUTF());
                                files.add(ff);
                            }

                            for (int count=0;count < fileSize;count ++){

                                sizes.add(clientData.readInt());
                            }

                            for (int count =0;count < fileSize ;count ++){

                                if (fileSize - count == 1){
                                    flag =false;
                                }

                                len=sizes.get(count);

                                Log.d(TAG, "File Size ="+len);

                                output = new FileOutputStream("mnt/sdcard/" + files.get(count));
                                dos=new DataOutputStream(output);
                                bos=new BufferedOutputStream(output);

                                byte[] buffer = new byte[1024];

                                bos.write(buffer, 0, buffer.length); //This line is important

                                while (len > 0 && (smblen = clientData.read(buffer)) > 0) {
                                    dos.write(buffer, 0, smblen);
                                    len = len - smblen;
                                    dos.flush();
                                }
                                dos.close();  //It should close to avoid continue deploy by resource under view
                            }

                        } */

        }

        public void tearDown() {
            try {

                getSocket().close();
            } catch (IOException ioe) {
                Log.e(CLIENT_TAG, "Error when closing server socket.");
            }
        }

        public void sendMessage(File fileToSend) {

            try {

                Socket socket = getSocket();
                Log.d(TAG, "About to start handshake");
                if (socket == null) {
                    Log.d(CLIENT_TAG, "Socket is null, wtf?");
                } else if (socket.getOutputStream() == null) {
                    Log.d(CLIENT_TAG, "Socket output stream is null, wtf?");
                }

                    File myFile = new File(String.valueOf(fileToSend));
                    byte[] mybytearray = new byte[(int) myFile.length()];

                    FileInputStream fis = new FileInputStream(myFile);
                    BufferedInputStream bis = new BufferedInputStream(fis);
                    //bis.read(mybytearray, 0, mybytearray.length);

                    DataInputStream dis = new DataInputStream(bis);
                    dis.readFully(mybytearray, 0, mybytearray.length);

                    OutputStream os = socket.getOutputStream();

                    //Sending file name and file size to the server
                    DataOutputStream dos = new DataOutputStream(os);
                    dos.writeUTF(myFile.getName());
                    dos.writeLong(mybytearray.length);
                    dos.write(mybytearray, 0, mybytearray.length);
                    dos.flush();

                    //Sending file data to the server
                    os.write(mybytearray, 0, mybytearray.length);
                    os.flush();

                    //Closing socket
                    os.close();
                    dos.close();

                    Log.d(CLIENT_TAG, "File Transfer Complete, sent file: " + myFile.getName());

            } catch (UnknownHostException e) {
                Log.d(CLIENT_TAG, "Unknown Host", e);
            } catch (IOException e) {
                Log.d(CLIENT_TAG, "I/O Exception", e);
            } catch (Exception e) {
                Log.d(CLIENT_TAG, "Error3", e);
            }
            Log.d(CLIENT_TAG, "Client sent:" + fileToSend);
        }

           /* BATCH SENDING
                    File[] sendFile = myFile.listFiles();
                    Log.d(TAG, String.valueOf(sendFile.length));

                    OutputStream os = socket.getOutputStream();
                    DataOutputStream dos = new DataOutputStream(os);

                    dos.writeInt(sendFile.length);

                    for (int count=0;count<sendFile.length;count ++){
                        dos.writeUTF(sendFile[count].getName());

                    }
                    for (int count=0;count<sendFile.length;count ++){

                        int filesize = (int) sendFile[count].length();
                        dos.writeInt(filesize);
                    }

                    for (int count=0;count<sendFile.length;count ++){

                        int filesize = (int) sendFile[count].length();
                        byte [] buffer = new byte [filesize];

                        //FileInputStream fis = new FileInputStream(myFile);
                        FileInputStream fis = new FileInputStream(sendFile[count].toString());
                        BufferedInputStream bis = new BufferedInputStream(fis);

                        //Sending file name and file size to the server
                        bis.read(buffer, 0, buffer.length); //This line is important

                        dos.write(buffer, 0, buffer.length);
                        dos.flush();
                    } */

    }
}