package com.example.notes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;

public class VerifyEmailLink extends AppCompatActivity {

    TextView txtEmailFromUser;
    MaterialButton btnOpenEmail, btnRecheckEmail;
    private static final int REQUEST_CODE_OPEN_GMAIL = 1001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_verify_email_link);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        txtEmailFromUser = findViewById(R.id.txtEmailAddress);
        btnOpenEmail = findViewById(R.id.btnOpenEmail);
        btnRecheckEmail = findViewById(R.id.btnRecheckEmail);

        String getEmail = getIntent().getStringExtra("email");

        txtEmailFromUser.setText(getEmail);

        btnOpenEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
             openGmailApp();
            }
        });

        btnRecheckEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(VerifyEmailLink.this,CreateUserProfile.class));
                finish();
            }
        });
    }


    public void openGmailApp() {
        // Launch Gmail with package name
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.setPackage("com.google.android.gm");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivityForResult(intent, REQUEST_CODE_OPEN_GMAIL);
    }

    private void changeActivity() {
        // Perform the action to change the activity here
        // For example:
        Intent intent = new Intent(VerifyEmailLink.this, Login.class);
        startActivity(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_OPEN_GMAIL) {
            // Check if the result is OK and perform your desired action
            if (resultCode == RESULT_OK) {
                // Gmail was opened successfully, perform your action here
                // For example, you can change the activity
                changeActivity();
            }
        }
    }
}