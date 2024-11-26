package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresPermission;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {


    private static final int SAMPLE_RATE = 44100; //采样率

    private static final int BUFFER_SIZE = 10 * SAMPLE_RATE; //缓冲区大小

    private static final double FREQUENCY = 20000.0; //超声波频率

    private static final double DURATION = 10.0; //超声波持续时间

    //private static final int REQUEST_RECORD_AUDIO_PERMISSION_CODE = 1;//录音权限，使用startActivityForResult()启动活动来请求权限时，这个作为请求码传递

    private static final int PERMISSONS_REQUEST_CODE = 123;
    private String[] PERMISSIONS = {
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS
    };

    private AudioTrack audioTrack;

    private AudioRecord audioRecord;

    private boolean isPlaying = false;

    private boolean isRecording = false;


    private Button playButton;
    private Button pauseButton;

    private Context mContext;
    private File fileDir;
    private File dataFile;
    private short[] audioBuffer;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);



        mContext = getApplicationContext();
        fileDir = mContext.getFilesDir();//获取当前应用程序的私有目录
        dataFile = new File(fileDir,"test.wav"); //创建保存数据的文件
        Log.d("Debug", String.valueOf(fileDir)+"--申请权限");


        //获取录音和扬声器权限
        //因为在 Android 6.0以下的版本申请权限方式不同，保证只有在Android6.0以上版本执行新的申请方式

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (checkSelfPermission(Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.MODIFY_AUDIO_SETTINGS)!=PackageManager.PERMISSION_GRANTED){
                //没有录音权限，请求权限
                requestPermissions(PERMISSIONS,PERMISSONS_REQUEST_CODE);
            }

        }
        // 初始化扬声器

        int minBufferSize = AudioTrack.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT);

        audioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT, minBufferSize, AudioTrack.MODE_STREAM);

        Log.d("Debug", String.valueOf(fileDir)+"--扬声器初始化");
        // 初始化麦克风
        BufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE,
                CHANNEL_CONFIG, AUDIO_FORMAT);
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                SAMPLE_RATE_HERTZ, CHANNEL_CONFIG, AUDIO_FORMAT, BufferSize);

        int maxBufferSize = AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);


        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, maxBufferSize);

        audioBuffer = new short[BUFFER_SIZE];//创建音频缓存数组(2s)，之后读写进文件就用这个

        Log.d("Debug", String.valueOf(fileDir)+"--麦克风初始化,state为"+audioRecord.getRecordingState());
        //判断AudioRecord的状态是否初始化完毕
        //在AudioRecord对象构造完毕之后，就处于AudioRecord.STATE_INITIALIZED状态了。
        int state = audioRecord.getState();
        if (state == AudioRecord.STATE_UNINITIALIZED) {
            throw new RuntimeException("AudioRecord STATE_UNINITIALIZED");
        }
        // 设置播放和暂停按钮

        Button playButton = findViewById(R.id.play_button);

        Button pauseButton = findViewById(R.id.pause_button);
        Log.d("Debug", String.valueOf(fileDir)+"--按钮已绑定");

        playButton.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View view) {

                isPlaying = true;

                isRecording = true;

                playAndRecord();

            }

        });


        pauseButton.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View view) {

                isPlaying = false;

                isRecording = false;

                audioTrack.stop();
                audioRecord.stop();
                audioTrack.release();
                audioRecord.release();
                //将audioBuffer里的数据传输进文件
                try{
                    System.out.println(fileDir);
                    FileOutputStream fos = new FileOutputStream(dataFile);
                    DataOutputStream dos = new DataOutputStream(fos);
                    for(short s:audioBuffer) {
                        dos.write(s);
                    }
                    dos.close();
                    fos.close();
                }catch (IOException e){
                    e.printStackTrace();
                }
            }

        });

    }



    private void playAndRecord() {

        new Thread(new Runnable() {

            @Override

            public void run() {
                //生成超声波
                short[] buffer = new short[BUFFER_SIZE];
                for (int i = 0; i < buffer.length; i++) {

                    buffer[i] = (short) (Short.MAX_VALUE * Math.sin(2 * Math.PI * i / (SAMPLE_RATE / FREQUENCY)));

                }
                Log.d("Debug", String.valueOf(fileDir)+"--准备发射");
                // 播放超声波并录制反射超声波
                while (isPlaying && isRecording) {
                    //将超声波写入音轨
                    audioTrack.write(buffer, 0, buffer.length);
                    //播放
                    audioTrack.play();
                    Log.d("Debug", String.valueOf(fileDir)+"--播放");
                    //开始录音
                    audioRecord.startRecording();
                    //用audioBuffer从音频缓冲区读取数据
                    audioRecord.read(audioBuffer, 0, audioBuffer.length);
                    Log.d("Debug", String.valueOf(fileDir)+"--录音");
                    //在这里对接收到的反射超声波进行处理，例如进行FFT变换，获得频谱等信息

                }




            }

        });
    }
}

//                private void playAndRecord() {
//                    // 初始化超声波缓冲区
//                    short[] buffer = new short[BUFFER_SIZE];
//                    for (int i = 0; i < buffer.length; i++) {
//                        buffer[i] = (short) (Short.MAX_VALUE * Math.sin(2 * Math.PI * i / (SAMPLE_RATE / FREQUENCY)));
//                    }
//
//                    // 播放超声波并录制反射超声波
//                    audioTrack.play();
//                    audioRecord.startRecording();
//
//                    // 初始化FFT
//                    int bufferSize = 2;
//                    while (bufferSize < BUFFER_SIZE) {
//                        bufferSize *= 2;
//                    }
//                    final FFT fft = new FFT(bufferSize);
//                    final double[] toTransform = new double[bufferSize];
//
//                    while (isPlaying && isRecording) {
//                        audioTrack.write(buffer, 0, buffer.length);
//                        short[] audioBuffer = new short[BUFFER_SIZE];
//                        audioRecord.read(audioBuffer, 0, audioBuffer.length);
//
//                        // 对反射超声波进行FFT变换
//                        for (int i = 0; i < bufferSize && i < audioBuffer.length; i++) {
//                            toTransform[i] = (double) audioBuffer[i] / Short.MAX_VALUE;
//                        }
//                        fft.forward(toTransform);
//
//                        // 获得频谱信息
//                        double[] spectrum = new double[bufferSize / 2];
//                        for (int i = 0; i < spectrum.length; i++) {
//                            double real = fft.real[i];
//                            double imaginary = fft.imaginary[i];
//                            spectrum[i] = Math.sqrt(real * real + imaginary * imaginary);
//                        }
//
//                        // 在这里对频谱信息进行处理，例如绘制频谱图等
//                    }
//                    audioTrack.stop();
//                    audioRecord.stop();
//                }