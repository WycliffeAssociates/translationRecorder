package wycliffeassociates.recordingapp.wav;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import wycliffeassociates.recordingapp.ProjectManager.Project;

/**
 * Created by sarabiaj on 10/4/2016.
 */
public class WavMetadata {

    String mProject = "";
    String mLanguage = "";
    String mSource = "";
    String mSlug = "";
    String mBookNumber = "";
    String mMode = "";
    String mChapter = "";
    String mStartVerse = "";
    String mEndVerse = "";
    List<WavCue> mCuePoints;

    public WavMetadata(Project p, String chapter, String startVerse, String endVerse) {
        mProject = p.getProject();
        mLanguage = p.getTargetLanguage();
        mSource = p.getSource();
        mSlug = p.getSlug();
        mBookNumber = p.getBookNumber();
        mMode = p.getMode();
        mChapter = chapter;
        mStartVerse = startVerse;
        mEndVerse = endVerse;
    }

    /**
     * Loads the user profile from json
     *
     * @param json
     * @return
     * @throws Exception
     */
    public WavMetadata(JSONObject json) throws JSONException {
        if (json != null) {
            mProject = "";
            if (json.has("project")) {
                mProject = json.getString("project");
            }
            mLanguage = "";
            if (json.has("language")) {
                mLanguage = json.getString("language");
            }
            mSource = "";
            if (json.has("source")) {
                mSource = json.getString("source");
            }
            mSlug = "";
            if (json.has("slug")) {
                mSlug = json.getString("slug");
            }
            mBookNumber = "";
            if (json.has("book_number")) {
                mBookNumber = json.getString("book_number");
            }
            mMode = "";
            if (json.has("mode")) {
                mMode = json.getString("mode");
            }
            mChapter = "";
            if (json.has("chapter")) {
                mChapter = json.getString("chapter");
            }
            mStartVerse = "";
            if (json.has("startv")) {
                mStartVerse = json.getString("startv");
            }
            mEndVerse = "";
            if (json.has("endv")) {
                mEndVerse = json.getString("endv");
            }
            if(json.has("markers")) {
                JSONObject markers = json.getJSONObject("markers");
                mCuePoints = parseMarkers(markers);
            }
        }
    }

    private List<WavCue> parseMarkers(JSONObject markers){
        try {
            mCuePoints = new ArrayList<>();
            while(markers.keys().hasNext()){
                String s = markers.keys().next();
                long position = markers.getLong(s);
                WavCue cue = new WavCue(s, position);
                mCuePoints.add(cue);
            }
            return mCuePoints;
        } catch (JSONException e){
            return null;
        }
    }

    /**
     * Returns the profile represented as a json object
     *
     * @return
     */
    public JSONObject toJSON() {
        try {
            JSONObject json = new JSONObject();
            json.put("project", mProject);
            json.put("language", mLanguage);
            json.put("source", mSource);
            json.put("slug", mSlug);
            json.put("book_number", mBookNumber);
            json.put("mode", mMode);
            json.put("chapter", mChapter);
            json.put("startv", mStartVerse);
            json.put("endv", mEndVerse);
            return json;
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

}
