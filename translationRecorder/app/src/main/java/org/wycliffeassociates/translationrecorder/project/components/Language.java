package org.wycliffeassociates.translationrecorder.project.components;

import android.os.Parcel;
import android.os.Parcelable;

import org.wycliffeassociates.translationrecorder.Utils;
import org.wycliffeassociates.translationrecorder.database.ProjectDatabaseHelper;

public class Language extends ProjectComponent implements Parcelable {

    public Language(String slug, String name){
        super(slug, name);
    }

    public static Language[] getLanguages(ProjectDatabaseHelper db) {
        Language[] languages =  db.getLanguages();
        return languages;
    }

    @Override
    public String getLabel(){
        return Utils.capitalizeFirstLetter(mName);
    }

    @Override
    public boolean displayItemIcon() {
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mSlug);
        dest.writeString(mName);
    }

    public static final Parcelable.Creator<Language> CREATOR = new Parcelable.Creator<Language>() {
        public Language createFromParcel(Parcel in) {
            return new Language(in);
        }

        public Language[] newArray(int size) {
            return new Language[size];
        }
    };

    public Language(Parcel in) {
        super(in);
    }
}