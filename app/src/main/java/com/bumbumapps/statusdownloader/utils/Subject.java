package com.bumbumapps.statusdownloader.utils;


public interface Subject {
    void register(Observer observer);

    void unregister(Observer observer);

    void notifyObservers();
}