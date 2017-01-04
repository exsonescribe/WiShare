package com.example.exson.nsdtest1;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ConversationListAdapter extends ArrayAdapter {
    private TextView txtname;
    private TextView txtmessage;
    private List chatMessageList = new ArrayList();

    public void add(Conversation object) {
        chatMessageList.add(object);
        super.add(object);
    }

    public ConversationListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public int getCount() {
        return this.chatMessageList.size();
    }

    public Object getItem(int index) {
        return this.chatMessageList.get(index);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) this.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.message, parent, false);
        }

        Conversation convo= (Conversation) getItem(position);
        txtname = (TextView) row.findViewById(R.id.tv_name);
        txtmessage = (TextView) row.findViewById(R.id.tv_message);
        txtmessage.setText(convo.getMessage());
        txtname.setText(convo.getName());

        return row;
    }

}
