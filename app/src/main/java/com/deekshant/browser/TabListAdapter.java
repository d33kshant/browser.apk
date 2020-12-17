package com.deekshant.browser;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class TabListAdapter extends RecyclerView.Adapter<TabListAdapter.ViewHolder>{

    ArrayList<View> tabItem;
    onTabItemClickListener mListener;

    MainActivity activity;

    public interface onTabItemClickListener
    {
        void onTabClicked(int position);
        void onCloseTabClicked(int position);
    }

    public void setTabItemClickListener(onTabItemClickListener listener)
    {
        mListener = listener;
    }
    public TabListAdapter(ArrayList<View> tabs, MainActivity act) {
        tabItem = tabs;
        activity = act;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        TextView tabTitle;
        ImageView tabIcon,tabClose;
        LinearLayout tab;
        public ViewHolder(@NonNull View itemView, final onTabItemClickListener listener) {
            super(itemView);
            tabTitle = itemView.findViewById(R.id.tabTitle);
            tabIcon = itemView.findViewById(R.id.tabIcon);
            tabClose = itemView.findViewById(R.id.closeTab);
            tab = itemView.findViewById(R.id.cardTab);

            tab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener!=null)
                    {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION)
                        {
                            listener.onTabClicked(position);
                        }
                    }
                }
            });

            tabClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (listener!=null)
                    {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION)
                        {
                            listener.onCloseTabClicked(position);
                        }
                    }
                }
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_tab_row,parent,false);
        ViewHolder viewHolder = new ViewHolder(view,mListener);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WebView currentTab = (WebView) tabItem.get(position);
        holder.tabTitle.setText(currentTab.getTitle());
        if (currentTab.getFavicon()!=null) {
            holder.tabIcon.setImageBitmap(currentTab.getFavicon());
        }
        if (position == activity.getCurrentTab()){
            holder.tabTitle.setTextColor(Color.rgb(78,168,255));
        }
    }

    @Override
    public int getItemCount() {
        return tabItem.size();
    }
}
