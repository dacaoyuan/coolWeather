package com.coolweather.app.activity;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.coolweather.app.R;
import com.coolweather.app.util.HttpCallBackListener;
import com.coolweather.app.util.HttpUtil;
import com.coolweather.app.util.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xutils.common.Callback;
import org.xutils.http.RequestParams;
import org.xutils.x;

/**
 * Created by abc on 2016/8/24.
 */
public class WeatherActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "WeatherActivity";

    private LinearLayout weatherInfoLayout;

    private TextView cityNameText;

    private TextView publishText;

    private TextView weatherDespText;

    private TextView temp1Text;

    private TextView temp2Text;

    private TextView currentDateText;


    private Button btnSwithch, btnRefresh;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.weather_layout);

        weatherInfoLayout = (LinearLayout) findViewById(R.id.weather_info_layout);
        cityNameText = (TextView) findViewById(R.id.city_name);
        publishText = (TextView) findViewById(R.id.publish_text);
        weatherDespText = (TextView) findViewById(R.id.weather_desp);
        temp1Text = (TextView) findViewById(R.id.temp1);
        temp2Text = (TextView) findViewById(R.id.temp2);
        currentDateText = (TextView) findViewById(R.id.current_date);

        btnSwithch = (Button) findViewById(R.id.switch_city);
        btnRefresh = (Button) findViewById(R.id.refresh);
        btnSwithch.setOnClickListener(this);
        btnRefresh.setOnClickListener(this);

        String countyCode = getIntent().getStringExtra("county_code");
        System.out.println("countyCode=" + countyCode);

        String countyName = getIntent().getStringExtra("county_name");
        System.out.println("countyName=" + countyName);

        if (!TextUtils.isEmpty(countyCode)) {
            //有县级代号就去查询天气
            publishText.setText("同步中...");
            weatherInfoLayout.setVisibility(View.INVISIBLE);
            cityNameText.setVisibility(View.INVISIBLE);
            queryWeatherCode(countyName);
        }


    }

    /**
     * 查询县级代号所对应的天气代号
     *
     * @param countyName
     */
    private void queryWeatherCode(String countyName) {
        String address = "https://free-api.heweather.com/v5/now?city=" + countyName + "&key=96a56f2a3c144e06a9c5d038249a9337";

        System.out.println("address=" + address);
        //String address = "http://www.weather.com.cn/data/list3/city" + countyCode + ".xml";
        // queryFromServer(address, "countyCode");
        getFromServer(address);
    }

    /**
     * 查询天气代号所对应的天气
     *
     * @param weatherCode
     */
    private void queryWeatherInfo(String weatherCode) {
        String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
        //queryFromServer(address, weatherCode);

    }

    private void getFromServer(String address) {
        RequestParams params = new RequestParams(address);
        // params.addQueryStringParameter("", "");
        x.http().get(params, new Callback.CommonCallback<String>() {
            @Override
            public void onSuccess(String result) {
                JSONObject jsonObject = null;
                try {
                    jsonObject = new JSONObject(result);
                    JSONArray weatherInfo = jsonObject.getJSONArray("HeWeather5");
                    String status = weatherInfo.getJSONObject(0).optString("status");
                    if (status.equals("ok")) {
                        Utility.handleWeatherResponse(WeatherActivity.this, result);
                        showWeather();
                    } else {
                        Log.i(TAG, "onSuccess: result=" + result);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

               /* runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        showWeather();
                    }
                });*/

            }

            @Override
            public void onError(Throwable ex, boolean isOnCallback) {
                Toast.makeText(x.app(), ex.getMessage(), Toast.LENGTH_LONG).show();
                Log.i(TAG, "onError: ");
            }

            @Override
            public void onCancelled(CancelledException cex) {
                System.out.println("WeatherActivity.onCancelled");
            }

            @Override
            public void onFinished() {

            }
        });
    }


    /**
     * 根据传入的地址和类型去向服务器查询天气代号或者天气信息
     *
     * @param address
     * @param type
     */
    private void queryFromServer(final String address, final String type) {
        HttpUtil.sendHttpRequest(address, new HttpCallBackListener() {
            @Override
            public void onFinish(String response) {
                if ("countyCode".equals(type)) {
                    if (!TextUtils.isEmpty(response)) {
                        //从服务器返回的数据中解析出天气代号
                        String[] array = response.split("\\|");
                        if (array != null && array.length == 2) {
                            String weatherCode = array[1];
                            queryWeatherInfo(weatherCode);
                        }
                    }
                } else if ("weatherCode".equals(type)) {
                    //处理服务器返回的天气信息
                    Utility.handleWeatherResponse(WeatherActivity.this, response);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            showWeather();
                        }
                    });
                }
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        publishText.setText("同步失败");
                    }
                });
            }
        });
    }

    /**
     * 从SharedPreferences文件中读取储存的天气信息，并显示到界面上
     */
    private void showWeather() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        cityNameText.setText(prefs.getString("city_name", ""));
        temp1Text.setText(prefs.getString("temp1", ""));
        temp2Text.setText(prefs.getString("temp2", ""));
        weatherDespText.setText(prefs.getString("weather_desp", ""));
        publishText.setText("今天" + prefs.getString("publish_time", "") + "发布");
        currentDateText.setText(prefs.getString("current_date", ""));

        weatherInfoLayout.setVisibility(View.VISIBLE);
        cityNameText.setVisibility(View.VISIBLE);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.switch_city:
                Intent intent = new Intent(this, ChooseAreaActivity.class);
                intent.putExtra("from_weather_activity", true);
                startActivity(intent);
                finish();

                break;
            case R.id.refresh:

                publishText.setText("刷新中，，，");
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                String cityName = preferences.getString("city_name", "");
                if (!TextUtils.isEmpty(cityName)) {
                    queryWeatherCode(cityName);
                }
                break;
            default:
                break;
        }

    }
}
