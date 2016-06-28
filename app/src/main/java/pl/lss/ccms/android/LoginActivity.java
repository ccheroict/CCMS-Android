package pl.lss.ccms.android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONException;
import org.json.JSONObject;


public class LoginActivity extends AppCompatActivity {

    private static final String USER_UUID = "uuid";
    private static final String USER_INITIAL = "initial";
    private static final String COMPANY_ID = "companyId";
    private Button loginBtn;
    private TextView usernameTv, passwordTv;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle(R.string.title_activity_login);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(I18n.pleaseWait);
        progressDialog.setCancelable(false);

        loginBtn = (Button) findViewById(R.id.button_login);
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressDialog.show();
                CustomRequest jreq = new CustomRequest(Request.Method.GET, "http://chungtq.linuxpl.eu/ccms/login.php?username=" + usernameTv.getText() + "&password=" + passwordTv.getText(), null,
                        new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    progressDialog.dismiss();
                                    int success = response.getInt("success");

                                    if (success == 1) {
                                        Cache.uuid = response.getString(USER_UUID);
                                        Cache.userInitial = response.getString(USER_INITIAL);
                                        Cache.companyId = response.getString(COMPANY_ID);

                                        Intent intent = new Intent(LoginActivity.this, OrdersListActivity.class);
                                        startActivity(intent);
                                    }
                                } catch (JSONException e) {
                                    Log.e("Login", e.getMessage());
                                }
                            }
                        }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressDialog.dismiss();
                        Log.e("Login", error.getMessage());
                    }
                });

                // Adding request to request queue
                AppController.getInstance().addToReqQueue(jreq);
            }
        });

        usernameTv = (TextView) findViewById(R.id.username);
        passwordTv = (TextView) findViewById(R.id.password);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Cache.uuid != null) {
            Intent intent = new Intent(LoginActivity.this, OrdersListActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }
}
