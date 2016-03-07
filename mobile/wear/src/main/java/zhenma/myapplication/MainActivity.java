package zhenma.myapplication;

import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

import meapsoft.FFT;
import zhenma.myapplication.accelerometer.Filter;

public class MainActivity extends WearableActivity implements SensorEventListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private BoxInsetLayout mContainerView;

    GoogleApiClient mGoogleApiClient;
    Node mNode;

    /**
     * Filter class required to filter noise from accelerometer
     */
    private Filter filter = null;

    /**
     * SensorManager
     */
    private SensorManager mSensorManager;
    /**
     * Accelerometer Sensor
     */
    private Sensor mAccelerometer;

    /**
     * Step count to be displayed in UI
     */
    private int stepCount = 0;

    /**
     * Is accelerometer running?
     */
    private static boolean isAccelRunning = false;

    //Sensor data files
    //private File mRawAccFile;
    //private FileOutputStream mRawAccOutputStream;
    private LinkedList<double[]> buffer = new LinkedList<>();


    //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    private LinkedList<Double> shakeSignalQueue = new LinkedList<>();
    //Sensor data files
    //private File shakeSignalFile;
    //private FileOutputStream shakeSignalOutputStream;



    /*
	 * Various UI components
	 */
    private CompoundButton accelButton;

    //classifier
    private LinkedBlockingDeque<Double> blockQueue;
    public static final int BLOCK_QUEUE_CAPACITY =  64;
    private List<Double> fectureVec;
    private ClassificationAsyncTask classificationAsyncTask;
    private ImageView appIcon;

    private long lastSendTime = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAmbientEnabled();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        appIcon = (ImageView) findViewById(R.id.appIcon);

        // Init files !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        //shakeSignalFile = new File(Environment.getExternalStorageDirectory(), "shake_signal.csv");
        //Log.d("SHAKE_DATA_PATH", shakeSignalFile.getAbsolutePath());
        //try {
        //    shakeSignalOutputStream = new FileOutputStream(shakeSignalFile);
        //} catch (FileNotFoundException e) {
        //    e.printStackTrace();
        //}

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        accelButton = (ToggleButton) findViewById(R.id.StartButton);
        accelButton.setChecked(isAccelRunning);
        accelButton.setOnCheckedChangeListener(
                new CompoundButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton btn,boolean isChecked) {
                        if(!isAccelRunning) {
                            startAccelerometer();
                            appIcon.setImageResource(R.drawable.green_handshack);
                            accelButton.setChecked(true);
                        }
                        else {
                            stopAccelerometer();
                            appIcon.setImageResource(R.drawable.blue_handshack);
                            accelButton.setChecked(false);

                        }
                    }
                }
        );

        //Classifier
        blockQueue = new LinkedBlockingDeque<Double>();
        fectureVec = new ArrayList<Double>();
        classificationAsyncTask = new ClassificationAsyncTask();
        classificationAsyncTask.execute();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try{
            //shakeSignalOutputStream.close();
            mGoogleApiClient.disconnect();
        }catch (Exception ex)
        {
        }
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
//        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
//        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
//        updateDisplay();
        super.onExitAmbient();
    }

    /**
     * start accelerometer
     */
    private void startAccelerometer() {
        mGoogleApiClient.connect();
        isAccelRunning = true;
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_FASTEST);
        //Set up filter
        //Following sets up smoothing filter from mcrowdviz
        int SMOOTH_FACTOR = 10;
        filter = new Filter(SMOOTH_FACTOR);
        //OR Use Butterworth filter from mcrowdviz
        //double CUTOFF_FREQUENCY = 0.3;
        //filter = new Filter(CUTOFF_FREQUENCY);
        stepCount = 0;
    }

    /**
     * stop accelerometer
     */
    private void stopAccelerometer() {
        //mGoogleApiClient.disconnect();
        isAccelRunning = false;
        mSensorManager.unregisterListener(this);

        //Free filter and step detector
        filter = null;
    }

    /**
     * Resolve the node = the connected device to send the message to
     */
    private void resolveNode() {

        Wearable.NodeApi.getConnectedNodes(mGoogleApiClient)
                .setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                    @Override
                    public void onResult(NodeApi.GetConnectedNodesResult nodes) {
                        for (Node node : nodes.getNodes()) {
                            mNode = node;
                        }
                    }
                });
    }

    @Override
    public void onConnected(Bundle bundle) {
        resolveNode();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    /**
     * Send message to mobile handheld
     */
    private void sendMessage(String Key) {

        if (mNode != null && mGoogleApiClient!= null && mGoogleApiClient.isConnected()) {
            Log.d("WearToPhone", "-- " + mGoogleApiClient.isConnected());
            Wearable.MessageApi.sendMessage(
                    mGoogleApiClient, mNode.getId(), "ID" + "--" + Key, null).setResultCallback(

                    new ResultCallback<MessageApi.SendMessageResult>() {
                        @Override
                        public void onResult(MessageApi.SendMessageResult sendMessageResult) {

                            if (!sendMessageResult.getStatus().isSuccess()) {
                                Log.e("WearToPhone", "Failed to send message with status code: "
                                        + sendMessageResult.getStatus().getStatusCode());
                            }
                        }
                    }
            );
        }

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

            float accel[] = event.values;
            /**
             * Classifer
             */
            double x = accel[0];
            double y = accel[1];
            double z = accel[2];

            double matitude = Math.sqrt(x * x + y * y + z * z);
            try {
                blockQueue.put(matitude);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            //First, Get filtered values
            double filtAcc[] = filter.getFilteredValues(accel[0], accel[1], accel[2]);

            boolean success = updateShakeSignal(filtAcc[0], filtAcc[1], filtAcc[2]);
        }

    }

    private class ClassificationAsyncTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
//            Log.i(TAG, "doInBackground");
            while(true){
                if(isCancelled()){
                    return null;
                }
                if(blockQueue.size() < BLOCK_QUEUE_CAPACITY){
                    continue;
                }
                double[] re = new double[BLOCK_QUEUE_CAPACITY];
                double[] im = new double[BLOCK_QUEUE_CAPACITY];

                double max = Double.MIN_VALUE;
                for(int i = 0; i < BLOCK_QUEUE_CAPACITY; i++){
                    double matitude = 0;
                    try {
                        matitude = blockQueue.take();
                        re[i] = matitude;
                        if(matitude > max){
                            max = matitude;
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

                FFT fft = new FFT(BLOCK_QUEUE_CAPACITY);
                fft.fft(re, im);

                for (int i = 0; i < re.length; i++) {
                    // Compute each coefficient
                    double mag = Math.sqrt(re[i] * re[i] + im[i]* im[i]);
                    // Adding the computed FFT coefficient to the
                    // featVect
                    fectureVec.add(mag);
                    // Clear the field
                    im[i] = .0;
                }
                fectureVec.add(max);
                try {
                    double ret = WekaClassifier.classify(fectureVec.toArray());
                    fectureVec.clear();

                    if(ret == 0.0){
                        Log.d("Shake" ,"classifyed 0");
                        //sendUpdatedActivityToUI("Standing");
                    }else if(ret == 1.0){
                        Log.d("Shake" ,"classifyed 1");
                        //sendUpdatedActivityToUI("Walking");
                    }else if(ret == 2.0){
                        Log.d("Shake" ,"classifyed 2");
                        //sendUpdatedActivityToUI("Running");
                    }else if(ret == 3.0){
//                        SystemClock.sleep(291);
                        Log.d("Shake" ,"classifyed 3");
                        sendshakeSignal();
                    }

                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }


    /* (non-Javadoc)
 * @see android.hardware.SensorEventListener#onAccuracyChanged(android.hardware.Sensor, int)
 */
    @Override
    public void onAccuracyChanged(Sensor arg0, int arg1) {

    }

    public boolean updateShakeSignal(double filt_acc_x, double filt_acc_y, double filt_acc_z) {

        buffer.add(new double[]{filt_acc_x, filt_acc_y, filt_acc_z});

        //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        double shakeSignalMagnitude = Math.sqrt(filt_acc_x * filt_acc_x + filt_acc_y * filt_acc_y + filt_acc_z * filt_acc_z);
        if (shakeSignalQueue.size() >= 100) {
            shakeSignalQueue.poll();
            shakeSignalQueue.add(shakeSignalMagnitude);
        } else {
            shakeSignalQueue.add(shakeSignalMagnitude);
        }

        return true;
    }

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    private void sendshakeSignal() {
        //DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        //Date date = new Date();
        //String currentTimeStamp = dateFormat.format(date); //2014/08/06 15:59:48

        long currentTime = System.currentTimeMillis();
        String currentTimeStamp = "" + currentTime;
        Log.d("Shake", "Prepare to send" + currentTimeStamp);
        if(lastSendTime == 0 || Math.abs(currentTime - lastSendTime) > 5000){
            Log.d("Shake", "Going to send last: " + lastSendTime);
            //sendMessage(currentTimeStamp);
            new SendActivityPhoneMessage("ID" + "--" + currentTimeStamp,"").start();
            Log.d("Shake", "Sent" + shakeSignalQueue.size());
            lastSendTime = currentTime;
        }
//        sendMessage(currentTimeStamp);
//        stopAccelerometer();
//        SystemClock.sleep(3000);
//        startAccelerometer();

        /**
         * save raw data to a file
         */
        String record ="";
        LinkedList<Double> copy = new LinkedList<Double>(shakeSignalQueue);
        for (Double signal : copy) {
            record  = record + signal + "\n";

        }
        //try {
        //    shakeSignalOutputStream.write(record.getBytes());
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}
    }



    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        Log.d("WearToPhone", "I am in send accept message method!");
        if(intent!=null && intent.getStringExtra("methodName")!=null && intent.getStringExtra("methodName").equals("sendAcceptMessage")){
            //sendAcceptMessage(intent.getStringExtra("friend_id"));
            new SendActivityPhoneMessage("AcceptSignal" + "--" + intent.getStringExtra("friend_id"),"").start();
        }
    }

    class SendActivityPhoneMessage extends Thread {
        String path;
        String message;

        // Constructor to send a message to the data layer
        SendActivityPhoneMessage(String p, String msg) {
            path = p;
            message = msg;
        }

        public void run() {
            //NodeApi.GetLocalNodeResult nodes = Wearable.NodeApi.getLocalNode(mGoogleApiClient).await();
            mGoogleApiClient.connect();
            NodeApi.GetConnectedNodesResult nodes =
                    Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
            //Node node = nodes.getNode();
            //Collection<String> nodes = getNodes();
            for (Node node : nodes.getNodes()) {
                Log.v("Wearable", "Activity Node is : " + node.getId() + " - " + node.getDisplayName());
                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), path, message.getBytes()).await();
                if (result.getStatus().isSuccess()) {
                    Log.v("Wearable", "Activity Message: {" + message + "} sent to: " + node.getDisplayName());
                } else {
                    // Log an error
                    Log.v("Wearable", "ERROR: failed to send Activity Message");
                }
            }

        }
    }

    /*private void sendAcceptMessage(String friend_id) {
        mGoogleApiClient.connect();
        NodeApi.GetConnectedNodesResult nodes =
                Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
        for (Node node : nodes.getNodes()) {
            Log.d("WearToPhone", "I am in send accept message method!");
            Log.d("WearToPhone", "-- " + mGoogleApiClient.isConnected());
            if (node != null && mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                Log.d("WearToPhone", "-- " + mGoogleApiClient.isConnected());
                Wearable.MessageApi.sendMessage(
                        mGoogleApiClient, node.getId(), "AcceptSignal" + "--" + friend_id, null).setResultCallback(

                        new ResultCallback<MessageApi.SendMessageResult>() {
                            @Override
                            public void onResult(MessageApi.SendMessageResult sendMessageResult) {

                                if (!sendMessageResult.getStatus().isSuccess()) {
                                    Log.e("WearToPhone", "Failed to send message with status code: "
                                            + sendMessageResult.getStatus().getStatusCode());
                                }
                            }
                        }
                );
            }
        }
    }*/
}
