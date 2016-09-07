package me.juanjo.audience.infraestructure.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;
import java.io.IOException;

/**
 * Created by juanjo on 6/9/16.
 */
public class RecordService extends Service {

  // interface for clients that bind
  private final IBinder mBinder = new RecordServiceBinder();

  private MediaRecorder mRecorder = null;
  private boolean recording;

  @Override public void onCreate() {
    super.onCreate();
    System.out.println("=> oncreate service");
  }

  @Override public int onStartCommand(Intent intent, int flags, int startId) {
    if (intent != null) {
      if ("INIT".equals(intent.getAction())) {
        System.out.println("=> service was initialized");
      } else if ("RECORD".equals(intent.getAction())) {
        System.out.println("=> start recording service");
        recording = true;
        startRecording();
      } else if ("STOP_RECORD".equals(intent.getAction())) {
        System.out.println("=> stop recording service");
        recording = false;
        stopRecording();
      }
    }
    return START_STICKY;
  }

  /**
   * When binding to the service, we return an interface to our messenger
   * for sending messages to the service.
   */
  @Override public IBinder onBind(Intent intent) {
    Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();
    return mBinder;
  }

  @Override public void onRebind(Intent intent) {
    super.onRebind(intent);
    // A client is binding to the service with bindService(),
    // after onUnbind() has already been called
  }

  @Override public boolean onUnbind(Intent intent) {
    System.out.println("=> unbind service ");
    return super.onUnbind(intent);
  }

  @Override public void onDestroy() {
    super.onDestroy();
    System.out.println("=> ondestroy service ");
  }

  private void startRecording() {
    String fileName = Environment.getExternalStorageDirectory().getAbsolutePath();
    fileName += "/audiorecordtest.mp4";

    mRecorder = new MediaRecorder();
    mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
    mRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
    mRecorder.setOutputFile(fileName);
    mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

    try {
      mRecorder.prepare();
      mRecorder.start();
    } catch (IOException e) {
      System.out.println("=> RECORD_SERVICE prepare() failed");
    }
  }

  private void stopRecording() {
    mRecorder.stop();
    mRecorder.release();
    mRecorder = null;
  }

  public class RecordServiceBinder extends Binder {
    public RecordService getService() {
      return RecordService.this;
    }
  }
}
