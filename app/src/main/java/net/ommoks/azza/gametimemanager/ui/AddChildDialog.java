package net.ommoks.azza.gametimemanager.ui;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import net.ommoks.azza.gametimemanager.R;

import java.util.Objects;

public class AddChildDialog extends DialogFragment {

    public interface Listener {
        void onChildAdded(String name);
    }

    private Listener mListener;
    private TextInputEditText mTextInputEditText;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());

        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.input_box, null);
        TextInputLayout mTextInputLayout = view.findViewById(R.id.input_name);
        mTextInputEditText = (TextInputEditText) mTextInputLayout.getEditText();
        builder.setView(view)
                .setPositiveButton(R.string.ok, (dialogInterface, i) -> {
                    if (mListener != null) {
                        mListener.onChildAdded(Objects.requireNonNull(mTextInputEditText.getText()).toString());
                    }
                    dialogInterface.dismiss();
                })
                .setNegativeButton(R.string.cancel, (dialogInterface, i) -> dialogInterface.dismiss());

        return builder.create();
    }

    @Override
    public void onResume() {
        super.onResume();
        mTextInputEditText.requestFocus();
    }

    public AddChildDialog setListener(Listener listener) {
        mListener = listener;
        return this;
    }
}
