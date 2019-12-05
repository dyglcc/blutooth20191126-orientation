package com.xiaobailong.bluetoothfaultboardcontrol;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.xiaobailong.bluetooth.FaultBean;
import com.xiaobailong.bluetooth.MediaFileListDialog;
import com.xiaobailong.bluetooth.MediaFileListDialogMainpage;
import com.xiaobailong.titile.WriteTitleActivity;
import com.xiaobailong.tools.ConstValue;
import com.xiaobailong.widget.ListScrollView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import me.grantland.widget.AutofitTextView;


public class MainActivity extends BaseActivity implements OnClickListener,
        OnTouchListener, OnScrollListener {

    public static final int ShortTable = 0;
    public static final int FalseTable = 1;
    public static final int BreakTable = 2;
    public static int TableState = ShortTable;
    private AutofitTextView tvFileName = null;
    private ListScrollView listscroll;
    /**
     * 短路1-120继电器状态数据
     */
    private ArrayList<Relay> shortList = new ArrayList<Relay>();

    /**
     * 虚接1-120继电器状态数据
     */
    private ArrayList<Relay> falseList = new ArrayList<Relay>();

    /**
     * 断路1-120继电器状态数据
     */
    private ArrayList<Relay> breakfaultList = new ArrayList<Relay>();

    /**
     * 1-120继电器操作监听
     */
    private Handler theFailurePointSetGVHandler = null;
    /**
     * 基本功能操作监听
     */
    private Handler faultboardOptionHandler = null;

    private TheFailurePointSetAdapter theFailurePointSetAdapter = null;
    /**
     * 基本操作功能类
     */
    private FaultboardOption faultboardOption = null;

    private Button ignitionButton = null;
    private Button startButton = null;
    private Button shutDownButton = null;
    private boolean hasStarted = false;

    private TabHost tabHost = null;
    private TabWidget tabWidget = null;

    private GridView theFailurePointSetGV = null;

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initSDcard();
        initHandler();
        initData(0);
        initView();


    }

    private int getCurentTab() {
        return tabHost.getCurrentTab();
    }

    /**
     * 初始化继电器状态数据
     */
    private void initData(int pos) {

        if (shortList.isEmpty()) {
            for (int i = 0; i < 6; i++) {
                shortList.add(new Relay(i + 1, i + 1, Relay.Green));
            }

            for (int i = 0; i < 120; i++) {
                breakfaultList.add(new Relay(i + 1, i + 1, Relay.Green));
            }

            for (int i = 0; i < 20; i++) {
                falseList.add(new Relay(i + 1, i + 1, Relay.Green));
            }
        }
        switch (pos) {
            case 0:
                faultboardOption = new FaultboardOption(this, faultboardOptionHandler,
                        breakfaultList);
                break;
            case 1:
                faultboardOption = new FaultboardOption(this, faultboardOptionHandler,
                        falseList);
                break;
            case 2:
                faultboardOption = new FaultboardOption(this, faultboardOptionHandler,
                        shortList);
                break;
        }


    }

    private void initHandler() {
        theFailurePointSetGVHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                Relay relay = (Relay) msg.obj;
                int state = relay.getState();
                relay.setState(Relay.Yellow);
                if (state == Relay.Red) {
                    faultboardOption.shutDown((byte) relay.getId(), msg.arg1);
                } else if (state == Relay.Green) {
                    int id = relay.getId();
                    faultboardOption.start((byte) id, msg.arg1);
                } else if (state == Relay.Yellow) {
                    Toast.makeText(MainActivity.this, "Command had send !",
                            Toast.LENGTH_SHORT).show();
                }
                theFailurePointSetAdapter.notifyDataSetChanged();
            }
        };

        faultboardOptionHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                theFailurePointSetAdapter.notifyDataSetChanged();
            }
        };
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void initSDcard() {
        if (ConstValue.haveSdcard()) {
            File file = new File(ConstValue.get_DIR());
            if (!file.exists()) {
                file.mkdirs();
            }
            File title = new File(ConstValue.get_title_File());
            if (!title.exists()) {
                try {
                    title.createNewFile();
                } catch (IOException e) {
                    Toast.makeText(MainActivity.this, "创建文件失败了！！", Toast.LENGTH_LONG).show();
                }
            }
        } else {
            nosdcard();
        }
    }

    public static String readTitleStr(Context context, File file) {
        String str = "";
        try {
            BufferedReader reader =
                    new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            while ((str = reader.readLine()) != null) {
                return str;
            }
        } catch (FileNotFoundException e) {
            String msg = e == null ? "" : e.getMessage();
            Toast.makeText(context, "读取文件出错！" + msg, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            String msg = e == null ? "" : e.getMessage();
            Toast.makeText(context, "读取文件出错！" + msg, Toast.LENGTH_LONG).show();
        }
//                todo 创建修改标题的文件，读取文件，修改文件更新标题。
        return str;
    }

    public void setFileName(File file) {
        if (file != null && !this.isFinishing()) {
            tvFileName.setVisibility(View.VISIBLE);
            String name = file.getName().toString();
            String[] names = name.split("\\.");
            if (names.length > 0) {
                tvFileName.setText(names[0]);
                // 修复bug:设置之后textview的高度不会减小
                afterSet(names[0]);
            }
        } else {
            tvFileName.setVisibility(View.GONE);
        }

    }

    private void afterSet(final String str) {
        tvFileName.setText(str + ".");
        new Handler(getMainLooper()).postDelayed(new Runnable() {
            @Override
            public void run() {
                tvFileName.setText(str);
            }
        }, 200);
    }

    public void setValues(List<FaultBean> datas) {
        if (datas == null) {
            return;
        }

        for (int i = 0; i < breakfaultList.size(); i++) {
            Relay relay = breakfaultList.get(i);
            for (int j = 0; j < datas.size(); j++) {
                FaultBean faultBean = datas.get(j);
                if (faultBean.getType() == ConstValue.type_duanlu) {
                    relay.setValue(faultBean.getValue());
                    relay.setType(faultBean.getType());
                    datas.remove(faultBean);
                    break;
                }

            }
        }
        for (int i = 0; i < falseList.size(); i++) {
            Relay relay = falseList.get(i);
            for (int j = 0; j < datas.size(); j++) {
                FaultBean faultBean = datas.get(j);
                if (faultBean.getType() == ConstValue.type_xujie) {
                    relay.setValue(faultBean.getValue());
                    relay.setType(faultBean.getType());
                    datas.remove(faultBean);
                    break;
                }

            }
        }
        for (int i = 0; i < shortList.size(); i++) {
            Relay relay = shortList.get(i);
            for (int j = 0; j < datas.size(); j++) {
                FaultBean faultBean = datas.get(j);
                if (faultBean.getType() == ConstValue.type_shortFault) {
                    relay.setValue(faultBean.getValue());
                    relay.setType(faultBean.getType());
                    datas.remove(faultBean);
                    break;
                }

            }
        }
        theFailurePointSetAdapter.notifyDataSetChanged();
//        Toast.makeText(this, "替换完毕", Toast.LENGTH_SHORT).show();

    }

    private void initView() {
        //创建中部标签浏览界面
        createTabHost();

        // 滑动区域冲突
        listscroll = (ListScrollView) findViewById(R.id.listscroll);
        listscroll.setListView(theFailurePointSetGV);
        // 点火
        ignitionButton = (Button) findViewById(R.id.Button_Ignition);
        ignitionButton.setOnClickListener(this);
        // 启动
        startButton = (Button) findViewById(R.id.Button_Start);
        startButton.setOnClickListener(this);
        startButton.setOnTouchListener(this);
        // 熄火
        shutDownButton = (Button) findViewById(R.id.Button_ShutDown);
        shutDownButton.setOnClickListener(this);
        // 故障点说明
        findViewById(R.id.Button_PointOfFailureThat).setOnClickListener(this);
        // 状态读取
//        findViewById(R.id.Button_StateIsRead).setOnClickListener(this);
        // 全部设置
        findViewById(R.id.Button_SetAll).setOnClickListener(this);
        // 全部清除
        findViewById(R.id.Button_ClearAll).setOnClickListener(this);
        // 全部清除
        findViewById(R.id.Button_Mode_Teach02).setOnClickListener(this);
        tvFileName = (AutofitTextView) findViewById(R.id.tv_name);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.Button_Ignition:// 点火
                if (hasStarted) {
                    Toast.makeText(this, "The machine had be started !",
                            Toast.LENGTH_SHORT).show();
                } else {
                    hasStarted = true;
                    Toast.makeText(this, "The machine be started !",
                            Toast.LENGTH_SHORT).show();
                    faultboardOption.ignition(R.id.Button_Ignition);
                }
                break;
            case R.id.Button_ShutDown:// 熄火
                if (hasStarted) {
                    hasStarted = false;
                    Toast.makeText(this, "The machine be shut down !",
                            Toast.LENGTH_SHORT).show();
                    faultboardOption.fireDown((byte) 0x65, R.id.Button_ShutDown);
                } else {
                    Toast.makeText(this, "The machine had not be started !",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case R.id.Button_PointOfFailureThat:// 故障点说明
//                PointOfFailureThatDialog pointOfFailureThatDialog = new PointOfFailureThatDialog(
//                        this);
//                pointOfFailureThatDialog.show();
                MediaFileListDialogMainpage page = new MediaFileListDialogMainpage(this);
                page.show();
                break;
//            case R.id.Button_StateIsRead:// 状态读取
//                faultboardOption.stateIsRead(R.id.Button_StateIsRead);
//                break;
            case R.id.Button_SetAll:// 全部设置
                faultboardOption.setAll(R.id.Button_SetAll);
                break;
            case R.id.Button_ClearAll:// 全部清除
                faultboardOption.clearAll(R.id.Button_ClearAll);
                break;
            case R.id.Button_Mode_Teach02:// 教学模式
//			todo 打开指定文件夹 /sdcard/
                if (ConstValue.haveSdcard()) {
                    File file = new File(ConstValue.get_DIR());
                    if (!file.exists()) {
                        Toast.makeText(MainActivity.this, "没有文件可以显示！文件路径 " + ConstValue.get_DIR()
                                , Toast.LENGTH_LONG).show();
                        return;
                    }

                    MediaFileListDialog bluetoothDevicesListDialog = new MediaFileListDialog(
                            MainActivity.this);
                    bluetoothDevicesListDialog.show();
                } else {
                    nosdcard();
                }

                break;
        }
    }

    private void nosdcard() {
        Toast.makeText(MainActivity.this, "没有sdcard,无法显示教学文件", Toast.LENGTH_LONG).show();
    }

    public static ArrayList<String> getDirFilesDir(File file) {
        ArrayList<String> list = new ArrayList<String>();

        if(file == null || file.listFiles() == null){
            return list;
        }
        for (File f : file.listFiles()) {
            list.add(f.getAbsolutePath());
        }
        return list;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                this.faultboardOption.bluetoothConnect();
                break;
            case R.id.action_exit:
                finish();
                break;
            case R.id.action_close:
                this.faultboardOption.closeBluetoothSocket();
                Toast.makeText(MainActivity.this, "连接已断开",
                        Toast.LENGTH_SHORT).show();
                initData(getCurentTab());
                break;
            case R.id.action_edit_title:
                editTitle();
                break;

            case R.id.change_oretation:
                SharedPreferences sharedPreferences = getSharedPreferences(ConstValue.SP_NAME, 0);
                int change_oretation = sharedPreferences.getInt(ConstValue.xbl_orentation, 1);
                change_oretation ^= 1;
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt(ConstValue.xbl_orentation, change_oretation);
                editor.commit();
                this.setRequestedOrientation(change_oretation);
                break;
            case R.id.action_set_pass:
                startActivity(new Intent(MainActivity.this, PasswordSettingActivity.class));
                break;
            default:
                break;
        }
        return true;
    }

    private void editTitle() {
        startActivityForResult(new Intent(MainActivity.this, WriteTitleActivity.class), 100);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == Activity.RESULT_OK) {
                File file = new File(ConstValue.get_title_File());
                String title = readTitleStr(MainActivity.this, file);
                if (title == null || title.equals("")) {
                    title = getString(R.string.app_name);
                }
                getActionBar().setTitle(title);
            }
        }
    }

    @Override
    public void finish() {
        faultboardOption.closeBluetoothSocket();
        super.finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
                    new AlertDialog.Builder(this).setMessage("确定退出?").setPositiveButton("确定",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    finish();
                                }
                            }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).create().show();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.Button_Start:// 启动
                if (hasStarted) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        Toast.makeText(this, "StartDown", Toast.LENGTH_SHORT).show();
                        faultboardOption.startDown((byte) 0x66, R.id.Button_Start);
                    } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        Toast.makeText(this, "StartUp", Toast.LENGTH_SHORT).show();
                        faultboardOption.startUp((byte) 0x66, R.id.Button_Start);
                    }
                } else {
                    Toast.makeText(this, "The machine had not be started !",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
        return true;
    }

    public void onScroll(AbsListView view, int firstVisibleItem,
                         int visibleItemCount, int totalItemCount) {
    }

    private void createTabHost() {
        {
            tabHost = (TabHost) findViewById(R.id.tabhost);
            tabWidget = (TabWidget) findViewById(android.R.id.tabs);
            tabHost.setup();
            tabHost.bringToFront();

            ArrayList<TabHost.TabSpec> hostlist = new ArrayList<TabHost.TabSpec>(
                    2);

            TabHost.TabSpec tabspec1 = tabHost.newTabSpec("0");
            tabspec1.setContent(R.id.GridView_TheFailurePointSet);
            TextView indicatorV = new TextView(this);
            indicatorV.setGravity(Gravity.CENTER);
            indicatorV.setBackgroundResource(R.drawable.channelsbg);
            indicatorV.setTextSize(16);
            indicatorV.setText(R.string.breakfault);
            tabspec1.setIndicator(indicatorV);
            hostlist.add(tabspec1);


            tabspec1 = tabHost.newTabSpec("2");
            tabspec1.setContent(R.id.GridView_TheFailurePointSet);
            indicatorV = new TextView(this);
            indicatorV.setGravity(Gravity.CENTER);
            indicatorV.setBackgroundResource(R.drawable.channelsbg);
            indicatorV.setTextSize(16);
            indicatorV.setText(R.string.falsefault);
            tabspec1.setIndicator(indicatorV);
            hostlist.add(tabspec1);

            tabspec1 = tabHost.newTabSpec("1");
            tabspec1.setContent(R.id.GridView_TheFailurePointSet);
            indicatorV = new TextView(this);
            indicatorV.setGravity(Gravity.CENTER);
            indicatorV.setBackgroundResource(R.drawable.channelsbg);
            indicatorV.setTextSize(16);
            indicatorV.setText(R.string.shortfault);
            tabspec1.setIndicator(indicatorV);
            hostlist.add(tabspec1);


            int j = hostlist.size();
            for (int i = 0; i < j; i++) {
                tabHost.addTab(hostlist.get(i));
            }

            tabHost.setCurrentTab(0);
            View view = tabWidget.getChildAt(0);
            view.setBackgroundDrawable(getResources().getDrawable(
                    R.drawable.presschannelbg));

            theFailurePointSetAdapter = new TheFailurePointSetAdapter(this,
                    shortList, theFailurePointSetGVHandler);

            theFailurePointSetGV = (GridView) findViewById(R.id.GridView_TheFailurePointSet);
            theFailurePointSetGV.setAdapter(theFailurePointSetAdapter);
            theFailurePointSetGV.setOnScrollListener(this);
            theFailurePointSetGV.setVisibility(View.VISIBLE);
            theFailurePointSetGV.setNumColumns(3);

            tabHost.setOnTabChangedListener(new OnTabChangeListener() {
                public void onTabChanged(String tabId) {

                    changeListData(tabId);
                    for (int i = 0; i < tabWidget.getChildCount(); i++) {
                        View view = tabWidget.getChildAt(i);
                        if (tabHost.getCurrentTab() == i) {
                            view.setBackgroundDrawable(getResources()
                                    .getDrawable(R.drawable.presschannelbg));
                        } else {
                            view.setBackgroundDrawable(getResources()
                                    .getDrawable(R.drawable.channelsbg));
                        }
                    }
                }
            });

            changeListData("0");
        }

    }

    private void changeListData(String tabId) {
        // 短路故障
        if (tabId.equals("1")) {
            TableState = ShortTable;
            theFailurePointSetAdapter = new TheFailurePointSetAdapter(this,
                    shortList, theFailurePointSetGVHandler);
            theFailurePointSetGV.setAdapter(theFailurePointSetAdapter);
            faultboardOption.setArray(shortList);
        } else if (tabId.equals("2")) { // 虚接故障
            TableState = FalseTable;
            theFailurePointSetAdapter = new TheFailurePointSetAdapter(this,
                    falseList, theFailurePointSetGVHandler);
            theFailurePointSetGV.setAdapter(theFailurePointSetAdapter);
            faultboardOption.setArray(falseList);
        } else {// 断路故障
            TableState = BreakTable;
            theFailurePointSetAdapter = new TheFailurePointSetAdapter(this,
                    breakfaultList, theFailurePointSetGVHandler);
            theFailurePointSetGV.setAdapter(theFailurePointSetAdapter);
            faultboardOption.setArray(breakfaultList);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }
}
