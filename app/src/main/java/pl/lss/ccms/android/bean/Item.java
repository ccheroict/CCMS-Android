package pl.lss.ccms.android.bean;

import java.io.Serializable;

/**
 * Created by ctran on 2015-07-03.
 */
public class Item implements Serializable {

    public Integer id;
    public Integer productId;
    public String supplierCode;
    public String productCode;
    public Integer productPackSize;
    public Integer requiredPack;
    public Integer quantity;
    public Double price;
    public Double value;
    public Integer orderId;
    public String productGender;
    public String productColour;
    public String productSize;
}
