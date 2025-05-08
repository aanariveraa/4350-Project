package com.example.listrandom;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

public class SlotMachine {
    private final ListView listView;
    private final ArrayList<Item> itemList;
    private final Context context;
    private final Handler handler;
    private final int spinCount = 25;
    private final int delay = 100;
    private int currentSpin = 0;
    private int selectedIndex = -1;
    private final Random random = new Random();
    private Runnable spinnerRunnable;
    private final Runnable onSpinComplete;

    public SlotMachine(Context context, ListView listView, ArrayList<Item> itemList, Runnable onSpinComplete) {
        this.context = context;
        this.listView = listView;
        this.itemList = itemList;
        this.handler = new Handler();
        this.onSpinComplete = onSpinComplete;
    }

    public void startSpin() {
        if (itemList.isEmpty()) {
            Toast.makeText(context, "List is empty!", Toast.LENGTH_SHORT).show();
            return;
        }

        currentSpin = 0;
        spinnerRunnable = new Runnable() {
            @Override
            public void run() {
                selectedIndex = random.nextInt(itemList.size());
                listView.smoothScrollToPosition(selectedIndex);

                if (currentSpin < spinCount) {
                    currentSpin++;
                    handler.postDelayed(this, delay);
                } else {
                    highlightSelectedItem();
                    showCelebration();
                    if (onSpinComplete != null) {
                        onSpinComplete.run();
                    }
                }
            }
        };

        handler.post(spinnerRunnable);
    }

    private void highlightSelectedItem() {
        for (int i = 0; i < listView.getChildCount(); i++) {
            View itemView = listView.getChildAt(i);
            if (i == selectedIndex - listView.getFirstVisiblePosition()) {
                itemView.setBackgroundColor(context.getResources().getColor(android.R.color.holo_green_light));
            } else {
                itemView.setBackgroundColor(context.getResources().getColor(android.R.color.transparent));
            }
        }
    }

    private void showCelebration() {
        String selectedItem = itemList.get(selectedIndex).getName();
        Toast.makeText(context, "🎉 Selected: " + selectedItem + " 🎉", Toast.LENGTH_LONG).show();
    }
}
