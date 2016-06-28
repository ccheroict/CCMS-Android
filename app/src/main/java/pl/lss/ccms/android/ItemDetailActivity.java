package pl.lss.ccms.android;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;

import pl.lss.ccms.android.bean.Item;
import pl.lss.ccms.android.bean.Order;

import static android.widget.Toast.LENGTH_SHORT;


public class ItemDetailActivity extends AppCompatActivity {

    public static final String INTENT_ORDER = "pl.lss.ccms.android.ItemDetailActivity.ORDER";
    public static final String INTENT_ITEM = "pl.lss.ccms.android.ItemDetailActivity.ITEM";
    private static final String ITEM_CODE = "product_code";
    private static final String ITEM_PACK_SIZE = "product_pack_size";
    private static final String ITEM_PRICE = "finalPrice";
    private static final String ITEM_SUPPLIER_CODE = "supplier_code";
    private static final String ITEM_ID = "item_id";
    private static final String ITEM_GENDER = "gender";
    private static final String ITEM_COLOUR = "colour";
    private static final String ITEM_SIZE = "size";

    private Order order;
    private Item item;
    private TextView productId;
    private TextView supplierCode;
    private TextView productColour;
    private TextView productGender;
    private TextView productPackSize;
    private TextView requiredPack;
    private TextView productQuantity;
    private TextView productPrice;
    private TextView productSize;
    private TextView value;
    private Button searchBtn;
    private Button saveBtn;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_detail);

        Intent intent = getIntent();
        order = (Order) intent.getSerializableExtra(ItemDetailActivity.INTENT_ORDER);
        item = (Item) intent.getSerializableExtra(ItemDetailActivity.INTENT_ITEM);

        productId = (TextView) findViewById(R.id.item_detail_product_id);
        productId.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                value.setEnabled(false);
            }
        });
        supplierCode = (TextView) findViewById(R.id.item_detail_supplier_code);
        productColour = (TextView) findViewById(R.id.item_detail_product_colour);
        productGender = (TextView) findViewById(R.id.item_detail_product_gender);
        productPackSize = (TextView) findViewById(R.id.item_detail_product_pack_size);
        productSize = (TextView) findViewById(R.id.item_detail_product_size);
        requiredPack = (TextView) findViewById(R.id.item_detail_required_pack);
        productQuantity = (TextView) findViewById(R.id.item_detail_product_quantity);
        productQuantity.setEnabled(false);
        productPrice = (TextView) findViewById(R.id.item_detail_product_price);
        productPrice.setEnabled(false);
        value = (TextView) findViewById(R.id.item_detail_value);
        value.setEnabled(false);
        saveBtn = (Button) findViewById(R.id.item_detail_button_save);

        searchBtn = (Button) findViewById(R.id.item_detail_button_search);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideKeyboardIfNeeded();
                try {
                    Integer id = Integer.parseInt(String.valueOf(productId.getText()));
                    searchProduct(id);
                } catch (NumberFormatException ex) {
                    showError(I18n.productIdIsInvalid);
                }
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Integer np = Integer.parseInt(String.valueOf(requiredPack.getText()));
                    item.requiredPack = np;
                    item.quantity = item.requiredPack * item.productPackSize;
                    item.value = item.price * item.quantity;
                    refreshViews();
                    hideKeyboardIfNeeded();
                    createOrUpdateItem();
                } catch (NumberFormatException ex) {
                    showError(I18n.requiredPackIsInvalid);
                    saveBtn.setEnabled(false);
                }
            }
        });
        saveBtn.setEnabled(false);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(I18n.pleaseWait);
        progressDialog.setCancelable(false);

        if (item.id != null) {
            productId.setEnabled(false);
            searchProduct(item.productId);
        }
    }

    private void hideKeyboardIfNeeded() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void refreshViews() {
        productId.setText(String.valueOf(item.productId));
        supplierCode.setText(item.supplierCode);
        productColour.setText(item.productColour);
        productGender.setText(item.productGender);
        productPackSize.setText(String.valueOf(item.productPackSize));
        productSize.setText(item.productSize);
        requiredPack.setText(String.valueOf(item.requiredPack));
        productQuantity.setText(String.valueOf(item.quantity));
        productPrice.setText(String.valueOf(item.price));
        value.setText(String.valueOf(Utils.round(item.value)));
    }

    private void showError(String message) {
        Toast.makeText(this, message, LENGTH_SHORT).show();
    }

    private void searchProduct(final Integer id) {
        progressDialog.show();
        saveBtn.setEnabled(false);
        CustomRequest jreq = new CustomRequest(Request.Method.GET, "http://chungtq.linuxpl.eu/ccms/read_product_info.php?id=" + id, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            progressDialog.dismiss();
                            int success = response.getInt("success");

                            if (success == 1) {
                                item.productCode = response.getString(ITEM_CODE);
                                item.productPackSize = response.getInt(ITEM_PACK_SIZE);
                                item.productSize = response.getString(ITEM_SIZE);
                                item.supplierCode = response.getString(ITEM_SUPPLIER_CODE);
                                item.productGender = response.getString(ITEM_GENDER);
                                item.productColour = response.getString(ITEM_COLOUR);
                                if (item.productId == null || !id.equals(item.productId)) {
                                    item.productId = id;
                                    item.price = response.getDouble(ITEM_PRICE);
                                    item.requiredPack = 0;
                                    item.quantity = 0;
                                    item.value = 0.0;
                                }
                                refreshViews();
                                requiredPack.setEnabled(true);
                                saveBtn.setEnabled(true);
                            } else showError(I18n.errorWhenReadingProduct);
                        } catch (JSONException e) {
                            Log.e("searchProduct", e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                showError(I18n.errorWhenReadingProduct);
            }
        });
        AppController.getInstance().addToReqQueue(jreq);
    }

    private void createOrUpdateItem() {
        for (Item it : order.items) {
            if (it.productId.equals(item.productId)) {
                if (item.id == null) {
                    it.requiredPack += item.requiredPack;
                    it.quantity += item.quantity;
                    it.value += item.value;
                } else {
                    it.requiredPack = item.requiredPack;
                    it.quantity = item.quantity;
                    it.value = item.value;
                }
                item = it;
            }
        }
        if (item.id == null) {
            if (item.quantity > 0) {
                order.items.add(item);
                createItem();
            }
        } else if (item.quantity > 0) updateItem();
        else {
            order.items.remove(item);
            deleteItem();
        }
    }

    private void deleteItem() {
        progressDialog.show();
        saveBtn.setEnabled(false);
        String url = "http://chungtq.linuxpl.eu/ccms/delete_item.php?uuid=" + Cache.uuid
                + "&id=" + String.valueOf(item.id);

        CustomRequest jreq = new CustomRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            progressDialog.dismiss();
                            int success = response.getInt("success");

                            if (success == 1) {
                                updateOrder();
                            } else showError(I18n.errorWhenUpdatingOrder);
                        } catch (JSONException e) {
                            Log.e("searchProduct", e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                showError(I18n.errorWhenUpdatingOrder);
            }
        });
        AppController.getInstance().addToReqQueue(jreq);
    }

    private void updateItem() {
        progressDialog.show();
        saveBtn.setEnabled(false);
        String url = "http://chungtq.linuxpl.eu/ccms/update_item.php?uuid=" + Cache.uuid
                + "&id=" + String.valueOf(item.id)
                + "&requiredPack=" + String.valueOf(item.requiredPack)
                + "&quantity=" + String.valueOf(item.quantity)
                + "&value=" + String.valueOf(item.value);

        CustomRequest jreq = new CustomRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            progressDialog.dismiss();
                            int success = response.getInt("success");

                            if (success == 1) {
                                updateOrder();
                            } else showError(I18n.errorWhenUpdatingOrder);
                        } catch (JSONException e) {
                            Log.e("searchProduct", e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                showError(I18n.errorWhenUpdatingOrder);
            }
        });
        AppController.getInstance().addToReqQueue(jreq);
    }

    private void createItem() {
        progressDialog.show();
        saveBtn.setEnabled(false);
        String url = "http://chungtq.linuxpl.eu/ccms/create_new_item.php?uuid=" + Cache.uuid
                + "&productId=" + String.valueOf(item.productId)
                + "&requiredPack=" + String.valueOf(item.requiredPack)
                + "&quantity=" + String.valueOf(item.quantity)
                + "&price=" + String.valueOf(item.price)
                + "&value=" + String.valueOf(item.value)
                + "&orderId=" + String.valueOf(item.orderId);

        CustomRequest jreq = new CustomRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            progressDialog.dismiss();
                            int success = response.getInt("success");

                            if (success == 1) {
                                item.id = response.getInt(ITEM_ID);
                                updateOrder();
                            } else showError(I18n.errorWhenCreatingNewItem);
                        } catch (JSONException e) {
                            Log.e("searchProduct", e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                showError(I18n.errorWhenUpdatingOrder);
            }
        });
        AppController.getInstance().addToReqQueue(jreq);
    }

    private void updateOrder() {
        order.packQuantity = 0;
        order.productQuantity = 0;
        order.value = 0.0;
        for (Item item : order.items) {
            order.packQuantity += item.requiredPack;
            order.productQuantity += item.quantity;
            order.value += item.value;
        }
        order.value = Utils.round(order.value);

        progressDialog.show();
        saveBtn.setEnabled(false);
        String url = "http://chungtq.linuxpl.eu/ccms/update_order.php?uuid=" + Cache.uuid
                + "&orderId=" + String.valueOf(order.id)
                + "&packQuantity=" + String.valueOf(order.packQuantity)
                + "&productQuantity=" + String.valueOf(order.productQuantity)
                + "&value=" + String.valueOf(order.value);

        CustomRequest jreq = new CustomRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            progressDialog.dismiss();
                            int success = response.getInt("success");

                            if (success == 1) {
                                gotoOrderDetailActivity();
                            } else showError(I18n.errorWhenUpdatingOrder);
                        } catch (JSONException e) {
                            Log.e("searchProduct", e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                progressDialog.dismiss();
                showError(I18n.errorWhenUpdatingOrder);
            }
        });
        AppController.getInstance().addToReqQueue(jreq);
    }

    private void gotoOrderDetailActivity() {
        Intent intent = new Intent(ItemDetailActivity.this, OrderDetailActivity.class);
        intent.putExtra(OrderDetailActivity.INTENT_ORDER, order);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_item_detail, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
