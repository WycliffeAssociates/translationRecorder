package org.wycliffeassociates.translationrecorder.project;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Joe on 3/31/2017.
 */

public class ProjectSlugs implements Parcelable {

    final String language;
    final String version;
    final int bookNumber;
    final String book;

    public ProjectSlugs(String language, String version, int bookNumber, String book) {
        this.language = language;
        this.version = version;
        this.bookNumber = bookNumber;
        this.book = book;
    }

    public String getLanguage() {
        return language;
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
        book = in.readString();
        version = in.readString();
        bookNumber = in.readInt();
    }

    @Override
    public boolean equals(Object slugs){
        if(slugs == null) {
            return false;
        }
        if(!(slugs instanceof ProjectSlugs)) {
            return false;
        } else {
            return (
                    getVersion().equals(((ProjectSlugs) slugs).getVersion())
                    && getBook().equals(((ProjectSlugs) slugs).getBook())
                    && getBookNumber() == ((ProjectSlugs) slugs).getBookNumber()
                    && getLanguage().equals(((ProjectSlugs) slugs).getLanguage())
            );
        }
    }
}