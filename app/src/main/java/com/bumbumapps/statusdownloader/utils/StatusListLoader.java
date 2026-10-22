package com.bumbumapps.statusdownloader.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumbumapps.statusdownloader.model.ModelStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Shared list loading for the status / saved fragments (background listing, empty + access states). */
public final class StatusListLoader {
    public static final int REQUEST_TREE_ACCESS = 4242;
    private static final ExecutorService IO = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());
    private static final String EMPTY = "No Status Available \n Check Out some Status & come back again...";

    public interface AdapterFactory {
        RecyclerView.Adapter<?> create(ArrayList<ModelStatus> data);
    }

    private StatusListLoader() {
    }

    /**
     * Loads statuses (or saved files when {@code saved}) of {@code dir} into {@code rv}. On Android 11+
     * without folder access, the empty text becomes a button that opens the folder picker.
     */
    public static void load(final Fragment fragment, final String dir, final String[] exts, final int type,
                            final boolean saved, final TextView emptyView, final RecyclerView rv,
                            final AdapterFactory factory) {
        final Context context = fragment.getContext();
        if (context == null) {
            return;
        }
        final Context app = context.getApplicationContext();
        emptyView.setOnClickListener(null);
        if (!saved && !StatusStorage.hasAccess(app, dir)) {
            emptyView.setVisibility(View.VISIBLE);
            emptyView.setText("Tap here to allow access to the statuses folder\n(select \".Statuses\" and tap \"Use this folder\")");
            emptyView.setOnClickListener(v -> {
                try {
                    fragment.startActivityForResult(StatusStorage.requestAccessIntent(dir), REQUEST_TREE_ACCESS);
                } catch (RuntimeException ignored) {
                }
            });
            rv.setAdapter(factory.create(new ArrayList<>()));
            rv.setLayoutManager(new GridLayoutManager(context, 2));
            return;
        }
        IO.execute(() -> {
            List<StatusStorage.Item> items = saved
                    ? StatusStorage.listSaved(app, dir, exts)
                    : StatusStorage.listStatuses(app, dir, exts);
            final ArrayList<ModelStatus> data = new ArrayList<>();
            for (StatusStorage.Item it : items) {
                String base = it.name.lastIndexOf('.') > 0 ? it.name.substring(0, it.name.lastIndexOf('.')) : it.name;
                data.add(new ModelStatus(it.id, base, type));
            }
            MAIN.post(() -> {
                if (!fragment.isAdded()) {
                    return;
                }
                if (data.isEmpty()) {
                    emptyView.setVisibility(View.VISIBLE);
                    emptyView.setText(EMPTY);
                } else {
                    emptyView.setVisibility(View.GONE);
                }
                rv.setAdapter(factory.create(data));
                rv.setLayoutManager(new GridLayoutManager(fragment.getActivity(), 2));
            });
        });
    }

    /** Call from Fragment.onActivityResult; returns true if the folder grant was handled. */
    public static boolean onActivityResult(Fragment fragment, String dir, int requestCode, int resultCode, Intent data) {
        if (requestCode != REQUEST_TREE_ACCESS) {
            return false;
        }
        if (resultCode == Activity.RESULT_OK && fragment.getContext() != null) {
            StatusStorage.onAccessGranted(fragment.getContext().getApplicationContext(), dir, data);
        }
        return true;
    }
}
