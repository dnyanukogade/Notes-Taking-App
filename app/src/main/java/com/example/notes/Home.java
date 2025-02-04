package com.example.notes;

import android.content.Intent;
import android.graphics.Canvas;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.Query;

import java.util.Locale;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;

public class Home extends AppCompatActivity {

    FloatingActionButton btnAddNewNote;
    Toolbar toolbar;
    RecyclerView recyclerView;
    NoteAdapter noteAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimaryDark, typedValue, true);
        int statusBarColor = typedValue.data;
        Window window = getWindow();
        window.setStatusBarColor(statusBarColor);


        toolbar = findViewById(R.id.home_toolbar);
        setSupportActionBar(toolbar);

        toolbar.setTitle("Notes Pro");
        toolbar.setOverflowIcon(getDrawable(R.drawable.baseline_logout_24));

        recyclerView = findViewById(R.id.recycleView);

        TypedValue value = new TypedValue();
        getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, value, true);
        int primaryColor = value.data;
        toolbar.setTitleTextColor(primaryColor);
        btnAddNewNote = findViewById(R.id.btnAddNewNote);
        btnAddNewNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Home.this, AddNewNote.class));
            }
        });

        setRecyclerView();

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logout, menu);
        MenuItem menuItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) menuItem.getActionView();
        assert searchView != null;
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                getOrderedByTitleQuery(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                getOrderedByTitleQuery(query);
                return false;
            }
        });
        return true;

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.action_logout) {
            boolean isConnected = Utility.isInternetConnected(Home.this);
            if (isConnected) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(Home.this, Login.class));
                finish();
            } else {
                Utility.showToast(Home.this, "No Internet Connection!");
            }

        }
        return super.onOptionsItemSelected(item);
    }

    private void setRecyclerView() {
        Query query = Utility.getCollectionReference().orderBy("timestamp", Query.Direction.DESCENDING);
        FirestoreRecyclerOptions<Note> options = new FirestoreRecyclerOptions.Builder<Note>().setQuery(query, Note.class)
                .build();
        noteAdapter = new NoteAdapter(options, this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(noteAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();
        noteAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        noteAdapter.stopListening();
    }

    @Override
    protected void onResume() {
        super.onResume();
        noteAdapter.notifyDataSetChanged();
    }


    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            if (direction == ItemTouchHelper.LEFT) {
                noteAdapter.deleteNoteFromFirebase(viewHolder, recyclerView);
            }
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(ContextCompat.getColor(Home.this, R.color.md_theme_dark_errorContainer))
                    .addSwipeLeftActionIcon(R.drawable.baseline_delete_24)
                    .create()
                    .decorate();
        }
    };

    public void getOrderedByTitleQuery(String queryText) {

        // Get a reference to your collection
        CollectionReference collectionRef = Utility.getCollectionReference();

        // Build the query with efficient case-insensitive ordering
        Query query;
        if (queryText.isEmpty()) {
            query = collectionRef.orderBy("timestamp", Query.Direction.DESCENDING);  // Order by title (ascending) if empty search term
        } else {

            String searchTermEnd = queryText + "\uf8ff";
            query = collectionRef.whereGreaterThanOrEqualTo("title", queryText)
                    .whereLessThanOrEqualTo("title", searchTermEnd)
                    .orderBy("timestamp", Query.Direction.DESCENDING);  // Order by timestamp (ascending) after search
        }

        // Set up FirestoreRecyclerOptions and adapter
        FirestoreRecyclerOptions<Note> options = new FirestoreRecyclerOptions.Builder<Note>()
                .setQuery(query, Note.class)
                .build();
        noteAdapter = new NoteAdapter(options, this); // Pass context to adapter constructor
        noteAdapter.startListening();


//        // Set the layout manager and adapter for the RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(noteAdapter);


    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}