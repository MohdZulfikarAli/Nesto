package com.devlacus.nestoapplication;

import android.content.Context;
import android.content.SharedPreferences;
import java.security.SecureRandom;
import java.util.Base64;

public class TokenManager {

    private static final String TOKEN_KEY = "token";
    private static final String TOKEN_EXPIRATION_KEY = "token_expiration";
    private static final long TOKEN_EXPIRATION_TIME_MS = 300000; // 5 min in milliseconds
    private SharedPreferences sharedPreferences;

    public TokenManager(Context context) {
        sharedPreferences = context.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
    }

    public String generateToken() {
        byte[] tokenBytes = new byte[32];
        new SecureRandom().nextBytes(tokenBytes);
        String token = android.util.Base64.encodeToString(tokenBytes, android.util.Base64.DEFAULT);


        // Calculate expiration time
        long currentTimeMillis = System.currentTimeMillis();
        long expirationTimeMillis = currentTimeMillis + TOKEN_EXPIRATION_TIME_MS;

        // Save token and expiration time to SharedPreferences
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(TOKEN_KEY, token);
        editor.putLong(TOKEN_KEY + "_expiration", expirationTimeMillis);
        editor.apply();

        return token;
    }

    public String getToken() {
        long expirationTimeMillis = sharedPreferences.getLong(TOKEN_KEY + "_expiration", 0);
        if (expirationTimeMillis != 0 && expirationTimeMillis > System.currentTimeMillis()) {
            // Token is not expired
            return sharedPreferences.getString(TOKEN_KEY, null);
        } else {
            // Token is expired or not found
            // Reset timer and return null
            resetTokenTimer(); // Reset timer
            return null;
        }
    }
    public void resetTokenTimer() {
        long currentTimeMillis = System.currentTimeMillis();
        long expirationTimeMillis = currentTimeMillis + TOKEN_EXPIRATION_TIME_MS;

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(TOKEN_EXPIRATION_KEY, expirationTimeMillis);
        editor.apply();
    }
}
