package io.camtact.android.view;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;
import java.util.List;

import io.camtact.android.R;
import io.camtact.android.adapter.MessageAdapter;
import io.camtact.android.model.dto.ChatRoom;
import io.camtact.android.mvp_interface.ChatMVP;
import io.camtact.android.presenter.ChatPresenter;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static io.camtact.android.presenter.ChatPresenter.isThreadActive;
import static io.camtact.android.presenter.ChatPresenter.timeCheckThread;

public class ChatActivity extends AppCompatActivity implements ChatMVP.View {

    ChatPresenter chatPresenter;

    private static final String TAG = "ChatActivity";

    private static final int IMAGE_REQUEST = 1;

    private TextView friendName, timeLimit;
    private EditText chatEdit;
    private ImageButton chatSendBtn, menuBtn, backBtn, closeBtn;

    private MessageAdapter messageAdapter;
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;

    private ProgressBar progressBar, progressCircle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setupMVP();

        if(chatPresenter.checkOnlineStatus(this)) {
            setContentView(R.layout.activity_chat);
            setupView();
            chatPresenter.getIntent(getIntent());
            chatPresenter.applyFriendNameAndReadMsg();
            chatPresenter.checkChatRoom();
        } else {
            setContentView(R.layout.activity_offline);
            setupOffline();
        }
    }

    private void setupMVP() {
        chatPresenter = new ChatPresenter(this);
    }

    private void setupView() {
        friendName = findViewById(R.id.friend_name);
        chatEdit = findViewById(R.id.chat_edit);
        chatSendBtn = findViewById(R.id.send_btn);
        menuBtn = findViewById(R.id.image_send_btn);
        backBtn = findViewById(R.id.back_btn);
        closeBtn = findViewById(R.id.close_btn);
        timeLimit = findViewById(R.id.time_limit);

        ArrayList<ChatRoom.Chat> chatList = new ArrayList<>();

        recyclerView = findViewById(R.id.chat_view);
        recyclerView.setHasFixedSize(true);
        linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        //show reverse recyclerView
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        messageAdapter = new MessageAdapter(this, chatList);
        recyclerView.setAdapter(messageAdapter);

        messageAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                //????????? ???????????? ????????? ???????????? ?????????
                recyclerView.scrollToPosition(messageAdapter.getItemCount() - 1);
            }
        });

        progressBar = findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);  //make invisible. On create, there is no progressing working.

        progressCircle = findViewById(R.id.progressbar_circle);


        //???????????? ????????? ????????????.  ?????? ??????????????? ????????? ????????????(??????????????????)
        /*//inputType??? ????????????????????? ????????? ????????? ???????????? ??????
        chatEdit.setRawInputType(InputType.TYPE_CLASS_TEXT);
        //enter btn in keyboard action listener
        chatEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                switch(actionId) {
                    case EditorInfo.IME_ACTION_SEND:
                        // ????????? ??????
                        String msg = chatEdit.getText().toString();

                        chatPresenter.checkMessage(msg);
                        chatEdit.setText("");
                        break;
                    default:
                        // ?????? ??????
                        break;
                }
                return false;
            }
        });*/

        chatSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String msg = chatEdit.getText().toString();

                if(chatPresenter.checkOnlineStatus(ChatActivity.this)) {
                    chatPresenter.checkMessage(msg);
                    chatEdit.setText("");
                } else {
                    setContentView(R.layout.activity_offline);
                    setupOffline();
                }
            }
        });

        menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openMenuDialog();
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showExitDialog();
            }
        });
    }

    private void setupOffline() {
        backBtn = findViewById(R.id.back_btn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    @Override
    public void setFriendNameText(String userName) {
        friendName.setText(userName);
    }

    @Override
    public void addMsg(ChatRoom.Chat chat) {
        messageAdapter.addItem(chat);
        progressCircle.setVisibility(View.GONE);
    }

    @Override
    public void removeAllMsg() {
        messageAdapter.removeAll();
    }

    @Override
    protected void onResume() {
        super.onResume();
        chatPresenter.seenMessage();
        chatPresenter.readMessage();
        chatPresenter.showLeftTime();
    }

    @Override
    protected void onPause() {
        super.onPause();
        chatPresenter.removeWholeListener();
        if(isThreadActive) {
            timeCheckThread.interrupt();
        }
    }

    private void openMenuDialog() {
        final CharSequence[] items = {"?????? ????????????", "????????????"};

        AlertDialog.Builder dialog = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
        dialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(which==0) {
                    //checkPermission();
                    if(chatPresenter.checkOnlineStatus(ChatActivity.this)) {
                        openImage();
                    } else {
                        setContentView(R.layout.activity_offline);
                        setupOffline();
                    }
                } else if(which==1) {
                    showReportDialog();
                }
            }
        }).show();
    }

    private void openImage() {

        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, IMAGE_REQUEST);
    }

    private void showReportDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
        dialog.setMessage("?????? ????????? ????????? ????????? ????????? ?????? ??? ????????????. ???????????? ?????????????????????????");
        dialog.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final CharSequence[] items = {"????????? ?????????", "??????, ?????? ???", "???????????? ??????", "?????? ??? ??????"};
                AlertDialog.Builder chooseDialog = new AlertDialog.Builder(ChatActivity.this, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
                chooseDialog.setTitle("?????? ????????? ??????????");
                chooseDialog.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(chatPresenter.checkOnlineStatus(ChatActivity.this)) {
                            chatPresenter.report(items[which].toString());
                            progressBar.setVisibility(View.VISIBLE);
                        } else {
                            setContentView(R.layout.activity_offline);
                            setupOffline();
                        }
                    }
                });
                chooseDialog.show();
            }
        });
        dialog.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialog.show();
    }

    private void showExitDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage("???????????? ???????????? ?????? ????????? ??? ????????????. ?????? ?????????????????????????");
        dialog.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if(chatPresenter.checkOnlineStatus(ChatActivity.this)) {
                    chatPresenter.deleteChatRoom();
                } else {
                    setContentView(R.layout.activity_offline);
                    setupOffline();
                }
            }
        });
        dialog.setNegativeButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        dialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        chatPresenter.getImage(requestCode, resultCode, data, IMAGE_REQUEST, this);
    }

    @Override
    public void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void showSnackBar(String message, int milliTime, boolean onTheTop) {
        Snackbar snackBar = Snackbar.make(findViewById(android.R.id.content), message, milliTime);
        if(onTheTop) {  //if the position of Snackbar is on the top
            View snackBarLayout = snackBar.getView();
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            // Layout must match parent layout type
            lp.setMargins(0, 300, 0, 0);
            // Margins relative to the parent view.
            snackBarLayout.setLayoutParams(lp);
        }
        snackBar.show();
    }

    @Override
    public void setLeftTime(String leftTime) {
        timeLimit.setText(leftTime);
    }

    @Override
    public void finishActivity(int reason) {
        Intent intent = new Intent(ChatActivity.this, MainActivity.class);
        switch(reason) {
            case 0 :
                intent.putExtra("reasonForFinish", 0);
                break;
            case 1 :
                intent.putExtra("reasonForFinish", 1);
                break;
            case 2 :
                intent.putExtra("reasonForFinish", 2);
                break;
            case 3 :
                intent.putExtra("reasonForFinish", 3);
                break;
            default :
                break;
        }
        intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }

    private void checkPermission() {

        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                if(chatPresenter.checkOnlineStatus(ChatActivity.this)) {
                    openImage();
                } else {
                    setContentView(R.layout.activity_offline);
                    setupOffline();
                }
            }

            @Override
            public void onPermissionDenied(List<String> deniedPermissions) {
                showSnackBar("???????????? ????????? ???????????? ??????????????? ??? ??? ????????????.", 3000, true);
            }

        };

        TedPermission.with(this)
                .setPermissionListener(permissionlistener)
                .setRationaleMessage("?????? ????????? ???????????? ???????????? ?????? ????????? ???????????????")
                .setDeniedMessage("??? ??????????????????...\n????????? [??????] > [??????] ?????? ????????? ????????? ??? ?????????")
                .setGotoSettingButtonText("?????? ???????????? ??????")
                .setPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
    }

}



















