package com.maple.maplexpose;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.maple.maplexpose.mqtt.MqttApiImpl;

public class DialogActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dialog);
        EditText et = findViewById(R.id.et_preFix);
        TextView tv = findViewById(R.id.tv_data);
        tv.setText("接收Topic: " + MqttApiImpl.SUB + "\n上报Topic: "
                + MqttApiImpl.PUB + "\n");
        findViewById(R.id.submit).setOnClickListener(v -> {
            String mtv = et.getText().toString();
            MqttApiImpl.setTopicPrefix(mtv);
            SharedPreferences sharedPreferences = this.getSharedPreferences("a",MODE_PRIVATE);
            sharedPreferences.edit().putString("prefix",mtv).commit();
            tv.setText("接收Topic: " + MqttApiImpl.SUB + "\n上报Topic: "
                    + MqttApiImpl.PUB + "\n");
            Toast.makeText(this, "重启后生效", Toast.LENGTH_SHORT).show();
        });
    }
}
