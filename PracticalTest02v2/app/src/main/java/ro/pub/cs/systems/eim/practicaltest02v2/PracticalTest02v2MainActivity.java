package ro.pub.cs.systems.eim.practicaltest02v2;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PracticalTest02v2MainActivity extends AppCompatActivity {
    private static final String DICTIONARY_BROADCAST = "com.example.dictionaryapp.DICTIONARY_BROADCAST";
    private EditText inputWord;
    private Button searchButton;
    private TextView resultText;

    private BroadcastReceiver dictionaryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Update the GUI with the received data
            String definition = intent.getStringExtra("definition");
            resultText.setText(definition != null ? definition : "No data received.");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inputWord = findViewById(R.id.inputWord);
        searchButton = findViewById(R.id.searchButton);
        resultText = findViewById(R.id.resultText);

        // Register the BroadcastReceiver to listen for dictionary data
        LocalBroadcastManager.getInstance(this).registerReceiver(
                dictionaryReceiver, new IntentFilter(DICTIONARY_BROADCAST));

        searchButton.setOnClickListener(v -> {
            String word = inputWord.getText().toString().trim();
            if (word.isEmpty()) {
                Toast.makeText(PracticalTest02v2MainActivity.this, "Please enter a word", Toast.LENGTH_SHORT).show();
            } else {
                fetchDefinition(word);
            }
        });
    }

    private void fetchDefinition(String word) {
        OkHttpClient client = new OkHttpClient();

        String url = "https://api.dictionaryapi.dev/api/v2/entries/en/" + word;

        Request request = new Request.Builder()
                .url(url)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                sendBroadcast("Failed to fetch data: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String responseBody = response.body().string();
                    try {
                        JSONArray jsonArray = new JSONArray(responseBody);
                        JSONObject wordObject = jsonArray.getJSONObject(0);
                        JSONArray meanings = wordObject.getJSONArray("meanings");

                        if (meanings.length() > 0) {
                            JSONObject firstMeaning = meanings.getJSONObject(0);
                            JSONArray definitions = firstMeaning.getJSONArray("definitions");

                            if (definitions.length() > 0) {
                                String firstDefinition = definitions.getJSONObject(0).getString("definition");
                                sendBroadcast(firstDefinition);
                            } else {
                                sendBroadcast("No definitions available.");
                            }
                        } else {
                            sendBroadcast("No meanings available.");
                        }
                    } catch (JSONException e) {
                        sendBroadcast("Failed to parse data: " + e.getMessage());
                    }
                } else {
                    sendBroadcast("Error: " + response.message());
                }
            }
        });
    }

    private void sendBroadcast(String message) {
        Intent intent = new Intent(DICTIONARY_BROADCAST);
        intent.putExtra("definition", message);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister the BroadcastReceiver when the activity is destroyed
        LocalBroadcastManager.getInstance(this).unregisterReceiver(dictionaryReceiver);
    }
}
