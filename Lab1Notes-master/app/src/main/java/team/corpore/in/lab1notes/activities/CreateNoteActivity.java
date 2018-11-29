package team.corpore.in.lab1notes.activities;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import team.corpore.in.lab1notes.Constants;
import team.corpore.in.lab1notes.R;
import team.corpore.in.lab1notes.data.Note;

public class CreateNoteActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private Note note;

    private ImageView iconIV;
    private TextInputLayout title_til;
    private TextInputEditText title_tiet;
    private TextInputEditText text_tiet;
    private AppCompatRadioButton firstClassRB;
    private AppCompatRadioButton secondClassRB;
    private AppCompatRadioButton thirdClassRB;

    public static void openResultCreateNoteActivity(Activity activity, Note note) {
        Intent intent = new Intent(activity, CreateNoteActivity.class);
        intent.putExtra(Constants.KEY_NOTE, note);
        activity.startActivityForResult(intent, Constants.CODE_EDIT_NOTE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_note);

        toolbar = findViewById(R.id.toolbar);
        iconIV = findViewById(R.id.icon_iv);
        title_til = findViewById(R.id.title_til);
        title_tiet = findViewById(R.id.title_tiet);
        text_tiet = findViewById(R.id.text_tiet);
        firstClassRB = findViewById(R.id.first_class_rb);
        secondClassRB = findViewById(R.id.second_class_rb);
        thirdClassRB = findViewById(R.id.third_class_rb);

        setupToolbar();
        getData(savedInstanceState);
        setupUI();
        initTitleAndText();
        initClassRadioButtons();
        setupIconIV();
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void setupIconIV() {
        iconIV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String selector = "image/*";

                Intent getIntent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                getIntent.addCategory(Intent.CATEGORY_OPENABLE);
                getIntent.setType(selector);

                startActivityForResult(getIntent, Constants.CODE_PICK_IMAGE);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode) {
            case Constants.CODE_PICK_IMAGE:
                if (resultCode == RESULT_OK && data != null) {
                    Uri selectedImage = data.getData();
                    setUriSelectedImage(selectedImage);
                }
                break;
        }
    }

    private void setUriSelectedImage(Uri uri) {
        if (uri != null) {
            note.setIcon(uri.toString());
            Picasso.get()
                    .load(uri)
                    .fit()
                    .centerCrop()
                    .into(iconIV);
        }
    }

    private void getData(Bundle savedInstance) {
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            note = (Note) extras.getSerializable(Constants.KEY_NOTE);
            if (note == null) {
                Toast.makeText(CreateNoteActivity.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                finish();
            }
        }
        if (savedInstance != null) {
            note = (Note) savedInstance.getSerializable(Constants.KEY_NOTE);
        }
    }

    private void setupUI() {
        if (note.getIcon() != null) {
            Picasso.get()
                    .load(Uri.parse(note.getIcon()))
                    .fit()
                    .centerCrop()
                    .into(iconIV);
        }
        if (note.getTitle() != null) {
            title_tiet.setText(note.getTitle());
            title_tiet.setSelection(note.getTitle().length());
        }
        if (note.getText() != null) {
            text_tiet.setText(note.getText());
            text_tiet.setSelection(note.getText().length());
        }
        switch (note.getClassImportance()) {
            case FirstClass:
                firstClassRB.setChecked(true);
                break;
            case SecondClass:
                secondClassRB.setChecked(true);
                break;
            case ThirdClass:
                thirdClassRB.setChecked(true);
                break;
        }
    }

    private void initTitleAndText() {
        title_tiet.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                note.setTitle(s.toString());
                title_til.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        text_tiet.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                note.setText(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void initClassRadioButtons() {
        firstClassRB.setOnCheckedChangeListener(new SelectClassImportance());
        secondClassRB.setOnCheckedChangeListener(new SelectClassImportance());
        thirdClassRB.setOnCheckedChangeListener(new SelectClassImportance());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_note, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_send:
                if (note.getTitle() == null || (note.getTitle() != null && note.getTitle().isEmpty())) {
                    title_til.setError(getString(R.string.error_empty_filed));
                    return true;
                }
                Intent intent = new Intent();
                intent.putExtra(Constants.KEY_NOTE, note);
                setResult(RESULT_OK, intent);
                finish();
                break;
        }
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putSerializable(Constants.KEY_NOTE, note);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    class SelectClassImportance implements CompoundButton.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (isChecked) {
                switch (buttonView.getId()) {
                    case R.id.first_class_rb:
                        note.setClassImportance(Note.ClassImportance.FirstClass);
                        break;
                    case R.id.second_class_rb:
                        note.setClassImportance(Note.ClassImportance.SecondClass);
                        break;
                    case R.id.third_class_rb:
                        note.setClassImportance(Note.ClassImportance.ThirdClass);
                        break;
                }
            }
        }
    }
}
