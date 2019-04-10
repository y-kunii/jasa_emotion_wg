package com.example.sleeptechui;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class SleepDaysGridAdapter extends BaseAdapter {
    final private String TAG = "SleepDaysGridAdapter";

    class ViewHolder {
        TextView mCellTextView;
    }

    private String[] mCellTexts;
    private LayoutInflater inflater;
    private int layoutId;
    private int mTextlistSize;

    public SleepDaysGridAdapter(Context context,
                int layoutId,
                int contents_size,
                String[] members) {
        super();
        this.inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.layoutId = layoutId;
        mCellTexts = members;
        mTextlistSize = contents_size;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.d(TAG,"getView: position ->["+ position + "]");
        ViewHolder holder;
        if (convertView == null) {
            // main.xml の <GridView .../> に grid_items.xml を inflate して convertView とする
            convertView = inflater.inflate(layoutId, parent, false);
            // ViewHolder を生成
            holder = new ViewHolder();
            holder.mCellTextView = convertView.findViewById(R.id.UI_ITEM_ID_CELL_TEXT);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.mCellTextView.setText(mCellTexts[position]);

        return convertView;
    }

    @Override
    public int getCount() {
        Log.d(TAG,"getCount is ["+ mTextlistSize + "]");
        return mTextlistSize;
    }

    @Override
    public Object getItem(int position) {
        return position;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
}
