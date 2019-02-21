package com.miaojun.spine;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private Button spine_boy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spine_boy = findViewById(R.id.spine_boy);
        View spine_view = findViewById(R.id.spine_view);

        spine_boy.setOnClickListener(this);
        spine_view.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.spine_boy:
                Intent intent = new Intent(this,SpineBoyActivity.class);
                startActivity(intent);
                break;
            case R.id.spine_view:
                Intent intent2 = new Intent(this,SpineViewActivity.class);
                startActivity(intent2);
                break;
        }
    }
}
