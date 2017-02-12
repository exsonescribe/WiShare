package com.example.exson.another2;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

public class ContactRecyclerAdapter extends RecyclerView.Adapter<ContactRecyclerAdapter.MyViewHolder> {

    private List<Contacts> mContactList;

    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView mContacts;
        public ImageView mImageContact;

    public MyViewHolder(View view) {
        super(view);
        mContacts = (TextView) view.findViewById(R.id.contact_TV);
        mImageContact = (ImageView) view.findViewById(R.id.contact_IV);
       }
    }

    public ContactRecyclerAdapter(List<Contacts> mContactList) {
        this.mContactList = mContactList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.contact_cardview, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Contacts mName = mContactList.get(position);
        holder.mContacts.setText(mName.getmName());
        holder.mImageContact.setImageResource(R.drawable.defaultuser);
    }

    @Override
    public int getItemCount() {
        return mContactList.size();
    }
}