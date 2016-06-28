package pl.lss.ccms.android;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Created by ctran on 2015-07-03.
 */
public class Utils {
    private static final DecimalFormat doubleFormatter = new DecimalFormat("####0.00");

    static {
        doubleFormatter.setMinimumFractionDigits(2);
        doubleFormatter.setMaximumFractionDigits(2);
    }

    public static Double round(double value) {
        BigDecimal number = new BigDecimal(value);
        value = number.setScale(2, RoundingMode.CEILING).doubleValue();
//        return Double.parseDouble(doubleFormatter.format(value).replace(",", "."));
        return value;
    }
}
