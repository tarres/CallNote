package com.rohan.callnote.fragments;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rohan.callnote.BaseCallNoteFragment;
import com.rohan.callnote.R;
import com.rohan.callnote.models.Note;
import com.rohan.callnote.network.ApiClient;
import com.rohan.callnote.network.ApiResponse;
import com.rohan.callnote.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * A simple {@link Fragment} subclass.
 */
public class AddNoteFragment extends BaseCallNoteFragment {

    @BindView(R.id.add_note_call_name_text_view)
    TextView mCallNameTextView;
    @BindView(R.id.add_note_call_number_text_view)
    TextView mCallNumberTextView;
    @BindView(R.id.add_note_edit_text)
    EditText mNoteEditText;
    @BindView(R.id.add_note_call_date_text_view)
    TextView mCallDateTextView;
    @BindView(R.id.add_note_call_type_image_view)
    ImageView mCallTypeImageView;

    @BindView(R.id.add_note_save_button)
    Button mSaveButton;
    @BindView(R.id.add_note_cancel_button)
    Button mCancelButton;

    String name;
    String number;
    int callType;
    long timestamp;
    boolean directlyFromCall = false;

    public AddNoteFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_add_note, container, false);
        ButterKnife.bind(this, v);

        mNoteEditText.requestFocus();
        Bundle b = getArguments();

        InputMethodManager imm = (InputMethodManager) getBaseCallNoteActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        if (b != null) {
            name = b.getString(getString(R.string.name_key));
            number = b.getString(getString(R.string.number_key));
            callType = b.getInt(getString(R.string.callType_key));
            timestamp = b.getLong(getString(R.string.date_key));
            directlyFromCall = b.getBoolean(Constants.ADD_NOTE_DIRECTLY_FROM_CALL);
        }

        //set caller name
        if (name == null) {
            mCallNameTextView.setText(number);
        } else {
            mCallNameTextView.setText(name);
        }

        if (callType == Constants.CALL_MISSED) {
            mCallTypeImageView.setImageResource(R.drawable.ic_call_missed_red_300_18dp);
        } else if (callType == Constants.CALL_RECEIVED) {
            mCallTypeImageView.setImageResource(R.drawable.ic_call_received_blue_300_18dp);
        } else {
            mCallTypeImageView.setImageResource(R.drawable.ic_call_made_green_300_18dp);
        }

        //set call number
        mCallNumberTextView.setText(number);

        //set call date
        Date date = new Date(timestamp);
        SimpleDateFormat formatter = new SimpleDateFormat("EEE, MMM d, HH:mm");
        String dateString = formatter.format(date);
        mCallDateTextView.setText(dateString);

        return v;
    }

    @OnClick(R.id.add_note_save_button)
    public void saveButtonClicked() {

        if (!getBaseCallNoteActivity().isNetworkConnected()) {
            Toast.makeText(getBaseCallNoteActivity(), getString(R.string
                    .please_connect_to_the_internet_toast), Toast.LENGTH_LONG).show();
            return;
        }

        getBaseCallNoteActivity().showProgressDialog(getString(R.string.saving_note));

        String noteText = mNoteEditText.getText().toString();

        if (noteText.isEmpty() || noteText.equals("")) {
            Toast.makeText(getBaseCallNoteActivity(), getString(R.string.note_cannot_be_empty),
                    Toast
                    .LENGTH_SHORT).show();
            return;
        }

        Call<ApiResponse<Note>> call = ApiClient.getApiService().addNote(number, noteText, callType);
        call.enqueue(new Callback<ApiResponse<Note>>() {
            @Override
            public void onResponse(Call<ApiResponse<Note>> call, Response<ApiResponse<Note>> response) {
                if (response.isSuccessful()) {
                    getBaseCallNoteActivity().switchFragment(new NotesFragment(), false, NotesFragment.class.getSimpleName());
                    getBaseCallNoteActivity().dismissProgressDialog();
                    getBaseCallNoteActivity().updateWidget();
                } else {
                    Toast.makeText(getBaseCallNoteActivity(), getString(R.string
                            .unable_to_save_note_toast), Toast.LENGTH_SHORT).show();
                    getBaseCallNoteActivity().switchFragment(new NotesFragment(), NotesFragment.class.getSimpleName());
                    getBaseCallNoteActivity().dismissProgressDialog();
                }
            }

            @Override
            public void onFailure(Call<ApiResponse<Note>> call, Throwable t) {
                Toast.makeText(getBaseCallNoteActivity(), getString(R.string
                        .unable_to_save_note_toast), Toast.LENGTH_SHORT).show();
                getBaseCallNoteActivity().dismissProgressDialog();

                getBaseCallNoteActivity().switchFragment(new NotesFragment(), NotesFragment.class.getSimpleName());
            }
        });

    }

    @OnClick(R.id.add_note_cancel_button)
    public void cancelButtonClicked() {

        if (directlyFromCall) {
            getBaseCallNoteActivity().finish();
            return;
        }

        getBaseCallNoteActivity().switchFragment(new NotesFragment(), NotesFragment.class.getSimpleName());
    }
}
