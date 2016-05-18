package com.quad;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.AdapterDataObserver;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.quad.model.ConversationMessage;
import com.quad.model.TypingIndicator;

import org.apache.commons.lang3.StringUtils;

public class ConversationActivityFragment extends Fragment {

  private Activity mActivity;
  private String mCurrentUserId;

  private Firebase mFirebaseRef;
  private Firebase mFirebaseTypingIndicatorRef;
  private ValueEventListener mConnectedListener;
  private ChildEventListener mMessageListener;
  private ChildEventListener mTypingIndicatorListener;

  // subviews
  private RecyclerView mRecyclerView;
  private EditText mComposeTextView;
  private TextView mSendButton;
  private TextView mTypingIndicator;

  private LinearLayoutManager mLayoutManager;
  private ConversationListAdapter mAdapter;

  public ConversationActivityFragment() {
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    mFirebaseRef = new Firebase(Constants.FIREBASE_URL).child("chat");
    mFirebaseTypingIndicatorRef = new Firebase(Constants.FIREBASE_URL).child("typing_indicator");

    mCurrentUserId = QuadApplication.getInstance().getUserId();
  }

  @Override
  public void onStart() {
    super.onStart();
    setupFbConnectionListener();
    setupFbMessageListener();
    mAdapter.registerAdapterDataObserver(new AdapterDataObserver() {
      @Override
      public void onChanged() {
        super.onChanged();
        // TODO(viet): detect is the user has scrolled away from the end position before deciding
        //             to do this to them!
        mRecyclerView.smoothScrollToPosition(mAdapter.getItemCount() - 1);
      }
    });
  }

  @Override
  public void onStop() {
    super.onStop();
    mFirebaseRef.getRoot().child(".info/connected").removeEventListener(mConnectedListener);
    mFirebaseRef.removeEventListener(mMessageListener);
    mFirebaseRef.removeEventListener(mTypingIndicatorListener);
    mAdapter.clear();
  }

  @Override
  public View onCreateView(
      LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_conversation, container, false);
    mComposeTextView = (EditText) v.findViewById(R.id.conversation_text_to_send);
    mRecyclerView = (RecyclerView) v.findViewById(R.id.conversation_list_view);
    mSendButton = (TextView) v.findViewById(R.id.conversation_compose_send_button);
    mTypingIndicator = (TextView) v.findViewById(R.id.conversation_typing_indicator);

    // use a linear layout manager
    mLayoutManager = new LinearLayoutManager(getActivity());
    mLayoutManager.setStackFromEnd(true);
    mRecyclerView.setLayoutManager(mLayoutManager);
    mAdapter = new ConversationListAdapter(mLayoutManager);
    mRecyclerView.setAdapter(mAdapter);

    setupListeners();

    return v;
  }

  @Override
  public void onAttach(Activity activity) {
    super.onAttach(activity);
    mActivity = activity;
  }

  private void setupListeners() {
    mSendButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        final String text = mComposeTextView.getText().toString();
        if (StringUtils.isEmpty(text)) {
          return;
        }

        sendMessage(text);
        mComposeTextView.setText("");
      }
    });

    mComposeTextView.addTextChangedListener(new TextWatcher() {
      @Override
      public void beforeTextChanged(CharSequence s, int start, int count, int after) {
      }

      @Override
      public void onTextChanged(CharSequence s, int start, int before, int count) {
      }

      @Override
      public void afterTextChanged(Editable s) {
        if (s.length() == 0) {
          mFirebaseTypingIndicatorRef.setValue(null);
        } else {
          mFirebaseTypingIndicatorRef.setValue(new TypingIndicator(mCurrentUserId));
        }
      }
    });

    mComposeTextView.setOnEditorActionListener(new EditText.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
          mSendButton.performClick();
          return true;
        }
        return false;
      }
    });
  }

  private void setupFbConnectionListener() {
    ValueEventListener fbConnectionListener = new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot dataSnapshot) {
        boolean connected = (Boolean) dataSnapshot.getValue();
        if (connected) {
          Toast.makeText(mActivity, "Connected to Firebase", Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(mActivity, "Disconnected from Firebase", Toast.LENGTH_SHORT).show();
        }
      }

      @Override
      public void onCancelled(FirebaseError firebaseError) {
        // No-op
      }
    };

    // Finally, a little indication of connection status
    mConnectedListener = mFirebaseRef
        .getRoot()
        .child(".info/connected")
        .addValueEventListener(fbConnectionListener);
  }

  private void setupFbMessageListener() {
    mMessageListener = mFirebaseRef.addChildEventListener(new ChildEventListener() {
      @Override
      public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
        mAdapter.onChildAdded(dataSnapshot, previousChildName);
      }

      @Override
      public void onChildChanged(DataSnapshot dataSnapshot, String s) {
        mAdapter.onChildChanged(dataSnapshot, s);
      }

      @Override
      public void onChildRemoved(DataSnapshot dataSnapshot) {
        mAdapter.onChildRemoved(dataSnapshot);
      }

      @Override
      public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
        mAdapter.onChildMoved(dataSnapshot, previousChildName);
      }

      @Override
      public void onCancelled(FirebaseError firebaseError) {
        Log.e("FirebaseListAdapter", "Listen was cancelled, no more updates will occur");
      }
    });

    mTypingIndicatorListener = mFirebaseTypingIndicatorRef.addChildEventListener(
        new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
              String userIdOfTyper = dataSnapshot.getValue(TypingIndicator.class).getUserId();
              if (StringUtils.equals(userIdOfTyper, mCurrentUserId)) {
                return;
              }
              mTypingIndicator.setVisibility(View.VISIBLE);
              mTypingIndicator.setText(String.format("%s is typing...", userIdOfTyper));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
              String userIdOfTyper = dataSnapshot.getValue(TypingIndicator.class).getUserId();
              if (StringUtils.equals(userIdOfTyper, mCurrentUserId)) {
                return;
              }
              mTypingIndicator.setVisibility(View.VISIBLE);
              mTypingIndicator.setText(String.format("%s is typing...", userIdOfTyper));
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
              mTypingIndicator.setText("");
              mTypingIndicator.setVisibility(View.GONE);
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
          }
    );
  }

  private void sendMessage(String message) {
    mFirebaseRef.push().setValue(new ConversationMessage(mCurrentUserId, message));
  }
}
