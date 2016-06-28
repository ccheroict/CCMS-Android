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
import pl.lss.ccms.android.bean.Order;

/**
 * Created by ctran on 2015-07-03.
 */
public class OrderListAdapter extends BaseAdapter {

    private Activity activity;
    private LayoutInflater inflater;
    private List<Order> orderList;

    public OrderListAdapter(Activity activity, List<Order> orderList) {
        this.activity = activity;
        this.orderList = orderList;
    }

    @Override
    public int getCount() {
        return orderList.size();
    }

    @Override
    public Object getItem(int location) {
        return orderList.get(location);
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
            convertView = inflater.inflate(R.layout.order_row, null);

        Order order = orderList.get(position);

        TextView code = (TextView) convertView.findViewById(R.id.order_code);
        TextView value = (TextView) convertView.findViewById(R.id.order_value);
        TextView createdDate = (TextView) convertView.findViewById(R.id.order_creadted_date);

        value.setText(String.valueOf(order.value) + " " + I18n.PLN);
        code.setText(order.code);
        createdDate.setText(order.createdDate);

        return convertView;
    }
}
