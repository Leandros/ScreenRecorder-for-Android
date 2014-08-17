package de.skilloverflow.screenrecorderforandroid;

import android.app.Activity;
import android.app.Fragment;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

public class MainActivity extends Activity {
    private static final int RUNNING_NOTIFICATION_ID = 73;
    private static final int FINISHED_NOTIFICATION_ID = 1337;

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

                try {
                    new SuTask(stringBuilder.toString().getBytes("ASCII")).execute();

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        };

        private class SuTask extends AsyncTask<Boolean, Void, Boolean> {
            private final byte[] mCommand;

            public SuTask(byte[] command) {
                super();
                this.mCommand = command;
            }

            @Override
            protected Boolean doInBackground(Boolean... booleans) {
                try {
                    Process sh = Runtime.getRuntime().exec("su", null, null);
                    OutputStream outputStream = sh.getOutputStream();

        			//avoid superuser toast recording
        			Thread.sleep(5000);
        			
                    outputStream.write(mCommand);
                    outputStream.flush();
                    outputStream.close();

                    final NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.notify(RUNNING_NOTIFICATION_ID, createRunningNotification(mContext));

                    sh.waitFor();
                    return true;

                } catch (InterruptedException e) {
                    e.printStackTrace();
                    Toast.makeText(mContext, mContext.getString(R.string.error_start_recording), Toast.LENGTH_LONG).show();

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(mContext, mContext.getString(R.string.error_start_recording), Toast.LENGTH_LONG).show();
                }

                return false;
            }

            @Override
            protected void onPostExecute(Boolean bool) {
                super.onPostExecute(bool);
                if (bool) {
                    final NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(NOTIFICATION_SERVICE);
                    notificationManager.cancel(RUNNING_NOTIFICATION_ID);

                    File file = new File(Environment.getExternalStorageDirectory().toString() + "/recording.mp4");
                    notificationManager.notify(FINISHED_NOTIFICATION_ID, createFinishedNotification(mContext, file));
                }
            }

            private Notification createRunningNotification(Context context) {
                Notification.Builder mBuilder = new Notification.Builder(context)
                        .setSmallIcon(android.R.drawable.stat_notify_sdcard)
                        .setContentTitle(context.getResources().getString(R.string.app_name))
                        .setContentText("Recording Running")
                        .setTicker("Recording Running")
                        .setPriority(Integer.MAX_VALUE)
                        .setOngoing(true);

                return mBuilder.build();
            }

            private Notification createFinishedNotification(Context context, File file) {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(file), "video/mp4");

                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT);

                Notification.Builder mBuilder = new Notification.Builder(context)
                        .setSmallIcon(android.R.drawable.stat_notify_sdcard)
                        .setContentTitle(context.getResources().getString(R.string.app_name))
                        .setContentText("Recording Finished")
                        .setTicker("Recording Finished")
                        .setContentIntent(pendingIntent)
                        .setOngoing(false)
                        .setAutoCancel(true);

                return mBuilder.build();
            }
        }
    }

}
