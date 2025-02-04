package com.example.notes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

public class CreateUserProfile extends AppCompatActivity {


    EditText edtEmail, edtPassword, edtConfirmPassword;
    Button btnCreateAccount;
    ProgressBar progressBar;
    TextView txtLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_create_user_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtEmail = findViewById(R.id.email_edit_text);
        edtPassword = findViewById(R.id.password_edit_text);
        edtConfirmPassword = findViewById(R.id.confirm_password_edit_text);

        btnCreateAccount = findViewById(R.id.create_account_btn);
        progressBar = findViewById(R.id.progress_bar);

        txtLogin = findViewById(R.id.login_text_view_btn);

        btnCreateAccount.setOnClickListener(view -> createAccount());

        txtLogin.setOnClickListener(view -> startActivity(new Intent(CreateUserProfile.this, Login.class)));
    }

    private void createAccount() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        String confirmPassword = edtConfirmPassword.getText().toString().trim();

        boolean isValidate = validate(email, password, confirmPassword);

        if (isValidate) {
            // everything is validate
            createAccountWithFirebase(email, password);
        }


    }

    private void createAccountWithFirebase(String email, String password) {
        Utility.changeInProgress(progressBar, btnCreateAccount, true);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            Utility.changeInProgress(progressBar, btnCreateAccount, false);
            if (task.isSuccessful()) {
                Toast.makeText(this, "Successfully Account Created! Check email for verification link.", Toast.LENGTH_LONG).show();
                Objects.requireNonNull(firebaseAuth.getCurrentUser()).sendEmailVerification();
                firebaseAuth.signOut();
                Intent intent = new Intent(CreateUserProfile.this, Login.class);
                intent.putExtra("email", email);
                startActivity(intent);
                finish();
            } else {
                if (task.getException() instanceof FirebaseNetworkException) {
                    // No internet connection
                    Utility.showToast(CreateUserProfile.this, "No internet connection!");
                } else {
                    // Other authentication errors
                    Utility.showToast(CreateUserProfile.this, Objects.requireNonNull(task.getException()).getLocalizedMessage());
                }
            }
        });
    }

    private boolean validate(String email, String password, String confirmPassword) {
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (password.length() > 6) {
                if (password.equals(confirmPassword)) {
                    return true;
                } else {
                    edtConfirmPassword.setError("Password Should equal to Confirm Password!");
                    return false;
                }
            } else {
                edtPassword.setError("Password Should grater than 6 characters!");
                return false;
            }
        } else {
            edtEmail.setError("Email is invalid!");
            return false;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}