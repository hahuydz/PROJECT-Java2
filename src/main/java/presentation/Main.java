package presentation;

import Utils.DB_Utils;
import Utils.Fomat;

import java.sql.*;
import java.util.Scanner;
import java.security.MessageDigest;

public class Main {

    static Scanner sc = new Scanner(System.in);

    // ================= UI =================
    public static void line() {
        System.out.println("==============================================================");
    }

    public static void title(String text) {
        line();
        System.out.printf("                  %s\n", text.toUpperCase());
        line();
    }

    public static void menuItem(int index, String text) {
        System.out.printf("| %-3d | %-45s |\n", index, text);
    }

    public static void input(String label) {
        System.out.printf("➤ %-15s: ", label);
    }

    // ================= HASH =================
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());

            StringBuilder hex = new StringBuilder();
            for (byte b : hash) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    // ================= MAIN =================
    public static void main(String[] args) {
        while (true) {
            title("MAIN MENU");

            menuItem(1, "Đăng ký");
            menuItem(2, "Đăng nhập");
            menuItem(0, "Thoát");
            line();

            int choice = Integer.parseInt(sc.nextLine());

            switch (choice) {
                case 1 -> register();
                case 2 -> login();
                case 0 -> { return; }
            }
        }
    }

    // ================= REGISTER =================
    public static void register() {
        title("ĐĂNG KÝ");

        try (Connection con = DB_Utils.getInstance().getConnection()) {

            input("Tên");
            String name = sc.nextLine();

            input("Email");
            String email = sc.nextLine();

            input("SĐT");
            String phone = sc.nextLine();

            input("Password");
            String pass = sc.nextLine();

            // ===== VALIDATE =====
            if (name.trim().isEmpty() || email.trim().isEmpty() || pass.trim().isEmpty()) {
                System.out.println(" Không được để trống!");
                return;
            }

            if (!email.contains("@")) {
                System.out.println(" Email phải có @");
                return;
            }

            if (!phone.matches("\\d{10}")) {
                System.out.println(" SĐT phải 10 số!");
                return;
            }

            // ===== HASH =====
            String hashed = hashPassword(pass);

            // ===== SQL =====
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO users(name,email,password,phone,role) VALUES(?,?,?,?, 'CUSTOMER')"
            );

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, hashed);
            ps.setString(4, phone);

            ps.executeUpdate();

            System.out.println(" Đăng ký thành công!");

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(" Lỗi đăng ký!");
        }
    }


    // ================= LOGIN =================
    public static void login() {
        title("ĐĂNG NHẬP");

        try (Connection con = DB_Utils.getInstance().getConnection()) {

            input("Tên"); String name = sc.nextLine();
            input("Password"); String pass = sc.nextLine();

            PreparedStatement ps = con.prepareStatement(
                    "SELECT * FROM users WHERE name=?"
            );
            ps.setString(1, name);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String hashedDB = rs.getString("password");

                if (hashPassword(pass).equals(hashedDB)) {
                    int userId = rs.getInt("id");
                    String role = rs.getString("role");

                    System.out.println(" Đăng nhập thành công!");

                    if (role.equals("ADMIN")) adminMenu();
                    else userMenu(userId);

                } else {
                    System.out.println(" Sai mật khẩu!");
                }
            } else {
                System.out.println(" Sai tài khoản!");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= USER MENU =================
    public static void userMenu(int userId) {
        while (true) {
            title("USER MENU");

            menuItem(1, "Xem sản phẩm");
            menuItem(2, "Thêm vào giỏ");
            menuItem(3, "Đặt hàng");
            menuItem(4, "Xem đơn hàng");
            menuItem(0, "Đăng xuất");
            line();

            int choice = Integer.parseInt(sc.nextLine());

            switch (choice) {
                case 1 -> viewProducts();
                case 2 -> addToCart(userId);
                case 3 -> checkout(userId);
                case 4 -> viewOrders(userId);
                case 0 -> { return; }
            }
        }
    }

    // ================= ADMIN MENU =================
    public static void adminMenu() {
        while (true) {
            title("ADMIN MENU");

            menuItem(1, "Xem tất cả đơn hàng");
            menuItem(2, "Quản lý sản phẩm");
            menuItem(0, "Đăng xuất");
            line();

            int choice = Integer.parseInt(sc.nextLine());



            switch (choice) {
                case 1 -> getAllOrders();
                case 2 -> productManagement();
                case 0 -> { return; }
            }
        }
    }

    // ================= PRODUCT MANAGEMENT =================
    public static void productManagement() {
        while (true) {
            title("QUẢN LÝ SẢN PHẨM");

            menuItem(1, "Xem danh sách sản phẩm");
            menuItem(2, "Thêm sản phẩm");
            menuItem(3, "Sửa sản phẩm");
            menuItem(4, "Xóa sản phẩm");
            menuItem(5, "Tìm kiếm theo tên");
            menuItem(6, "Sắp xếp theo giá");
            menuItem(0, "Quay lại");
            line();

            int choice = Integer.parseInt(sc.nextLine());

            switch (choice) {
                case 1 -> viewProductsPaging();
                case 2 -> addProduct();
                case 3 -> updateProduct();
                case 4 -> deleteProduct();
                case 5 -> searchProduct();
                case 6 -> sortProduct();
                case 0 -> { return; }
            }
        }
    }

    // ================= VIEW PRODUCTS =================
    public static void viewProducts() {
        try (Connection con = DB_Utils.getInstance().getConnection()) {

            ResultSet rs = con.prepareStatement(
                    "SELECT * FROM products WHERE stock > 0"
            ).executeQuery();

            title("DANH SÁCH SẢN PHẨM");

            // HEADER
            System.out.printf("| %-3s | %-15s | %-10s | %-10s | %-8s | %-12s |\n",
                    "ID", "Tên", "Brand", "Dung lượng", "Màu", "Giá");
            line();

            boolean hasData = false;

            while (rs.next()) {
                hasData = true;

                String brand = rs.getString("brand");
                String storage = rs.getString("storage");
                String color = rs.getString("color");

                // tránh null cho đẹp UI
                if (brand == null) brand = "-";
                if (storage == null) storage = "-";
                if (color == null) color = "-";

                System.out.printf("| %-3d | %-15s | %-10s | %-10s | %-8s | %-12s |\n",
                        rs.getInt("id"),
                        rs.getString("name"),
                        brand,
                        storage,
                        color,
                        Fomat.formatVND(rs.getDouble("price")));
            }

            if (!hasData) {
                System.out.println(" Không có sản phẩm!");
            }

            line();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // ================= PAGING =================
    public static void viewProductsPaging() {
        try (Connection con = DB_Utils.getInstance().getConnection()) {

            int page = 1, limit = 5;

            while (true) {
                int offset = (page - 1) * limit;

                PreparedStatement ps = con.prepareStatement(
                        "SELECT * FROM products LIMIT ? OFFSET ?");
                ps.setInt(1, limit);
                ps.setInt(2, offset);

                ResultSet rs = ps.executeQuery();

                title("DANH SÁCH SẢN PHẨM - PAGE " + page);

                // HEADER
                System.out.printf("| %-3s | %-15s | %-10s | %-10s | %-8s | %-12s | %-6s |\n",
                        "ID", "Tên", "Brand", "Dung lượng", "Màu", "Giá", "Kho");
                line();

                boolean hasData = false;

                while (rs.next()) {
                    hasData = true;

                    String brand = rs.getString("brand");
                    String storage = rs.getString("storage");
                    String color = rs.getString("color");

                    // xử lý null cho đẹp
                    if (brand == null) brand = "-";
                    if (storage == null) storage = "-";
                    if (color == null) color = "-";

                    System.out.printf("| %-3d | %-15s | %-10s | %-10s | %-8s | %-12s | %-6d |\n",
                            rs.getInt("id"),
                            rs.getString("name"),
                            brand,
                            storage,
                            color,
                            Fomat.formatVND(rs.getDouble("price")),
                            rs.getInt("stock"));
                }

                if (!hasData) {
                    System.out.println(" Không có dữ liệu!");
                }

                line();
                System.out.print("N-next | P-prev | 0-exit: ");
                String c = sc.nextLine();

                if (c.equalsIgnoreCase("n")) page++;
                else if (c.equalsIgnoreCase("p") && page > 1) page--;
                else break;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // ================= ADD PRODUCT =================
    public static void addProduct() {
        try (Connection con = DB_Utils.getInstance().getConnection()) {

            title("THÊM SẢN PHẨM");

            input("Tên");
            String name = sc.nextLine();

            input("Brand");
            String brand = sc.nextLine();

            input("Dung lượng");
            String storage = sc.nextLine();

            input("Màu");
            String color = sc.nextLine();

            input("Giá");
            double price = Double.parseDouble(sc.nextLine());

            input("Kho");
            int stock = Integer.parseInt(sc.nextLine());

            // ===== VALIDATE =====
            if (name.isEmpty() || brand.isEmpty() || storage.isEmpty() || color.isEmpty()) {
                System.out.println(" Không được để trống!");
                return;
            }

            if (price <= 0 || stock <= 0) {
                System.out.println(" Giá/Kho phải > 0");
                return;
            }

            // ===== INSERT FULL FIELD =====
            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO products(name, brand, storage, color, price, stock) VALUES(?,?,?,?,?,?)"
            );

            ps.setString(1, name);
            ps.setString(2, brand);
            ps.setString(3, storage);
            ps.setString(4, color);
            ps.setDouble(5, price);
            ps.setInt(6, stock);

            ps.executeUpdate();

            System.out.println(" Thêm sản phẩm thành công!");

        } catch (NumberFormatException e) {
            System.out.println(" Giá/Kho phải là số!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // ================= UPDATE PRODUCT =================
    public static void updateProduct() {
        try (Connection con = DB_Utils.getInstance().getConnection()) {

            input("ID"); int id = Integer.parseInt(sc.nextLine());

            PreparedStatement ps = con.prepareStatement(
                    "UPDATE products SET name=?,price=?,stock=? WHERE id=?"
            );

            input("Tên"); ps.setString(1, sc.nextLine());
            input("Giá"); ps.setDouble(2, Double.parseDouble(sc.nextLine()));
            input("Kho"); ps.setInt(3, Integer.parseInt(sc.nextLine()));
            ps.setInt(4, id);

            ps.executeUpdate();
            System.out.println("✅ Updated!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= DELETE =================
    public static void deleteProduct() {
        try (Connection con = DB_Utils.getInstance().getConnection()) {

            input("ID"); int id = Integer.parseInt(sc.nextLine());

            System.out.print("Xác nhận (Y/N): ");
            if (!sc.nextLine().equalsIgnoreCase("Y")) return;

            PreparedStatement ps = con.prepareStatement("DELETE FROM products WHERE id=?");
            ps.setInt(1, id);
            ps.executeUpdate();

            System.out.println(" Đã xóa!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= SEARCH =================
    public static void searchProduct() {
        try (Connection con = DB_Utils.getInstance().getConnection()) {

            input("Tên"); String key = sc.nextLine();

            PreparedStatement ps = con.prepareStatement("SELECT * FROM products WHERE name LIKE ?");
            ps.setString(1, "%" + key + "%");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                System.out.println(rs.getString("name") + " - " + Fomat.formatVND(rs.getDouble("price")));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= SORT =================
    public static void sortProduct() {
        try (Connection con = DB_Utils.getInstance().getConnection()) {

            System.out.print("1.ASC | 2.DESC: ");
            int c = Integer.parseInt(sc.nextLine());

            String order = (c == 1) ? "ASC" : "DESC";

            ResultSet rs = con.prepareStatement(
                    "SELECT * FROM products ORDER BY price " + order
            ).executeQuery();

            while (rs.next()) {
                System.out.println(rs.getString("name") + " - " + Fomat.formatVND(rs.getDouble("price")));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= ADD TO CART =================
    public static void addToCart(int userId) {
        try (Connection con = DB_Utils.getInstance().getConnection()) {

            input("Product ID"); int pid = Integer.parseInt(sc.nextLine());
            input("Số lượng"); int qty = Integer.parseInt(sc.nextLine());

            PreparedStatement check = con.prepareStatement(
                    "SELECT stock FROM products WHERE id=?"
            );
            check.setInt(1, pid);
            ResultSet rs = check.executeQuery();

            if (!rs.next() || rs.getInt("stock") < qty) {
                System.out.println(" Không đủ hàng!");
                return;
            }

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO cart(user_id, product_id, quantity) VALUES(?,?,?)"
            );
            ps.setInt(1, userId);
            ps.setInt(2, pid);
            ps.setInt(3, qty);

            ps.executeUpdate();
            System.out.println(" Đã thêm!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= CHECKOUT =================
    public static void checkout(int userId) {
        try (Connection con = DB_Utils.getInstance().getConnection()) {

            con.setAutoCommit(false);

            input("Địa chỉ");
            String address = sc.nextLine();

            PreparedStatement cartPS = con.prepareStatement(
                    "SELECT c.product_id, c.quantity, p.price, p.stock " +
                            "FROM cart c JOIN products p ON c.product_id = p.id WHERE c.user_id=?",
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY
            );
            cartPS.setInt(1, userId);
            ResultSet cartRS = cartPS.executeQuery();

            double total = 0;

            while (cartRS.next()) {
                if (cartRS.getInt("stock") < cartRS.getInt("quantity")) {
                    System.out.println("❌ Hết hàng!");
                    con.rollback();
                    return;
                }
                total += cartRS.getDouble("price") * cartRS.getInt("quantity");
            }

            PreparedStatement orderPS = con.prepareStatement(
                    "INSERT INTO orders(user_id,total,shipping_address) VALUES(?,?,?)",
                    Statement.RETURN_GENERATED_KEYS
            );
            orderPS.setInt(1, userId);
            orderPS.setDouble(2, total);
            orderPS.setString(3, address);
            orderPS.executeUpdate();

            ResultSet key = orderPS.getGeneratedKeys();
            key.next();
            int orderId = key.getInt(1);

            cartRS.beforeFirst();

            while (cartRS.next()) {

                PreparedStatement detailPS = con.prepareStatement(
                        "INSERT INTO order_details(order_id, product_id, quantity, price) VALUES(?,?,?,?)"
                );
                detailPS.setInt(1, orderId);
                detailPS.setInt(2, cartRS.getInt("product_id"));
                detailPS.setInt(3, cartRS.getInt("quantity"));
                detailPS.setDouble(4, cartRS.getDouble("price"));
                detailPS.executeUpdate();

                PreparedStatement updateStock = con.prepareStatement(
                        "UPDATE products SET stock = stock - ? WHERE id=?"
                );
                updateStock.setInt(1, cartRS.getInt("quantity"));
                updateStock.setInt(2, cartRS.getInt("product_id"));
                updateStock.executeUpdate();
            }

            PreparedStatement clear = con.prepareStatement(
                    "DELETE FROM cart WHERE user_id=?"
            );
            clear.setInt(1, userId);
            clear.executeUpdate();

            con.commit();
            System.out.println("✅ Đặt hàng thành công!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ================= VIEW ORDERS =================
    public static void viewOrders(int userId) {
        try (Connection con = DB_Utils.getInstance().getConnection()) {

            PreparedStatement ps = con.prepareStatement(
                    "SELECT o.id AS order_id, p.name, p.brand, p.storage, p.color, " +
                            "od.quantity, od.price " +
                            "FROM orders o " +
                            "JOIN order_details od ON o.id = od.order_id " +   // ✅ FIX Ở ĐÂY
                            "JOIN products p ON od.product_id = p.id " +
                            "WHERE o.user_id = ? " +
                            "ORDER BY o.id DESC"
            );


            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            title("ĐƠN HÀNG CỦA BẠN");

            // HEADER
            System.out.printf("| %-5s | %-15s | %-10s | %-10s | %-8s | %-5s | %-12s |\n",
                    "ID", "Tên", "Brand", "Dung lượng", "Màu", "SL", "Giá");
            line();

            boolean hasData = false;
            int currentOrderId = -1;
            double total = 0;

            while (rs.next()) {
                hasData = true;

                int orderId = rs.getInt("order_id");

                // Nếu sang đơn mới → in tổng đơn cũ
                if (currentOrderId != -1 && currentOrderId != orderId) {
                    System.out.println("   → Tổng: " + Fomat.formatVND(total));
                    line();
                    total = 0;
                }

                currentOrderId = orderId;

                String brand = rs.getString("brand");
                String storage = rs.getString("storage");
                String color = rs.getString("color");

                if (brand == null) brand = "-";
                if (storage == null) storage = "-";
                if (color == null) color = "-";

                int quantity = rs.getInt("quantity");
                double price = rs.getDouble("price");

                total += price * quantity;

                System.out.printf("| %-5d | %-15s | %-10s | %-10s | %-8s | %-5d | %-12s |\n",
                        orderId,
                        rs.getString("name"),
                        brand,
                        storage,
                        color,
                        quantity,
                        Fomat.formatVND(price)
                );
            }

            // In tổng đơn cuối
            if (hasData) {
                System.out.println("   → Tổng: " + Fomat.formatVND(total));
            } else {
                System.out.println("❌ Bạn chưa có đơn hàng nào!");
            }

            line();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // ================= ADMIN ORDERS =================
    public static void getAllOrders() {
        try (Connection con = DB_Utils.getInstance().getConnection()) {

            String sql = "SELECT * FROM orders";
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            title("DANH SÁCH ĐƠN HÀNG");

            System.out.printf("| %-5s | %-7s | %-10s | %-15s | %-12s |\n",
                    "ID", "User", "Total", "Address", "Status");
            line();

            while (rs.next()) {

                int orderId = rs.getInt("id");

                System.out.printf("| %-5d | %-7d | %-10.0f | %-15s | %-12s |\n",
                        orderId,
                        rs.getInt("user_id"),
                        rs.getDouble("total"),
                        rs.getString("shipping_address"),
                        rs.getString("status"));

                // ===== HIỂN THỊ SẢN PHẨM TRONG ĐƠN =====
                PreparedStatement detailPS = con.prepareStatement(
                        "SELECT p.name, od.quantity, od.price " +
                                "FROM order_details od JOIN products p ON od.product_id = p.id " +
                                "WHERE od.order_id=?"
                );
                detailPS.setInt(1, orderId);

                ResultSet detailRS = detailPS.executeQuery();

                System.out.println("   -> Sản phẩm:");

                while (detailRS.next()) {
                    System.out.printf("      + %-20s | SL: %-3d | Giá: %.0f\n",
                            detailRS.getString("name"),
                            detailRS.getInt("quantity"),
                            detailRS.getDouble("price"));
                }

                line();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}