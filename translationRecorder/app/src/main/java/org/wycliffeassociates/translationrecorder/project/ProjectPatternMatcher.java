package org.wycliffeassociates.translationrecorder.project;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Joe on 3/31/2017.
 */

public class ProjectPatternMatcher {

    String mRegex;
    String mGroups;
    Pattern mPattern;
    Matcher mMatch;
    int[] locations;

    String mName;
    boolean mMatched = false;

    private ProjectSlugs mProjectSlugs;
    private TakeInfo mTakeInfo;

    public ProjectPatternMatcher(String regex, String groups) {
        mRegex = regex;
        mGroups = groups;
        mPattern = Pattern.compile(regex);
        parseLocations();
    }

    public boolean matched() {
        return mMatched;
    }

    private void parseLocations() {
        String[] groups = mGroups.split(" ");
        locations = new int[groups.length];
        for(int i = 0; i < locations.length; i++) {
            locations[i] = Integer.parseInt(groups[i]);
        }
    }

    public String getRegex() {
        return mRegex;
    }

    public String getGroups() {
        return mGroups;
    }

    public boolean match(File file){
        return match(file.getName());
    }

    public boolean match(String file) {
        String[] values = new String[locations.length];

        if (!(file.equals(mName))) {
            mName = file;
            mMatch = mPattern.matcher(file);
            mMatch.find();
            if(mMatch.matches()) {
                mMatched = true;
                for (int i = 0; i < locations.length; i++) {
                    if (locations[i] != -1) {
                        values[i] = mMatch.group(locations[i]);
                    } else {
                        values[i] = "";
                    }
                }
                mProjectSlugs = new ProjectSlugs(values[0], values[1], values[2], Integer.parseInt(values[3]), values[4]);

                mTakeInfo = new TakeInfo(mProjectSlugs, Integer.parseInt(values[5]), Integer.parseInt(values[6]),
                        Integer.parseInt(values[7]), Integer.parseInt(values[8]));

            } else {
                mMatched = false;
                mProjectSlugs = null;
                mName = null;
            }
        }
        return mMatched;
    }

    public ProjectSlugs getProjectSlugs(){
        return mProjectSlugs;
    }

    public TakeInfo getTakeInfo() {
        return mTakeInfo;
    }
}
