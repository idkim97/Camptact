package io.camtact.android.view.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.regex.Pattern;

import io.camtact.android.R;
import io.camtact.android.mvp_interface.SetMVP;
import io.camtact.android.presenter.SetPresenter;
import io.camtact.android.view.AuthActivity;

public class SetFragment extends Fragment implements SetMVP.View {

    private SetPresenter setPresenter;

    private Button askBtn, errorReportBtn, leaveBtn;
    private ImageButton editNameImgBtn;
    private TextView editNameText, contactText;
    private RelativeLayout editNameLayout;
    private ProgressBar progressBar;

    private InputMethodManager imm;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v;

        setupMVP();

        if(getContext() != null && setPresenter.checkOnlineStatus(getContext())) {
            v = inflater.inflate(R.layout.fragment_set, container, false);
            setupView(v);
            setPresenter.getUserName();
            initAd(v);
        } else {
            v = inflater.inflate(R.layout.fragment_offline, container, false);
        }

        return v;
    }

    private void setupMVP() {
        setPresenter = new SetPresenter(this);
    }

    private void setupView(View v) {
        editNameLayout = v.findViewById(R.id.edit_name_layout);
        editNameText = v.findViewById(R.id.edit_name_text);
        editNameImgBtn = v.findViewById(R.id.edit_name_img_btn);
        askBtn = v.findViewById(R.id.ask_btn);
        errorReportBtn = v.findViewById(R.id.error_report_btn);
        leaveBtn = v.findViewById(R.id.leave_btn);
        contactText = v.findViewById(R.id.contact_text);
        progressBar = v.findViewById(R.id.progressbar);
        progressBar.setVisibility(View.GONE);

        if(getContext() != null) {
            imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        }

        editNameImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog();
            }
        });

        editNameLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showEditDialog();
            }
        });

        askBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAskDialog();
            }
        });

        errorReportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showReportDialog();
            }
        });

        leaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertDialog();
            }
        });

        contactText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                Intent email = new Intent(Intent.ACTION_SEND);
                email.setType("plain/text");
                // email setting ????????? ????????? ?????? ?????? ??????
                String[] address = {"eskapizmy@gmail.com"};
                email.putExtra(Intent.EXTRA_EMAIL, address);
                email.putExtra(Intent.EXTRA_SUBJECT,"camtact Chat ????????? ????????????.");
                if(firebaseUser != null) {
                    email.putExtra(Intent.EXTRA_TEXT, "????????? uid : " + firebaseUser.getUid() + "\n");
                }
                startActivity(email);
            }
        });

    }

    private void initAd(View v) {

        MobileAds.initialize(getActivity(), new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });
        AdView mAdView = v.findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);
    }

    private void showEditDialog() {
        if(getContext() != null) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
            dialog.setTitle("????????? ????????????");
            dialog.setMessage("???????????? ???????????? ????????? ????????? ????????? ?????? ??? ????????????.");
            // EditText ????????????
            final EditText editText = new EditText(getContext());
            editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
            dialog.setView(editText);
            dialog.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String userName = editText.getText().toString();
                    if (!Pattern.matches("^[0-9a-zA-Z???-???]*$", userName)) {
                        showSnackBar("??????, ??????, ?????? ????????? ??????(??????,?????? ?????? ??????)??? ??? ??? ????????????. ", 3000, true);
                    } else if (userName.length() < 2 || userName.length() > 12) {
                        showSnackBar("?????? ?????? 2~12??? ????????? ?????????.", 3000, true);
                    } else {
                        if (getContext() != null && setPresenter.checkOnlineStatus(getContext())) {
                            setPresenter.editUserName(userName);
                            progressBar.setVisibility(View.VISIBLE);
                            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        } else {
                            showSnackBar("???????????? ?????? ????????? ?????? ????????????. ?????? ??? ?????? ??????????????????.", 3000, true);
                            imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        }
                    }

                }
            });
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Log.e("SetFragment", "cancel");
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                }
            });
            dialog.show();
            editText.requestFocus();
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    private void showAskDialog() {
        if(getContext() != null) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
            dialog.setTitle("?????? ??? ??????                                          ");
            // EditText ????????????
            final EditText editText = new EditText(getContext());
            editText.setMaxLines(7);
            dialog.setView(editText);
            dialog.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (getContext() != null && setPresenter.checkOnlineStatus(getContext())) {
                        setPresenter.sendInquiry(editText.getText().toString());
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        progressBar.setVisibility(View.VISIBLE);
                    } else {
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        showSnackBar("???????????? ?????? ????????? ?????? ????????????. ?????? ??? ?????? ??????????????????.", 3000, true);
                    }

                }
            });
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Log.e("SetFragment", "cancel");
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                }
            });

            dialog.show();
            editText.requestFocus();
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    private void showReportDialog() {
        if(getContext() != null) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
            dialog.setTitle("?????? ?????????");
            dialog.setMessage("?????? '??????'??? ?????? '??????'?????? '?????????' ????????? ??????????????? ????????? ?????????????????? ?????????????????????!");
            // EditText ????????????
            final EditText editText = new EditText(getContext());
            editText.setMaxLines(7);
            dialog.setView(editText);
            dialog.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (getContext() != null &&  setPresenter.checkOnlineStatus(getContext())) {
                        setPresenter.sendError(editText.getText().toString());
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        progressBar.setVisibility(View.VISIBLE);
                    } else {
                        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        showSnackBar("???????????? ?????? ????????? ?????? ????????????. ?????? ??? ?????? ??????????????????.", 3000, true);
                    }
                }
            });
            dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    Log.e("SetFragment", "cancel");
                    imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                }
            });

            dialog.show();
            editText.requestFocus();
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    }

    private void showAlertDialog() {
        if(getContext() != null) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
            dialog.setMessage("????????? ?????? ?????? ?????? ????????? ???????????????. \n ?????? ?????????????????????????");
            dialog.setPositiveButton("??????", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (getContext() != null && setPresenter.checkOnlineStatus(getContext())) {
                        progressBar.setVisibility(View.VISIBLE);
                        setPresenter.outOfMembership();
                    } else {
                        showSnackBar("???????????? ?????? ????????? ?????? ????????????. ?????? ??? ?????? ??????????????????.", 3000, true);
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
    }

    @Override
    public void showLogoutDialog() {
        if(getContext() != null) {
            AlertDialog.Builder dialog = new AlertDialog.Builder(getContext(), android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar);
            dialog.setMessage("?????? ????????? ?????? ????????? ????????? ????????? ????????? ???????????? ????????? ???????????? ?????? ????????? ?????????.\n" +
                    "??????????????? ??????????????? ???????????????. ????????? ?????? ???????????? ??? ???????????? ???????????? ????????????.");
            dialog.setPositiveButton("????????????", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (getContext() != null && setPresenter.checkOnlineStatus(getContext())) {
                        setPresenter.logout(false);
                    } else {
                        showSnackBar("???????????? ?????? ????????? ?????? ????????????. ?????? ??? ?????? ??????????????????.", 3000, true);
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
    }

    @Override
    public void showSnackBar(String message, int milliTime, boolean onTheTop) {
        if(getActivity() != null) {
            Snackbar snackbar = Snackbar.make(getActivity().findViewById(android.R.id.content), message, milliTime);
            if (onTheTop) {  //if the position of Snackbar is on the top
                View snackBarLayout = snackbar.getView();
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                // Layout must match parent layout type
                lp.setMargins(0, 300, 0, 0);
                // Margins relative to the parent view.
                snackBarLayout.setLayoutParams(lp);
            }
            snackbar.show();
        }
    }

    @Override
    public void setUserName(String userName) {
        editNameText.setText("??? ????????? : "+userName);
    }

    @Override
    public void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void goAuthActivity(boolean isLeaving) {
        Intent intent = new Intent(getContext(), AuthActivity.class);
        if(isLeaving) {
            intent.putExtra("leave", true);
        }
        startActivity(intent);
        if(getActivity() != null)
        getActivity().finish();
    }
}
