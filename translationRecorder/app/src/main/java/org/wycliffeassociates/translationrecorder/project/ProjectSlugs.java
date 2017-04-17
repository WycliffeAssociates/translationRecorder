package org.wycliffeassociates.translationrecorder.project;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Joe on 3/31/2017.
 */

public class ProjectSlugs implements Parcelable {

    String language;
    String anthology;
    String version;
    int bookNumber;
    String book;

    public ProjectSlugs(String language, String anthology, String version, int bookNumber, String book) {
        this.language = language;
        this.anthology = anthology;
        this.version = version;
        this.bookNumber = bookNumber;
        this.book = book;
    }

    public String getLanguage() {
        return language;
    }

    public String getAnthology() {
        return anthology;
    }

    public String getVersion() {
        return version;
    }

    public int getBookNumber() {
        return bookNumber;
    }

    public String getBook() {
        return book;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(language);
        dest.writeString(anthology);
        dest.writeString(book);
        dest.writeString(version);
        dest.writeInt(bookNumber);
    }

    public static final Parcelable.Creator<ProjectSlugs> CREATOR = new Parcelable.Creator<ProjectSlugs>() {
        public ProjectSlugs createFromParcel(Parcel in) {
            return new ProjectSlugs(in);
        }

        public ProjectSlugs[] newArray(int size) {
            return new ProjectSlugs[size];
        }
    };

    public ProjectSlugs(Parcel in) {
        language = in.readString();
        anthology = in.readString();
        book = in.readString();
        version = in.readString();
        bookNumber = in.readInt();
    }
}