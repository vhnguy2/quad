package com.quad;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.quad.model.ConversationMessage;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

public class ConversationListAdapter extends
    RecyclerView.Adapter<ConversationListAdapter.MessageVH> {

  private final String mLoggedInUserId;
  private List<ConversationMessage> mConversationMessageList;
  private Map<String, ConversationMessage> mKeyToMessageMap;

  private enum RowType {
    ME, OTHER
  }

  public ConversationListAdapter() {
    mConversationMessageList = Lists.newArrayList();
    mKeyToMessageMap = Maps.newHashMap();
    mLoggedInUserId = QuadApplication.getInstance().getUserId();
  }

  /**
   * TODO(viet): remove this and properly use limitToLast() and startAt() to properly handle
   *             cold boots and onresume() properly
   */
  public void clear() {
    mConversationMessageList.clear();
    mKeyToMessageMap.clear();
  }

  public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
    ConversationMessage message = dataSnapshot.getValue(ConversationMessage.class);
    mKeyToMessageMap.put(dataSnapshot.getKey(), message);

    // Insert into the correct location, based on previousChildName
    if (previousChildName == null) {
      mConversationMessageList.add(0, message);
    } else {
      ConversationMessage previousMessage = mKeyToMessageMap.get(previousChildName);
      int previousIndex = mConversationMessageList.indexOf(previousMessage);
      int nextIndex = previousIndex + 1;
      if (nextIndex == mConversationMessageList.size()) {
        mConversationMessageList.add(message);
      } else {
        mConversationMessageList.add(nextIndex, message);
      }
    }

    notifyDataSetChanged();
  }

  public void onChildChanged(DataSnapshot dataSnapshot, String s) {
    // One of the mModels changed. Replace it in our list and name mapping
    String messageKey = dataSnapshot.getKey();
    ConversationMessage oldMessage = mKeyToMessageMap.get(messageKey);
    ConversationMessage newMessage = dataSnapshot.getValue(ConversationMessage.class);
    int index = mConversationMessageList.indexOf(oldMessage);

    mConversationMessageList.set(index, newMessage);
    mKeyToMessageMap.put(messageKey, newMessage);

    notifyDataSetChanged();
  }

  public void onChildRemoved(DataSnapshot dataSnapshot) {

  }

  public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {

  }

  @Override
  public MessageVH onCreateViewHolder(ViewGroup parent, int viewType) {
    if (viewType == RowType.ME.ordinal()) {
      View v = LayoutInflater
        .from(parent.getContext())
        .inflate(R.layout.me_message_row_view, parent, false);
      return new MessageVH(v);
    } else {
      View v = LayoutInflater
        .from(parent.getContext())
        .inflate(R.layout.message_row_view, parent, false);
      return new MessageVH(v);
    }
  }

  @Override
  public int getItemViewType(int position) {
    if (isMe(mConversationMessageList.get(position).getUserId())) {
      return RowType.ME.ordinal();
    } else {
      return RowType.OTHER.ordinal();
    }
  }

  @Override
  public void onBindViewHolder(MessageVH holder, int position) {
    holder.bindData(mConversationMessageList.get(position));
  }

  @Override
  public int getItemCount() {
    return mConversationMessageList.size();
  }

  private boolean isMe(String userId) {
    return StringUtils.equals(mLoggedInUserId, userId);
  }

  public static class MessageVH extends RecyclerView.ViewHolder {
    public TextView senderView;
    public TextView bodyView;

    public MessageVH(View v) {
      super(v);
      senderView = (TextView) v.findViewById(R.id.message_sender);
      bodyView = (TextView) v.findViewById(R.id.message_body);
    }

    void bindData(ConversationMessage conversationMessage) {
      bodyView.setText(conversationMessage.getMessage());
      if (senderView != null) {
        senderView.setText(conversationMessage.getUserId());
      }
    }
  }
}
