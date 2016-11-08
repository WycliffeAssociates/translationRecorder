package wycliffeassociates.recordingapp.Playback.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import wycliffeassociates.recordingapp.R;
import wycliffeassociates.recordingapp.widgets.DragableViewFrame;
import wycliffeassociates.recordingapp.widgets.DraggableImageView;

/**
 * Created by sarabiaj on 11/4/2016.
 */

public class WaveformFragment extends Fragment {

    DragableViewFrame mDraggableViewFrame;

    public static WaveformFragment newInstance(){
        WaveformFragment f = new WaveformFragment();
        return f;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.fragment_waveform, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findViews();
        addStartMarker();
        addEndMarker();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    private void findViews(){
        View view = getView();
        mDraggableViewFrame = (DragableViewFrame) view.findViewById(R.id.draggable_view_frame);
    }

    public void addStartMarker(){
        DraggableImageView div = DraggableImageView.newInstance(getActivity(), R.drawable.ic_startmarker_cyan);
        mDraggableViewFrame.addView(div);
        div.bringToFront();
        
    }

    public void addEndMarker(){
        DraggableImageView div = DraggableImageView.newInstance(getActivity(), R.drawable.ic_endmarker_cyan);
        mDraggableViewFrame.addView(div);
        div.bringToFront();
    }

    public void addVerseMarker(){

    }

}
