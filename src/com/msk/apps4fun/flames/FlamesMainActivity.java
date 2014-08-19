package com.msk.apps4fun.flames;

import com.amazon.device.ads.Ad;
import com.amazon.device.ads.AdError;
import com.amazon.device.ads.AdLayout;
import com.amazon.device.ads.AdListener;
import com.amazon.device.ads.AdProperties;
import com.amazon.device.ads.AdRegistration;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.FrameLayout.LayoutParams;

public class FlamesMainActivity extends Activity {
    private EditText etOne;
    private EditText etTwo;
    private Button submit;
    private TextView result;
    private Animation anim_trans_left;
    private Animation anim_trans_bottom;
    private Animation anim_trans_right;
    private Animation anim_trans_left_back;
    private Animation anim_trans_bottom_back;
    private Animation anim_trans_right_back;
    private Animation anim_spin_zoom;
    private Toast mToast;
    private String name_one;
    private String name_two;
    private InputMethodManager imm;
    private MenuItem mShareItem;
    private MenuItem mRefreshItem;
    
    private ViewGroup adViewContainer; // View group to which the ad view will be added.
    private AdLayout currentAdView; // The ad that is currently visible to the user.
    private AdLayout nextAdView; // A placeholder for the next ad so we can keep the current ad visible while the next ad loads.
    private static final String APP_KEY = "e8482846fd934d2a978db4ea36b1e910";


    private InputFilter onlytext = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest,
                int dstart, int dend) {
            for (int i = start; i < end; i++) {
                if (!(Character.isLetter(source.charAt(i)) || source.charAt(i) == ' ')) {
                    mToast.setText("Only alphabets are allowed");
                    mToast.show();
                    return source.subSequence(0, i);
                }
            }
            return null;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        intialize();
        startInwardAnimation();
        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        
        AdRegistration.setAppKey(APP_KEY);
        loadAd();
    }

    private void intialize() {
        etOne = (EditText) findViewById(R.id.name_1);
        etTwo = (EditText) findViewById(R.id.name_2);
        submit = (Button) findViewById(R.id.submit);
        result = (TextView) findViewById(R.id.result);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        anim_trans_left = AnimationUtils.loadAnimation(this, R.anim.translate_from_left);
        anim_trans_right = AnimationUtils.loadAnimation(this, R.anim.translate_from_right);
        anim_trans_bottom = AnimationUtils.loadAnimation(this, R.anim.translate_from_bottom);
        anim_trans_left_back = AnimationUtils.loadAnimation(this, R.anim.translate_back_from_left);
        anim_trans_right_back = AnimationUtils.loadAnimation(this, R.anim.translate_back_from_right);
        anim_trans_bottom_back = AnimationUtils.loadAnimation(this,R.anim.translate_back_from_bottom);
        anim_spin_zoom = AnimationUtils.loadAnimation(this, R.anim.spin_and_scale_text);
        adViewContainer = (ViewGroup) findViewById(R.id.root_layout);
        etOne.setFilters(new InputFilter[] { onlytext });
        etTwo.setFilters(new InputFilter[] { onlytext });
        anim_trans_bottom_back.setAnimationListener(new AnimationListener() {

            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                etOne.setVisibility(View.GONE);
                etTwo.setVisibility(View.GONE);
                submit.setVisibility(View.GONE);
                etOne.setText("");
                etTwo.setText("");

            }
        });
        anim_spin_zoom.setAnimationListener(new AnimationListener() {
            
            @Override
            public void onAnimationStart(Animation animation) {
            }
            
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
            
            @Override
            public void onAnimationEnd(Animation animation) {
                mRefreshItem.setVisible(true);
                mShareItem.setVisible(true);
            }
        });
    }

    public void onSubmitClick(View v) {
        loadAd();
        if (etOne.getText().toString().isEmpty() || etTwo.getText().toString().isEmpty()) {
            mToast.setText("Input is empty");
            mToast.show();
            return;
        }
        switch (flameUp(getCount())) {
        case 'F':
            result.setText("Friends");
            break;
        case 'L':
            result.setText("Lovers");
        case 'A':
            result.setText("Ancestors");
            break;
        case 'M':
            result.setText("Marriage");
            break;
        case 'E':
            result.setText("Enemies");
            break;
        case 'S':
            result.setText("Siblings");
            break;
        default:
            result.setText("Disaster");
            break;

        }
        startOutwardAnimation();
    }
    
    public void onTryAnother(){
        loadAd();
        result.setVisibility(View.GONE);
        mShareItem.setVisible(false);
        mRefreshItem.setVisible(false);

        etOne.setVisibility(View.VISIBLE);
        etTwo.setVisibility(View.VISIBLE);
        submit.setVisibility(View.VISIBLE);

        startInwardAnimation();
    }
    
    private void loadAd() {
        if (this.nextAdView == null) { // Create and configure a new ad if the next ad doesn't currently exist.
            this.nextAdView = new AdLayout(this);
            final LayoutParams layoutParams = new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            
            
            this.nextAdView.setLayoutParams(layoutParams);
            // Register our ad handler that will receive callbacks for state changes during the ad lifecycle.
            this.nextAdView.setListener(new AdListener() {
                
                @Override
                public void onAdLoaded(Ad arg0, AdProperties arg1) {
                    if(currentAdView!=null)
                        swapCurrentAd();
                    else {
                        currentAdView = nextAdView;
                        nextAdView = null;
                        showCurrentAd();
                    }
                    
                }
                
                @Override
                public void onAdFailedToLoad(Ad arg0, AdError arg1) {
                    // TODO Auto-generated method stub
                }
                
                @Override
                public void onAdExpanded(Ad arg0) {
                    // TODO Auto-generated method stub
                    
                }
                
                @Override
                public void onAdDismissed(Ad arg0) {
                    // TODO Auto-generated method stub
                    
                }
                
                @Override
                public void onAdCollapsed(Ad arg0) {
                    // TODO Auto-generated method stub
                    
                }
            });
        }
                
        this.nextAdView.loadAd();
    }
   
    public void onShareClick(){
        Intent i = new Intent();
        
        i.setAction(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_TEXT, "FLAMES result of \'"+name_one+"\' & \'"+name_two+"\' is "+result.getText());
        startActivity(Intent.createChooser(i, getResources().getString(R.string.share_via)));
        
    }

    private int getCount() {
        name_one = etOne.getText().toString();
        name_two = etTwo.getText().toString();
        char tmp;
        byte[] count = new byte[26];
        int total = 0;
        for (int i = 0; i < 26; i++)
            count[i] = 0;
        for (int i = 0; i < name_one.length(); i++) {
            tmp = name_one.charAt(i);
            if (tmp >= 'a' && tmp <= 'z')
                count[tmp - 'a']++;
            if (tmp >= 'A' && tmp <= 'Z')
                count[tmp - 'A']++;
        }
        for (int i = 0; i < name_two.length(); i++) {
            tmp = name_two.charAt(i);
            if (tmp >= 'a' && tmp <= 'z')
                count[tmp - 'a']--;
            if (tmp >= 'A' && tmp <= 'Z')
                count[tmp - 'A']--;
        }
        for (int i = 0; i < 26; i++)
            total += Math.abs(count[i]);

        return total;

    }

    private void startInwardAnimation() {

        etOne.startAnimation(anim_trans_left);
        etTwo.startAnimation(anim_trans_right);
        submit.startAnimation(anim_trans_bottom);
    }

    private void startOutwardAnimation() {

        hideKeyboard();

        etOne.startAnimation(anim_trans_left_back);
        etTwo.startAnimation(anim_trans_right_back);
        submit.startAnimation(anim_trans_bottom_back);
        
        result.setVisibility(View.VISIBLE);
        
        result.startAnimation(anim_spin_zoom);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (menu != null){
            mShareItem = menu.findItem(R.id.share);
            mRefreshItem = menu.findItem(R.id.refresh);
        }
        mRefreshItem.setVisible(false);
        mShareItem.setVisible(false);
     
        return super.onPrepareOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.share:
            onShareClick();
            break;
        case R.id.refresh:
            onTryAnother();
            break;
        default:
            break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void hideKeyboard() {
        if(etOne.hasFocus())
            imm.hideSoftInputFromWindow(etOne.getWindowToken(), 0);
        else
            imm.hideSoftInputFromWindow(etTwo.getWindowToken(), 0);
    }

    private static char flameUp(int count) {
        if (count == 0)
            return 'z';
        StringBuilder sb = new StringBuilder("FLAMES");
        int prevIndex = 0;
        for (int i = 0; i < 5; i++) {
            prevIndex = (count - 1 + prevIndex) % (6 - i);
            sb.deleteCharAt(prevIndex);
        }
        return sb.charAt(0);

    }
    
    private void swapCurrentAd() {
        final Animation slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down);
        slideDown.setAnimationListener(new AnimationListener() {
            public void onAnimationEnd(final Animation animation) {
               showNextAd();
            }

            public void onAnimationRepeat(final Animation animation) {
            }
            
            public void onAnimationStart(final Animation animation) {
            }
        });
        this.currentAdView.startAnimation(slideDown);
    }
    
    /**
     * Shows the ad that is in the current ad view to the user.
     */
    private void showCurrentAd() {
        this.adViewContainer.addView(this.currentAdView);
        final Animation slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up);
        this.currentAdView.startAnimation(slideUp);
    }
    
    /**
     * Shows the ad that is in the next ad view to the user.
     */
    private void showNextAd() {
        this.adViewContainer.removeView(this.currentAdView);
        final AdLayout tmp = this.currentAdView;
        this.currentAdView = this.nextAdView;
        this.nextAdView = tmp;
        showCurrentAd();
    }
    



}
