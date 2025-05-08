package com.example.listrandom;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.transition.TransitionManager;
import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {
    private ImageButton add_btn;
    private ImageButton delete_btn;
    private ImageButton edit_btn;
    private ImageButton random_btn;
    private EditText itemEdt;
    private ArrayList<Item> lngList;
    private ArrayList<Item> ogList = new ArrayList<>();
    private ListView listvew;
    private int itemIndex = -1;
    private SlotMachine slotMachine;
    private Button spin_btn;


    private boolean isRand = false;
    private final String PREFS_NAME = "ListPrefs";
    private final String LIST_KEY = "listData";
    private final String RANDOMIZED_KEY = "isRand";
    private final String OG_LIST_KEY = "ogListData";
    ItemAdapter adapter;

    private final String DELIMITER=";;";
    private final String ITEM_SEPERATOR = "::";


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // EdgeToEdge.enable(this); // Uncomment if you use EdgeToEdge mode.
        setContentView(R.layout.activity_main);

        // Initialize views
        listvew = findViewById(R.id.listvew);
        add_btn = findViewById(R.id.btn_add);
        delete_btn = findViewById(R.id.btn_delete);
        edit_btn = findViewById(R.id.btn_edit);
        random_btn = findViewById(R.id.btn_randomize);
        itemEdt = findViewById(R.id.idEdtItemName);
        spin_btn = findViewById(R.id.btn_spin);

        // Setup insets if needed (this should not conflict with adjustResize if well tweaked)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize the list and adapter
        lngList = new ArrayList<>();
        adapter = new ItemAdapter(this, lngList);
        listvew.setAdapter(adapter);

        loadList();

        // Save index of clicked list item for editing
        listvew.setOnItemClickListener((parent, view, position, id) -> itemIndex = position);

        // Add button: add item from text entry then hide keyboard
        add_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String item = itemEdt.getText().toString().trim();
                if (!item.isEmpty()) {
                    lngList.add(new Item(item,0));
                    adapter.notifyDataSetChanged();
                    itemEdt.setText("");
                    saveList();
                    hideKeyboard();
                }
            }
        });

        // Delete button: delete item that matches text entry then hide keyboard
        delete_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String itemToDelete = itemEdt.getText().toString().trim();
                if (!itemToDelete.isEmpty()) {
                    for (int i = 0; i < lngList.size(); i++) {
                        if (lngList.get(i).getName().equalsIgnoreCase(itemToDelete)) {
                            lngList.remove(i);
                            adapter.notifyDataSetChanged();
                            itemEdt.setText("");
                            saveList();
                            hideKeyboard();
                            break;
                        }
                    }
                }
            }
        });


        // Edit button: replace selected item with new text then hide keyboard
        edit_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newItemText = itemEdt.getText().toString().trim();
                if (!newItemText.isEmpty() && itemIndex != -1) {
                    lngList.set(itemIndex, new Item(newItemText, lngList.get(itemIndex).getProgress()));
                    adapter.notifyDataSetChanged();
                    itemEdt.setText("");
                    saveList();
                    hideKeyboard();
                }
            }
        });

        //Randomize the list
        random_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRand){
                    ogList.clear();
                    ogList.addAll(lngList);
                    Collections.shuffle(lngList);
                    adapter.notifyDataSetChanged();
                    isRand = true;

                    random_btn.setImageResource(R.drawable.baseline_undo_24);
                    Toast.makeText(MainActivity.this, "List Randomized!", Toast.LENGTH_SHORT).show();
                } else {
                    lngList.clear();
                    lngList.addAll(ogList);
                    adapter.notifyDataSetChanged();
                    isRand = false;

                    random_btn.setImageResource(R.drawable.ic_randomize);
                    Toast.makeText(MainActivity.this, "Original Order Restored!", Toast.LENGTH_SHORT).show();
                }

            }
        });

        // initialize the new spin button
        slotMachine = new SlotMachine(this, listvew, lngList, () -> {
            spin_btn.setEnabled(true); // re-enable after spin ends
        });

        spin_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spin_btn.setEnabled(false);
                slotMachine.startSpin();
            }
          });

        listvew.setOnItemClickListener((parent, view, position, id) -> {
            // index of clicked item
            itemIndex = position;
            Item item = lngList.get(position);

            // display alert to adjust the progress
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setTitle("Set Progress for: " + item.getName());

            LinearLayout layout = new LinearLayout(MainActivity.this);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setPadding(32,32,32,32);

            // display current progress
            final TextView progressLabel = new TextView(MainActivity.this);
            progressLabel.setText("Progress: " + item.getProgress() + "%");

            layout.addView(progressLabel);

            // seekbar to allow user to set the progress
            final SeekBar seekBar = new SeekBar(MainActivity.this);
            seekBar.setMax(100);
            seekBar.setProgress(item.getProgress());
            layout.addView(seekBar);

            seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    progressLabel.setText("Progress: " + progress + "%");
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            builder.setView(layout);
            builder.setPositiveButton("Save", (dialog, which) -> {
                // update the progress on save
                item.setProgress(seekBar.getProgress());
                adapter.notifyDataSetChanged();
                // save progress
                saveList();
            });
            builder.setNegativeButton("Cancel", null);
            builder.show();
        });
    }

    // Save the list into SharedPreferences
    private void saveList() {
        SharedPreferences prefs = getSharedPreferences("ListPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Build a single string from all items
        StringBuilder sb = new StringBuilder();
        for (Item item : lngList) {
            sb.append(item.getName()).append(ITEM_SEPERATOR).append(item.getProgress()).append(DELIMITER);

        }
        editor.putString("listData", sb.toString());
        editor.apply();
    }

    // Load the list from SharedPreferences
    private void loadList() {
        SharedPreferences prefs = getSharedPreferences("ListPrefs", MODE_PRIVATE);
        String savedData = prefs.getString("listData", "");
        isRand = prefs.getBoolean(RANDOMIZED_KEY, false);

        lngList.clear();
        if (!savedData.isEmpty()) {
            String[] items = savedData.split(DELIMITER);
            for (String entry : items) {
                if (!entry.isEmpty() && entry.contains(ITEM_SEPERATOR)) {
                    String[] parts = entry.split(ITEM_SEPERATOR);
                    String name = parts[0];
                    int progress = Integer.parseInt(parts[1]);
                    lngList.add(new Item(name, progress));
                }
            }
            adapter.notifyDataSetChanged();
        }
        ogList.clear();
        if (isRand) {
            String ogData = prefs.getString(OG_LIST_KEY, "");
            if (!ogData.isEmpty()) {
                String[] items = ogData.split(DELIMITER);
                for (String entry : items) {
                    if (!entry.isEmpty() && entry.contains(ITEM_SEPERATOR)) {
                        String[] parts = entry.split(ITEM_SEPERATOR);
                        String name = parts[0];
                        int progress = Integer.parseInt(parts[1]);
                        ogList.add(new Item(name, progress));
                    }
                }
            }
            random_btn.setImageResource(R.drawable.baseline_undo_24);
        }else{
            ogList.addAll(lngList);
            random_btn.setImageResource(R.drawable.ic_randomize);
        }
        adapter.notifyDataSetChanged();
    }


    // Helper method to hide the soft keyboard.
    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }



    @Override
    protected void onPause() {
        super.onPause();
        saveList();
    }
}
