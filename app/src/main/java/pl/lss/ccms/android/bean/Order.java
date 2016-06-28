package pl.lss.ccms.android.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ctran on 2015-07-03.
 */
public class Order implements Serializable{

    public Integer id;
    public String code;
    public Integer packQuantity;
    public Integer productQuantity;
    public Double total;
    public Double value;
    public String createdDate;
    public List<Item> items = new ArrayList<>();
}
