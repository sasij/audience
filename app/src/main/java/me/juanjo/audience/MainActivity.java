package me.juanjo.audience;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
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
import me.juanjo.audience.infraestructure.service.RecordService;

/**
 * Created by juanjo.
 */
public class MainActivity extends AppCompatActivity implements View.OnClickListener {

  private static final String LOG_TAG = "AudioRecordTest";

  @BindView(R.id.record_button) Button recordButton;
  @BindView(R.id.stop_record_button) Button stopRecordButton;
  @BindView(R.id.play_sound_button) Button playButton;

  /** Flag indicating whether we have called bind on the service. */
  boolean mBound;
  private MediaPlayer mPlayer = null;
  private boolean recording;
  private boolean playing;
  /**
   * Class for interacting with the main interface of the service.
   */
  private ServiceConnection mConnection = new ServiceConnection() {
    public void onServiceConnected(ComponentName className, IBinder service) {
      // This is called when the connection with the service has been
      // established, giving us the object we can use to
      // interact with the service.  We are communicating with the
      // service using a Messenger, so here we get a client-side
      // representation of that from the raw IBinder object.
      mBound = true;
    }

    public void onServiceDisconnected(ComponentName className) {
      // This is called when the connection with the service has been
      // unexpectedly disconnected -- that is, its process crashed.
      mBound = false;
    }
  };

  @Override public void onCreate(Bundle icicle) {
    super.onCreate(icicle);
    setContentView(R.layout.activity_main);
    ButterKnife.bind(this);

    if (requestPermissionOnClick()) return;

    recordButton.setOnClickListener(this);
    stopRecordButton.setOnClickListener(this);
    playButton.setOnClickListener(this);

    initService();

    final Button multiple = (Button) findViewById(R.id.button_test);
    multiple.setOnClickListener(new View.OnClickListener() {
      @Override public void onClick(View view) {
        initService();
      }
    });
  }

  @Override protected void onStart() {
    super.onStart();
    // Bind to the service
    bindService(new Intent(this, RecordService.class), mConnection, Context.BIND_AUTO_CREATE);
  }

  @Override public void onPause() {
    super.onPause();

    if (mPlayer != null) {
      mPlayer.release();
      mPlayer = null;
    }
  }

  @Override protected void onStop() {
    super.onStop();

    // Unbind from the service
    if (mBound) {
      unbindService(mConnection);
      mBound = false;
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
      String fileName = Environment.getExternalStorageDirectory().getAbsolutePath();
      fileName += "/audiorecordtest.mp4";
      mPlayer.setDataSource(fileName);
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

  private boolean requestPermissionOnClick() {
    //TODO test for request permission
    //Move this to interactor => Check Permission
    final Button multiple = (Button) findViewById(R.id.button_test);
    multiple.setOnClickListener(new View.OnClickListener() {
                                  @Override public void onClick(View v) {
                                    if (Dexter.isRequestOngoing()) {
                                      return;
                                    }
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
                                        System.out.println("=> onPermissionRationaleShouldBeShown" + permissions.toString());
                                      }
                                    }, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO);
                                  }
                                }

    );
    return false;
  }

  private void initService() {
    Intent intent = new Intent(getApplicationContext(), RecordService.class);
    intent.setAction("INIT");
    intent.putExtra("MY_USER_ID", "xxxdsasdcsdfc");
    getApplicationContext().startService(intent);
  }

  private void startRecording() {
    if (!mBound) return;
    Intent intent = new Intent(getApplicationContext(), RecordService.class);
    intent.setAction("RECORD");
    getApplicationContext().startService(intent);
  }

  private void stopRecording() {
    if (!mBound) return;
    Intent intent = new Intent(getApplicationContext(), RecordService.class);
    intent.setAction("STOP_RECORD");
    getApplicationContext().startService(intent);
  }
}
