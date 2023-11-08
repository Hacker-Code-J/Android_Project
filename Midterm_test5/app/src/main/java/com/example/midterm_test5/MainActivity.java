package com.example.midterm_test5;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    Button CngBtn;
    TextView Name, ID;
    ImageView img;

    EditText edit1, edit2;
    Button Add, Sub, Mul, Div;
    TextView textResult;
    String num1, num2;
    Integer result;
    Button[] numBtn = new Button[10];
    Integer[] numBtnIDs = {
            R.id.Num0, R.id.Num1, R.id.Num2, R.id.Num3, R.id.Num4,
            R.id.Num5, R.id.Num6, R.id.Num7, R.id.Num8, R.id.Num9
    };
    int i;
    int cng = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("그리드레이아웃 계산기");

        CngBtn = (Button) findViewById(R.id.CngBtn);
        Name = (TextView) findViewById(R.id.Name);
        ID = (TextView) findViewById(R.id.ID);

        CngBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cng = 1-cng;
                if( cng == 0) {
                   img.setImageResource(R.drawable.me);
                   Name.setText("Name: 지용현");
                   ID.setText("ID: 20192250");
                } else {
                    img.setImageResource(R.drawable.newton);
                    Name.setText("Name: Newton");
                    ID.setText("ID: 16430104");
                }
            }
        });


        edit1 = (EditText) findViewById(R.id.Edit1);
        edit2 = (EditText) findViewById(R.id.Edit2);

        Add = (Button) findViewById(R.id.Add);
        Sub = (Button) findViewById(R.id.Sub);
        Mul = (Button) findViewById(R.id.Mul);
        Div = (Button) findViewById(R.id.Div);

        Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                num1 = edit1.getText().toString();
                num2 = edit2.getText().toString();
                if(num1.equals("") || num2.equals("")) {
                    Toast.makeText(getApplicationContext(), "값을 입력하세요", Toast.LENGTH_SHORT).show();
                } else {
                    result = Integer.parseInt(num1) + Integer.parseInt(num2);
                    textResult.setText("계산결과 : " + result.toString());
                }
            }
        });
        Sub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                num1 = edit1.getText().toString();
                num2 = edit2.getText().toString();
                if(num1.equals("") || num2.equals("")) {
                    Toast.makeText(getApplicationContext(), "값을 입력하세요", Toast.LENGTH_SHORT).show();
                } else {
                    result = Integer.parseInt(num1) - Integer.parseInt(num2);
                    textResult.setText("계산결과 : " + result.toString());
                }
            }
        });
        Mul.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                num1 = edit1.getText().toString();
                num2 = edit2.getText().toString();
                if(num1.equals("") || num2.equals("")) {
                    Toast.makeText(getApplicationContext(), "값을 입력하세요", Toast.LENGTH_SHORT).show();
                } else {
                    result = Integer.parseInt(num1) * Integer.parseInt(num2);
                    textResult.setText("계산결과 : " + result.toString());
                }
            }
        });
        Div.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                num1 = edit1.getText().toString();
                num2 = edit2.getText().toString();
                if(Integer.parseInt(num2) == 0) {
                    Toast.makeText(getApplicationContext(), "0으로 나눌 수 없습니다", Toast.LENGTH_SHORT).show();
                } else if(num1.equals("") || num2.equals("")) {
                    Toast.makeText(getApplicationContext(), "값을 입력하세요", Toast.LENGTH_SHORT).show();
                } else {
                    result = Integer.parseInt(num1) / Integer.parseInt(num2);
                    textResult.setText("계산결과 : " + result.toString());
                }
            }
        });

        for(i=0; i<numBtnIDs.length; i++) {
            numBtn[i] = (Button) findViewById(numBtnIDs[i]);
        }
        for(i=0; i<numBtnIDs.length; i++) {
            final int idx;
            idx = i;

            numBtn[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(edit1.isFocused() == true) {
                        num1 = edit1.getText().toString() + numBtn[i].getText().toString();
                        edit1.setText(num1);
                    } else if(edit2.isFocused() == true) {
                        num1 = edit2.getText().toString() + numBtn[i].getText().toString();
                        edit2.setText(num2);
                    } else {
                        Toast.makeText(getApplicationContext(), "먼저 EditText를 선택하세요", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}
