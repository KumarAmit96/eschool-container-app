package com.tech.eskool.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.tech.eskool.R;
import com.tech.eskool.dto.SessionResponse;

import java.util.ArrayList;

public class SessionCustomAdapter extends BaseAdapter {

    ArrayList<SessionResponse> sessionResponseArrayList;
    Context context;

    public SessionCustomAdapter(Context context, ArrayList<SessionResponse> sessionResponseArrayList) {
        this.sessionResponseArrayList = sessionResponseArrayList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return sessionResponseArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return sessionResponseArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null)
            convertView = LayoutInflater.from(context).inflate(R.layout.custom_spinner_layout, parent, false);

        TextView textView = convertView.findViewById(R.id.text);
        SessionResponse sessionResponse = sessionResponseArrayList.get(position);

        if(sessionResponse.getSessionName() != null && sessionResponse.getSessionId() != null && !sessionResponse.getSessionName().isEmpty() && !sessionResponse.getSessionId().isEmpty())
        {
            textView.setText(sessionResponse.getSessionName());
            textView.setTag(sessionResponse.getSessionId());
        }

        return convertView;
    }
}
