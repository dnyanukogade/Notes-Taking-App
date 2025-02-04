package com.example.notes;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;

public class Login extends AppCompatActivity {

    EditText edtEmail, edtPassword;
    Button btnLogin;
    ProgressBar progressBar;
    TextView txtCreateAccount, txtForgotPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        edtEmail = findViewById(R.id.emailLogin_edit_text);
        edtPassword = findViewById(R.id.passwordLogin_edit_text);
        btnLogin = findViewById(R.id.login_account_btn);
        progressBar = findViewById(R.id.progress_bar_login);
        txtCreateAccount = findViewById(R.id.createAccount_text_view_btn);
        txtForgotPassword = findViewById(R.id.txtForgotPassword);

        txtCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Login.this, CreateUserProfile.class));
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                login();
            }
        });

        txtForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, ForgotPassword.class);
                startActivity(intent);
            }
        });
    }


    private void login() {
        String email = edtEmail.getText().toString().trim();
        String password = edtPassword.getText().toString().trim();
        boolean isValidate = validate(email, password);
        if (isValidate) {
            loginWithFirebase(email, password);
        }
    }

    private void loginWithFirebase(String email, String password) {
        Utility.changeInProgress(progressBar, btnLogin, true);
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                Utility.changeInProgress(progressBar, btnLogin, false);
                if (task.isSuccessful()) {
                    if (firebaseAuth.getCurrentUser().isEmailVerified()) {
                        startActivity(new Intent(Login.this, Home.class));
                        finish();
                    }
                } else {
                    // Handle other cases like incorrect password or no internet connection
                    if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                        // User doesn't exist or incorrect email
                        Utility.showToast(Login.this, "Invalid email or password");
                    } else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                        // Invalid password
                        Utility.showToast(Login.this, "Incorrect password");
                        edtPassword.setError("Incorrect password");
                    } else if (task.getException() instanceof FirebaseNetworkException) {
                        // No internet connection
                        Utility.showToast(Login.this, "No internet connection");
                    } else {
                        // Other authentication errors
                        Utility.showToast(Login.this, task.getException().getLocalizedMessage());
                    }

                }
            }
        });
    }

    private boolean validate(String email, String password) {
        if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            if (password.length() > 6) {
                return true;
            } else {
                edtPassword.setError("Password Should grater than 6 characters!");
                return false;
            }
        } else {
            edtEmail.setError("Email is invalid!");
            return false;
        }
    }


}