package com.example.ellewardinventoryapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * InventoryAdapter manages the display of inventory items in a RecyclerView.
 * Each item shows the name, quantity, and buttons for increasing, decreasing,
 * and deleting the item. Low stock items are visually highlighted.
 */
public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder> {

    // List of inventory items to display
    private List<DatabaseHelper.InventoryItem> items;
    
    // Interface for handling item actions
    private OnItemActionListener listener;
    
    // Low stock threshold for visual indicator
    private static final int LOW_STOCK_THRESHOLD = 5;

    /**
     * Interface for handling user actions on inventory items
     */
    public interface OnItemActionListener {
        void onIncreaseQuantity(DatabaseHelper.InventoryItem item);
        void onDecreaseQuantity(DatabaseHelper.InventoryItem item);
        void onDeleteItem(DatabaseHelper.InventoryItem item);
    }

    /**
     * Constructor for InventoryAdapter
     * @param items List of inventory items to display
     * @param listener Listener for item action events
     */
    public InventoryAdapter(List<DatabaseHelper.InventoryItem> items, OnItemActionListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @NonNull
    @Override
    public InventoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory, parent, false);
        return new InventoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InventoryViewHolder holder, int position) {
        DatabaseHelper.InventoryItem item = items.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Updates the list of items and refreshes the display
     * @param newItems The new list of inventory items
     */
    public void updateItems(List<DatabaseHelper.InventoryItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    /**
     * ViewHolder class that holds references to the views for each inventory item
     */
    class InventoryViewHolder extends RecyclerView.ViewHolder {
        
        private TextView textItemName;
        private TextView textQuantity;
        private ImageButton buttonIncrease;
        private ImageButton buttonDecrease;
        private ImageButton buttonDeleteItem;
        private View viewLowStockIndicator;

        /**
         * Constructor for InventoryViewHolder
         * @param itemView The view for a single inventory item
         */
        public InventoryViewHolder(@NonNull View itemView) {
            super(itemView);
            
            // Initialize view references
            textItemName = itemView.findViewById(R.id.textItemName);
            textQuantity = itemView.findViewById(R.id.textQuantity);
            buttonIncrease = itemView.findViewById(R.id.buttonIncrease);
            buttonDecrease = itemView.findViewById(R.id.buttonDecrease);
            buttonDeleteItem = itemView.findViewById(R.id.buttonDeleteItem);
            viewLowStockIndicator = itemView.findViewById(R.id.viewLowStockIndicator);
        }

        /**
         * Binds data from an InventoryItem to the views
         * @param item The inventory item to display
         */
        public void bind(DatabaseHelper.InventoryItem item) {
            // Set item name and quantity
            textItemName.setText(item.getName());
            textQuantity.setText(String.valueOf(item.getQuantity()));
            
            // Show low stock indicator if quantity is below threshold
            if (item.getQuantity() <= LOW_STOCK_THRESHOLD) {
                viewLowStockIndicator.setVisibility(View.VISIBLE);
            } else {
                viewLowStockIndicator.setVisibility(View.GONE);
            }
            
            // Set up increase quantity button
            buttonIncrease.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onIncreaseQuantity(item);
                    }
                }
            });
            
            // Set up decrease quantity button
            buttonDecrease.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onDecreaseQuantity(item);
                    }
                }
            });
            
            // Set up delete button
            buttonDeleteItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        listener.onDeleteItem(item);
                    }
                }
            });
        }
    }
}
