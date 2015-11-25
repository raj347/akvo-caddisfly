package org.akvo.akvoqr;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import org.akvo.akvoqr.choose_striptest.StripTest;
import org.akvo.akvoqr.detector.FinderPatternInfo;
import org.akvo.akvoqr.ui.ProgressIndicatorView;
import org.akvo.akvoqr.ui.QualityCheckView;
import org.akvo.akvoqr.util.Constant;
import org.akvo.akvoqr.util.FileStorage;
import org.json.JSONArray;

import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link CameraViewListener} interface
 * to handle interaction events.
 * Use the {@link CameraStartTestFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CameraStartTestFragment extends CameraSharedFragment {

    private CameraViewListener mListener;
    private Button startButton;
    private List<StripTest.Brand.Patch> patches;
    private ProgressIndicatorView progressIndicatorViewAnim;
    private int timeLapsed = 0;
    private Handler handler;
    private String brandName;
    private int patchesCovered = -1;

    private int imageCount = 0;
    private JSONArray imagePatchArray = new JSONArray();
    private long initTimeMillis;

    public static CameraStartTestFragment newInstance(String brandName) {
        CameraStartTestFragment fragment = new CameraStartTestFragment();
        Bundle args = new Bundle();
        args.putString(Constant.BRAND, brandName);
        fragment.setArguments(args);
        return fragment;
    }

    public CameraStartTestFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_camera_starttest, container, false);
        startButton = (Button) rootView.findViewById(R.id.activity_cameraStartButton);

        if(getArguments()!=null) {

            brandName = getArguments().getString(Constant.BRAND);

            //get the patches ordered by time-lapse
            patches = StripTest.getInstance().getBrand(brandName).getPatchesOrderedByTimelapse();

            progressIndicatorViewAnim = (ProgressIndicatorView) rootView.findViewById(R.id.activity_cameraProgressIndicatorViewAnim);
            for (int i = 0; i < patches.size(); i++) {

                //System.out.println("***patches: " + i + " timelapse: " + patches.get(i).getTimeLapse());

                if (i > 0) {
                    if (patches.get(i).getTimeLapse() - patches.get(i - 1).getTimeLapse() == 0) {
                        continue;
                    }
                }
                progressIndicatorViewAnim.addStep(i, (int) patches.get(i).getTimeLapse());
            }
        }
        handler = new Handler(Looper.getMainLooper());

        //use brightness view as a button to switch on and off the flash
        QualityCheckView exposureView = (QualityCheckView) rootView.findViewById(R.id.activity_cameraImageViewExposure);
        if(exposureView!=null) {
            exposureView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    mListener.switchFlash();
                }
            });
        }

        return rootView;
    }

    public void showStartButton() {

        if(startButton==null)
            return;

        //System.out.println("***count quality check qualityChecksOK test fragment " );

        startButton.setVisibility(View.VISIBLE);
        startButton.setCompoundDrawablesWithIntrinsicBounds(R.drawable.checked_box, 0, 0, 0);

        if(progressIndicatorViewAnim!=null) {
            progressIndicatorViewAnim.setStart(true);
        }
    }

    public void setStepsTaken(final int number)
    {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(progressIndicatorViewAnim!=null) {
                    progressIndicatorViewAnim.setStepsTaken(number);
                }
            }
        };
        handler.post(runnable);

    }

    private int stepsCovered = 0;
    public void sendData(final byte[] data, long timeMillis,
                         final FinderPatternInfo info)
    {
        //check if picture is taken on time for the patch.
        //assumed is that some tests require time for a color to develop.
        //reading may be done after that time, but not before.
        for(int i=patchesCovered+1;i<patches.size();i++) {
            //in case the reading is done after the time lapse we want to save the data for all patches before the time-lapse...
            if (timeMillis > initTimeMillis + patches.get(i).getTimeLapse() * 1000) {

                //keep track of which patches are 'done'
                patchesCovered = i;

                //keep track of stepsCovered ('step' is the patches that have the same time lapse)
                if (i > 0) {
                    if (patches.get(i).getTimeLapse() - patches.get(i - 1).getTimeLapse() > 0) {
                        stepsCovered ++;
                    }
                }
                //keep track of which image belongs to which patch
                JSONArray array = new JSONArray();
                array.put(imageCount);
                array.put(patches.get(i).getOrder());
                imagePatchArray.put(array);

            }
        }

        new StoreDataTask(getActivity(), imageCount, data, info).execute();

        //add one to imageCount
        imageCount ++;

        setStepsTaken(stepsCovered);

        //System.out.println("***xxximageCount: " + imageCount + " patchesCovered: " + patchesCovered);

    }

    public void dataSent(int format, int width, int height)
    {

        //set to true if you want to see the original and calibrated images in DetectStripActivity
        //set to false if you want to go to the ResultActivity directly
        boolean develop = false;

        if(patchesCovered == patches.size()-1)
        {
            if(imagePatchArray.length()>0) {
                //write image/patch info to internal storage
                FileStorage.writeToInternalStorage(Constant.IMAGE_PATCH, imagePatchArray.toString());

                Intent detectStripIntent = createDetectStripIntent(format, width, height);

                //if develop
                if (develop) {
                    detectStripIntent.setClass(getActivity(), DetectStripActivity.class);
                    startActivity(detectStripIntent);
                } else {
                    new DetectStripTask(getActivity()).execute(detectStripIntent);
                }
            }
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (CameraViewListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement CameraViewListener");
        }

        //reset quality checks count to zero
        if(mListener!=null)
            mListener.setCountQualityCheckResultZero();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        try {
            final FrameLayout parentView = (FrameLayout) getActivity().findViewById(((View) getView().getParent()).getId());

            //find the transparent view in which to show part of the preview
            final RelativeLayout transView = (RelativeLayout) getView().findViewById(R.id.overlay);
            final Animation slideUp = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_up);

            transView.post(new Runnable() {

                @Override
                public void run() {

                    //enlarge the transparent view based on a factor of its parent height
                    ViewGroup.LayoutParams params = transView.getLayoutParams();
                    params.height = (int) Math.round(parentView.getHeight() * Constant.CROP_CAMERAVIEW_FACTOR);
                    transView.setLayoutParams(params);
                    params = parentView.getLayoutParams();
                    params.height = (int) Math.round(parentView.getHeight() * Constant.CROP_CAMERAVIEW_FACTOR);
                    parentView.setLayoutParams(params);
                    transView.startAnimation(slideUp);
                }
            });

            startCountdown();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private Runnable startCountdownRunnable = new Runnable() {
        @Override
        public void run() {

            progressIndicatorViewAnim.setTimeLapsed(timeLapsed);

            timeLapsed++;
            handler.postDelayed(this, 1000);
        }
    };

    private void startCountdown() {

        timeLapsed = 0;
        initTimeMillis = System.currentTimeMillis();

        handler.post(startCountdownRunnable);

        //Post the callback on time for each patch
        for (int i = 0; i < patches.size(); i++) {
            if (i > 0) {
                if (patches.get(i).getTimeLapse() - patches.get(i - 1).getTimeLapse() == 0) {
                    continue;
                }
            }
            mListener.takeNextPicture((long) patches.get(i).getTimeLapse() * 1000);
        }
    }

    private Intent createDetectStripIntent(int format, int width, int height)
    {
        Intent detectStripIntent = new Intent();
        //put Extras into intent
        detectStripIntent.putExtra(Constant.BRAND, brandName);
        detectStripIntent.putExtra(Constant.FORMAT, format);
        detectStripIntent.putExtra(Constant.WIDTH, width);
        detectStripIntent.putExtra(Constant.HEIGHT, height);

        detectStripIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        return detectStripIntent;
    }

    @Override
    public void countQuality(int count)
    {

        if(startButton!=null)
        {
            try {
                count = Math.min(Constant.COUNT_QUALITY_CHECK_LIMIT, count);
                startButton.setText("Quality checks: " + String.valueOf(count) + " out of " + Constant.COUNT_QUALITY_CHECK_LIMIT);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }
}
