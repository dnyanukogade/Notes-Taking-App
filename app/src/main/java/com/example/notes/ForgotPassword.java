package com.example.notes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;

import io.reactivex.rxjava3.annotations.NonNull;

public class ForgotPassword extends AppCompatActivity {

    EditText edtEmailForgotPassword;
    MaterialButton btnResetPassword, btnBack;
    ProgressBar forgotPasswordProgressBar;
    String email;

    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_forgot_password);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        edtEmailForgotPassword = findViewById(R.id.edtEmailForgotPassword);
        btnResetPassword = findViewById(R.id.btnReset);
        btnBack = findViewById(R.id.btnBack);
        forgotPasswordProgressBar = findViewById(R.id.forgetPasswordProgressbar);


        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
                finish();
            }
        });

        btnResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = edtEmailForgotPassword.getText().toString().trim();
                boolean isValidate = validate(email);
                if (isValidate) {
                    resetPassword();
                }
            }
        });

    }

    private void resetPassword() {
        forgotPasswordProgressBar.setVisibility(View.VISIBLE);
        btnResetPassword.setVisibility(View.INVISIBLE);

        mAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        forgotPasswordProgressBar.setVisibility(View.INVISIBLE);
                        btnResetPassword.setVisibility(View.VISIBLE);
                        Utility.showToast(ForgotPassword.this, "Reset Password link has been sent to your registered Email");
                        Intent intent = new Intent(ForgotPassword.this, Login.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Utility.showToast(ForgotPassword.this, "Error :- " + e.getMessage());
                        forgotPasswordProgressBar.setVisibility(View.INVISIBLE);
                        btnResetPassword.setVisibility(View.VISIBLE);
                    }
                });
    }


    private boolean validate(String email) {
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            return true;
        } else {
            edtEmailForgotPassword.setError("Email is invalid!");
            return false;
        }
    }
}