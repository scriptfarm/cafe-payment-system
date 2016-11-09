package com.example.andre.cafeterminalapp.order;

import android.app.Activity;
import android.util.Log;
import android.widget.Toast;

import com.example.andre.cafeterminalapp.utils.CustomLocalStorage;
import com.example.andre.cafeterminalapp.utils.ServerRestClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import cz.msebera.android.httpclient.Header;

/**
 * Created by andre on 07/11/2016.
 * Class used to store orders when no internet connection is available
 */

public class Order implements Serializable {
    private String products;
    private String user_id, user_pin;
    private String vouchers;
    private String timestamp;

    private static ArrayList<Order> unsentOrders;

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getVouchers() {
        return vouchers;
    }

    public void setVouchers(String vouchers) {
        this.vouchers = vouchers;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getUser_pin() {
        return user_pin;
    }

    public void setUser_pin(String user_pin) {
        this.user_pin = user_pin;
    }

    public String getProducts() {
        return products;
    }

    public void setProducts(String ps) {
        this.products = ps;
    }

    ////////////////////////////////////////
    ////////// UNSENT ORDERS //////////////
    public static ArrayList<Order> getUnsentOrders(Activity a) {
        Order.getSavedUnsentOrders(a);
        return unsentOrders;
    }

    private static void getSavedUnsentOrders(Activity a) {
        try {
            unsentOrders = CustomLocalStorage.getUnsentOrders(a);
        } catch (Exception e) {
            unsentOrders = new ArrayList<>();
        }
    }

    public static void saveUnsentOrders(Activity a) {
        try {
            CustomLocalStorage.saveUnsentOrders(a, Order.unsentOrders);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void addUnsentOrder(Order o) {
        unsentOrders.add(o);
    }

    public static void sendUnsentOrders(Activity a) {
        for (Order o: unsentOrders)
            sendUnsentOrder(o, a);
    }

    private static void sendUnsentOrder(final Order o, final Activity a) {
        HashMap<String, String> order_params = new HashMap<>();
        RequestParams order = new RequestParams();

        order_params.put("cart",o.getProducts());
        order_params.put("user",o.getUser_id());
        order_params.put("pin",o.getUser_pin());
        order_params.put("vouchers",o.getVouchers());
        order_params.put("timestamp",o.getTimestamp());
        order.put("order",order_params);

        ServerRestClient.post("transaction", order, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try{
                    String error = response.get("error").toString();
                    return;
                }
                catch(JSONException e){
                    //normal behaviour when there are no errors.
                }

                Toast.makeText(a, "Order successfull", Toast.LENGTH_SHORT).show();
                Log.e("order",response.toString());
                unsentOrders.remove(o);
                Order.saveUnsentOrders(a);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String error, Throwable throwable) {
                Log.e("FAILURE:", "~JSON OBJECT - status: "+statusCode);
                Log.e("FAILURE:", error);

                //error: order stays in unsent orders
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject object) {
                Log.e("FAILURE:", "some error I dont know how to handle. timeout?");
                Log.e("FAILURE:", "JSON OBJECT - status: "+statusCode);

                //error: order stays in unsent orders
            }
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;

        Order order = (Order) o;

        if (products != null ? !products.equals(order.products) : order.products != null)
            return false;
        if (!user_id.equals(order.user_id))
            return false;
        if (!user_pin.equals(order.user_pin))
            return false;
        if (vouchers != null ? !vouchers.equals(order.vouchers) : order.vouchers != null)
            return false;
        return timestamp.equals(order.timestamp);
    }
}
