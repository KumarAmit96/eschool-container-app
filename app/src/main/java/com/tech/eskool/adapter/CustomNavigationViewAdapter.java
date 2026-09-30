package com.tech.eskool.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tech.eskool.R;
import com.tech.eskool.dto.MenuResponse;

import java.util.List;
import java.util.Map;

public class CustomNavigationViewAdapter extends BaseExpandableListAdapter {

    private final Context context;
    private final List<MenuResponse> parentMenu;
    private final Map<Integer, List<MenuResponse>> menuMap;

    public CustomNavigationViewAdapter(Context context, List<MenuResponse> parentMenu, Map<Integer, List<MenuResponse>> menuMap) {
        this.context = context;
        this.parentMenu = parentMenu;
        this.menuMap = menuMap;
    }

    @Override
    public Object getChild(int listPosition, int expandedListPosition) {
        return this.menuMap.get(this.parentMenu.get(listPosition).getId())
                .get(expandedListPosition);
    }

    @Override
    public long getChildId(int listPosition, int expandedListPosition) {
        return expandedListPosition;
    }

    @Override
    public View getChildView(int listPosition, final int expandedListPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {
        final MenuResponse menuResponse = (MenuResponse) getChild(listPosition, expandedListPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.nav_child_menu_item, null);
        }
        TextView expandedListTextView = convertView.findViewById(R.id.title);
        ImageView icon = convertView.findViewById(R.id.icon);
        expandedListTextView.setText(menuResponse.getName());
        if(menuResponse.getIconSmall() == null || menuResponse.getIconSmall().isEmpty()){
            icon.setVisibility(View.GONE);
        }else {
            icon.setVisibility(View.VISIBLE);
            Picasso.get().load(menuResponse.getIconSmall()).into(icon);
        }
        return convertView;
    }

    @Override
    public int getChildrenCount(int listPosition) {
        int id = this.parentMenu.get(listPosition).getId();
        return this.menuMap.containsKey(id) ? this.menuMap.get(id).size() : 0;
    }

    @Override
    public Object getGroup(int listPosition) {
        return this.parentMenu.get(listPosition);
    }

    @Override
    public int getGroupCount() {
        return this.parentMenu.size();
    }

    @Override
    public long getGroupId(int listPosition) {
        return listPosition;
    }

    @Override
    public View getGroupView(int listPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {
        final MenuResponse menuResponse = (MenuResponse) getGroup(listPosition);
        if (convertView == null) {
            LayoutInflater layoutInflater = (LayoutInflater) this.context.
                    getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.nav_menu_item, null);
        }
        TextView expandedListTextView = convertView
                .findViewById(R.id.title);
        ImageView arrow = convertView.findViewById(R.id.arrow);
        ImageView icon = convertView.findViewById(R.id.icon);
        expandedListTextView.setText(menuResponse.getName());
        if(menuResponse.getIconSmall() == null || menuResponse.getIconSmall().isEmpty()){
            icon.setVisibility(View.GONE);
        }else {
            icon.setVisibility(View.VISIBLE);
            Picasso.get().load(menuResponse.getIconSmall()).into(icon);
        }
        if(getChildrenCount(listPosition) > 0){
            arrow.setVisibility(View.VISIBLE);
        }else{
            arrow.setVisibility(View.GONE);
        }
        if(isExpanded){
            arrow.setImageResource(R.drawable.arrow_down);
        } else {
            arrow.setImageResource(R.drawable.arrow_right);
        }
        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public boolean isChildSelectable(int listPosition, int expandedListPosition) {
        return true;
    }
}
