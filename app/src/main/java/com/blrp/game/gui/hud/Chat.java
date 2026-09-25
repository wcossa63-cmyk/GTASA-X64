package com.blrp.game.gui.hud;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.text.Spanned;
import android.util.TypedValue;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.blrp.game.R;
import com.blrp.game.gui.util.Utils;
import com.blrp.launcher.storage.Storage;

import org.jetbrains.annotations.NotNull;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ALL")

public class Chat {
    public Activity activity;
    native void SendChatButton(int buttonID);
    static native void SendChatMessage(byte[] str);
    native void toggleNativeKeyboard(boolean toggle);
    native void nativeToggleInputState(boolean toggle);
    native void clickCameraMode();
    EditText chat_input;
    ConstraintLayout chat_input_layout;

    public LinearLayout hide_chat;
    private LinearLayout binder_butt;
    private LinearLayout camera_butt;
    private ImageButton send_butt;
    public ConstraintLayout chat_box;

    private int chatFontSize = 34;

    private RecyclerView chat;

    Chat.ChatAdapter adapter;
    ArrayList<Spanned> chat_lines = new ArrayList<>();

    private boolean bShowChat = true;
    private boolean bShowInput = false;

    private List<String> messageHistory = new ArrayList<>();
    private int currentHistoryIndex = -1;
    private boolean loadHistory = false;

    private ImageButton history_next;
    private ImageButton history_previous;

    public Chat(Activity activity) {
        this.activity = activity;

        chat_box = activity.findViewById(R.id.chat);
        binder_butt = activity.findViewById(R.id.binder_btn);
        binder_butt.setOnClickListener(view -> {
            binder_butt.animate()
                    .scaleX(0.9f).scaleY(0.9f)
                    .setDuration(60)
                    .withEndAction(() ->
                            binder_butt.animate()
                                    .scaleX(1f).scaleY(1f)
                                    .setDuration(100)
                                    .setInterpolator(new DecelerateInterpolator())
                                    .start()
                    ).start();
            toggleKeyboard(false);
        });
        camera_butt = activity.findViewById(R.id.camera_btn);
        camera_butt.setOnClickListener(view->{
            camera_butt.animate()
                    .scaleX(0.9f).scaleY(0.9f)
                    .setDuration(60)
                    .withEndAction(() ->
                            camera_butt.animate()
                                    .scaleX(1f).scaleY(1f)
                                    .setDuration(100)
                                    .setInterpolator(new DecelerateInterpolator())
                                    .start()
                    ).start();
            clickCameraMode();
        });
        hide_chat = activity.findViewById(R.id.hide_chat_btn);
        hide_chat.setOnClickListener(view -> {
            hide_chat.animate()
                    .scaleX(0.9f).scaleY(0.9f)
                    .setDuration(60)
                    .withEndAction(() ->
                            hide_chat.animate()
                                    .scaleX(1f).scaleY(1f)
                                    .setDuration(100)
                                    .setInterpolator(new DecelerateInterpolator())
                                    .start()
                    ).start();

            if (bShowChat) {
                hideTransaction();
            } else {
                showTransaction();
            }
            bShowChat = !bShowChat;
        });

        chat_input_layout = activity.findViewById(R.id.input_container);
        chat_input_layout.setVisibility(View.GONE);

        chat_input = activity.findViewById(R.id.input);
        chat_input.setShowSoftInputOnFocus(false);

        history_next = activity.findViewById(R.id.history_next);
        history_previous = activity.findViewById(R.id.history_previous);

        history_next.setVisibility(View.GONE);
        history_previous.setVisibility(View.GONE);

        send_butt = activity.findViewById(R.id.send_message_button);
        send_butt.setOnClickListener(view->{
            String text = chat_input.getText().toString().trim();

            if (!text.isEmpty()) {
                onInputEnd(text);
            } else {
                hideInput();
            }
        });

        history_next.setOnClickListener(view -> {
            if (currentHistoryIndex < messageHistory.size() - 1) {
                currentHistoryIndex++;
                insertText(chat_input, messageHistory.get(currentHistoryIndex));
                history_previous.setVisibility(View.VISIBLE);
                if (currentHistoryIndex == messageHistory.size() - 1) {
                    history_next.setVisibility(View.GONE);
                }
            }
        });

        history_previous.setOnClickListener(view -> {
            if (currentHistoryIndex > 0) {
                currentHistoryIndex--;
                insertText(chat_input, messageHistory.get(currentHistoryIndex));
            } else {
                chat_input.setText("");
                history_previous.setVisibility(View.GONE);
                currentHistoryIndex = -1;
            }
            if (currentHistoryIndex < messageHistory.size() - 1) {
                history_next.setVisibility(View.VISIBLE);
            }
        });

        chat_input.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEND) {
                onInputEnd(chat_input.getText().toString());
                return true;
            }
            return false;
        });

        chat = activity.findViewById(R.id.chat_messages);

        Storage.setInt("defaultChatHeight", chat.getMinimumHeight());

        int height = Storage.getInt("chatHeight");
        if (height > 100) {
            ConstraintLayout.LayoutParams layoutParams =
                    (ConstraintLayout.LayoutParams) chat.getLayoutParams();
            layoutParams.height = height;
            chat.setLayoutParams(layoutParams);
        }

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(activity);
        mLayoutManager.setStackFromEnd(true);
        chat.setLayoutManager(mLayoutManager);

        adapter = new ChatAdapter(activity, chat_lines);
        chat.setAdapter(adapter);
    }

    private void insertText(EditText input, String text) {
        input.setText(text);
        input.setSelection(text.length());
    }

    private void onChatInputEnd(String str) {
        try {
            String cleaned = str.replace("\n", "");
            Charset cp1251 = Charset.forName("windows-1251");
            SendChatMessage(cleaned.getBytes(cp1251));
        } catch (Exception e) {
            SendChatMessage(str.getBytes());
            e.printStackTrace();
        }
    }

    private void onInputEnd(String str) {
        onChatInputEnd(str);
        String text = chat_input.getText().toString();
        if (text.length() > 0) {
            messageHistory.add(0, text);
            chat_input.getText().clear();
            if (messageHistory.size() > 0) {
                history_next.setVisibility(View.VISIBLE);
            }
            saveMessageHistory();
            currentHistoryIndex = -1;
            history_previous.setVisibility(View.GONE);
        }
        chat_input.setText("");
        hideInput();
    }

    private void showTransaction() {
        activity.runOnUiThread(() -> {
            Display defaultDisplay = activity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            defaultDisplay.getSize(size);
            chat.clearAnimation();
            chat.setTranslationY(-size.y);
            chat.animate().setDuration(300L).translationY(0f).start();
            chat.animate().setDuration(300L).alpha(1f).start();
        });
    }

    private void hideTransaction() {
        activity.runOnUiThread(() -> {
            Display defaultDisplay = activity.getWindowManager().getDefaultDisplay();
            Point size = new Point();
            defaultDisplay.getSize(size);
            chat.clearAnimation();
            chat.animate().setDuration(300L).translationY(-size.y).start();
            chat.animate().setDuration(300L).alpha(0f).start();
        });
    }

    void showInput() {
        activity.runOnUiThread(() -> {
            if (bShowInput) {
                hideInput();
            }
            bShowInput = true;

            if (!loadHistory) {
                loadHistory = true;
                loadMessageHistory();
            }

            if (Storage.getBoolean("isAndroidKeyboard")) {
                chat_input_layout.setVisibility(View.VISIBLE);
                chat_input.requestFocus();
                nativeToggleInputState(true);
                InputMethodManager imm =
                        (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(chat_input, InputMethodManager.SHOW_IMPLICIT);
            } else {
                toggleNativeKeyboard(true);
            }
        });
    }

    void hideInput() {
        activity.runOnUiThread(() -> {
            if (!bShowInput) return;
            bShowInput = false;

            if (Storage.getBoolean("isAndroidKeyboard")) {
                View focus = activity.getCurrentFocus();
                if (focus != null) {
                    InputMethodManager imm =
                            (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(focus.getWindowToken(), 0);
                }
                nativeToggleInputState(false);
            } else {
                toggleNativeKeyboard(false);
            }

            chat_input_layout.setVisibility(View.GONE);
            chat_input.getText().clear();
        });
    }

    void toggleKeyboard(boolean toggle) {
        if (toggle) {
            showInput();
        } else {
            hideInput();
        }
    }

    private void saveMessageHistory() {
        SharedPreferences.Editor editor = activity
                .getSharedPreferences("ChatPrefs", 0).edit();
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < Math.min(messageHistory.size(), 10); i++) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(messageHistory.get(i).replace("\"", "\\\"")).append("\"");
        }
        sb.append("]");
        editor.putString("messageHistory", sb.toString());
        editor.apply();
    }

    private void loadMessageHistory() {
        SharedPreferences prefs = activity.getSharedPreferences("ChatPrefs", 0);
        String json = prefs.getString("messageHistory", null);
        if (json != null && !json.isEmpty()) {
            messageHistory = new ArrayList<>();
            String inner = json.trim();
            if (inner.startsWith("[") && inner.endsWith("]")) {
                inner = inner.substring(1, inner.length() - 1);
                for (String part : inner.split(",")) {
                    String s = part.trim().replaceAll("^\"|\"$", "").replace("\\\"", "\"");
                    if (!s.isEmpty()) messageHistory.add(s);
                }
            }
            if (!messageHistory.isEmpty()) {
                activity.runOnUiThread(() -> {
                    history_next.setVisibility(View.VISIBLE);
                    currentHistoryIndex = -1;
                });
            }
        }
    }

    public void ToggleChat(boolean toggle) {
        activity.runOnUiThread(() -> {
            if (toggle) {
                chat.setVisibility(View.VISIBLE);
            } else {
                chat.setVisibility(View.GONE);
            }
        });
    }

    public void AddChatMessage(String msg) {
        adapter.addItem(msg);
    }

    public void ChangeChatFontSize(int size) {
        activity.runOnUiThread(() -> {
            if (size == -1) {
                chatFontSize = (int) Math.ceil(27 * 1.3f);
            } else {
                chatFontSize = size;
            }
            adapter.notifyDataSetChanged();
        });
    }

    public void AddToChatInput(String msg) {
        activity.runOnUiThread(() -> insertText(chat_input, msg));
    }

    public void ToggleChatInput(boolean toggle) {
        if (toggle) {
            showInput();
        } else {
            hideInput();
        }
    }

    public void ClickChatj() {
        activity.runOnUiThread(() -> {
            if (!bShowInput) {
                showInput();
            } else {
                hideInput();
            }
        });
    }

    public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {

        private final LayoutInflater inflater;
        private final List<Spanned> chat_lines;

        ChatAdapter(Context context, List<Spanned> chat_lines) {
            this.chat_lines = chat_lines;
            this.inflater = LayoutInflater.from(context);
        }

        @NotNull
        @Override
        public ChatAdapter.ViewHolder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
            View view = inflater.inflate(R.layout.hud_chat_line, parent, false);
            view.setOnClickListener(view1 -> ClickChatj());
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ChatAdapter.ViewHolder holder, int position) {
            holder.chat_line_text.setTextSize(TypedValue.COMPLEX_UNIT_PX, chatFontSize);
            holder.chat_line_text.setText(chat_lines.get(position));
        }

        @Override
        public int getItemCount() {
            return chat_lines.size();
        }

        public List<Spanned> getItems() {
            return chat_lines;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            final TextView chat_line_text;

            ViewHolder(View view) {
                super(view);
                chat_line_text = view.findViewById(R.id.text);
            }
        }

        public void addItem(String item) {
            activity.runOnUiThread(() -> {
                if (this.chat_lines.size() > 40) {
                    this.chat_lines.remove(0);
                    notifyItemRemoved(0);
                }
                this.chat_lines.add(Utils.transfromColors(item));
                notifyItemInserted(this.chat_lines.size() - 1);

                if (chat.getScrollState() == RecyclerView.SCROLL_STATE_IDLE) {
                    chat.scrollToPosition(this.chat_lines.size() - 1);
                }
            });
        }
    }
}