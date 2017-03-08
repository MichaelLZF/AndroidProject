package com.lzf.ffcontacts;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.lzf.ffcontacts.db.DBHelper;
import com.lzf.ffcontacts.utlis.ExcelUtils;
import com.lzf.ffcontacts.utlis.PersonInfo;

import java.io.File;
import java.util.ArrayList;

@SuppressLint("SimpleDateFormat")
public class MainActivity extends AppCompatActivity {
    private LinearLayout mainLayout;
    /**
     * nameEdit 姓名
     * phoneEdit 电话
     */
    private EditText nameEdit;
    private EditText phoneEdit;
    /**
     * addBtn 添加联系人
     * deleteBtn 删除联系人
     * inputBtn 导入联系人
     * outputBtn 导出联系人
     */
    private Button addBtn;
    private Button deleteBtn;
    private Button inputBtn;
    private Button outputBtn;
    //文件处理相关
    private DBHelper mDbHelper;
    private File file;
    private String[] title = {"姓名","电话1","电话2","电话3","电话4","电话5"};
    private ArrayList<ArrayList<String>> bill2List;
    private long insert;

    private String[] addContacts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //获取组件
        nameEdit = (EditText) findViewById(R.id.name);
        phoneEdit = (EditText) findViewById(R.id.phone);
        addBtn = (Button) findViewById(R.id.add);
        deleteBtn = (Button) findViewById(R.id.delete);
        inputBtn = (Button) findViewById(R.id.input);
        outputBtn = (Button) findViewById(R.id.output);
        mainLayout = (LinearLayout)findViewById(R.id.main);
        //添加联系人
//        addContacts = new String[]{nameEdit.getText().toString().trim(),
//                phoneEdit.getText().toString().trim()};
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                if(canSave(addContacts)) {
                    addContacts();
//                }else {
//                    Toast.makeText(MainActivity.this, "请填写任意一项内容", Toast.LENGTH_SHORT).show();
//                }
            }
        });
        //收回软键盘
        mainLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                return closeKeyBoard();
            }
        });
        mDbHelper = new DBHelper(this);
        mDbHelper.open();
        bill2List = new ArrayList<ArrayList<String>>();
        //导出联系人
        outputBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                readContacts();
            }
        });
        //导入联系人
        inputBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getExcelData();
            }
        });
    }
    //读取联系人
    private void readContacts(){
        ContentValues values = new ContentValues();
        Cursor cursor = getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI, null, null,
                null, null);
        // 遍历查询结果，获取系统中所有联系人
        while (cursor.moveToNext()) {
            // 获取联系人ID
            String contactId = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.Contacts._ID));
            // 获取联系人的名字
            String name = cursor.getString(cursor.getColumnIndex(
                    ContactsContract.Contacts.DISPLAY_NAME));
            values.put("name",name);
            // 使用ContentResolver查找联系人的电话号码
            Cursor phones = getContentResolver().query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID
                            + " = " + contactId, null, null);
            // 遍历查询结果，获取该联系人的多个电话号码
            while (phones.moveToNext()) {
                // 获取查询结果中电话号码列中数据
                String phoneNumber = phones.getString(phones
                        .getColumnIndex(ContactsContract
                                .CommonDataKinds.Phone.NUMBER));
                values.put("phone",phoneNumber);
            }
            phones.close();
            insert = mDbHelper.insert("contacts_table",values);
        }

        if (insert>0){
            initData();
        }
        cursor.close();

    }
    //初始化文件夹及路径
    @SuppressLint("SimpleDateFormat")
    public void initData(){
        file = new File(getSDPath()+"/FFContacts");
        makeDir(file);
        ExcelUtils.initExcel(file.toString()+"/contacts.xls",title);
        ExcelUtils.writeObjListToExcel(getContactsData(),getSDPath()
        +"/FFContacts/contacts.xls",this);
    }
    private ArrayList<ArrayList<String>> getContactsData() {
        Cursor mCursor = mDbHelper.exeSql("select * from contacts_table");
        while (mCursor.moveToNext()) {
            ArrayList<String> beanList = new ArrayList<String>();
            beanList.add(mCursor.getString(1));
            beanList.add(mCursor.getString(2));
//            beanList.add(mCursor.getString(3));
//            beanList.add(mCursor.getString(4));
//            beanList.add(mCursor.getString(5));
//            beanList.add(mCursor.getString(6));
            bill2List.add(beanList);
        }
        mCursor.close();
        return bill2List;
    }
    //创建文件夹
    public static void makeDir(File dir){
        if (!dir.getParentFile().exists()){
            makeDir(dir.getParentFile());
        }
        dir.mkdir();
    }
    //获取SD卡路径
    public String getSDPath(){
        File sdDit = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED);
        if (sdCardExist){
            sdDit = Environment.getExternalStorageDirectory();
        }
        String dir = sdDit.toString();
        return dir;
    }


    //从excel中获取数据
    private void getExcelData(){
        ArrayList<PersonInfo> billList = (ArrayList<PersonInfo>) ExcelUtils
                .read2DB(new File(getSDPath() + "/FFContacts/contacts.xls"), this);

        for (int i = 0;i<billList.size();i++) {
            String name = billList.get(i).getName();
            String phone = billList.get(i).getPhone();
            //创建空的ContentValues
            ContentValues values = new ContentValues();
            //空值插入，获取rawContactId
            Uri rawContactUri = getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI,
                    values);
            long rawContactId = ContentUris.parseId(rawContactUri);
            //添加联系人姓名
            values.clear();
            values.put(ContactsContract.Data.RAW_CONTACT_ID,rawContactId);
            values.put(ContactsContract.RawContacts.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
            values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,name);
            getContentResolver().insert(ContactsContract.Data.CONTENT_URI,values);
            //添加联系人电话
            values.clear();
            values.put(ContactsContract.Data.RAW_CONTACT_ID,rawContactId);
            values.put(ContactsContract.RawContacts.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
            values.put(ContactsContract.CommonDataKinds.Phone.NUMBER,phone);
            values.put(ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
            getContentResolver().insert(ContactsContract.Data.CONTENT_URI,values);
        }

    }






    //添加联系人（未判定输入内容）
    private void addContacts(){
        //获取文本框内容
        String name = nameEdit.getText().toString();
        String phone = phoneEdit.getText().toString();
        //创建空的ContentValues
        ContentValues values = new ContentValues();
        //空值插入，获取rawContactId
        Uri rawContactUri = getContentResolver().insert(
                ContactsContract.RawContacts.CONTENT_URI,values
        );
        long rawContactId = ContentUris.parseId(rawContactUri);
        //添加联系人姓名
        values.clear();
        values.put(ContactsContract.Data.RAW_CONTACT_ID,rawContactId);
        values.put(ContactsContract.RawContacts.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME,name);
        getContentResolver().insert(ContactsContract.Data.CONTENT_URI,values);
        //添加联系人电话
        values.clear();
        values.put(ContactsContract.Data.RAW_CONTACT_ID,rawContactId);
        values.put(ContactsContract.RawContacts.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        values.put(ContactsContract.CommonDataKinds.Phone.NUMBER,phone);
        values.put(ContactsContract.CommonDataKinds.Phone.TYPE,
                ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE);
        getContentResolver().insert(ContactsContract.Data.CONTENT_URI,values);
        Toast.makeText(MainActivity.this, "成功添加联系人！", Toast.LENGTH_SHORT).show();
    }
    //判断是否为空
//    private boolean canSave(String[] data) {
//        boolean isOk = false;
//        for (int i = 0; i < data.length; i++) {
//            if (i > 0 && i < data.length) {
//                if (!TextUtils.isEmpty(data[i])) {
//                    isOk = true;
//                }
//            }
//        }
//        return isOk;
//    }
    //收起软键盘
    private boolean closeKeyBoard(){
        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        return imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),0);
    }
}
