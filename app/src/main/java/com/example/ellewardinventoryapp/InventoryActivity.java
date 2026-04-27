package com.example.ellewardinventoryapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * InventoryActivity displays the inventory items in a scrollable list.
 * Users can add new items, update quantities, and delete items.
 */
public class InventoryActivity extends AppCompatActivity implements InventoryAdapter.OnItemActionListener {

    // UI elements
    private RecyclerView recyclerViewInventory;
    private TextView textEmptyState;
    private FloatingActionButton fabAddItem;
    
    // Data and adapter
    private DatabaseHelper databaseHelper;
    private InventoryAdapter adapter;
    private List<DatabaseHelper.InventoryItem> inventoryItems;
    
    // User information
    private int userId;
    private String username;
    
    // Low stock threshold for SMS notifications
    private static final int LOW_STOCK_THRESHOLD = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);
        
        // Get user information from intent
        userId = getIntent().getIntExtra("USER_ID", -1);
        username = getIntent().getStringExtra("USERNAME");
        
        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);
        
        // Initialize UI elements
        recyclerViewInventory = findViewById(R.id.recyclerViewInventory);
        textEmptyState = findViewById(R.id.textEmptyState);
        fabAddItem = findViewById(R.id.fabAddItem);
        
        // Set up RecyclerView
        inventoryItems = new ArrayList<>();
        adapter = new InventoryAdapter(inventoryItems, this);
        recyclerViewInventory.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewInventory.setAdapter(adapter);
        
        // Load inventory items from database
        loadInventoryItems();
        
        // Set up FAB click listener for adding new items
        fabAddItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddItemDialog();
            }
        });
        
        // Navigate to SMS permission screen on first launch
        navigateToSmsPermissionIfNeeded();
    }

    /**
     * Loads all inventory items for the current user from the database
     */
    private void loadInventoryItems() {
        inventoryItems = databaseHelper.getInventoryItems(userId);
        adapter.updateItems(inventoryItems);
        
        // Show or hide empty state message
        if (inventoryItems.isEmpty()) {
            textEmptyState.setVisibility(View.VISIBLE);
            recyclerViewInventory.setVisibility(View.GONE);
        } else {
            textEmptyState.setVisibility(View.GONE);
            recyclerViewInventory.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Shows a dialog for adding a new inventory item
     */
    private void showAddItemDialog() {
        // Inflate the dialog layout
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_item, null);
        
        // Get references to dialog views
        EditText editItemName = dialogView.findViewById(R.id.editDialogItemName);
        EditText editItemQuantity = dialogView.findViewById(R.id.editDialogQuantity);
        
        // Create and show the dialog
        new AlertDialog.Builder(this)
                .setTitle("Add New Item")
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String itemName = editItemName.getText().toString().trim();
                    String quantityStr = editItemQuantity.getText().toString().trim();
                    
                    // Validate inputs
                    if (itemName.isEmpty()) {
                        Toast.makeText(this, "Item name is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    if (quantityStr.isEmpty()) {
                        Toast.makeText(this, "Quantity is required", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    
                    int quantity = Integer.parseInt(quantityStr);
                    
                    // Add item to database
                    long result = databaseHelper.addInventoryItem(itemName, quantity, userId);
                    
                    if (result != -1) {
                        Toast.makeText(this, "Item added successfully", Toast.LENGTH_SHORT).show();
                        loadInventoryItems(); // Reload the list
                    } else {
                        Toast.makeText(this, "Failed to add item", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Called when the increase quantity button is clicked for an item
     */
    @Override
    public void onIncreaseQuantity(DatabaseHelper.InventoryItem item) {
        int newQuantity = item.getQuantity() + 1;
        int rowsAffected = databaseHelper.updateInventoryQuantity(item.getId(), newQuantity);
        
        if (rowsAffected > 0) {
            loadInventoryItems(); // Reload to update the display
        } else {
            Toast.makeText(this, "Failed to update quantity", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Called when the decrease quantity button is clicked for an item
     */
    @Override
    public void onDecreaseQuantity(DatabaseHelper.InventoryItem item) {
        int newQuantity = item.getQuantity() - 1;
        
        // Prevent quantity from going below 0
        if (newQuantity < 0) {
            Toast.makeText(this, "Quantity cannot be negative", Toast.LENGTH_SHORT).show();
            return;
        }
        
        int rowsAffected = databaseHelper.updateInventoryQuantity(item.getId(), newQuantity);
        
        if (rowsAffected > 0) {
            loadInventoryItems(); // Reload to update the display
            
            // Check if item is now at low stock and show notification option
            if (newQuantity <= LOW_STOCK_THRESHOLD) {
                checkLowStockAlert(item.getName(), newQuantity);
            }
        } else {
            Toast.makeText(this, "Failed to update quantity", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Called when the delete button is clicked for an item
     */
    @Override
    public void onDeleteItem(DatabaseHelper.InventoryItem item) {
        // Show confirmation dialog before deleting
        new AlertDialog.Builder(this)
                .setTitle("Delete Item")
                .setMessage("Are you sure you want to delete " + item.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    int rowsDeleted = databaseHelper.deleteInventoryItem(item.getId());
                    
                    if (rowsDeleted > 0) {
                        Toast.makeText(this, "Item deleted", Toast.LENGTH_SHORT).show();
                        loadInventoryItems(); // Reload the list
                    } else {
                        Toast.makeText(this, "Failed to delete item", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    /**
     * Checks if a low stock alert should be shown for an item
     * @param itemName The name of the item
     * @param quantity The current quantity
     */
    private void checkLowStockAlert(String itemName, int quantity) {
        // If SMS permission is granted, send an SMS alert
        if (SmsHelper.hasSmsPermission(this)) {
            // For testing:
            String phoneNumber = "5555555555";
            
            boolean smsSent = SmsHelper.sendLowStockAlert(this, phoneNumber, itemName, quantity);
            
            if (smsSent) {
                Toast.makeText(this, 
                        "SMS alert sent: " + itemName + " is low in stock", 
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, 
                        itemName + " is low in stock (" + quantity + " remaining)", 
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            // No SMS permission, just show a toast
            Toast.makeText(this, 
                    itemName + " is low in stock (" + quantity + " remaining)", 
                    Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Navigates to SMS permission screen if this is the first time
     */
    private void navigateToSmsPermissionIfNeeded() {
        // Navigate to SMS permission activity
        Intent intent = new Intent(this, SmsPermissionActivity.class);
        intent.putExtra("USER_ID", userId);
        intent.putExtra("USERNAME", username);
        startActivity(intent);
    }
}
