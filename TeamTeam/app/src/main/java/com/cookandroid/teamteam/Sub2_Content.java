package com.cookandroid.teamteam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

public class Sub2_Content extends AppCompatActivity {

    private FirebaseFirestore store = FirebaseFirestore.getInstance();
    private TextView Sub2_title, Sub2_content, Sub2_nickname;
    private String id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub2__content);

        Sub2_title = (TextView)findViewById(R.id.sub2_title);
        Sub2_content = (TextView)findViewById(R.id.sub2_content);
        Sub2_nickname = (TextView)findViewById(R.id.sub2_nickname);

        // SubActivity2 엑티비티 -> Sub2_Content 엑티비티 전환
        Intent getIntent = getIntent();

        // item 컬렉션의 id 값을 받아옴
        id = getIntent.getStringExtra("id");
        Log.e("ITEM DOCUMENT ID: ", id);

        // item 컬렉션의 id를 이용하여 화면에 id와 연관된 title, content, nickname을 띄어줌
        store.collection("item").document(id)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            if(task.getResult() != null){
                                Map<String , Object> snap = task.getResult().getData();
                                String title = String.valueOf(snap.get("title"));
                                String content = String.valueOf(snap.get("content"));
                                String nickname = String.valueOf(snap.get("nickname"));

                                Sub2_title.setText(title);
                                Sub2_content.setText(content);
                                Sub2_nickname.setText(nickname);
                            }
                        }
                    }
                });
    }
}
