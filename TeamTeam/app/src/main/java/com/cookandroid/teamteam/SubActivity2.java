package com.cookandroid.teamteam;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.Toast;


import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

public class SubActivity2 extends AppCompatActivity implements View.OnClickListener, RecyclerViewItemClickListener.OnItemClickListener {

    private RecyclerView recyclerView;
    private FloatingActionButton floatingActionButton;

    private Sub2Adapter sub2Adapter;
    private List<list_item> list_itemList;

    private FirebaseFirestore store = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub2);

        setTitle("후기 게시판");

        list_itemList = new ArrayList<>();

        recyclerView = (RecyclerView)findViewById(R.id.recyclerview);
        findViewById(R.id.floatingActionButton).setOnClickListener(this);

        // 리사이클러뷰의 클릭 이벤트 클래스 이용
        recyclerView.addOnItemTouchListener(new RecyclerViewItemClickListener(this, recyclerView, this));
    }

    @Override
    protected void onStart() {
        super.onStart();

        // firebase 데이터베이스의 item 컬렉션을 이용
        store.collection("item")
                // 내림차순을 이용해 시간 순으로 업로드
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (queryDocumentSnapshots != null) {
                            list_itemList.clear();
                            // 실시간 업로드
                            for (DocumentSnapshot dc : queryDocumentSnapshots.getDocuments()) {
                                Map<String, Object> shot = dc.getData();
                                // id, title, content, nickname 정보를 가져옴
                                String id = String.valueOf(shot.get("id"));
                                String title = String.valueOf(shot.get("title"));
                                String content = String.valueOf(shot.get("content"));
                                String nickname = String.valueOf(shot.get("nickname"));

                                // list_item 클래스의 객체를 생성하여 가져온 id, title, content, nickname 정보를 저장함
                                list_item data = new list_item(id, title, content, nickname);
                                // 리스트에 추가
                                list_itemList.add(data);
                            }
                            sub2Adapter = new Sub2Adapter(list_itemList);
                            recyclerView.setAdapter(sub2Adapter);
                        }
                    }
                });
    }

    // FloatingAcitionBar 클릭시 화면 전환
    @Override
    public void onClick(View view) {
        Intent intent = new Intent(this, WriteActivity.class);
        startActivity(intent);
    }

    // 리사이클러뷰의 특정 위치 리스트를 클릭했을 때 화면 전환
    @Override
    public void onItemClick(View view, int position) {
        Intent intent = new Intent(this, Sub2_Content.class);
        // 특정 위치 리스트의 id를 전달
        intent.putExtra("id", list_itemList.get(position).getId());
        startActivity(intent);
    }

    // 리사이클러뷰의 특정 위치 리스트를 꾹 눌렀을 때
    @Override
    public void onItemLongClick(View view, final int position) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        // 삭제 메시지를 띄움
        dialog.setMessage("삭제 하시겠습니까?");
        dialog.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // 삭제 버튼을 누르면 특정 위치 리스트의 item 컬렉션 id와 관련된 모든 정보를 지움
                store.collection("item").document(list_itemList.get(position).getId()).delete();
                // 삭제되었다는 toast 메시지를 띄움
                Toast.makeText(getApplication(), "삭제 되었습니다.", Toast.LENGTH_SHORT).show();
            }
            // 취소 버튼을 누르면
        }).setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                // 취소되었다는 toast 메시지를 띄움
                Toast.makeText(getApplicationContext(), "취소 되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });
        dialog.setTitle("삭제 알림");
        dialog.show();
    }

    // 어뎁터 구현
    private class Sub2Adapter extends RecyclerView.Adapter<Sub2Adapter.Sub2ViewHolder> {

        private List<list_item> list_itemList;

        //constructor
        public Sub2Adapter(List<list_item> list_itemList) {
            this.list_itemList = list_itemList;
        }

        // 뷰 홀더 생성
        @NonNull
        @Override
        public Sub2ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new Sub2ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_sub, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull Sub2ViewHolder holder, int position) {
            // list_item의 객체를 생성하여 특정 리스트의 위치를 넣어줌
            list_item data = list_itemList.get(position);

            // 홀더에 특정 리스트 위치에 관한 title을 넣어줌
            holder.titleTextView.setText(data.getTitle());

            // 홀더에 특정 리스트 위치에 관한 nickname을 넣어줌
            holder.nicknameTextView.setText(data.getNickname());
        }

        // 리스트 총 길이 가져옴
        @Override
        public int getItemCount() {
            return list_itemList.size();
        }

        // item_sub xml과 연결
        class Sub2ViewHolder extends  RecyclerView.ViewHolder{

            private TextView titleTextView;
            private TextView nicknameTextView;

            public Sub2ViewHolder(View itemview){
                super(itemview);

                titleTextView = itemview.findViewById(R.id.title_text);
                nicknameTextView = itemview.findViewById(R.id.nickname_text);
            }
        }
    }
}

