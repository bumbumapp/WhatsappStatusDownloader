package com.bumbumapps.statusdownloader.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class StatusStorageTest {
    @Test
    public void savedFoldersMapToPicturesCollection() {
        assertEquals("Pictures/WhatsAppStatusesDir/WhatsApp/", StatusStorage.savedRelativePath("/storage/emulated/0/WhatsAppStatusesDir/Media/WhatsApp/"));
        assertEquals("Pictures/WhatsAppStatusesDir/WhatsAppBusiness/", StatusStorage.savedRelativePath("/storage/emulated/0/WhatsAppStatusesDir/Media/WhatsAppBusiness/"));
        assertEquals("Pictures/WhatsAppStatusesDir/GWAActivity/", StatusStorage.savedRelativePath("/storage/emulated/0/WhatsAppStatusesDir/Media/GWAActivity/"));
    }

    @Test
    public void statusFoldersPointIntoAndroidMedia() {
        assertEquals("Android/media/com.whatsapp/WhatsApp/Media/.Statuses",
                StatusStorage.statusesRelativePath("/storage/emulated/0/Android/media/com.whatsapp/WhatsApp/Media/.Statuses/"));
        assertEquals("Android/media/com.whatsapp.w4b/WhatsApp Business/Media/.Statuses",
                StatusStorage.statusesRelativePath("/storage/emulated/0/WhatsApp Business/Media/.Statuses/"));
    }

    @Test
    public void extensionFilterIsCaseInsensitive() {
        assertTrue(StatusStorage.matches("ABC.JPG", new String[]{".jpg"}));
        assertTrue(StatusStorage.matches("a.mp4", new String[]{".jpg", ".mp4"}));
        assertFalse(StatusStorage.matches(".nomedia", new String[]{".jpg", ".mp4"}));
    }
}
