package wycliffeassociates.recordingapp;

import android.app.Activity;
import android.media.MediaPlayer;
import android.media.MediaRecorder;

import android.os.Bundle;
import android.os.Environment;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import android.widget.ImageView;
import android.widget.Toast;
import java.io.IOException;


public class MainActivity extends Activity {
   Button play,stop,record;
   private MediaRecorder myAudioRecorder;
   private String outputFile = null;

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      play=(Button)findViewById(R.id.button3);
      stop=(Button)findViewById(R.id.button2);
      record=(Button)findViewById(R.id.button);

      stop.setEnabled(false);
      play.setEnabled(false);
      outputFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/recording.3gp";;

      myAudioRecorder=new MediaRecorder();
      myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
      myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
      myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
      myAudioRecorder.setOutputFile(outputFile);

      record.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            try {
               myAudioRecorder.prepare();
               myAudioRecorder.start();
            }

            catch (IllegalStateException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }

            catch (IOException e) {
               // TODO Auto-generated catch block
               e.printStackTrace();
            }

            record.setEnabled(false);
            stop.setEnabled(true);

            Toast.makeText(getApplicationContext(), "Recording started", Toast.LENGTH_LONG).show();
         }
      });

      stop.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            myAudioRecorder.stop();
            myAudioRecorder.release();
            myAudioRecorder  = null;

            stop.setEnabled(false);
            play.setEnabled(true);

            Toast.makeText(getApplicationContext(), "Audio recorded successfully",Toast.LENGTH_LONG).show();
         }
      });

      play.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) throws IllegalArgumentException,SecurityException,IllegalStateException {
            MediaPlayer m = new MediaPlayer();

            try {
               m.setDataSource(outputFile);
            }

            catch (IOException e) {
               e.printStackTrace();
            }

            try {
               m.prepare();
            }

            catch (IOException e) {
               e.printStackTrace();
            }

            m.start();
            Toast.makeText(getApplicationContext(), "Playing audio", Toast.LENGTH_LONG).show();
         }
      });
   }

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      // Inflate the menu; this adds items to the action bar if it is present.
      getMenuInflater().inflate(R.menu.menu_main, menu);
      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      // Handle action bar item clicks here. The action bar will
      // automatically handle clicks on the Home/Up button, so long
      // as you specify a parent activity in AndroidManifest.xml.

      int id = item.getItemId();

      //noinspection SimplifiableIfStatement
      if (id == R.id.action_settings) {
         return true;
      }
      return super.onOptionsItemSelected(item);
   }
}