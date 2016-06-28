package pl.lss.ccms.android.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import pl.lss.ccms.android.I18n;
import pl.lss.ccms.android.R;
import pl.lss.ccms.android.Utils;
import pl.lss.ccms.android.bean.Item;

/**
 * Created by ctran on 2015-07-03.
 */
public class ItemListAdapter extends BaseAdapter {

    private Activity activity;
    private LayoutInflater inflater;
    private List<Item> itemsList;

    public ItemListAdapter(Activity activity, List<Item> itemsList) {
        this.activity = activity;
        this.itemsList = itemsList;
    }

    @Override
    public int getCount() {
        return itemsList.size();
    }

    @Override
    public Object getItem(int location) {
        return itemsList.get(location);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (inflater == null)
            inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (convertView == null)
            convertView = inflater.inflate(R.layout.item_row, null);

        Item item = itemsList.get(position);

        TextView code = (TextView) convertView.findViewById(R.id.product_code);
        TextView value = (TextView) convertView.findViewById(R.id.item_value);
        TextView desc = (TextView) convertView.findViewById(R.id.item_desc);

        code.setText(item.productCode);
        value.setText(String.valueOf(Utils.round(item.value)) + " " + I18n.PLN);
        desc.setText(item.requiredPack + " x " + item.quantity + " x " + item.price + I18n.PLN);

        return convertView;
    }
}