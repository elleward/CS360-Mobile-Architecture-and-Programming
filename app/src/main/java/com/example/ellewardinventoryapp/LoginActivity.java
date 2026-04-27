package com.example.ellewardinventoryapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * LoginActivity handles user authentication and account creation.
 * Users can log in with existing credentials or create a new account.
 * On successful login, the user is navigated to the inventory screen.
 */
public class LoginActivity extends AppCompatActivity {
    
    // UI elements
    private EditText editUsername;
    private EditText editPassword;
    private TextView textLoginError;
    private Button buttonLogin;
    private Button buttonCreateAccount;
    
    // Database helper
    private DatabaseHelper databaseHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);
        
        // Initialize UI elements
        editUsername = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);
        textLoginError = findViewById(R.id.textLoginError);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonCreateAccount = findViewById(R.id.buttonCreateAccount);
        
        // Set up login button click listener
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });
        
        // Set up create account button click listener
        buttonCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleCreateAccount();
            }
        });
    }
    
    /**
     * Handles the login process when the login button is clicked.
     * Validates input, checks credentials against the database,
     * and navigates to the inventory screen on success.
     */
    private void handleLogin() {
        // Get input values
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        
        // Clear any previous error messages
        textLoginError.setText("");
        
        // Validate inputs
        if (username.isEmpty()) {
            textLoginError.setText("Username is required");
            return;
        }
        
        if (password.isEmpty()) {
            textLoginError.setText("Password is required");
            return;
        }
        
        // Check credentials against database
        if (databaseHelper.checkUser(username, password)) {
            // Login successful, get user ID and navigate to inventory
            int userId = databaseHelper.getUserId(username);
            navigateToInventory(userId, username);
        } else {
            // Login failed
            textLoginError.setText("Invalid username or password");
        }
    }
    
    /**
     * Handles account creation when the create account button is clicked.
     * Validates input, checks if username already exists,
     * creates the account, and navigates to the inventory screen on success.
     */
    private void handleCreateAccount() {
        // Get input values
        String username = editUsername.getText().toString().trim();
        String password = editPassword.getText().toString().trim();
        
        // Clear any previous error messages
        textLoginError.setText("");
        
        // Validate inputs
        if (username.isEmpty()) {
            textLoginError.setText("Username is required");
            return;
        }
        
        if (password.isEmpty()) {
            textLoginError.setText("Password is required");
            return;
        }
        
        // Check password length for basic security
        if (password.length() < 4) {
            textLoginError.setText("Password must be at least 4 characters");
            return;
        }
        
        // Check if username already exists
        if (databaseHelper.usernameExists(username)) {
            textLoginError.setText("Username already exists");
            return;
        }
        
        // Create the new account
        long result = databaseHelper.addUser(username, password);
        
        if (result != -1) {
            // Account created successfully, get user ID and navigate to inventory
            int userId = databaseHelper.getUserId(username);
            navigateToInventory(userId, username);
        } else {
            // Account creation failed
            textLoginError.setText("Account creation failed. Please try again.");
        }
    }
    
    /**
     * Navigates to the InventoryActivity and passes the user ID and username.
     * @param userId The ID of the logged-in user
     * @param username The username of the logged-in user
     */
    private void navigateToInventory(int userId, String username) {
        Intent intent = new Intent(LoginActivity.this, InventoryActivity.class);
        intent.putExtra("USER_ID", userId);
        intent.putExtra("USERNAME", username);
        startActivity(intent);
        finish(); // Close login activity so user can't go back without logging out
    }
}
