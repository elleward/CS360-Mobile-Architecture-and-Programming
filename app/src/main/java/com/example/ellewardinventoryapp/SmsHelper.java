package com.example.ellewardinventoryapp;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.telephony.SmsManager;
import androidx.core.content.ContextCompat;

/**
 * SmsHelper provides methods for sending SMS notifications.
 * This class checks for SMS permission before attempting to send messages
 * and handles low stock alerts for inventory items.
 */
public class SmsHelper {

    /**
     * Sends a low stock alert via SMS if permission is granted.
     * @param context The application context
     * @param phoneNumber The phone number to send the alert to
     * @param itemName The name of the item that is low in stock
     * @param quantity The current quantity of the item
     * @return true if SMS was sent successfully, false otherwise
     */
    public static boolean sendLowStockAlert(Context context, String phoneNumber, 
                                           String itemName, int quantity) {
        // Check if SMS permission is granted
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, cannot send SMS
            return false;
        }
        
        try {
            // Create the SMS message
            String message = "TagTalk: " + itemName + " is running low (" 
                           + quantity + " remaining). Time to reorder.";
            
            // Get SmsManager and send the message
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            
            return true;
        } catch (Exception e) {
            // SMS send failed
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Checks if the app has permission to send SMS.
     * @param context The application context
     * @return true if permission is granted, false otherwise
     */
    public static boolean hasSmsPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Creates a low stock alert message without sending it.
     * Useful for previewing or logging alert messages.
     * @param itemName The name of the item
     * @param quantity The current quantity
     * @return The formatted alert message
     */
    public static String createLowStockMessage(String itemName, int quantity) {
        return "TagTalk: " + itemName + " is running low (" 
               + quantity + " remaining). Time to reorder.";
    }
}
