package team.corpore.in.lab1notes.adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import team.corpore.in.lab1notes.R;
import team.corpore.in.lab1notes.activities.MainActivity;
import team.corpore.in.lab1notes.data.Note;
import team.corpore.in.lab1notes.db.DBHelper;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.VH> {

    public interface LongClickByItemListener {
        void onLongClick(View itemView, int position);
    }

    private List<Note> notes;
    private List<Note> filteredNotes;
    private LongClickByItemListener longClickByItemListener;
    private String searchQuery = "";
    private ArrayList<Note.ClassImportance> filterClasses = new ArrayList<>();
    private DBHelper dbHelper;

    public NotesAdapter(List<Note> notes, LongClickByItemListener longClickByItemListener, Context context) {
        this.notes = notes;
        this.filteredNotes = new ArrayList<>(notes);
        this.longClickByItemListener = longClickByItemListener;
        dbHelper = new DBHelper(context);
    }

    public Note getItemByPosition(int position) {
        return filteredNotes.get(position);
    }

    public void removeNote(int position) {
        Note deleteNote = filteredNotes.remove(position);
        notes.remove(deleteNote);
        notifyItemRemoved(position);

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("notes", "id=" + deleteNote.getId(), null);
    }

    public void addNote(Note note) {
        notes.add(note);

        if (checkNoteByFilter(note)) {
            filteredNotes.add(note);
            int position = filteredNotes.size() - 1;
            notifyItemInserted(position);
        }

        ContentValues cv = new ContentValues();
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        cv.put("id", note.getId());
        cv.put("title", note.getTitle());
        cv.put("text", note.getText());
        cv.put("time", note.getTime());
        cv.put("importance", note.getClassImportance().ordinal());
        cv.put("icon", note.getIcon());

        db.insert("notes", null, cv);
    }

    public void changeNote(Note note, int position) {
        notes.set(position, note);
        int filteredPosition = getPositionNoteByIdInList(note, filteredNotes);
        filteredNotes.set(filteredPosition, note);
        notifyItemChanged(filteredPosition);

        ContentValues cv = new ContentValues();
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        cv.put("id", note.getId());
        cv.put("title", note.getTitle());
        cv.put("text", note.getText());
        cv.put("time", note.getTime());
        cv.put("importance", note.getClassImportance().ordinal());
        cv.put("icon", note.getIcon());

        db.update("notes", cv, "id="+note.getId(), null);
    }

    public void search(String searchQuery) {
        this.searchQuery = searchQuery.toLowerCase(Locale.getDefault());
        filterNotes();
    }

    public void filter(Note.ClassImportance filterClass, boolean remove) {
        if (remove) {
            this.filterClasses.remove(filterClass);
        }
        else {
            this.filterClasses.add(filterClass);
        }
        filterNotes();
    }

    private void filterNotes() {
        filteredNotes.clear();
        for (Note note : notes) {
            if (checkNoteByFilter(note)) {
                filteredNotes.add(note);
            }
        }
        notifyDataSetChanged();
    }

    private boolean checkNoteByFilter(Note note) {
        boolean filterByClass = filterClassContainsInList(note.getClassImportance(), filterClasses);
        if (note.getText() == null) {
            return note.getTitle().toLowerCase().contains(searchQuery) && filterByClass;
        }
        return (note.getText().toLowerCase().contains(searchQuery) || note.getTitle().toLowerCase().contains(searchQuery)) && filterByClass;
    }

    private boolean filterClassContainsInList(Note.ClassImportance classImportance, List<Note.ClassImportance> list) {
        if (list.isEmpty()) {
            return true;
        }
        for (Note.ClassImportance item : list) {
            if (item == classImportance) {
                return true;
            }
        }
        return false;
    }

    private int getPositionNoteByIdInList(Note note, List<Note> notes) {
        for (int i = 0; i < notes.size(); ++i) {
            if (note.getId() == notes.get(i).getId()) {
                return i;
            }
        }
        return -1;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_note, parent, false);
        return new VH(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        Note note = filteredNotes.get(position);
        holder.bindData(note);
    }

    @Override
    public int getItemCount() {
        return filteredNotes.size();
    }

    class VH extends RecyclerView.ViewHolder {

        private ImageView iconIV;
        private ImageView classImportanceIV;
        private TextView timeTV;
        private TextView titleTV;
        private TextView textTV;

        VH(View itemView) {
            super(itemView);

            iconIV = itemView.findViewById(R.id.icon_iv);
            classImportanceIV = itemView.findViewById(R.id.class_importance_iv);
            timeTV = itemView.findViewById(R.id.time_tv);
            titleTV = itemView.findViewById(R.id.title_tv);
            textTV = itemView.findViewById(R.id.text_tv);

            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    longClickByItemListener.onLongClick(v, getAdapterPosition());
                    return false;
                }
            });
        }

        void bindData (Note note) {
            if (note.getIcon() == null) {
                Picasso.get()
                        .load(R.color.full_opacity_color)
                        .centerCrop()
                        .fit()
                        .into(iconIV);
            }
            else {
                Picasso.get()
                        .load(Uri.parse(note.getIcon()))
                        .centerCrop()
                        .fit()
                        .into(iconIV);
            }
            titleTV.setText(note.getTitle());
            if (note.getText() == null) {
                textTV.setText("");
            }
            else {
                textTV.setText(note.getText());
            }
            timeTV.setText(note.getTime());
            switch (note.getClassImportance()) {
                case FirstClass:
                    classImportanceIV.setBackground(classImportanceIV.getContext().getDrawable(R.drawable.first_class_24dp));
                    break;
                case SecondClass:
                    classImportanceIV.setBackground(classImportanceIV.getContext().getDrawable(R.drawable.second_class_24dp));
                    break;
                case ThirdClass:
                    classImportanceIV.setBackground(classImportanceIV.getContext().getDrawable(R.drawable.third_class_24dp));
                    break;
            }
        }
    }
}