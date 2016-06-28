package pl.lss.ccms.android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import pl.lss.ccms.android.adapter.ItemListAdapter;
import pl.lss.ccms.android.bean.Item;
import pl.lss.ccms.android.bean.Order;


public class OrderDetailActivity extends AppCompatActivity {

    public static final String INTENT_ORDER = "pl.lss.ccms.android.OrderDetailActivity.ORDER";
    private static final String ITEM_ID = "id";
    private static final String ITEM_PRODUCT_ID = "productId";
    private static final String ITEM_PRODUCT_CODE = "code";
    private static final String ITEM_REQUIRED_PACK = "requiredPack";
    private static final String ITEM_QUANTITY = "quantity";
    private static final String ITEM_PRICE = "price";
    private static final String ITEM_VALUE = "value";
    private Order order;
    private ProgressDialog progressDialog;
    private ItemListAdapter adapter;
    private ListView listView;
    private TextView requiredPack;
    private TextView productQuantity;
    private TextView value;
    private Button newItemBtn;
    private Button backToOrdersListBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_detail);
        order = (Order) getIntent().getSerializableExtra(OrderDetailActivity.INTENT_ORDER);
        setTitle(order.code);

        requiredPack = (TextView) findViewById(R.id.order_required_pack);
        productQuantity = (TextView) findViewById(R.id.order_product_quantity);
        value = (TextView) findViewById(R.id.order_value);
        newItemBtn = (Button) findViewById(R.id.order_detail_button_add);
        newItemBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewItem();
            }
        });
        backToOrdersListBtn = (Button) findViewById(R.id.order_detail_button_back_to_orders_list);
        backToOrdersListBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(OrderDetailActivity.this, OrdersListActivity.class);
                startActivity(intent);
            }
        });

        listView = (ListView) findViewById(R.id.order_items_list);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                gotoItemDetailActivity(order.items.get(position));
            }
        });
        adapter = new ItemListAdapter(this, order.items);
        listView.setAdapter(adapter);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(I18n.pleaseWait);
        progressDialog.setCancelable(false);
        refreshOrderDetail();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_order_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            refreshOrderDetail();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void createNewItem() {
        Item item = new Item();
        item.orderId = order.id;
        gotoItemDetailActivity(item);
    }

    private void gotoItemDetailActivity(Item item) {
        Intent intent = new Intent(OrderDetailActivity.this, ItemDetailActivity.class);
        intent.putExtra(ItemDetailActivity.INTENT_ORDER, order);
        intent.putExtra(ItemDetailActivity.INTENT_ITEM, item);
        startActivity(intent);
    }

    private void refreshOrderDetail() {
        progressDialog.show();
        CustomRequest jreq = new CustomRequest(Request.Method.GET, "http://chungtq.linuxpl.eu/ccms/read_order_items.php?id=" + String.valueOf(order.id), null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            progressDialog.dismiss();
                            int success = response.getInt("success");

                            if (success == 1) {
                                JSONArray ja = response.getJSONArray("items");
                                order.items.clear();
                                for (int i = 0; i < ja.length(); i++) {

                                    JSONObject jobj = ja.getJSONObject(i);
                                    Item item = new Item();
                                    item.id = jobj.getInt(ITEM_ID);
                                    item.productId = jobj.getInt(ITEM_PRODUCT_ID);
                                    item.productCode = jobj.getString(ITEM_PRODUCT_CODE);
                                    item.requiredPack = jobj.getInt(ITEM_REQUIRED_PACK);
                                    item.quantity = jobj.getInt(ITEM_QUANTITY);
                                    item.price = jobj.getDouble(ITEM_PRICE);
                                    item.value = jobj.getDouble(ITEM_VALUE);


                                    order.items.add(item);
                                } // for loop ends
                            } else showError(I18n.errorWhenReadingOrderDetail);
                            adapter.notifyDataSetChanged();
                            refreshSummarizedInfo();
                        } catch (JSONException e) {
                            Log.e("readItems", e.getMessage());
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
            }
        });

        // Adding request to request queue
        AppController.getInstance().addToReqQueue(jreq);
    }

    private void showError(String message) {
        Toast.makeText(OrderDetailActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void refreshSummarizedInfo() {
        int rp = 0, pq = 0;
        double v = 0.0;
        for (Item item : order.items) {
            rp += item.requiredPack;
            pq += item.quantity;
            v += item.value;
        }
        requiredPack.setText(String.valueOf(rp));
        productQuantity.setText(String.valueOf(pq));
        String s = String.valueOf(Utils.round(v));
        value.setText(s + " " + I18n.PLN);
    }
}
