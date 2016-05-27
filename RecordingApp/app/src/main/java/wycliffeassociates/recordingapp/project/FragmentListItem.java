package wycliffeassociates.recordingapp.project;

import android.app.Fragment;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.TextView;

import wycliffeassociates.recordingapp.R;

/**
 * Created by sarabiaj on 5/26/2016.
 */
public class FragmentListItem extends Fragment{

    public FragmentListItem(){}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }
    private void removeView(int id){
        ViewGroup view = (ViewGroup)getView().findViewById(id).getParent();
        view.removeView(view.findViewById(id));
    }
    public void setMajorText(String text){
        TextView majorText = (TextView)getView().findViewById(R.id.majorText);
        if(majorText != null) {
            majorText.setText(text);
        }
    }
    public void setMinorText(String text){
        TextView majorText = (TextView)getView().findViewById(R.id.minorText);
        if(majorText != null) {
            majorText.setText(text);
        }
    }

    public static class Builder{

        private FragmentListItem mFragment;
        private boolean mMore = false;
        private boolean mProject = false;
        private String mMajorText = "";
        private String mMinorText = "";

        public Builder(){
            mFragment = new FragmentListItem();
        }
        public Builder hasMore(boolean more){
            this.mMore = true;
            return this;
        }
        public Builder isProject(boolean project){
            this.mProject = project;
            return this;
        }
        public Builder majorText(String majorText){
            this.mMajorText = majorText;
            return this;
        }
        public Builder minorText(String minorText){
            this.mMinorText = minorText;
            return this;
        }
        public FragmentListItem build(){
            mFragment = new FragmentListItem();
            if(!mMore){
                mFragment.removeView(R.id.moreIcon);
            }
            if(!mProject){
                mFragment.removeView(R.id.moreIcon);
            }
            mFragment.setMajorText(mMajorText);
            mFragment.setMinorText(mMinorText);
            return mFragment;
        }
    }
}
