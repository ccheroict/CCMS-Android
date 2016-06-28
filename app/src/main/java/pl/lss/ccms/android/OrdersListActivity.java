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
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import pl.lss.ccms.android.adapter.OrderListAdapter;
import pl.lss.ccms.android.bean.Order;


public class OrdersListActivity extends AppCompatActivity {

    private static final String ORDER_ID = "id";
    private static final String ORDER_CODE = "code";
    private static final String ORDER_PACK_QUANTITY = "packQuantity";
    private static final String ORDER_PRODUCT_QUANTITY = "productQuantity";
    private static final String ORDER_VALUE = "value";
    private static final String ORDER_CREATED_DATE = "createdDate";

    private ListView listView;
    private ArrayList<Order> ordersList;
    private ProgressDialog progressDialog;
    private OrderListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orders_list);

        ordersList = new ArrayList<>();
        adapter = new OrderListAdapter(this, ordersList);

        listView = (ListView) findViewById(R.id.all_orders);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(OrdersListActivity.this, OrderDetailActivity.class);
                intent.putExtra(OrderDetailActivity.INTENT_ORDER, ordersList.get(position));
                startActivity(intent);
            }
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(I18n.pleaseWait);
        progressDialog.setCancelable(false);

        Button btn  = (Button) findViewById(R.id.action_new_order);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewOrder();
            }
        });
        refreshOrdersList();
    }

    private void createNewOrder() {
        progressDialog.show();
        String url = "http://chungtq.linuxpl.eu/ccms/create_new_order.php?uuid=" + Cache.uuid + "&initial=" + Cache.userInitial + "&company_id=" + Cache.companyId;
        CustomRequest jreq = new CustomRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    progressDialog.dismiss();
                    int success = response.getInt("success");

                    if (success == 1) {
                        refreshOrdersList();
                    } else showErrow(I18n.errorWhenCreatingNewOrder);
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
        AppController.getInstance().addToReqQueue(jreq);
    }

    private void showErrow(String message) {
        Toast.makeText(OrdersListActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void refreshOrdersList() {
        progressDialog.show();
        String url = "http://chungtq.linuxpl.eu/ccms/read_all_orders.php?initial=" + Cache.userInitial;
        CustomRequest jreq = new CustomRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            progressDialog.dismiss();
                            int success = response.getInt("success");

                            if (success == 1) {
                                JSONArray ja = response.getJSONArray("orders");
                                ordersList.clear();
                                for (int i = 0; i < ja.length(); i++) {

                                    JSONObject jobj = ja.getJSONObject(i);
                                    Order order = new Order();
                                    order.id = jobj.getInt(ORDER_ID);
                                    order.code = jobj.getString(ORDER_CODE);
                                    order.packQuantity = jobj.getInt(ORDER_PACK_QUANTITY);
                                    order.productQuantity = jobj.getInt(ORDER_PRODUCT_QUANTITY);
                                    order.value = jobj.getDouble(ORDER_VALUE);
                                    order.createdDate = jobj.getString(ORDER_CREATED_DATE);

                                    ordersList.add(order);
                                }
                            } else showErrow(I18n.errorWhenRefreshingOrderList);
                            adapter.notifyDataSetChanged();
                        } catch (JSONException e) {
                            Log.e("readOrders", e.getMessage());
                        }

                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                Toast.makeText(OrdersListActivity.this, I18n.errorWhenRefreshingOrderList, Toast.LENGTH_SHORT).show();
            }
        });

        AppController.getInstance().addToReqQueue(jreq);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_orders_list, menu);
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
            refreshOrdersList();
            return true;
        }
        if (id == R.id.action_add_order) {
            createNewOrder();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
