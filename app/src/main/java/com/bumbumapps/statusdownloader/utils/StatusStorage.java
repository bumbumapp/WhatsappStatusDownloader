package com.bumbumapps.statusdownloader.utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.UriPermission;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Scoped-storage access for status files.
 * <ul>
 * <li>Android 11+: WhatsApp keeps statuses in Android/media/&lt;pkg&gt;/..., which apps can only read
 * through the Storage Access Framework. The user grants the .Statuses folder once
 * (ACTION_OPEN_DOCUMENT_TREE) and the grant is persisted - no "All files access" needed.</li>
 * <li>Saved statuses go to MediaStore (Pictures/WhatsAppStatusesDir/...) on Android 10+.</li>
 * <li>Android 10 and lower keep the old file paths (legacy storage permission).</li>
 * </ul>
 * Items are identified by a String: a file path (legacy) or a content:// Uri string.
 */
public final class StatusStorage {
    private static final String PREFS = "status_storage";
    private static final String EXTERNAL_DOCS = "com.android.externalstorage.documents";

    private StatusStorage() {
    }

    /** True if statuses must be read through a granted SAF tree (Android 11+). */
    public static boolean usesTreeAccess() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.R;
    }

    /** Relative folder (from the storage root) of the .Statuses folder for a legacy Config path. */
    @NonNull
    public static String statusesRelativePath(@NonNull String legacyDir) {
        if (legacyDir.contains("WhatsApp Business")) {
            return "Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses";
        } else if (legacyDir.contains("/GWAActivity/") || legacyDir.contains("GBWhatsApp")) {
            return "GBWhatsApp/Media/.Statuses";
        }
        return "Android/media/com.whatsapp/WhatsApp/Media/.Statuses";
    }

    /** ACTION_OPEN_DOCUMENT_TREE intent that opens the picker directly at the statuses folder. */
    @NonNull
    public static Intent requestAccessIntent(@NonNull String legacyDir) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Uri initial = DocumentsContract.buildDocumentUri(EXTERNAL_DOCS,
                    "primary:" + statusesRelativePath(legacyDir));
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, initial);
        }
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        return intent;
    }

    /** Persists the tree the user picked for this status source. */
    public static void onAccessGranted(@NonNull Context context, @NonNull String legacyDir, @Nullable Intent data) {
        Uri tree = data == null ? null : data.getData();
        if (tree == null) {
            return;
        }
        try {
            context.getContentResolver().takePersistableUriPermission(tree, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } catch (SecurityException ignored) {
            return;
        }
        prefs(context).edit().putString(legacyDir, tree.toString()).apply();
    }

    public static boolean hasAccess(@NonNull Context context, @NonNull String legacyDir) {
        if (!usesTreeAccess()) {
            return true;
        }
        Uri tree = grantedTree(context, legacyDir);
        return tree != null;
    }

    @Nullable
    private static Uri grantedTree(Context context, String legacyDir) {
        String saved = prefs(context).getString(legacyDir, null);
        if (saved == null) {
            return null;
        }
        Uri tree = Uri.parse(saved);
        for (UriPermission p : context.getContentResolver().getPersistedUriPermissions()) {
            if (p.isReadPermission() && p.getUri().equals(tree)) {
                return tree;
            }
        }
        return null;
    }

    /** A status/saved item: id is a path or content Uri string, name is the file name. */
    public static final class Item {
        public final String id;
        public final String name;

        Item(String id, String name) {
            this.id = id;
            this.name = name;
        }
    }

    /** Lists files of a status folder (legacy Config path) whose names end with one of {@code exts}. */
    @NonNull
    public static List<Item> listStatuses(@NonNull Context context, @NonNull String legacyDir, @NonNull String[] exts) {
        List<Item> out = new ArrayList<>();
        if (!usesTreeAccess()) {
            File[] files = new File(legacyDir).listFiles();
            if (files != null) {
                for (File f : files) {
                    if (matches(f.getName(), exts)) {
                        out.add(new Item(f.getAbsolutePath(), f.getName()));
                    }
                }
            }
            return out;
        }
        Uri tree = grantedTree(context, legacyDir);
        if (tree == null) {
            return out;
        }
        Uri children = DocumentsContract.buildChildDocumentsUriUsingTree(tree,
                DocumentsContract.getTreeDocumentId(tree));
        String[] cols = {DocumentsContract.Document.COLUMN_DOCUMENT_ID, DocumentsContract.Document.COLUMN_DISPLAY_NAME};
        try (Cursor c = context.getContentResolver().query(children, cols, null, null, null)) {
            while (c != null && c.moveToNext()) {
                String name = c.getString(1);
                if (name != null && matches(name, exts)) {
                    Uri doc = DocumentsContract.buildDocumentUriUsingTree(tree, c.getString(0));
                    out.add(new Item(doc.toString(), name));
                }
            }
        } catch (RuntimeException ignored) {
            // folder deleted / grant revoked: show the empty state
        }
        return out;
    }

    /** Lists statuses saved by this app into the folder for a legacy save path. */
    @NonNull
    public static List<Item> listSaved(@NonNull Context context, @NonNull String legacySaveDir, @NonNull String[] exts) {
        List<Item> out = new ArrayList<>();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            File[] files = new File(legacySaveDir).listFiles();
            if (files != null) {
                for (File f : files) {
                    if (matches(f.getName(), exts)) {
                        out.add(new Item(f.getAbsolutePath(), f.getName()));
                    }
                }
            }
            return out;
        }
        String rel = savedRelativePath(legacySaveDir);
        queryOwn(context, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, rel, exts, out);
        queryOwn(context, MediaStore.Video.Media.EXTERNAL_CONTENT_URI, rel, exts, out);
        return out;
    }

    private static void queryOwn(Context context, Uri collection, String rel, String[] exts, List<Item> out) {
        String[] cols = {MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DISPLAY_NAME};
        try (Cursor c = context.getContentResolver().query(collection, cols,
                MediaStore.MediaColumns.RELATIVE_PATH + "=?", new String[]{rel}, null)) {
            while (c != null && c.moveToNext()) {
                String name = c.getString(1);
                if (name != null && matches(name, exts)) {
                    out.add(new Item(ContentUris.withAppendedId(collection, c.getLong(0)).toString(), name));
                }
            }
        } catch (RuntimeException ignored) {
        }
    }

    /** MediaStore relative path used for a legacy save folder, e.g. "Pictures/WhatsAppStatusesDir/WhatsApp/". */
    @NonNull
    public static String savedRelativePath(@NonNull String legacySaveDir) {
        String marker = "WhatsAppStatusesDir/Media/";
        int i = legacySaveDir.indexOf(marker);
        String sub = i >= 0 ? legacySaveDir.substring(i + marker.length()) : "";
        if (!sub.endsWith("/")) {
            sub = sub + "/";
        }
        return "Pictures/WhatsAppStatusesDir/" + sub; // Environment.DIRECTORY_PICTURES
    }

    static boolean matches(String name, String[] exts) {
        String lower = name.toLowerCase(Locale.ROOT);
        for (String e : exts) {
            if (lower.endsWith(e)) {
                return true;
            }
        }
        return false;
    }

    /** File name of an item (path or content Uri). */
    @NonNull
    public static String nameOf(@NonNull String id) {
        if (id.startsWith("content://")) {
            String last = Uri.parse(id).getLastPathSegment();
            if (last == null) {
                return "";
            }
            int slash = last.lastIndexOf('/');
            return slash >= 0 ? last.substring(slash + 1) : last;
        }
        return new File(id).getName();
    }

    public static boolean isVideo(@Nullable String id) {
        if (id == null) {
            return false;
        }
        if (id.startsWith("content://media/") && id.contains("/video/")) {
            return true;
        }
        return nameOf(id).toLowerCase(Locale.ROOT).endsWith(".mp4");
    }

    public static boolean isImage(@Nullable String id) {
        if (id == null) {
            return false;
        }
        if (id.startsWith("content://media/") && id.contains("/images/")) {
            return true;
        }
        String n = nameOf(id).toLowerCase(Locale.ROOT);
        return n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".gif") || n.endsWith(".png");
    }

    private static InputStream open(Context context, String id) throws IOException {
        if (id.startsWith("content://")) {
            InputStream in = context.getContentResolver().openInputStream(Uri.parse(id));
            if (in == null) {
                throw new IOException("No stream for " + id);
            }
            return in;
        }
        return new FileInputStream(id);
    }

    /** Copies a status into the save folder; returns false on failure. */
    public static boolean save(@NonNull Context context, @NonNull String id, @NonNull String legacySaveDir) {
        String name = nameOf(id);
        boolean video = isVideo(id);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = context.getContentResolver();
                Uri collection = video ? MediaStore.Video.Media.EXTERNAL_CONTENT_URI : MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                ContentValues values = new ContentValues();
                values.put(MediaStore.MediaColumns.DISPLAY_NAME, name);
                values.put(MediaStore.MediaColumns.MIME_TYPE, video ? "video/mp4" : (name.toLowerCase(Locale.ROOT).endsWith(".gif") ? "image/gif" : "image/jpeg"));
                values.put(MediaStore.MediaColumns.RELATIVE_PATH, savedRelativePath(legacySaveDir));
                values.put(MediaStore.MediaColumns.IS_PENDING, 1);
                Uri target = resolver.insert(collection, values);
                if (target == null) {
                    return false;
                }
                try (InputStream in = open(context, id); OutputStream os = resolver.openOutputStream(target)) {
                    if (os == null) {
                        throw new IOException("No output stream");
                    }
                    copy(in, os);
                } catch (IOException e) {
                    resolver.delete(target, null, null);
                    throw e;
                }
                values.clear();
                values.put(MediaStore.MediaColumns.IS_PENDING, 0);
                resolver.update(target, values, null, null);
                return true;
            }
            File dir = new File(legacySaveDir);
            if (!dir.isDirectory() && !dir.mkdirs()) {
                return false;
            }
            try (InputStream in = open(context, id); OutputStream os = new FileOutputStream(new File(dir, name))) {
                copy(in, os);
            }
            return true;
        } catch (IOException | RuntimeException e) {
            return false;
        }
    }

    private static void copy(InputStream in, OutputStream os) throws IOException {
        byte[] buf = new byte[64 * 1024];
        int n;
        while ((n = in.read(buf)) > 0) {
            os.write(buf, 0, n);
        }
    }

    /** Uri that another app may read (FileProvider for legacy files). */
    @NonNull
    public static Uri shareUri(@NonNull Context context, @NonNull String id) {
        if (id.startsWith("content://")) {
            return Uri.parse(id);
        }
        return FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", new File(id));
    }

    /** Opens the system share sheet for an item; never throws. */
    public static void share(@NonNull Context context, @NonNull String id, @NonNull String mime) {
        try {
            Intent sharingIntent = new Intent(Intent.ACTION_SEND);
            sharingIntent.setType(mime);
            sharingIntent.putExtra(Intent.EXTRA_STREAM, shareUri(context, id));
            sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Intent chooser = Intent.createChooser(sharingIntent, "Share via");
            if (!(context instanceof Activity)) {
                chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            }
            context.startActivity(chooser);
        } catch (RuntimeException ignored) {
        }
    }

    /** Deletes a saved item; returns true if it is gone. */
    public static boolean delete(@NonNull Context context, @NonNull String id) {
        try {
            if (id.startsWith("content://")) {
                return context.getContentResolver().delete(Uri.parse(id), null, null) > 0;
            }
            File f = new File(id);
            return !f.exists() || f.delete();
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
