package com.cookandroid.teamteam;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

// 리사이클러뷰의 클릭이벤트를 만들어줌
public class RecyclerViewItemClickListener implements RecyclerView.OnItemTouchListener {

    // 2.OnItemClickListener의 listner 생성
    private OnItemClickListener listener;
    // 3.제스처
    private GestureDetector gestureDetector;

    // 1.OnitemClickListner 인터페이스
    public interface OnItemClickListener{
        void onItemClick(View view, int position);
        void onItemLongClick(View view, int position);
    }

    // 4. 생성자
    public RecyclerViewItemClickListener(Context context, final RecyclerView recyclerView, final OnItemClickListener listener) {
        this.listener = listener;

        // SimpleOnGestureListener을 통해 원하는 메서드를 받을 수 있음
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override
            // 한번 탭
            public boolean onSingleTapUp(MotionEvent e) {
                return true;
            }

            @Override
            // 길게 탭
            public void onLongPress(MotionEvent e) {
                View v = recyclerView.findChildViewUnder(e.getX(), e.getY());
                if (v != null && listener != null) {
                    listener.onItemLongClick(v, recyclerView.getChildAdapterPosition(v));
                }
            }
        });
    }

    // interface
    @Override
    public boolean onInterceptTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {
        View view = rv.findChildViewUnder(e.getX(), e.getY());
        if (view != null && gestureDetector.onTouchEvent(e)){
            listener.onItemClick(view, rv.getChildAdapterPosition(view));
            return true;
        }
        return false;
    }

    @Override
    public void onTouchEvent(@NonNull RecyclerView rv, @NonNull MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }
}
