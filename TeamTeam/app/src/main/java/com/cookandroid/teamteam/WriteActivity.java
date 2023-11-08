package com.cookandroid.teamteam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class WriteActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseFirestore store = FirebaseFirestore.getInstance();

    private EditText writeTitleText, writeContentText;
    private  String id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write);

        writeTitleText = findViewById(R.id.write_title_text);
        writeContentText = findViewById(R.id.write_content_text);

        findViewById(R.id.write_upload_btn).setOnClickListener(this);

    }

    @Override
    public void onClick(View view) {

        // firebase 데이터베이스에서 item 컬렉션의 id를 가져옴
        id = store.collection("item").document().getId();
        Map<String, Object> post = new HashMap<>();
        // 데이터베이스에 id, title, content, timestamp 필드 추가
        post.put("id", id);
        post.put("title", writeTitleText.getText().toString());
        post.put("content", writeContentText.getText().toString());
        post.put("timestamp", FieldValue.serverTimestamp());

        // id를 이용해 업로드(성공 or 실패)
        store.collection("item").document(id).set(post)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(getApplicationContext(), "업로드 성공", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getApplicationContext(), "업로드 실패!", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
