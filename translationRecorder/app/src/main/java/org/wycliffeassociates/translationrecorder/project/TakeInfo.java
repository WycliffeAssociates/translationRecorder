package org.wycliffeassociates.translationrecorder.project;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by sarabiaj on 4/17/2017.
 */

public class TakeInfo implements Parcelable {

    ProjectSlugs mSlugs;
    int mChapter;
    int mStartVerse;
    int mEndVerse;
    int mTake;

    public TakeInfo(ProjectSlugs slugs, int chapter, int startVerse, int endVerse, int take) {
        mSlugs = slugs;
        mChapter = chapter;
        mStartVerse = startVerse;
        mEndVerse = endVerse;
        mTake = take;
    }

    public TakeInfo(ProjectSlugs slugs, String chapter, String startVerse, String endVerse, String take) {
        mSlugs = slugs;
        //If there is only one chapter in the book, set default the chapter to 1
        if(chapter != null && !chapter.equals("")) {
            mChapter = Integer.parseInt(chapter);
        } else {
            mChapter = 1;
        }
        mStartVerse = Integer.parseInt(startVerse);
        if(endVerse != null) {
            mEndVerse = Integer.parseInt(endVerse);
        } else {
            mEndVerse = -1;
        }
        if(take != null) {
            mTake = Integer.parseInt(take);
        } else {
            mTake = 0;
        }
    }

    public ProjectSlugs getProjectSlugs() {
        return mSlugs;
    }

    public int getChapter() {
        return mChapter;
    }

    public int getStartVerse() {
        return mStartVerse;
    }

    public int getTake() {
        return mTake;
    }

    public int getEndVerse() {
        //if there is no end verse, there is no verse range, so the end verse is the start verse
        if(mEndVerse == -1) {
            return mStartVerse;
        }
        return mEndVerse;
    }

//    public String getNameWithoutTake() {
//        if (mSlugs.anthology != null && mSlugs.anthology.compareTo("obs") == 0) {
//            return mSlugs.language + "_obs_c" + String.format("%02d", mChapter) + "_v" + String.format("%02d", mStartVerse);
//        } else {
//            String name;
//            String end = (mEndVerse != -1 && mStartVerse != mEndVerse) ? String.format("-%02d", mEndVerse) : "";
//            if (mSlugs.book.compareTo("psa") == 0 && mChapter != 119) {
//                name = mSlugs.language + "_" + mSlugs.version + "_b" + String.format("%02d", mSlugs.bookNumber) + "_" + mSlugs.book + "_c" + String.format("%03d", mChapter) + "_v" + String.format("%02d", mStartVerse) + end;
//            } else if (mSlugs.book.compareTo("psa") == 0) {
//                end = (mEndVerse != -1) ? String.format("-%03d", mEndVerse) : "";
//                name = mSlugs.language + "_" + mSlugs.version + "_b" + String.format("%02d", mSlugs.bookNumber) + "_" + mSlugs.book + "_c" + ProjectFileUtils.chapterIntToString(mSlugs.book, mChapter) + "_v" + String.format("%03d", mStartVerse) + end;
//            } else {
//                name = mSlugs.language + "_" + mSlugs.version + "_b" + String.format("%02d", mSlugs.bookNumber) + "_" + mSlugs.book + "_c" + ProjectFileUtils.chapterIntToString(mSlugs.book, mChapter) + "_v" + String.format("%02d", mStartVerse) + end;
//            }
//            return name;
//        }
//    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mSlugs, flags);
        dest.writeInt(mChapter);
        dest.writeInt(mStartVerse);
        dest.writeInt(mEndVerse);
        dest.writeInt(mTake);
    }

    public static final Parcelable.Creator<TakeInfo> CREATOR = new Parcelable.Creator<TakeInfo>() {
        public TakeInfo createFromParcel(Parcel in) {
            return new TakeInfo(in);
        }

        public TakeInfo[] newArray(int size) {
            return new TakeInfo[size];
        }
    };

    public TakeInfo(Parcel in) {
        mSlugs = in.readParcelable(ProjectSlugs.class.getClassLoader());
        mChapter = in.readInt();
        mStartVerse = in.readInt();
        mEndVerse = in.readInt();
        mTake = in.readInt();
    }

//    @Override
//    public boolean equals(Object takeInfo){
//        if(takeInfo == null) {
//            return false;
//        }
//        if(!(takeInfo instanceof TakeInfo)) {
//            return false;
//        } else {
//            return (getProjectSlugs().equals(((TakeInfo) takeInfo).getProjectSlugs())
//                    && getChapter() == ((TakeInfo) takeInfo).getChapter()
//                    && getStartVerse() == ((TakeInfo) takeInfo).getStartVerse()
//                    && getEndVerse() == ((TakeInfo) takeInfo).getEndVerse()
//                    && getTake() == ((TakeInfo) takeInfo).getTake());
//        }
//    }

    public boolean equalBaseInfo(TakeInfo takeInfo) {
        if(takeInfo == null) {
            return false;
        }
        if(!(takeInfo instanceof TakeInfo)) {
            return false;
        } else {
            return (getProjectSlugs().equals(takeInfo.getProjectSlugs())
                    && getChapter() == takeInfo.getChapter()
                    && getStartVerse() == takeInfo.getStartVerse()
                    && getEndVerse() == takeInfo.getEndVerse());
        }
    }
}
