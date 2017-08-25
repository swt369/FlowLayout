package com.example.swt369.flowlayout;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FlowLayout flowLayout = (FlowLayout)findViewById(R.id.flowLayout);

        final EditText editText = (EditText)findViewById(R.id.editText);

        (findViewById(R.id.button_add)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String sentence = editText.getText().toString();
                if(sentence.length() > 0){
                    TextView textView = new TextView(MainActivity.this);
                    textView.setText(sentence);
                    flowLayout.addView(textView);
                    editText.setText("");
                }
            }
        });

        (findViewById(R.id.button_left)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flowLayout.setAlignment(FlowLayout.ALIGNMENT_LEFT);
            }
        });

        (findViewById(R.id.button_center)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flowLayout.setAlignment(FlowLayout.ALIGNMENT_CENTER);
            }
        });

        (findViewById(R.id.button_right)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                flowLayout.setAlignment(FlowLayout.ALIGNMENT_RIGHT);
            }
        });

        ((ToggleButton)findViewById(R.id.toggleButton)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    flowLayout.openSplitLines();
                }else {
                    flowLayout.closeSplitLines();
                }
            }
        });
    }
}
