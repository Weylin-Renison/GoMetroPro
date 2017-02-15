package com.gometro.gometropro;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by wprenison on 25/01/16.
 */
public class ListAdapterProjectCode extends ArrayAdapter<String>
{
    List<String> lstData;
    int layoutRes;

    public ListAdapterProjectCode(Context context, int resource, List<String> lstData)
    {
        super(context, resource);
        this.lstData = lstData;
        this.layoutRes = resource;
    }

    @Override
    public int getCount()
    {
        return lstData.size();
    }

    @Override
    public String getItem(int position)
    {
        return lstData.get(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if(convertView == null)
        {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(layoutRes, null);
        }

        TextView txtvProjCode = (TextView) convertView.findViewById(R.id.txtvCenteredItem);
        txtvProjCode.setText(lstData.get(position));

        return convertView;
    }
}
