package com.deekshant.browser;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;

public class TabListSheet extends BottomSheetDialogFragment {

    ArrayList<View> tabList;
    RecyclerView recyclerView;
    TabListAdapter adapter;
    RecyclerView.LayoutManager layoutManager;

    OnTabSheetButtonClick mListener;
    MainActivity activity;

    public TabListSheet(ArrayList<View> tabs, MainActivity act)
    {
        tabList = tabs;
        activity = act;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_tabs_bottom_sheet,container);

        recyclerView = view.findViewById(R.id.tabsList);
        ImageButton addTab = view.findViewById(R.id.addNewTab);

        addTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onAddTabClicked();
            }
        });
        adapter = new TabListAdapter(tabList,activity);
        adapter.setTabItemClickListener(new TabListAdapter.onTabItemClickListener() {
            @Override
            public void onTabClicked(int position) {
                mListener.onTabClicked(position);
            }

            @Override
            public void onCloseTabClicked(int position) {
                if (tabList.size()>1) {
                    mListener.onCloseTabClicked(position);
                    adapter.notifyDataSetChanged();
                }else
                    {
                        mListener.onAddTabClicked();
                        mListener.onCloseTabClicked(position);
                        adapter.notifyDataSetChanged();
                    }
            }
        });
        layoutManager = new LinearLayoutManager(getContext());

        recyclerView.setHasFixedSize(true);
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(layoutManager);
        return view;
    }

    public interface OnTabSheetButtonClick
    {
        void onAddTabClicked();
        void onTabClicked(int position);
        void onCloseTabClicked(int position);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (OnTabSheetButtonClick) context;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }
}
