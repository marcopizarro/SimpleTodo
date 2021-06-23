package com.example.simpletodo;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import android.content.Intent;
import android.os.Bundle;
//import android.os.FileUtils;
import org.apache.commons.io.FileUtils;

import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    public static final String KEY_ITEM_TEXT = "item_text";
    public static final String KEY_ITEM_POSITION = "item_position";
    public static final int EDIT_TEXT_CODE = 20;

    List<String> items;

    Button btnAdd;
    EditText etItem;
    RecyclerView rvItems;
    ItemsAdapter itemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnAdd = findViewById(R.id.btnAdd);
        etItem = findViewById(R.id.etItem);
        rvItems = findViewById(R.id.rvItems);

        loadItems();

        ItemsAdapter.OnLongClickListener OnLongClickListener = new ItemsAdapter.OnLongClickListener(){
            @Override
            public void onItemLongClicked(int position) {
                //delete item
                items.remove(position);
                //notify adapter
                itemsAdapter.notifyItemRemoved(position);
                Toast.makeText(getApplicationContext(), "Item Removed", Toast.LENGTH_SHORT).show();
                saveItems();
            }
        };

        ItemsAdapter.OnClickListener OnClickListener = new ItemsAdapter.OnClickListener() {
            @Override
            public void onItemClicked(int position) {
                //create new activity
                Intent i = new Intent(MainActivity.this, EditActivity.class);
                //pass the data being edited
                i.putExtra(KEY_ITEM_TEXT, items.get(position));
                i.putExtra(KEY_ITEM_POSITION, position);
                //display the activity
                startActivityForResult(i, EDIT_TEXT_CODE);
            }
        };
        itemsAdapter = new ItemsAdapter(items, OnLongClickListener, OnClickListener);
        rvItems.setAdapter(itemsAdapter);
        rvItems.setLayoutManager(new LinearLayoutManager(this));

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String todoText = etItem.getText().toString();
                //add item to model
                items.add(todoText);
                //notify adapter that item has been inserted
                itemsAdapter.notifyItemInserted(items.size() - 1);
                etItem.setText("");
                Toast.makeText(getApplicationContext(), "Item Added", Toast.LENGTH_SHORT).show();
                saveItems();
            }
        });
    }

//    handle result of edit
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable @org.jetbrains.annotations.Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == EDIT_TEXT_CODE) {
            //retrieve updated text
            String itemText = data.getStringExtra(KEY_ITEM_TEXT);
            //extract original position
            int itemPosition = data.getExtras().getInt(KEY_ITEM_POSITION);
            //update the model with new text
            items.set(itemPosition, itemText);
            //notify adapter
            itemsAdapter.notifyItemChanged(itemPosition);
            //persist changes
            saveItems();
            Toast.makeText(getApplicationContext(), "Successfully Updated", Toast.LENGTH_SHORT).show();
        } else {
            Log.w("Main Activity", "Unknown call to onActivityResult");
        }
    }

    private File getDataFile() {
        return new File(getFilesDir(), "data.txt");
    }

    // load items by reading ever line of file
    private void loadItems(){

        try {
            items = new ArrayList<>(FileUtils.readLines(getDataFile(), Charset.defaultCharset()));
        } catch (IOException e) {
            Log.e("Main Activty", "Error reading items", e);
            items = new ArrayList<>();
        }


    }

    //write file
    private void saveItems(){
        try {
            FileUtils.writeLines(getDataFile(), items);
        } catch (IOException e) {
            Log.e("Main Activty", "Error writing items", e);
        }
    }
}