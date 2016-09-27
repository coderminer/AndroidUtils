package com.coderminer.utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

public class WifiInfoUtils{
    
    private Log log = Log.getLogger(WifiInfoHelper.class);
    private WifiManager mWifiManger;
    private List<ScanResult> mWifiList;
    private List<WifiConfiguration> mWifiConfigurations;
    private Context mContext;
    

    
    private List<WifiState> mWifiState = new ArrayList<WifiState>();
    
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)){
                int s = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN);
                if(s == WifiManager.WIFI_STATE_ENABLED){
                }else if(s == WifiManager.WIFI_STATE_DISABLED){
                }
            }else if(action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)){
                List<ScanResult> scanResults = mWifiManger.getScanResults();

            }
        }
    };

    
    public WifiInfoUtils(Context c){
        mWifiManger = (WifiManager)c.getSystemService(Context.WIFI_SERVICE);
        mContext = c;
    }

    

    
    public List<WifiState> getScanResult(){
        return mWifiState;
    }
    
    
    public void open(){
        if(!mWifiManger.isWifiEnabled()){
            mWifiManger.setWifiEnabled(true);
        }else{
            log.e("open wifi state: "+mWifiManger.getWifiState());
        }
    }
    
    public void close(){
        if(mWifiManger.isWifiEnabled()){
            mWifiManger.setWifiEnabled(false);
        }else{
            log.e("close wifi state: " +mWifiManger.getWifiState() );
        }
    }
    
    public String connectedInfo(){
        if(mWifiManger.isWifiEnabled()){
            WifiInfo wi = mWifiManger.getConnectionInfo();
            if(null != wi){
                return wi.getSSID().replace("\"", "");
            }
        }
        
        return null;
    }
    
    public int checkState(){
        return mWifiManger.getWifiState();
    }
    
    public List<WifiConfiguration> getConfiguration(){
        return mWifiManger.getConfiguredNetworks();
    }
    
    public void connectConfiguration(int index){
        if(index > mWifiConfigurations.size()){
            return;
        }
        mWifiManger.enableNetwork(mWifiConfigurations.get(index).networkId, true);
    }
    
    public void startScan(){
        mWifiManger.startScan();
        
    }
    
    public List<ScanResult> getWifiList(){
        return mWifiList;
    }
    
    public List<WifiState> getWifiState(){
        
        mWifiList = mWifiManger.getScanResults();
        
        mWifiState.clear();
        log.e("wifi scan list "+mWifiList.size());
        for(ScanResult sr : mWifiList){
            WifiState ws = new WifiState();
            ws.ssid = sr.SSID;
            ws.level = WifiManager.calculateSignalLevel(sr.level, 5);
            ws.type = securityType(sr.capabilities);
            ws.sec = ws.type == 0 ? false : true;
            mWifiState.add(ws);
            log.e("wifi state "+mWifiState.size());
        }
        
        Collections.sort(mWifiState);
        return mWifiState;
    }
    
    
    private int securityType(String cap){
        if(!TextUtils.isEmpty(cap)){
            if(cap.toLowerCase().contains("wpa")){
                return SecurityType.SECURITY_WPA.ordinal();
            }else if(cap.toLowerCase().contains("wep")){
                return SecurityType.SECURITY_WEP.ordinal();
            }else{
                return SecurityType.SECURITY_NONE.ordinal();
            }
        }
        
        return SecurityType.SECURITY_NONE.ordinal();
    }
    
    public void disconnectWifi(int netId){
        mWifiManger.disableNetwork(netId);
        mWifiManger.disconnect();
    }
    
    public void connect(WifiConfiguration config){
        int netId = mWifiManger.addNetwork(config);
        boolean b = mWifiManger.enableNetwork(netId, true);
        log.e(" connected with config : "+b);
    }
    
    public void connect(int netId){
        boolean b = mWifiManger.enableNetwork(netId, true);
        log.e(" connected with netId : "+netId+" conned: "+b);
    }
    
    
    
    public WifiConfiguration createWifiInfo(String ssid,String pwd,int type){
        log.e("create wifif ssid: "+ssid+" pwd: "+pwd+" type "+type);
        WifiConfiguration cfg = new WifiConfiguration();
        WifiConfiguration tempConfig = this.isExist(ssid);              
        if(tempConfig != null) {     
            mWifiManger.removeNetwork(tempConfig.networkId);     
        }
        
        cfg.SSID = "\"" + ssid + "\"";
        if( !TextUtils.isEmpty(pwd)) {
            if(type == 0){
                cfg.wepKeys[0] = "";
                cfg.allowedKeyManagement.set(0);
                cfg.wepTxKeyIndex = 0;
            }else if( type == 1 ) {
                cfg.wepKeys[0]   = "\"" + pwd + "\"";
                cfg.wepTxKeyIndex = 0;
            }else {
                cfg.preSharedKey = "\"" + pwd + "\"";
            }                  
        }
       
        return cfg;
    }
    
    private WifiConfiguration isExist(String ssid){
        List<WifiConfiguration> configs = mWifiManger.getConfiguredNetworks();
        WifiConfiguration config = null;
        if(null != configs && configs.size() > 0){
            for(WifiConfiguration c : configs){
                if(c.SSID.equals('"'+ssid+'"')){
                    config = c;
                    break;
                }
            }

        }

        return config;
    }
}