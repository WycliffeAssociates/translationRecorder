package org.wycliffeassociates.translationrecorder.project.components;

import java.io.File;

/**
 * Created by sarabiaj on 5/1/2018.
 */

public class User {

    private int id = 1;
    private File audio;
    private String hash;

    public User(File audio) {
        this.audio = audio;
    }

    public User(File audio, String hash) {
        this.audio = audio;
        this.hash = hash;
    }

    public User(int id, File audio, String hash) {
        this.audio = audio;
        this.hash = hash;
        this.id = id;
    }

    public int getId(){
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public File getAudio() {
        return audio;
    }

    public String getHash() {
        return hash;
    }

    @Override
    public String toString() {
        return String.format("{\"id\":%d, \"hash\":\"%s\", \"audio\":\"%s\"}", id, hash, audio);
    }
}
