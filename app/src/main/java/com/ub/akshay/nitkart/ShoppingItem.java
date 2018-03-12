package com.ub.akshay.nitkart;

import java.io.Serializable;
import java.text.NumberFormat;

/**
 * Created by akshay on 4/4/17.
 */

public class ShoppingItem implements Serializable{

    private String name, type, description, productID;
    private int price, quantity;

    public ShoppingItem(String productId, String name, String type, String description, int price, int quantity){
        this.productID = productId;
        this.name = name;
        this.type = type;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }

    public void setQuantity(int quantity){
        this.quantity = quantity;
    }

    public String getProductID() {
        return productID;
    }

    public String getTitle() {
        return name;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getPrice() {
        return NumberFormat.getCurrencyInstance().format(price);
    }
}
