package team.corpore.in.lab1notes.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

import team.corpore.in.lab1notes.Constants;
import team.corpore.in.lab1notes.R;
import team.corpore.in.lab1notes.adapters.NotesAdapter;
import team.corpore.in.lab1notes.data.Note;
import team.corpore.in.lab1notes.db.DBHelper;

public class MainActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView notesRV;
    private SearchView searchView;
    private NotesAdapter notesAdapter;
    private ArrayList<Note> notes = new ArrayList<>();
    private boolean firstClassIsChecked, secondClassIsChecked, thirdClassIsChecked;
    private String searchQuery;
    private DBHelper dbHelper;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        notesRV = findViewById(R.id.notes_rv);
        dbHelper = new DBHelper(this);

        if (savedInstanceState != null) {
            notes = (ArrayList<Note>) savedInstanceState.getSerializable(Constants.KEY_NOTES);
        }
        else{
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            Cursor c = db.query("notes", null, null, null, null, null, null);

            if (c.moveToFirst()) {
                int idColIndex = c.getColumnIndex("id");
                int titleColIndex = c.getColumnIndex("title");
                int textColIndex = c.getColumnIndex("text");
                int timeColIndex = c.getColumnIndex("time");
                int importanceColIndex = c.getColumnIndex("importance");
                int iconColIndex = c.getColumnIndex("icon");

                do {
                    Note note = new Note(
                            c.getInt(idColIndex),
                            c.getString(titleColIndex),
                            c.getString(textColIndex),
                            c.getString(timeColIndex),
                            Note.ClassImportance.values()[c.getInt(importanceColIndex)],
                            c.getString(iconColIndex));
                    notes.add(note);
                } while (c.moveToNext());
            }

            c.close();
            dbHelper.close();
        }

        setupToolbar();
        initNotesRecyclerView();

        if (savedInstanceState != null) {
            firstClassIsChecked = savedInstanceState.getBoolean(Constants.FIRST_CLASS_IS_CHECKED);
            secondClassIsChecked = savedInstanceState.getBoolean(Constants.SECOND_CLASS_IS_CHECKED);
            thirdClassIsChecked = savedInstanceState.getBoolean(Constants.THIRD_CLASS_IS_CHECKED);
            searchQuery = savedInstanceState.getString(Constants.SEARCH_QUERY);
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
    }

    private void initNotesRecyclerView() {
        notesAdapter = new NotesAdapter(notes, new NotesAdapter.LongClickByItemListener() {
            @Override
            public void onLongClick(View itemView, int position) {
                itemView.setOnCreateContextMenuListener(new ContextMenuRecyclerView(position));
            }
        }, this);
        notesRV.setAdapter(notesAdapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_action_notes, menu);

        final MenuItem myActionMenuItem = menu.findItem( R.id.action_search);
        searchView = (SearchView) myActionMenuItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (TextUtils.isEmpty(newText)) {
                    notesAdapter.search("");
                } else {
                    notesAdapter.search(newText);
                }
                return true;
            }
        });

        MenuItem firstClassItem = menu.findItem(R.id.action_filter_first_class);
        firstClassItem.setChecked(firstClassIsChecked);
        MenuItem secondClassItem = menu.findItem(R.id.action_filter_second_class);
        secondClassItem.setChecked(secondClassIsChecked);
        MenuItem thirdClassItem = menu.findItem(R.id.action_filter_third_class);
        thirdClassItem.setChecked(thirdClassIsChecked);

        notesAdapter.filter(Note.ClassImportance.FirstClass, !firstClassIsChecked);
        notesAdapter.filter(Note.ClassImportance.SecondClass, !secondClassIsChecked);
        notesAdapter.filter(Note.ClassImportance.ThirdClass, !thirdClassIsChecked);

        if (searchQuery != null && !searchQuery.isEmpty()){
            myActionMenuItem.expandActionView();
            searchView.setQuery(searchQuery, true);
            searchView.clearFocus();

            notesAdapter.search(searchQuery);
        }

        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                int id = 0;
                if (notes.size() != 0) {
                    id = notes.get(notes.size() - 1).getId() + 1;
                }
                Note note = new Note(id);
                CreateNoteActivity.openResultCreateNoteActivity(MainActivity.this, note);
                break;
            case R.id.action_filter:

                break;
            case R.id.action_filter_first_class:
                notesAdapter.filter(Note.ClassImportance.FirstClass, item.isChecked());
                firstClassIsChecked = !item.isChecked();
                break;
            case R.id.action_filter_second_class:
                notesAdapter.filter(Note.ClassImportance.SecondClass, item.isChecked());
                secondClassIsChecked = !item.isChecked();
                break;
            case R.id.action_filter_third_class:
                notesAdapter.filter(Note.ClassImportance.ThirdClass, item.isChecked());
                thirdClassIsChecked = !item.isChecked();
                break;
        }

        switch (item.getItemId()) {
            case R.id.action_filter_first_class:
                item.setChecked(!item.isChecked());
                break;
            case R.id.action_filter_second_class:
                item.setChecked(!item.isChecked());
                break;
            case R.id.action_filter_third_class:
                item.setChecked(!item.isChecked());
                break;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Constants.CODE_EDIT_NOTE:
                if (resultCode == RESULT_OK && data != null) {
                    Note note = (Note) data.getSerializableExtra(Constants.KEY_NOTE);
                    int index = -1;
                    for (int i = 0; i < notes.size(); ++i) {
                        if (notes.get(i).getId() == note.getId()) {
                            index = i;
                            break;
                        }
                    }
                    if (index == -1) {
                        notesAdapter.addNote(note);
                    }
                    else {
                        notesAdapter.changeNote(note, index);
                    }
                }
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(Constants.KEY_NOTES, notes);
        outState.putBoolean(Constants.FIRST_CLASS_IS_CHECKED, firstClassIsChecked);
        outState.putBoolean(Constants.SECOND_CLASS_IS_CHECKED, secondClassIsChecked);
        outState.putBoolean(Constants.THIRD_CLASS_IS_CHECKED, thirdClassIsChecked);

        searchQuery = searchView.getQuery().toString();
        outState.putString(Constants.SEARCH_QUERY, searchQuery);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    public class ContextMenuRecyclerView implements View.OnCreateContextMenuListener, MenuItem.OnMenuItemClickListener {
        private int position;

        ContextMenuRecyclerView(int position) {
            this.position = position;
        }

        @Override
        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
            MenuInflater menuInflater = MainActivity.this.getMenuInflater();
            menuInflater.inflate(R.menu.menu_edit_note, menu);
            for (int index = 0; index < menu.size(); ++index) {
                menu.getItem(index).setOnMenuItemClickListener(this);
            }
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            switch (item.getItemId()) {
                case R.id.action_edit:
                    CreateNoteActivity.openResultCreateNoteActivity(MainActivity.this, notesAdapter.getItemByPosition(position));
                    break;
                case R.id.action_remove:
                    notesAdapter.removeNote(position);
                    break;
            }
            return false;
        }
    }
}
