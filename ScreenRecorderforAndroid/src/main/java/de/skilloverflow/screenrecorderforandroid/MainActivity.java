package de.skilloverflow.screenrecorderforandroid;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.container, new MainFragment())
                    .commit();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static class MainFragment extends Fragment {
        private Context mContext;

        private EditText mWidthEditText;
        private EditText mHeightEditText;
        private EditText mBitrateEditText;
        private EditText mTimeEditText;
        private Button mRecordButton;

        public MainFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            mContext = getActivity();

            mRecordButton = (Button) rootView.findViewById(R.id.btn_record);
            mRecordButton.setOnClickListener(RecordOnClickListener);

            mWidthEditText = (EditText) rootView.findViewById(R.id.et_width);
            mHeightEditText = (EditText) rootView.findViewById(R.id.et_height);
            mBitrateEditText = (EditText) rootView.findViewById(R.id.et_bitrate);
            mBitrateEditText.addTextChangedListener(BitrateTextWatcher);
            mTimeEditText = (EditText) rootView.findViewById(R.id.et_time);
            mTimeEditText.addTextChangedListener(TimeTextWatcher);

            return rootView;
        }

        private TextWatcher BitrateTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                // Not used.
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (TextUtils.isEmpty(charSequence)) {
                    return;
                }

                int value = Integer.valueOf(charSequence.toString());
                if (value > 50 || value == 0) {
                    mBitrateEditText.setError(mContext.getString(R.string.error_bitrate_edittext));
                    return;
                }

                mTimeEditText.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Not used.
            }
        };

        private TextWatcher TimeTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                // Not used.
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                if (TextUtils.isEmpty(charSequence)) {
                    return;
                }

                int value = Integer.valueOf(charSequence.toString());
                if (value > 180 || value == 0) {
                    mTimeEditText.setError(mContext.getString(R.string.error_time_editext));
                    return;
                }

                mTimeEditText.setError(null);
            }

            @Override
            public void afterTextChanged(Editable editable) {
                // Not used.
            }
        };

        private View.OnClickListener RecordOnClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(mTimeEditText.getError()) || !TextUtils.isEmpty(mBitrateEditText.getError())) {
                    Toast.makeText(mContext, mContext.getString(R.string.toast_invalid_values), Toast.LENGTH_LONG).show();
                    return;
                }

                boolean widthSet = !TextUtils.isEmpty(mWidthEditText.getText());
                boolean heightSet = !TextUtils.isEmpty(mHeightEditText.getText());
                if ((!widthSet && heightSet) || (widthSet && !heightSet)) {
                    Toast.makeText(mContext, mContext.getString(R.string.error_invalid_wxh), Toast.LENGTH_LONG).show();
                    return;
                }

                boolean bitrateSet = !TextUtils.isEmpty(mBitrateEditText.getText());
                boolean timeSet = !TextUtils.isEmpty(mTimeEditText.getText());

                try {
                    StringBuilder stringBuilder = new StringBuilder("/system/bin/screenrecord");
                    if (widthSet) {
                        stringBuilder.append(" --size ").append(mWidthEditText.getText()).append("x").append(mHeightEditText.getText());
                    }
                    if (bitrateSet) {
                        stringBuilder.append(" --bit-rate ").append(mBitrateEditText.getText());
                    }
                    if (timeSet) {
                        stringBuilder.append(" --time-limit ").append(mTimeEditText.getText());
                    }

                    // TODO User definable location.
                    stringBuilder.append(" ").append(Environment.getExternalStorageDirectory().toString()).append("/recording.mp4");
                    Log.d("TAG", "comamnd: " + stringBuilder.toString());

                    Process sh = Runtime.getRuntime().exec("su", null, null);
                    OutputStream outputStream = sh.getOutputStream();
                    outputStream.write(stringBuilder.toString().getBytes("ASCII"));
                    outputStream.flush();
                    outputStream.close();
                    sh.waitFor();

                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Toast.makeText(mContext, mContext.getString(R.string.error_start_recording), Toast.LENGTH_LONG).show();

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(mContext, mContext.getString(R.string.error_start_recording), Toast.LENGTH_LONG).show();
                }
            }
        };
    }

}
