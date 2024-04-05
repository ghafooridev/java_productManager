package me.crud;

import java.awt.EventQueue;

import me.crud.database.DatabaseManager;
import me.crud.database.ProductManager;
import me.crud.form.MainForm;
import me.crud.model.Product;


public class Main {
    public static void main(String[] args) {
        DatabaseManager databaseManager = new DatabaseManager();
        DatabaseManager.setInstance(databaseManager);

        ProductManager productManager = new ProductManager();
        ProductManager.setInstance(productManager);

        EventQueue.invokeLater(() -> {
            MainForm mainForm = new MainForm();
            MainForm.setInstance(mainForm);
            mainForm.setVisible(true);
            
            mainForm.initLoading();
            mainForm.updateProductsTable();

            /* test
            for (Product p : productManager.getProductsByName("a")) {
                System.out.println(p.getId());
                System.out.println(p.getName());
                System.out.println(p.getPrice());
                System.out.println(p.getQuantity());
            }*/
        });
    }
}
