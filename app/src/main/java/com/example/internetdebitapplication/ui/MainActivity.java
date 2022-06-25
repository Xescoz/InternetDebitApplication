package com.example.internetdebitapplication.ui;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import com.example.internetdebitapplication.R;
import com.example.internetdebitapplication.databinding.ActivityMainBinding;
import com.example.internetdebitapplication.model.DownDebit;
import com.example.internetdebitapplication.room.DownDebitViewModel;

import java.io.IOException;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    public static final String CHANNEL_ID = "NotificationAppChannel";
    private final OkHttpClient client = new OkHttpClient();
    private ActivityMainBinding binding;
    private final Timer timer = new Timer();
    private DownDebitViewModel downDebitViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        downDebitViewModel = new ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication())).get(DownDebitViewModel.class);

        callAsynchronousTask();
        createNotificationChannel();

        showNotificationIfDownSpeedToLow();

        binding.stopServiceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                stopService();
            }
        });
    }

    //use OkHttp to download a file and then calculate the down speed
    private void runDownCalculate(long startTime) throws IOException {

        Request request = new Request.Builder()
                .url("http://test-debit.free.fr/8192.rnd")
                .build();

        try (Response response = client.newCall(request).execute()) {
            long fileSize = Objects.requireNonNull(response.body()).contentLength();
            String file = Objects.requireNonNull(response.body()).string();
            downSpeedCalculate(fileSize, startTime);
        }
    }

    //Calculate down speed and insert in the database the value
    private void downSpeedCalculate(long fileSize, long startTime) {
        long endTime = System.currentTimeMillis();
        double timeTakenMills = Math.floor(endTime - startTime);
        double downSpeed = (fileSize / timeTakenMills) / 1000;

        String downSpeedString = String.format("%.2f", downSpeed) + " Mb/s";
        startService(downSpeedString);

        DownDebit downDebit = new DownDebit(downSpeed, downSpeedString);
        downDebitViewModel.insert(downDebit);

        //Update the ui
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                binding.downSpeedValueTextview.setText(downSpeedString);
            }
        });

    }

    //start foreground service
    public void startService(String downSpeed) {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        serviceIntent.putExtra("inputExtra", downSpeed);
        ContextCompat.startForegroundService(this, serviceIntent);
    }

    //stop foreground service and timer loop
    public void stopService() {
        Intent serviceIntent = new Intent(this, ForegroundService.class);
        stopService(serviceIntent);
        timer.cancel();
    }

    //show a notification if the last download value it too low
    public void showNotificationIfDownSpeedToLow() {
        downDebitViewModel.getAll().observe(this, downDebits -> {
            double downDebitSum = 0;

            for (int i = 0; i < downDebits.size(); i++) {
                downDebitSum += downDebits.get(i).getDownValue();
            }
            double downDebitMoyToTest = (downDebitSum / downDebits.size() - 1);
            double lastDownDebitValue = downDebits.get(downDebits.size() - 1).getDownValue();

            if (lastDownDebitValue < downDebitMoyToTest)
                createNotificationDownDebitLow();
        });
    }

    //timer that loop every minute
    public void callAsynchronousTask() {
        final Handler handler = new Handler();

        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    public void run() {
                        try {
                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        long startTime = System.currentTimeMillis();
                                        runDownCalculate(startTime);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });
                            thread.start();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 0, 60000); //execute in every 1 minute
    }

    //create the notification with the low debit
    private void createNotificationDownDebitLow() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("DownDebitApplication")
                .setContentText("You're download debit is low")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
        notificationManager.notify(2, builder.build());

    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "Notification App channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(serviceChannel);
        }
    }

}