package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 * 
 * @author stevko
 *
 */
public class GroupMessengerActivity extends Activity {
    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    static final int SERVER_PORT = 10000;
    String [] remotePort = {"11108","11112","11116","11120","11124"};
    private static final String KEY_FIELD = "key";
    private static final String VALUE_FIELD = "value";
    private ContentResolver contentProvider;
    private int largestProposedSeq=0;
    private int largestAgreedSeq=0;
    private static int msgKey = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);
        contentProvider=getContentResolver();
        /*
         * Calculate the port number that this AVD listens on.
         * It is just a hack that I came up with to get around the networking limitations of AVDs.
         * The explanation is provided in the PA1 spec.
         */
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));

        try {
            /*
             * Create a server socket as well as a thread (AsyncTask) that listens on the server
             * port.
             *
             * AsyncTask is a simplified thread construct that Android provides. Please make sure
             * you know how it works by reading
             * http://developer.android.com/reference/android/os/AsyncTask.html
             */
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            /*
             * Log is a good way to debug your code. LogCat prints out all the messages that
             * Log class writes.
             *
             * Please read http://developer.android.com/tools/debugging/debugging-projects.html
             * and http://developer.android.com/tools/debugging/debugging-log.html
             * for more information on debugging.
             */
            Log.e(TAG, "Can't create a ServerSocket");
            return;
        }

        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());

        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));

        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */
        // Reference:https://developer.android.com/reference/android/widget/Button
        final EditText editText= (EditText) findViewById(R.id.editText1);
        Button sendButton= (Button) findViewById(R.id.button4);
        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
//                TextView tx=(TextView) findViewById(R.id.textView1);
                String msg=editText.getText().toString()+"\n";
                editText.setText("");
//                tx.append("\t"+msg);
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);

//                getContentResolver().insert()
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }

    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {
        PriorityQueue<Message> deliveryQueue=new PriorityQueue<Message>(25,new MessageComparator());
        final Uri mUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");
        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            Socket client;
            InputStream is;
            BufferedInputStream br;
            ByteArrayOutputStream ret;
            BufferedReader bf;
            String msg = "";
            DataInputStream di=null;
            DataOutputStream ds=null;
            //reference:https://docs.oracle.com/javase/7/docs/api/java/net/Socket.html
            //reference:https://docs.oracle.com/javase/7/docs/api/java/io/BufferedReader.html
            //Each process Q in a group g keeps AQg ,
            // the largest agreed sequence number it has seen in g and PgQ its own largest proposed sequence number.
            while(!serverSocket.isClosed()) {
                try {
                    client = serverSocket.accept();
                    is = client.getInputStream();
                   // bf = new BufferedReader(new InputStreamReader(is));
                    String s = null;
                    di=new DataInputStream(is);
                    msg=di.readUTF();
                    Message m=Message.decodeMessage(msg);
                    //first time message sent to all avds asking for proposed seq no.
                    //Each process Q replies to P with a proposal for the message’s agreed sequence number PgQ = max(AQg , PgQ) + 1.
                    // Q provisionally assigns the proposed sequence number to the message and places it in its hold-back queue.
                    if(m.getProposed_seq()==-1){
                        largestProposedSeq=Math.max(largestProposedSeq,largestAgreedSeq)+1;
                        m.setProposed_seq(largestProposedSeq);
                        deliveryQueue.add(m);
                        //send message to all avds with proposed sequence no
                        OutputStream os=client.getOutputStream();
                        ds=new DataOutputStream(os);
                       // os.write(Message.encodeMessage(m).getBytes());
                        ds.writeUTF(Message.encodeMessage(m));
                    }
                    else {
                        largestAgreedSeq = Math.max(m.getProposed_seq(), largestAgreedSeq);
                        //attach agreed seq to message,and reinsert
                        //Each process Q in g sets AQg = max(AQg , a)
                        // and attaches a to the message (identified by i), reordering the hold-back queue if necessary.
                        Message temp = null;
                        for (Message element : deliveryQueue) {
                            if (element.getAvd_id() == m.getAvd_id() && element.getMessage().equals(m.getMessage())) {
                                temp = element;
                                break;
                            }
                        }
                        deliveryQueue.remove(temp);
                        deliveryQueue.add(m);
                        //display messages
                        while (!deliveryQueue.isEmpty()&&deliveryQueue.peek().isDelivered()) {
                            Message remove = deliveryQueue.poll();
                            ContentValues c = new ContentValues();
                            c.put(KEY_FIELD, "" + msgKey++);
                            c.put(VALUE_FIELD, remove.getMessage());
                            contentProvider.insert(mUri, c);
                            publishProgress(remove.getMessage().replace("\n",""));
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Can't listen to client/issue with connection");
                }
            }
            return null;
        }




        protected void onProgressUpdate(String...strings) {
            /*
             * The following code displays what is received in doInBackground().
             */

            /*
             * The following code creates a file in the AVD's internal storage and stores a file.
             *
             * For more information on file I/O on Android, please take a look at
             * http://developer.android.com/training/basics/data-storage/files.html
             */
            String strReceived = strings[0].trim();
            TextView tx=(TextView) findViewById(R.id.textView1);
            tx.append(strReceived + "\t\n");
            //Reference:https://developer.android.com/guide/topics/data/data-storage#filesInternal
            String filename = "GroupMessengerActivity";
            // String filePath= getApplicationContext().getFilesDir().getPath()+ File.separator+filename;

            String string = strReceived + "\n";
            FileOutputStream outputStream;

            try {
                outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(string.getBytes());
                outputStream.close();
            } catch (Exception e) {
                Log.e(TAG, "File write failed");
            }

            return;
        }
    }

    /***
     * ClientTask is an AsyncTask that should send a string over the network.
     * It is created by ClientTask.executeOnExecutor() call whenever OnKeyListener.onKey() detects
     * an enter key press event.
     *
     * @author stevko
     *
     */
    private class ClientTask extends AsyncTask<String, Void, Void> {
        InputStream is;
        BufferedReader bf;
        String msg;
        String myPort;
        DataOutputStream ds=null;
        DataInputStream di=null;
        List<Integer> allProposedNo=new ArrayList<Integer>(5);
        Message newMsg;
        @Override
        protected Void doInBackground(String... msgs) {
            try {
                myPort=msgs[1];
                //When a process P wishes to multicast a message m to group g
                // it B-multicasts ⟨m, i⟩ to g, where i is a unique identifier for m.
                for(int i=0;i<remotePort.length;i++){
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(remotePort[i]));
                    String msgToSend = msgs[0];
                    //send message as delivered 'false' with avd and generated seq no.
                    Message m=new Message(-1,msgToSend,false,Integer.parseInt(myPort));
                    //socket.setSoTimeout(500);
                    //reference:https://docs.oracle.com/javase/7/docs/api/java/net/Socket.html
                    //reference:https://docs.oracle.com/javase/7/docs/api/java/io/OutputStream.html
                    OutputStream os=socket.getOutputStream();
                    ds=new DataOutputStream(os);
                    ds.writeUTF(Message.encodeMessage(m));
                   // os.write(Message.encodeMessage(m).getBytes());
                    is = socket.getInputStream();
                   // bf = new BufferedReader(new InputStreamReader(is));
                    di=new DataInputStream(is);
                    //msg=bf.readLine();
                    msg=di.readUTF();
                    newMsg=Message.decodeMessage(msg);
                    if(newMsg.getAvd_id()==Integer.parseInt(myPort)){
                        System.out.println(myPort+" "+newMsg.getProposed_seq());
                        allProposedNo.add(newMsg.getProposed_seq());
                    }
                    socket.close();
                }

            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            }
            //P collects all the proposed sequence numbers and selects the largest a; it then B-multicasts ⟨i, a⟩ to g.
            int agreedno=Collections.max(allProposedNo);
            newMsg.setProposed_seq(agreedno);
            newMsg.setDelivered(true);
            try {
                for(int i=0;i<remotePort.length;i++){
                    Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(remotePort[i]));
                    String msgToSend = Message.encodeMessage(newMsg);
                    //send message as delivered 'false' with avd and generated seq no.
                   // Message m=new Message(-1,msgToSend,false,Integer.parseInt(myPort));
                    //socket.setSoTimeout(500);
                    //reference:https://docs.oracle.com/javase/7/docs/api/java/net/Socket.html
                    //reference:https://docs.oracle.com/javase/7/docs/api/java/io/OutputStream.html
                    OutputStream os=socket.getOutputStream();
                    ds=new DataOutputStream(os);
                    ds.writeUTF(msgToSend);
                    socket.close();
                }

            } catch (UnknownHostException e) {
                Log.e(TAG, "ClientTask UnknownHostException");
            } catch (IOException e) {
                Log.e(TAG, "ClientTask socket IOException");
            }
            return null;
        }
    }

    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }
}
