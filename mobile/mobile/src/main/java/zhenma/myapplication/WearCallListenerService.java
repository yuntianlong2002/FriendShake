package zhenma.myapplication;

/**
 * Created by RaymiGaga on 2/29/16.
 */
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

/**
 * Created by Wasim on 08-05-2015.
 */
public class WearCallListenerService extends WearableListenerService implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    public static String SERVICE_CALLED_WEAR = "WearListClicked";
    private Firebase myFirebaseRef;
    private long last_shake_time = 0;
    private static final String TAG = "PhoneService";
    GoogleApiClient mGoogleApiClient;
    public static final String CONFIG_START = "config/start";
    public static final String CONFIG_STOP = "config/stop";
    private String UID = "";


    @Override
    public void onCreate(){
        super.onCreate();
        Firebase.setAndroidContext(this);

        if(null == mGoogleApiClient) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(Wearable.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
            Log.v(TAG, "GoogleApiClient created");
        }

        if(!mGoogleApiClient.isConnected()){
            mGoogleApiClient.connect();
            Log.v(TAG, "Connecting to GoogleApiClient..");
        }

        myFirebaseRef = new Firebase("https://graphdata.firebaseio.com/");
        myFirebaseRef.child("shake").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot snapshot, String previousChildKey) {
                String potential_friend_id = snapshot.getValue(String.class);
                long friend_shake_time = Long.parseLong(snapshot.getKey());
                if(Math.abs(friend_shake_time - last_shake_time)<10000 && !UID.equals(potential_friend_id)){
                    Firebase newRef = myFirebaseRef.child("vertex").child(UID).child("friendlist").child(potential_friend_id);
                    newRef.setValue("1");
                    Firebase newRef_link = myFirebaseRef.child("edge").push();
                    if(potential_friend_id.compareTo(UID)>0) {
                        newRef_link.child("sourceId").setValue(potential_friend_id);
                        newRef_link.child("targetId").setValue(UID);
                    }
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

        if(null != mGoogleApiClient){
            if(mGoogleApiClient.isConnected()){
                //mGoogleApiClient.disconnect();
                //Log.v(TAG, "GoogleApiClient disconnected");
            }
        }

        super.onDestroy();
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Log.v(TAG,"onConnectionSuspended called");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.v(TAG,"onConnectionFailed called");
    }

    @Override
    public void onConnected(Bundle connectionHint) {
        Log.v(TAG,"onConnected called");

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
            last_shake_time = Long.parseLong(message[1]);
            Firebase newRef = myFirebaseRef.child("shake").child(message[1]);
            newRef.setValue(UID);
        } else if (message[0].equals(CONFIG_START)){
            UID = message[1];
            Log.d("Got UID: ", UID);
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

}
