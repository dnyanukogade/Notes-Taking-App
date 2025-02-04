package com.example.notes;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class NoteAdapter extends FirestoreRecyclerAdapter<Note, NoteAdapter.ViewHolder> {

    Context context;
    DocumentReference documentReference;
    Note deletedNote;

    public NoteAdapter(@NonNull FirestoreRecyclerOptions<Note> options, Context context) {
        super(options);
        this.context = context;
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder viewHolder, int position, @NonNull Note note) {
        viewHolder.txtNoteTitle.setText(note.getTitle());
        viewHolder.txtNoteContent.setText(note.getContent());
        viewHolder.txtNoteTimestamp.setText(Utility.timestampToString(note.getTimestamp(), "hh:mm a"));
        viewHolder.txtDate.setText(Utility.timestampToString(note.getTimestamp(), "MMM dd yyyy"));

        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AddNewNote.class);
                intent.putExtra("title", note.getTitle());
                intent.putExtra("content", note.getContent());
                String docId = getSnapshots().getSnapshot(position).getId();
                intent.putExtra("docId", docId);
                context.startActivity(intent);
            }
        });
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.single_note_layout, parent, false));
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtNoteTitle, txtNoteContent, txtNoteTimestamp, txtDate;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);


            txtNoteTitle = itemView.findViewById(R.id.txtTitle);
            txtNoteContent = itemView.findViewById(R.id.txtNoteContent);
            txtNoteTimestamp = itemView.findViewById(R.id.txtNoteTimestamp);
            txtDate = itemView.findViewById(R.id.txtDate);


        }
    }


    public void deleteNoteFromFirebase(RecyclerView.ViewHolder viewHolder, RecyclerView recyclerView) {
        String docId = getSnapshots().getSnapshot(viewHolder.getAdapterPosition()).getId();
        deletedNote = getSnapshots().getSnapshot(viewHolder.getAdapterPosition()).toObject(Note.class);
        documentReference = Utility.getCollectionReference().document(docId);
        documentReference.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    showSnackbar(recyclerView);
                } else {
                    Utility.showToast(context, task.getException().getLocalizedMessage());
                }
            }
        });
    }


    private void showSnackbar(RecyclerView recyclerView) {
        Snackbar.make(recyclerView, "Deleted Note : " + deletedNote.getTitle(), Snackbar.LENGTH_SHORT)
                .setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        documentReference.set(deletedNote).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Utility.showToast(context, "Undo Done.");
                                } else {
                                    Utility.showToast(context, task.getException().getLocalizedMessage());
                                }
                            }
                        });
                    }
                }).show();

    }
}
