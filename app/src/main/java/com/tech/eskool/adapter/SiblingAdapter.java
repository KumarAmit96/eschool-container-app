package com.tech.eskool.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tech.eskool.R;
import com.tech.eskool.dto.StudentLoginResponse;

import java.util.ArrayList;

public class SiblingAdapter extends BaseAdapter {
    private final ArrayList<StudentLoginResponse> arrayList;
    private final Context context;

    public SiblingAdapter(ArrayList<StudentLoginResponse> arrayList, Context context) {
        this.arrayList = arrayList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return arrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.sibiling_adapter_layout, parent, false);
            TextView textView=convertView.findViewById(R.id.class_homework);
            ImageView image = convertView.findViewById(R.id.image_homework);
            image.setVisibility(View.GONE);
            StudentLoginResponse response = arrayList.get(position);
            try {
                if (response.getStudentName() != null) {
                    textView.setText(arrayList.get(position).getStudentName());
                }else{
                    textView.setText("-");
                }
            }
            catch (Exception e){
                Log.i("HOMEWORK ADAPTER","MSG"+e.getMessage());
            }
        }
        return convertView;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }
}
