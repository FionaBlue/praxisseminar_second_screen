package com.wildLive.secondScreen;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class DialogHandler extends DialogFragment {

    // registering interface for sending input data (connection id) to current activity
    public interface OnInputListener {
        void sendInput(String input, int buttonId);
        void sendInput(int buttonId);
    }
    public OnInputListener onInputListener;

    // dialog title item
    private TextView dialogTitle;
    String title;

    // dialog description items
    private TextView dialogInstruction;
    private EditText dialogInputField;
    String instruction;
    boolean inputField;

    // dialog action buttons
    private Button dialogPositiveButton, dialogNegativeButton;
    String posButton, negButton;
    boolean hasPosButton, hasNegButton;
    int POS_BUTTON_ID = 1;                  // id for interface sending button code whether positive button was clicked
    int NEG_BUTTON_ID = 0;                  // id for interface sending button code whether negative button was clicked

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // binding view to fundamental dialog xml layout
        View dialogLayoutView = inflater.inflate(R.layout.dialog, container, false);

        // identifying and registering ui components
        registerComponents(dialogLayoutView);

        // setting new dialog values
        setComponentData();

        // setting action buttons behaviour
        setActionButtonBehaviour();

        return dialogLayoutView;
    }

    // setter before view creation
    public void setDialogTitle(String dialogTitle) {
        title = dialogTitle;
    }
    public void setDialogDescription(String dialogInstruction, boolean hasInputField) {
        instruction = dialogInstruction;
        inputField = hasInputField;
    }
    public void setDialogActionButtons(String positiveButton, boolean hasPositiveButton, String negativeButton, boolean hasNegativeButton) {
        posButton = positiveButton;
        hasPosButton = hasPositiveButton;
        negButton = negativeButton;
        hasNegButton = hasNegativeButton;
    }

    // identifying and registering ui components
    private void registerComponents(View dialogLayoutView) {
        dialogTitle = (TextView) dialogLayoutView.findViewById(R.id.dialogTitle);
        dialogInputField = (EditText) dialogLayoutView.findViewById(R.id.dialogInputField);
        dialogInstruction = (TextView) dialogLayoutView.findViewById(R.id.dialogInstruction);
        dialogPositiveButton = (Button) dialogLayoutView.findViewById(R.id.dialogPositiveButton);
        dialogNegativeButton = (Button) dialogLayoutView.findViewById(R.id.dialogNegativeButton);
    }

    // setting new dialog values
    private void setComponentData() {
        dialogTitle.setText(title);
        dialogInstruction.setText(instruction);
        // setting input text field if available
        if (inputField == false) {
            dialogInputField.setVisibility(View.GONE);
        }
        // setting positive button if available
        if (hasPosButton) {
            dialogPositiveButton.setText(posButton);
        } else {
            dialogPositiveButton.setVisibility(View.GONE);
        }
        // setting negative button if available
        if (hasNegButton) {
            dialogNegativeButton.setText(negButton);
        } else {
            dialogNegativeButton.setVisibility(View.GONE);
        }
    }

    // setting action buttons behaviour
    private void setActionButtonBehaviour() {
        // setting negative button behaviour for closing dialog
        if (hasNegButton == true) {
            dialogNegativeButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // closing dialog
                    onInputListener.sendInput(NEG_BUTTON_ID);
                    getDialog().dismiss();
                }
            });
        }
        // setting positive button behaviour (e.g. sending input text, closing dialog, etc.)
        if (hasPosButton == true) {
            dialogPositiveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // sending input text via interface to current activity
                    if (inputField == true) {

                        // check if input text is not empty
                        String input = dialogInputField.getText().toString();
                        if (!input.equals("")) {
                            onInputListener.sendInput(input, POS_BUTTON_ID);
                            dialogInputField.setText("");
                            // closing dialog
                            getDialog().dismiss();
                        }
                    } else {
                        onInputListener.sendInput(POS_BUTTON_ID);
                        // closing dialog
                        getDialog().dismiss();
                    }
                }
            });
        }
    }

    // attaching to current activity context for further input processing
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            onInputListener = (OnInputListener) getActivity();
        } catch (ClassCastException e) {
            System.out.println(e.getMessage());
        }
    }
}
