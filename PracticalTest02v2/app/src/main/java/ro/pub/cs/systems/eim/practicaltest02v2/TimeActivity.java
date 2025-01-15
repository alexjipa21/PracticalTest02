package ro.pub.cs.systems.eim.practicaltest02v2;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;

public class TimeActivity extends AppCompatActivity {
    private TextView timeTextView;
    private boolean isRunning = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time);

        timeTextView = findViewById(R.id.timeTextView);

        // Start the thread to fetch time data
        new Thread(() -> fetchTimeData()).start();
    }

    private void fetchTimeData() {
        try (Socket socket = new Socket("192.168.150.178", 12345);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            while (isRunning) {
                String timeData = reader.readLine();
                if (timeData != null) {
                    runOnUiThread(() -> timeTextView.setText(timeData));
                }
            }
        } catch (Exception e) {
            runOnUiThread(() -> timeTextView.setText("Error: " + e.getMessage()));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        isRunning = false;  // Stop the thread when activity is destroyed
    }
}

