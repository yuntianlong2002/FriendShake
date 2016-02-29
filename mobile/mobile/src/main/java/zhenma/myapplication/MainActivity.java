package zhenma.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kymjs.kjframe.KJActivity;
import org.kymjs.kjframe.KJHttp;
import org.kymjs.kjframe.http.HttpCallBack;
import org.kymjs.kjframe.http.HttpConfig;
import org.kymjs.kjframe.ui.BindView;
import org.kymjs.kjframe.utils.KJLoger;

import java.util.ArrayList;

import zhenma.myapplication.bean.Contact;
import zhenma.myapplication.widget.SideBar;

public class MainActivity extends KJActivity implements SideBar
        .OnTouchingLetterChangedListener, TextWatcher {

    @BindView(id = R.id.school_friend_member)
    private ListView mListView;
    private ContactAdapter mAdapter;
    private ArrayList<Contact> datas = new ArrayList<>();
    private ArrayList<String> ids = new ArrayList<>();
    private TextView mFooterView;
    private KJHttp kjh = null;
    private String id;
    private Firebase myFirebaseRef;

    @JsonIgnoreProperties({ "friendlist" })
    public static class UserProfile {
        private String birthYear;
        private String email;
        private String label;
        private String number;
        private String position;
        private String username;

        public String getBirthYear() {
            return birthYear;
        }
        public String getEmail() {
            return email;
        }
        public String getLabel() {
            return label;
        }
        public String getNumber() {
            return number;
        }
        public String getPosition() {
            return position;
        }
        public String getUsername() {
            return username;
        }
    }



    @Override
    public void setRootView() {
        setContentView(R.layout.activity_main);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Firebase.setAndroidContext(this);
        Intent intent = getIntent();
        id = "";
        id = intent.getStringExtra("UID");
        myFirebaseRef = new Firebase("https://graphdata.firebaseio.com/");

        myFirebaseRef.child("vertex").child(id).child("friendlist").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                System.out.println(snapshot.getValue());  //prints "Do you have data? You'll love Firebase."
                System.out.println("There are " + snapshot.getChildrenCount() + " blog posts");
                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    //Contact data = new Contact();
                    String friend_id = postSnapshot.getKey();
                    //UserProfile facts = postSnapshot.getValue(UserProfile.class);
                    //System.out.println(facts.getLabel());
                    //data.setName(facts.getLabel());
                    //data.setPinyin(facts.getLabel());
                    //data.setUrl(id);
                    //datas.add(data);
                    ids.add(friend_id);
                    System.out.println(friend_id);
                }
                parser(ids);
            }

            @Override
            public void onCancelled(FirebaseError error) {
            }
        });
//
//        // initWidget()
//        super.initWidget();
//        SideBar mSideBar = (SideBar) findViewById(R.id.school_friend_sidrbar);
//        TextView mDialog = (TextView) findViewById(R.id.school_friend_dialog);
//        EditText mSearchInput = (EditText) findViewById(R.id.school_friend_member_search_input);
//
//        mSideBar.setTextView(mDialog);
//        mSideBar.setOnTouchingLetterChangedListener(this);
//        mSearchInput.addTextChangedListener(this);
//
//        // 给listView设置adapter
//        mFooterView = (TextView) View.inflate(aty, R.layout.item_list_contact_count, null);
//        mListView.addFooterView(mFooterView);
//
//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
    }

    @Override
    public void initData() {
        super.initData();
        HttpConfig config = new HttpConfig();
        HttpConfig.sCookie = "oscid=V" +
                "%2BbmxZFR8UfmpvrBHAcVRKALrd72iPWknXaWDa5Is3veK7WsxTSWY80kRXB1LoH%2F4VJ" +
                "%2F9s7K3Kd9CwCC1CAV%2BnJ7T3ka0blF8vZojozhUdOYkq6D6Laldg%3D%3D; Domain=.oschina" +
                ".net; Path=/; ";
        config.cacheTime = 0;
        kjh = new KJHttp();
    }

    @Override
    public void initWidget() {
        super.initWidget();
        SideBar mSideBar = (SideBar) findViewById(R.id.school_friend_sidrbar);
        TextView mDialog = (TextView) findViewById(R.id.school_friend_dialog);
        EditText mSearchInput = (EditText) findViewById(R.id.school_friend_member_search_input);

        mSideBar.setTextView(mDialog);
        mSideBar.setOnTouchingLetterChangedListener(this);
        mSearchInput.addTextChangedListener(this);

        // 给listView设置adapter
        mFooterView = (TextView) View.inflate(aty, R.layout.item_list_contact_count, null);
        mListView.addFooterView(mFooterView);

        doHttp();
    }

    private void doHttp() {
        /*kjh.get("http://zb.oschina.net/action/zbApi/contacts_list?uid=863548", new HttpCallBack() {
            @Override
            public void onSuccess(String t) {
                super.onSuccess(t);
                parser(t);
            }
        });*/
    }

    private void parser(ArrayList<String> ids) {
        for (String friend_id: ids) {
            //final Contact data = new Contact();
            System.out.println(friend_id);
            myFirebaseRef.child("vertex").child(friend_id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    Contact data = new Contact();
                    UserProfile facts = snapshot.getValue(UserProfile.class);
                    System.out.println(facts.getLabel());
                    data.setName(facts.getLabel());
                    data.setPinyin(facts.getLabel());
                    datas.add(data);
                    mFooterView.setText(datas.size() + " friends");
                    mAdapter = new ContactAdapter(mListView, datas);
                    mListView.setAdapter(mAdapter);
                }
                @Override
                public void onCancelled(FirebaseError error) {
                }
            });

            //data.setUrl(id);
            //datas.add(data);
        }

    }

    private void parser(String json) {
        /*try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject object = array.optJSONObject(i);
                Contact data = new Contact();
                data.setName(object.optString("name"));
                data.setUrl(object.optString("portrait"));
                data.setId(object.optInt("id"));
                data.setPinyin(HanziToPinyin.getPinYin(data.getName()));
                datas.add(data);
            }
            mFooterView.setText(datas.size() + "位联系人");
            mAdapter = new ContactAdapter(mListView, datas);
            mListView.setAdapter(mAdapter);
        } catch (JSONException e) {
            KJLoger.debug("解析异常" + e.getMessage());
        }*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTouchingLetterChanged(String s) {
        int position = 0;
        // 该字母首次出现的位置
        if (mAdapter != null) {
            position = mAdapter.getPositionForSection(s.charAt(0));
        }
        if (position != -1) {
            mListView.setSelection(position);
        } else if (s.contains("#")) {
            mListView.setSelection(0);
        }
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        ArrayList<Contact> temp = new ArrayList<>(datas);
        for (Contact data : datas) {
            if (data.getName().contains(s) || data.getPinyin().contains(s)) {
            } else {
                temp.remove(data);
            }
        }
        if (mAdapter != null) {
            mAdapter.refresh(temp);
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }
}
