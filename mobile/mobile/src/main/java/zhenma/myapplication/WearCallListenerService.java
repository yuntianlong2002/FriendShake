package zhenma.myapplication;

/**
 * Created by RaymiGaga on 2/29/16.
 */

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ServerValue;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Wasim on 08-05-2015.
 */
public class WearCallListenerService extends WearableListenerService implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static String SERVICE_CALLED_WEAR = "WearListClicked";
    private Firebase myFirebaseRef;
    private long last_shake_time = 0;
    private static final String TAG = "PhoneService";
    GoogleApiClient mGoogleApiClient;
    GoogleApiClient mLocGoogleApiClient;
    public static final String CONFIG_START = "config/start";
    public static final String CONFIG_STOP = "config/stop";
    private String UID = "";
    private Location mLastLocation;


    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);

        if (null == mGoogleApiClient) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            Log.v(TAG, "GoogleApiClient created");
        }
        if (mLocGoogleApiClient == null) {
            mLocGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
            mLocGoogleApiClient.connect();
            Log.v(TAG, "Connecting to GoogleApiClient..");
        }

        myFirebaseRef = new Firebase("https://graphdata.firebaseio.com/");
        myFirebaseRef.child("shake").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                final String potential_friend_id = snapshot.getValue(String.class);
                long friend_shake_time = Long.parseLong(snapshot.getKey());
                //ServerValue.TIMESTAMP;
                if (Math.abs(friend_shake_time - last_shake_time) < 10000 && !UID.equals(potential_friend_id)) {

                    //.child("friendlist").child(potential_friend_id).
                    myFirebaseRef.child("vertex").child(UID).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            boolean already_exist = snapshot.child("friendlist").child(potential_friend_id).exists();
                            System.out.println("Sh"+"data changes");
                            if (already_exist == true) {
                                System.out.println("Sh" + "already exist");
                                myFirebaseRef.child("vertex").child(potential_friend_id).child("label").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(DataSnapshot snapshot) {
                                        System.out.println("Sh"+"read name ");
                                        String name = snapshot.getValue(String.class);
                                        new SendActivityPhoneMessage("OLD_FRIEND" + "--" + potential_friend_id + "--" +name,"").start();
                                    }
                                    @Override
                                    public void onCancelled(FirebaseError error) {
                                    }
                                });


                                //new SendActivityPhoneMessage("OLD_FRIEND" + "--" + potential_friend_id + "--" +name,"").start();
                            } else {
                                Firebase newRef = myFirebaseRef.child("vertex").child(UID).child("friendlist").child(potential_friend_id);
                                newRef.setValue("1");
                                if (potential_friend_id.compareTo(UID) > 0) {
                                    Firebase newRef_link = myFirebaseRef.child("edge").push();
                                    //newRef_link.child("sourceId").setValue(potential_friend_id);
                                    //newRef_link.child("targetId").setValue(UID);
                                    Map<String, String> post1 = new HashMap<String, String>();
                                    post1.put("sourceId", potential_friend_id);
                                    post1.put("targetId", UID);
                                    newRef_link.setValue(post1);
                                }
                                new SendActivityPhoneMessage(CONFIG_STOP + "--" + potential_friend_id, "").start();
                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError error) {
                        }
                    });

                }
                //System.out.println("Author: " + newPost.getAuthor());
                //System.out.println("Title: " + newPost.getTitle());

            }

            @Override
            public void onChildRemoved(DataSnapshot snapshot) {

            }

            @Override
            public void onChildChanged(DataSnapshot snapshot, String previousChildKey) {

            }

            @Override
            public void onChildMoved(DataSnapshot snapshot, String previousChildKey) {

            }

            @Override
            public void onCancelled(FirebaseError error) {
            }

        });
    }

    @Override
    public void onDestroy() {

        Log.v(TAG, "Destroyed");

        if (null != mGoogleApiClient) {
            if (mGoogleApiClient.isConnected()) {
                mGoogleApiClient.disconnect();
                mLocGoogleApiClient.disconnect();
                Log.v(TAG, "GoogleApiClient disconnected");
            }
        }

        super.onDestroy();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.v(TAG, "onConnectionSuspended called");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.v(TAG, "onConnectionFailed called");
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.v(TAG, "onConnected called");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                mLocGoogleApiClient);
        if (mLastLocation != null) {
//            mLatitudeText.setText(String.valueOf(mLastLocation.getLatitude()));
//            mLongitudeText.setText(String.valueOf(mLastLocation.getLongitude()));
            Log.d("Latitude: ", ""+ mLastLocation.getLatitude());
            Log.d("Longitude: ", ""+ mLastLocation.getLongitude());
        }
    }

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);
        Log.v(TAG, "Data Changed");
    }


    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        super.onMessageReceived(messageEvent);

        String event = messageEvent.getPath();

        Log.d("Listclicked", event);

        String [] message = event.split("--");

        if (message[0].equals("ID")) {
            Log.d("WearToPhone: ", message[1]);
            //String time_str = ServerValue.TIMESTAMP.get(".sv");
            last_shake_time = System.currentTimeMillis();
            //Firebase newRef = myFirebaseRef.child("shake").child(message[1]);
            //last_shake_time = ServerValue.TIMESTAMP.get();
            Firebase newRef = myFirebaseRef.child("shake").child(Long.toString(last_shake_time));
            newRef.setValue(UID);
        } else if (message[0].equals(CONFIG_START)){
            UID = message[1];
            Log.d("Got UID: ", UID);
        } else if (message[0].equals("AcceptSignal")) {
            //message 1 is the target friend ID
            Firebase newRef = myFirebaseRef.child("vertex").child(UID).child("friendlist").child(message[1]);
            newRef.setValue("0");
        }
    }

    @Override
    public void onPeerConnected(Node peer) {
        super.onPeerConnected(peer);
        Log.v(TAG, "Peer Connected " + peer.getDisplayName());
    }

    @Override
    public void onPeerDisconnected(Node peer) {
        super.onPeerDisconnected(peer);
        Log.v(TAG, "Peer Disconnected " + peer.getDisplayName());
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
            NodeApi.GetConnectedNodesResult nodes =
                    Wearable.NodeApi.getConnectedNodes(mGoogleApiClient).await();
            //Node node = nodes.getNode();
            //Collection<String> nodes = getNodes();
            for (Node node : nodes.getNodes()) {
                Log.v(TAG, "Activity Node is : " + node.getId() + " - " + node.getDisplayName());
                MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), path, message.getBytes()).await();
                if (result.getStatus().isSuccess()) {
                    Log.v(TAG, "Activity Message: {" + message + "} sent to: " + node.getDisplayName());
                } else {
                    // Log an error
                    Log.v(TAG, "ERROR: failed to send Activity Message");
                }
            }

        }
    }

}
