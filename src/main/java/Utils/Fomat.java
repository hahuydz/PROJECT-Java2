package Utils;
import java.text.NumberFormat;
import java.util.Locale;

public class Fomat {
    public static String formatVND(double price) {
        NumberFormat vn = NumberFormat.getInstance(new Locale("vi", "VN"));
        return vn.format(price) + " VNĐ";
    }
}