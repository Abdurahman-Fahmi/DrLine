package com.wecareapp.android.utilities;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.wecareapp.android.R;

import java.util.ArrayList;

/**
 * Created by uday kiran on 10-04-2017.
 */

public class PopUtilities {

    public static Dialog dialog;

    public static void dialogListShow(Context mContext, ArrayList arrayList, String mTitle, final EditText mEditText) {
        if (Utility.INSTANCE.isMarshmallowOS()) {
            dialog = new Dialog(mContext, android.R.style.Theme_Material_Light_Dialog);
        } else {
            dialog = new Dialog(mContext);
        }
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawableResource(R.color.white);
        dialog.setContentView(R.layout.dialog_popup);
        dialog.setTitle(mTitle);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
// TODO Auto-generated method stub
//                Toast.makeText(RegisterActivity.this,
//                        "OnCancelListener",
//                        Toast.LENGTH_LONG).show();
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
// TODO Auto-generated method stub
//                Toast.makeText(RegisterActivity.this,
//                        "OnDismissListener",
//                        Toast.LENGTH_LONG).show();
            }
        });

//Prepare ListView in dialog
        ListView dialog_ListView = (ListView) dialog.findViewById(R.id.dialog_listview);
        ArrayAdapter<String> adapter
                = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_list_item_1, arrayList);
        dialog_ListView.setAdapter(adapter);
        dialog_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
// TODO Auto-generated method stub
//                Toast.makeText(RegisterActivity.this,
//                        parent.getItemAtPosition(position).toString() + " clicked",
//                        Toast.LENGTH_LONG).show();
//                value = parent.getItemAtPosition(position).toString();
                mEditText.setText(parent.getItemAtPosition(position).toString());
                dialog.dismiss();

            }
        });
        dialog.show();
    }

    public static void dialogListShowTextView(Context mContext, ArrayList arrayList, String mTitle, final TextView mTextView,
                                              final ReturnValue returnValue) {
        if (Utility.INSTANCE.isMarshmallowOS()) {
            dialog = new Dialog(mContext, android.R.style.Theme_Material_Light_Dialog);
        } else {
            dialog = new Dialog(mContext);
        }
        //dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawableResource(R.color.white);
        dialog.setContentView(R.layout.dialog_popup);
        dialog.setTitle(mTitle);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
// TODO Auto-generated method stub
//                Toast.makeText(RegisterActivity.this,
//                        "OnCancelListener",
//                        Toast.LENGTH_LONG).show();
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
// TODO Auto-generated method stub
//                Toast.makeText(RegisterActivity.this,
//                        "OnDismissListener",
//                        Toast.LENGTH_LONG).show();
            }
        });

//Prepare ListView in dialog
        ListView dialog_ListView = (ListView) dialog.findViewById(R.id.dialog_listview);
        ArrayAdapter<String> adapter
                = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_list_item_1, arrayList);
        dialog_ListView.setAdapter(adapter);
        dialog_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
// TODO Auto-generated method stub
//                Toast.makeText(RegisterActivity.this,
//                        parent.getItemAtPosition(position).toString() + " clicked",
//                        Toast.LENGTH_LONG).show();
//                value = parent.getItemAtPosition(position).toString();

                returnValue.returnValues(parent.getItemAtPosition(position).toString(), position);

                mTextView.setText(parent.getItemAtPosition(position).toString());
                dialog.dismiss();

            }
        });
        dialog.show();
    }

    public static void dialogListReturnValue(Context mContext, ArrayList arrayList, String mTitle,
                                             final EditText mEditText, final ReturnValue returnValue) {
        if (Utility.INSTANCE.isMarshmallowOS()) {
            dialog = new Dialog(mContext, android.R.style.Theme_Material_Light_Dialog);
        } else {
            dialog = new Dialog(mContext);
        }
        dialog.getWindow().setBackgroundDrawableResource(R.color.white);
        dialog.setContentView(R.layout.dialog_popup);
        dialog.setTitle(mTitle);
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);

        dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {

            @Override
            public void onCancel(DialogInterface dialog) {
// TODO Auto-generated method stub
//                Toast.makeText(RegisterActivity.this,
//                        "OnCancelListener",
//                        Toast.LENGTH_LONG).show();
            }
        });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
// TODO Auto-generated method stub
//                Toast.makeText(RegisterActivity.this,
//                        "OnDismissListener",
//                        Toast.LENGTH_LONG).show();
            }
        });

//Prepare ListView in dialog
        ListView dialog_ListView = (ListView) dialog.findViewById(R.id.dialog_listview);
        ArrayAdapter<String> adapter
                = new ArrayAdapter<String>(mContext,
                android.R.layout.simple_list_item_1, arrayList);
        dialog_ListView.setAdapter(adapter);
        dialog_ListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {

                returnValue.returnValues(parent.getItemAtPosition(position).toString(), position);

//                Toast.makeText(RegisterActivity.this,
//                        parent.getItemAtPosition(position).toString() + " clicked",
//                        Toast.LENGTH_LONG).show();
//                value = parent.getItemAtPosition(position).toString();
                mEditText.setText(parent.getItemAtPosition(position).toString());
                dialog.dismiss();

            }
        });
        dialog.show();
    }
}
