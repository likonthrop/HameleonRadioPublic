package com.anisimov.radioonline.item.itemhelper;

public interface ItemTouchHelper {
        void onRowDismiss(int position);
        void onRowMoved(int from, int to);
    }