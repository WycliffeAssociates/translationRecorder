package org.wycliffeassociates.translationrecorder.project.components;

import java.io.File;

/**
 * Created by sarabiaj on 5/1/2018.
 */

public class User {

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
}
