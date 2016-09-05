package me.juanjo.audience;

import android.Manifest;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import butterknife.BindView;
import butterknife.ButterKnife;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import java.io.IOException;
import java.util.List;

/**
 * Created by juanjo.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

   @BindView(R.id.record_button) Button recordButton;
   @BindView(R.id.stop_record_button) Button stopRecordButton;
   @BindView(R.id.play_sound_button) Button playButton;

   private static final String LOG_TAG = "AudioRecordTest";
   private static String mFileName = null;

   private MediaRecorder mRecorder = null;
   private MediaPlayer mPlayer = null;

   private boolean recording;
   private boolean playing;

   @Override public void onCreate(Bundle icicle) {
      super.onCreate(icicle);
      setContentView(R.layout.activity_main);
      ButterKnife.bind(this);

      //TODO test for request permission
      //Move this to interactor => Check Permission
      final Button multiple = (Button) findViewById(R.id.button_test);
      if (Dexter.isRequestOngoing()) {
         return;
      }
      multiple.setOnClickListener(new View.OnClickListener() {
                                     @Override public void onClick(View v) {
                                        Dexter.checkPermissionsOnSameThread(new MultiplePermissionsListener() {
                                           @Override public void onPermissionsChecked(MultiplePermissionsReport report) {
                                              System.out.println("=> onPermissionChecked" + report);
                                              if (!report.areAllPermissionsGranted()) {
                                                 Toast.makeText(MainActivity.this,
                                                     "In order to use the application, check the permissions, please.",
                                                     Toast.LENGTH_SHORT).show();
                                              }
                                           }

                                           @Override
                                           public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions,
                                               PermissionToken token) {
                                              token.continuePermissionRequest();
                                              System.out.println(
                                                  "=> onPermissionRationaleShouldBeShown" + permissions.toString());
                                           }
                                        }, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO);
                                     }
                                  }

      );

      recordButton.setOnClickListener(this);
      stopRecordButton.setOnClickListener(this);
      playButton.setOnClickListener(this);

      mFileName = Environment.getExternalStorageDirectory().

          getAbsolutePath();

      mFileName += "/audiorecordtest.mp4";
   }

   @Override public void onPause() {
      super.onPause();
      if (mRecorder != null) {
         mRecorder.release();
         mRecorder = null;
      }

      if (mPlayer != null) {
         mPlayer.release();
         mPlayer = null;
      }
   }

   @Override public void onClick(View v) {
      switch (v.getId()) {
         case R.id.record_button:
            recording = true;
            startRecording();
            break;
         case R.id.stop_record_button:
            if (recording) {
               recording = false;
               stopRecording();
            } else if (playing) {
               playing = false;
               stopPlaying();
            }
            break;
         case R.id.play_sound_button:
            playing = true;
            startPlaying();
            break;
      }
   }

   private void startPlaying() {
      mPlayer = new MediaPlayer();
      try {
         mPlayer.setDataSource(mFileName);
         mPlayer.prepare();
         mPlayer.start();
      } catch (IOException e) {
         Log.e(LOG_TAG, "prepare() failed");
      }
   }

   private void stopPlaying() {
      if (mPlayer != null) {
         mPlayer.release();
         mPlayer = null;
      }
   }

   private void startRecording() {
      mRecorder = new MediaRecorder();
      mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
      mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
      mRecorder.setOutputFile(mFileName);
      mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

      try {
         mRecorder.prepare();
      } catch (IOException e) {
         Log.e(LOG_TAG, "prepare() failed");
      }

      mRecorder.start();
   }

   private void stopRecording() {
      mRecorder.stop();
      mRecorder.release();
      mRecorder = null;
   }
}
