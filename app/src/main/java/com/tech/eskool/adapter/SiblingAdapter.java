package com.tech.eskool.adapter;

import android.content.Context;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tech.eskool.R;
import com.tech.eskool.dto.StudentLoginResponse;

import java.util.ArrayList;

/**
 * Adapter for the "Switch student" spinner in the student drawer header.
 * Position 0 is a placeholder (rendered as a caption in the dropdown);
 * every other position is a sibling shown with avatar, name and enrollment number.
 */
public class SiblingAdapter extends BaseAdapter {
    private final ArrayList<StudentLoginResponse> arrayList;
    private final Context context;
    private final String currentStudentId;

    public SiblingAdapter(ArrayList<StudentLoginResponse> arrayList, Context context, String currentStudentId) {
        this.arrayList = arrayList;
        this.context = context;
        this.currentStudentId = currentStudentId;
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

    public boolean isCurrentStudent(int position) {
        String id = arrayList.get(position).getStudentId();
        return id != null && id.equals(currentStudentId);
    }

    /** Closed state: always a call-to-action label, since picking a sibling reloads the dashboard. */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_sibling_selected, parent, false);
        }
        ((TextView) convertView).setText(R.string.switch_sibling);
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        if (position == 0) {
            if (convertView == null || convertView.getTag() != null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_sibling_dropdown_header, parent, false);
            }
            return convertView;
        }

        ViewHolder holder;
        if (convertView == null || !(convertView.getTag() instanceof ViewHolder)) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_sibling_dropdown, parent, false);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        StudentLoginResponse student = arrayList.get(position);
        holder.name.setText(TextUtils.isEmpty(student.getStudentName()) ? "-" : student.getStudentName().trim());

        if (TextUtils.isEmpty(student.getEnrollment_no())) {
            holder.subtitle.setVisibility(View.GONE);
        } else {
            holder.subtitle.setVisibility(View.VISIBLE);
            holder.subtitle.setText(context.getString(R.string.sibling_enrollment, student.getEnrollment_no()));
        }

        if (TextUtils.isEmpty(student.getPhoto())) {
            Picasso.get().cancelRequest(holder.avatar);
            holder.avatar.setImageResource(R.drawable.ic_teacher);
        } else {
            Picasso.get().load(student.getPhoto())
                    .placeholder(R.drawable.ic_teacher)
                    .error(R.drawable.ic_teacher)
                    .into(holder.avatar);
        }

        boolean current = isCurrentStudent(position);
        holder.badge.setVisibility(current ? View.VISIBLE : View.GONE);
        convertView.setAlpha(current ? 0.6f : 1f);
        return convertView;
    }

    private static class ViewHolder {
        final ImageView avatar;
        final TextView name;
        final TextView subtitle;
        final TextView badge;

        ViewHolder(View view) {
            avatar = view.findViewById(R.id.sibling_avatar);
            name = view.findViewById(R.id.sibling_name);
            subtitle = view.findViewById(R.id.sibling_subtitle);
            badge = view.findViewById(R.id.sibling_current_badge);
        }
    }
}
