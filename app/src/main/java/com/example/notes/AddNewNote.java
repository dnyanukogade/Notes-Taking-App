package com.example.notes;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import java.util.ArrayList;
import java.util.Locale;

public class AddNewNote extends AppCompatActivity {


    EditText edtTitle, edtContent;
    Button btnAddNote, btnCancel;
    ProgressBar progressBar;
    TextView txtPageTitle;
    String title, content, docId;
    boolean isEditMode = false;

    ImageView img_btn_mic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_new_note);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        edtTitle = findViewById(R.id.edtTitle);
        edtContent = findViewById(R.id.edtContent);
        btnAddNote = findViewById(R.id.btnAddNote);
        btnCancel = findViewById(R.id.btnCancel);
        progressBar = findViewById(R.id.progress_bar_addNote);
        txtPageTitle = findViewById(R.id.page_title);
        img_btn_mic = findViewById(R.id.img_btn_mic);

        title = getIntent().getStringExtra("title");
        content = getIntent().getStringExtra("content");
        docId = getIntent().getStringExtra("docId");

        edtTitle.setText(title);
        edtContent.setText(content);

        if (docId != null && !docId.isEmpty()) {
            isEditMode = true;
        }

        if (isEditMode) {
            txtPageTitle.setText("Edit Your Note");
            btnAddNote.setText("Update Note");
        }


        btnAddNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean isConnected = Utility.isInternetConnected(AddNewNote.this);
                if (isConnected) {
                    addNote();
                } else {
                    Utility.showToast(AddNewNote.this, "No Internet Connection!");
                }

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        img_btn_mic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speechToText();
            }
        });

    }

    private void speechToText() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, 1);
        } else {
            Utility.showToast(AddNewNote.this, "Your Device Not Supported For Speech Input!");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> speechData = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                edtContent.setText(String.format("%s %s", edtContent.getText(), speechData.get(0)));
            }
        }
    }

    private void addNote() {

        String title = edtTitle.getText().toString().trim();
        String content = edtContent.getText().toString().trim();

        if (title.isEmpty()) {
            edtTitle.setError("Title Should Not Be Empty!");
            return;
        } else {
            Note note = new Note();
            note.setTitle(title);
            note.setContent(content);
            note.setTimestamp(Timestamp.now());


            saveNoteToFirebase(note);
        }

    }

    private void saveNoteToFirebase(Note note) {
        Utility.changeInProgress(progressBar, btnAddNote, true);
        DocumentReference documentReference;
        if (isEditMode) {
            // update the note
            documentReference = Utility.getCollectionReference().document(docId);
        } else {
            // create the note
            documentReference = Utility.getCollectionReference().document();
        }

        documentReference.set(note).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Utility.changeInProgress(progressBar, btnAddNote, false);
                if (task.isSuccessful()) {
                    if (isEditMode) {
                        Utility.showToast(AddNewNote.this, "Note Updated Successfully.");
                    }
                    finish();
                } else {
                    Utility.showToast(AddNewNote.this, "Something Went Wrong!");
                }
            }
        });
    }
}