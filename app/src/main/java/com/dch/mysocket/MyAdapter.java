package com.dch.mysocket;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

public class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final LayoutInflater mLayoutInflater;
    private ArrayList<MyBean> lists = new ArrayList<>();

    public MyAdapter(Context mContext, ArrayList<MyBean> lists) {
        this.lists = lists;
        mLayoutInflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MyViewHolder(mLayoutInflater.inflate(R.layout.item_rv, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (viewHolder instanceof MyViewHolder) {
            MyBean myBean = lists.get(i);
            ((MyViewHolder) viewHolder).setData(myBean);
        }
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);

            textView = (TextView) itemView.findViewById(R.id.text);
        }

        public void setData(MyBean myBean) {
            if (myBean != null) {
                String msg = myBean.getMsg();
                int type = myBean.getType();
                if (type == 2) {//自己的信息在右边
                    textView.setGravity(Gravity.RIGHT);
                } else {
                    textView.setGravity(Gravity.LEFT);
                }
                if (!TextUtils.isEmpty(msg)) {
                    textView.setText(msg);
                }
            }
        }
    }


}
