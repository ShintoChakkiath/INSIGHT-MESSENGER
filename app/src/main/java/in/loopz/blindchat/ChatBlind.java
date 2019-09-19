package in.loopz.blindchat;


import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class ChatBlind extends AppCompatActivity implements MediaPlayer.OnCompletionListener{
    LinearLayout layout;
    RelativeLayout layout_2;
    ImageView record;
    ScrollView scrollView;
    Firebase reference1, reference2;
    Toolbar toolbar;
    ImageView userImage;
    TextView userName;
    TextView tvRcd;

    private static String mFileName = null;
    private static String mName = "audiotest";
    String messageText;
    private MediaRecorder mRecorder = null;
    StorageReference storageReference;
    ProgressDialog p;

    private MediaPlayer mPlayer = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_blind);

        layout = (LinearLayout) findViewById(R.id.layout1);
        layout_2 = (RelativeLayout)findViewById(R.id.layout2);
        record = (ImageView)findViewById(R.id.record);
        scrollView = (ScrollView)findViewById(R.id.scrollView);
        toolbar = (Toolbar)findViewById(R.id.toolbar);
        userImage = (ImageView)findViewById(R.id.userImage);
        userName = (TextView) findViewById(R.id.userName);
        tvRcd = (TextView) findViewById(R.id.tvrcd);

        userName.setText(UserDetails.chatWith);

        p=new ProgressDialog(this);
        storageReference= FirebaseStorage.getInstance().getReference();
        mFileName =getExternalCacheDir().getAbsolutePath()+"/"+mName+".3gp";

        Firebase.setAndroidContext(this);
        reference1 = new Firebase(UserDetails.BaseUrl+"messages/" + UserDetails.username + "_" + UserDetails.chatWith);
        reference2 = new Firebase(UserDetails.BaseUrl+"messages/" + UserDetails.chatWith + "_" + UserDetails.username);

        record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });
        record.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if(motionEvent.getAction()==MotionEvent.ACTION_DOWN){
                    startRecording();
                    tvRcd.setText("Recording...");
                    tvRcd.setVisibility(View.VISIBLE);
                }else if(motionEvent.getAction()==MotionEvent.ACTION_UP){
                    stopRecording();
                    tvRcd.setText("Recording finished");
                    tvRcd.setVisibility(View.GONE);
                }
                return false;
            }
        });

        reference1.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Map map = dataSnapshot.getValue(Map.class);
                String message = map.get("message").toString();
                String userName = map.get("user").toString();

                if(userName.equals(UserDetails.username)){
                    addMessageBox("You:-\n" + message, 1);
                }
                else{
                    addMessageBox(UserDetails.chatWith + ":-\n" + message, 2);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    public void addMessageBox(String message, int type){
        final TextView textView = new TextView(ChatBlind.this);
        textView.setText("_______________");
        textView.setTag(message);

        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        lp2.setMargins(0,5,0,5);
        lp2.weight = 1.0f;

        if(type == 1) {
            lp2.gravity = Gravity.RIGHT;
            textView.setPadding(20,15,20,15);
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_play,0,0,0);
            textView.setWidth(300);
        }
        else{
            lp2.gravity = Gravity.LEFT;
            textView.setPadding(20,15,20,40);
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_action_play,0,0,0);
            textView.setWidth(300);
        }
        textView.setLayoutParams(lp2);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                downloadFile(textView.getTag().toString());
            }
        });
        layout.addView(textView);
        scrollView.fullScroll(View.FOCUS_DOWN);
    }


    private void startRecording() {
        messageText=UserDetails.username + "_" + UserDetails.chatWith+DateFormat.getDateTimeInstance().format(new Date());;
        messageText=messageText.replace(" ","_");
        messageText=messageText.replace("-","_");
        messageText=messageText.replace(":","_");
        mName=messageText;
        mFileName =getExternalCacheDir().getAbsolutePath()+"/"+mName+".3gp";
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
            mRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void stopRecording() {
        try {
            mRecorder.stop();
            mRecorder.release();
            mRecorder = null;
            uploadVoice();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void uploadVoice() {
        p.setMessage("Uploading Voice");
        p.show();
        StorageReference reference=storageReference.child("Audio").child(mName+".3gp");
        Uri uri=Uri.fromFile(new File(mFileName));
        reference.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                p.dismiss();
                Log.e("message",messageText);
                Map<String, String> map = new HashMap<String, String>();
                map.put("message", messageText);
                map.put("user", UserDetails.username);
                reference1.push().setValue(map);
                reference2.push().setValue(map);
                Toast.makeText(ChatBlind.this, "Voice Sent", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                p.dismiss();
                Toast.makeText(ChatBlind.this, "Voice Sent Failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void downloadFile(String mFileName){
        p.setMessage("Downloading Voice..");
        p.show();
        Log.e("mfile",mFileName);
        mFileName=mFileName.replace("You:-\n","");
        final String url= "gs://blindchat-5fc1d.appspot.com/Audio/"+mFileName+".3gp";
        Log.e("mfile",url);
        StorageReference downRef=FirebaseStorage.getInstance().getReferenceFromUrl(url);
      //  StorageReference downRef=storageReference.child("Audio/"+mFileName+".3gp");
        Log.e("Address",downRef.toString());
        File localFile = null;
        try {
            localFile = File.createTempFile("Video", "3gp");
        } catch (IOException e) {
            e.printStackTrace();
        }

        downRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Toast.makeText(ChatBlind.this, "Voice Retrieved", Toast.LENGTH_SHORT).show();
                startPlaying(uri.toString());
                p.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                p.dismiss();
            }
        });
    }


    private void startPlaying(String url) {
        mPlayer = new MediaPlayer();
        try {
            mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mPlayer.setDataSource(url);

            mPlayer.prepare();
            mPlayer.start();
        } catch (IOException e) {
        }
    }

    private void stopPlaying() {
        mPlayer.release();
        mPlayer = null;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        mediaPlayer.release();
        mediaPlayer = null;
    }
}
