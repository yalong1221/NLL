package com.emjiayuan.nll.activity.address;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alibaba.fastjson.JSON;
import com.emjiayuan.nll.Global;
import com.emjiayuan.nll.R;
import com.emjiayuan.nll.base.BaseActivity;
import com.emjiayuan.nll.event.AddressEvent;
import com.emjiayuan.nll.utils.AddressPickTask;
import com.emjiayuan.nll.utils.MyOkHttp;
import com.emjiayuan.nll.utils.MyUtils;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import androidx.appcompat.widget.Toolbar;
import butterknife.BindView;
import cn.addapp.pickers.entity.City;
import cn.addapp.pickers.entity.County;
import cn.addapp.pickers.entity.Province;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Response;

public class AddAddressActivity extends BaseActivity implements View.OnClickListener {

    @BindView(R.id.icon_back)
    ImageView mIconBack;
    @BindView(R.id.tv_title)
    TextView mTvTitle;
    @BindView(R.id.icon_search)
    ImageView mIconSearch;
    @BindView(R.id.toolbar)
    Toolbar mToolbar;
    @BindView(R.id.et_name)
    EditText mEtName;
    @BindView(R.id.et_phone)
    EditText mEtPhone;
    @BindView(R.id.et_ssq)
    TextView mEtSsq;
    @BindView(R.id.et_detailed)
    EditText mEtDetailed;
    @BindView(R.id.check)
    CheckBox mCheck;
    @BindView(R.id.tv_default)
    TextView mTvDefault;
    @BindView(R.id.default_ll)
    LinearLayout mDefaultLl;
    @BindView(R.id.tv_save)
    TextView mTvSave;
    private String is_default = "0";
    private String provinceName;
    private String cityName;
    private String districtName;
    private String ssq;

    @Override
    protected int setLayoutId() {
        return R.layout.activity_add_address;
    }

    @Override
    protected void initData() {
        mTvTitle.setText("添加地址");
        mTvTitle.setVisibility(View.VISIBLE);
        mTvSave.setVisibility(View.VISIBLE);
    }

    public void onAddressPicker() {
        AddressPickTask task = new AddressPickTask(this);
        task.setHideProvince(false);
        task.setHideCounty(false);
        task.setCallback(new AddressPickTask.Callback() {
            @Override
            public void onAddressInitFailed() {
                MyUtils.showToast(mActivity, "数据初始化失败");
            }

            @Override
            public void onAddressPicked(Province province, City city, County county) {
                if (county == null) {
                    ssq = province.getAreaName() + " " + city.getAreaName();
                } else {
                    ssq = province.getAreaName() + " " + city.getAreaName() + " " + county.getAreaName();
                }
                mEtSsq.setText(ssq);
                provinceName = province.getAreaName();
                cityName = city.getAreaName();
                districtName = county.getAreaName();
            }
        });
        if (provinceName != null) {
            task.execute(provinceName, cityName, districtName);
        } else {
            task.execute("浙江", "宁波市", "镇海区");
        }

    }

    @Override
    protected void initView() {

    }

    @Override
    protected void setListener() {
        mIconBack.setOnClickListener(this);
        mTvSave.setOnClickListener(this);
        mDefaultLl.setOnClickListener(this);
        mEtSsq.setOnClickListener(this);
        mCheck.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    is_default = "1";
                } else {
                    is_default = "0";
                }
            }
        });
    }

    public void getDatas() {
        FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
//new call
        Call call = MyOkHttp.GetCall("public.getAddressAndroid", formBody);
//请求加入调度
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Log.d("------------", e.toString());
//                        myHandler.sendEmptyMessage(1);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String result = response.body().string();
                Log.d("------获取城市结果------", result);
                Message message = new Message();
                message.what = 1;
                Bundle bundle = new Bundle();
                bundle.putString("result", result);
                message.setData(bundle);
                myHandler.sendMessage(message);
            }
        });
    }

    @Override
    public void onClick(View view) {
        if (!MyUtils.isFastClick()) {
            return;
        }
        switch (view.getId()) {
            case R.id.icon_back:
                finish();
                break;
            case R.id.et_ssq:
                getDatas();
                break;
            case R.id.tv_save:
                String name = mEtName.getText().toString();
                String phone = mEtPhone.getText().toString();
                String ssq = mEtSsq.getText().toString();
                String detailed = mEtDetailed.getText().toString().replaceAll(" ", "");
                if ("".equals(name)) {
                    MyUtils.showToast(this, "请输入收货人！");
                    return;
                }
                if ("".equals(phone)) {
                    MyUtils.showToast(this, "请输入联系电话！");
                    return;
                }
                if (phone.length() < 11) {
                    MyUtils.showToast(this, "请输入11位手机号码！");
                    return;
                }
                if ("".equals(ssq)) {
                    MyUtils.showToast(this, "请输入省市县！");
                    return;
                }
                if ("".equals(detailed)) {
                    MyUtils.showToast(this, "请输入详细地址！");
                    return;
                }
                FormBody.Builder formBody = new FormBody.Builder();//创建表单请求体
                formBody.add("userid", Global.loginResult.getId());//传递键值对参数
                formBody.add("username", name);//传递键值对参数
                formBody.add("telphone", phone);//传递键值对参数
                formBody.add("address", ssq + " " + detailed);//传递键值对参数
                formBody.add("shengfen", provinceName);//传递键值对参数
                formBody.add("default", is_default);//传递键值对参数
//new call
                Call call = MyOkHttp.GetCall("userAddress.addAddress", formBody);
//请求加入调度
                call.enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                        Log.d("------------", e.toString());
//                        myHandler.sendEmptyMessage(1);
                    }

                    @Override
                    public void onResponse(Call call, Response response) throws IOException {
                        String result = response.body().string();
                        Log.d("------添加地址结果------", result);
                        Message message = new Message();
                        message.what = 0;
                        Bundle bundle = new Bundle();
                        bundle.putString("result", result);
                        message.setData(bundle);
                        myHandler.sendMessage(message);
                    }
                });
                break;
            case R.id.default_ll:
//                MyUtils.showToast(this,"已设为默认");
                break;

        }
    }

    Handler myHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Bundle bundle = msg.getData();
            String result = bundle.getString("result");
            switch (msg.what) {
                case 0:
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        String code = jsonObject.getString("code");
                        String message = jsonObject.getString("message");
                        String data = jsonObject.getString("data");
                        if ("200".equals(code)) {
                            MyUtils.showToast(AddAddressActivity.this, message);
                            EventBus.getDefault().post(new AddressEvent());
                            finish();
                        } else {
                            MyUtils.showToast(AddAddressActivity.this, message);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case 1:
                    try {
                        JSONObject jsonObject = new JSONObject(result);
                        String code = jsonObject.getString("code");
                        String message = jsonObject.getString("message");
                        String data = jsonObject.getString("data");
                        Global.datas = new ArrayList<>();
                        if ("200".equals(code)) {
//                        String json = ConvertUtils.toString(activity.getAssets().open("city.json"));
                            Global.datas.addAll(JSON.parseArray(data, Province.class));
                            onAddressPicker();
                        } else {
                            MyUtils.showToast(mActivity, message);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        }
    };
}
