package me.crud.database;

import me.crud.model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ProductManager {
    private static ProductManager instance;
    private final HashMap<Integer, Product> products;
    private boolean isLoaded;

    public ProductManager() {
        this.isLoaded = false;
        this.products = new HashMap<>();
    }

    public HashMap<Integer, Product> getProducts() {
        return products;
    }

    public void loadAllProducts() throws SQLException {
        System.out.println("Loading products");
        if (this.isLoaded) return;
        DatabaseManager databaseManager = DatabaseManager.getInstance();
        if (databaseManager == null || databaseManager.isClosed()) return;

        Connection connection = databaseManager.getConnection();
        PreparedStatement statement = connection.prepareStatement("SELECT * FROM products;");
        ResultSet result = statement.executeQuery();
        int count = 0;
        while (result.next()) {
            int id = result.getInt("id");
            String name = result.getString("name");
            float price = result.getFloat("price");
            int quantity = result.getInt("quantity");
            Product product = new Product(id, name, price, quantity);
            products.put(id, product);
            count++;
        }

        this.isLoaded = true;
        System.out.println(count + " products loaded successfully");
    }

    public Product getProductById(int id) {
        return products.get(id);
    }

    public Product getProductByName(String name) {
        for (Map.Entry<Integer, Product> e : products.entrySet()) {
            Product product = e.getValue();
            if (product.getName().equalsIgnoreCase(name)) {
                return product;
            }
        }
        return null;
    }

    private String treatProductNameForComparison(String s) {
        s = s.toLowerCase();
        s = Normalizer.normalize(s, Normalizer.Form.NFD).replaceAll("\\p{M}", "");

        StringBuilder newName = new StringBuilder();
        String ignore = "-.@!#$%¨'\"&*()/\\";
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (ignore.indexOf(c) == -1) {
                newName.append(c);
            }
        }

        return newName.toString();
    }

    public ArrayList<Product> getProductsByName(String name) {
        name = treatProductNameForComparison(name);
        String[] words = name.split(" ");
        ArrayList<Product> result = new ArrayList<>();

        for (Map.Entry<Integer, Product> e : products.entrySet()) {
            Product product = e.getValue();
            String productName = treatProductNameForComparison(product.getName());
            boolean failed = false;
            for (String word : words) {
                if (!productName.contains(word)) {
                    failed = true;
                    break;
                }
            }

            if (!failed) {
                result.add(product);
            }
        }

        return result;
    }

    public Product createProduct(String name, float price, int quantity) {
        if (getProductByName(name) != null) return null;

        DatabaseManager databaseManager = DatabaseManager.getInstance();
        if (databaseManager.isClosed()) return null;
        Connection connection = databaseManager.getConnection();
        try {
            PreparedStatement statement = connection.prepareStatement("INSERT INTO products (name, price, quantity) VALUES (?,?,?) RETURNING id;");
            statement.setString(1, name);
            statement.setFloat(2, price);
            statement.setInt(3, quantity);
            ResultSet result = statement.executeQuery();
            if (result.first()) {
                int id = result.getInt("id");
                Product product = new Product(id, name, price, quantity);
                products.put(id, product);
                return product;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return null;
        }

        return null;
    }

    public boolean updateProduct(Product product) {
        if (products.get(product.getId()) != product) return false;

        DatabaseManager databaseManager = DatabaseManager.getInstance();
        if (databaseManager.isClosed()) return false;
        Connection connection = databaseManager.getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement("UPDATE products SET name=?, price=?, quantity=? WHERE id=?;");
            statement.setString(1, product.getName());
            statement.setFloat(2, product.getPrice());
            statement.setInt(3, product.getQuantity());
            statement.setInt(4, product.getId());
            statement.execute();
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    public void deleteProduct(int id) {
        Product product = getProductById(id);
        if (product == null) return;

        DatabaseManager databaseManager = DatabaseManager.getInstance();
        Connection connection = databaseManager.getConnection();
        if (databaseManager.isClosed()) return;

        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM products WHERE id=?;");
            statement.setInt(1, id);
            statement.execute();
        }
        catch (Exception e) {
            System.out.println("An error occurred when deleting product (id: " + id + ")");
            return;
        }

        products.remove(id);
    }

    public static void setInstance(ProductManager instance) {
        ProductManager.instance = instance;
    }

    public static ProductManager getInstance() {
        return ProductManager.instance;
    }

    public boolean isLoaded() {
        return isLoaded;
    }
}