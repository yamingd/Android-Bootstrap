package com.argo.sdk.core;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.location.Location;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.util.ArrayMap;

import com.argo.sdk.AppSession;
import com.argo.sdk.http.APIClientProvider;
import com.argo.sdk.protobuf.PAppSession;
import com.argo.sdk.providers.UserAgentProvider;
import com.argo.sdk.realm.RealmBlock;
import com.argo.sdk.realm.RealmContext;
import com.argo.sdk.util.AssetsUtil;
import com.argo.sdk.util.HMAC;
import com.argo.sdk.util.Strings;
import com.google.protobuf.InvalidProtocolBufferException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;
import okio.ByteString;
import timber.log.Timber;


/**
 * Created by user on 11/6/14.
 */
public class AppSessionImpl implements AppSession {

    public static final String KEY_SESSION = "session";
    public static final String GUEST = "Guest";
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    public static final String DB_BASE = "base";
    public static final String DB_COMMON = "common";

    protected ApplicationInfo appInfo;

    protected PackageInfo packageInfo;

    protected TelephonyManager telephonyManager;

    protected Context context;

    public static final String D_ANDROID = "1";

    /**
     * 获取用户id, 姓名
     */
    public static PAppSession current = null;
    /**
     * 单例
     */
    public static AppSession instance = null;

    /**
     * 配置信息
     */
    public Map<String, Object> config = new ArrayMap<String, Object>();

    /**
     * 暂存的变量
     */
    public Map<String, Object> flash = new ArrayMap<String, Object>();

    /**
     * 生成签名的Http Header值
     */
    public Map<String, String> header = null;

    private RealmContext sessionDb = null;
    private byte[] salt = null;
    private String appName = null;

    /**
     * 构造函数
     * @param context
     * @param appInfo
     * @param packageInfo
     * @param telephonyManager
     */
    public AppSessionImpl(String appName, Context context, ApplicationInfo appInfo, PackageInfo packageInfo, TelephonyManager telephonyManager){
        this.appName = appName;
        this.context = context;
        this.appInfo = appInfo;
        this.packageInfo = packageInfo;
        this.telephonyManager = telephonyManager;

        UserAgentProvider.APP_NAME = appName;

        loadDbTemplateAndSalt();

        sessionDb = new RealmContext(context, this, DB_BASE, salt, 0, new AppSessionModule(), null);
        sessionDb.queryAndClose(new RealmBlock() {
            @Override
            public void execute(Realm realm) {

                RealmResults<AppSessionData> rs = realm.allObjects(AppSessionData.class);
                for (int i=0; i<rs.size(); i++){
                    AppSessionData item = rs.get(i);
                    if (item.getKey().equalsIgnoreCase(KEY_SESSION)){
                        initSession(item);
                    }else{
                        initConfig(item);
                    }
                }

            }
        });

         if (current == null){
            newAnonymousUser(getBuilder());
        }

        initHeaders();

        instance = this;

        // Timber.d("Session: %s", current.toString());

    }

    @Override
    public boolean isDebug() {
        return BuildConfig.DEBUG;
    }

    @Override
    public Map<String, String> getHttpHeader() {
        return header;
    }

    @Override
    public byte[] getSalt() {
        return salt;
    }

    private void loadDbTemplateAndSalt(){

        try {
            InputStream in = this.context.getResources().getAssets().open("cert");
            byte[] tmp = new byte[128];
            in.read(tmp);
            salt = Arrays.copyOfRange(tmp, 10, 74);
        } catch (IOException e) {
            Timber.e(e, "load cert");
        }

        File file = RealmContext.getDbFolder(context, DB_BASE);
        if (file.exists()) {
            Timber.i("Realm file exists: %s", file);
            return;
        }
        boolean ok = AssetsUtil.copyFromAssets(context, file, DB_BASE);
        Timber.i("Copy Done: %s, %s", ok, file);

    }

    /**
     *
     * Save Session to disk
     */
    private void save(){

        sessionDb.update(new RealmBlock() {
            @Override
            public void execute(Realm realm) {

                AppSessionData session = new AppSessionData();
                session.setData(current.toByteArray());
                session.setKey(KEY_SESSION);
                realm.copyToRealmOrUpdate(session);

            }
        });

        sessionDb.close();
    }

    @Override
    public void rememberLocation(Location location){
        this.flash.put("location", location);
    }

    /**
     * 登录时调用
     * @param userId
     * @param userName
     * @param realName
     * @param userKind
     * @param profileImageUrl
     */
    @Override
    public void remember(long userId, String userName, String realName, int userKind, String profileImageUrl, boolean demo){
        PAppSession.Builder builder = getBuilder();
        builder.setUserId(userId);
        builder.setUserName(userName);
        builder.setUserKind(userKind);
        builder.setRealName(realName);
        builder.setProfileImageUrl(profileImageUrl);
        builder.setSessionId(System.currentTimeMillis() / 1000 + "");
        builder.setUserDemo(demo ? 1 : 0);
        AppSessionImpl.current = builder.build();
        save();
        initHeaders();

        markVars(false);

        if (APIClientProvider.instance != null){
            APIClientProvider.instance.resetHeaders();
        }
    }

    private void markVars(boolean out){



    }

    /**
     * load session from disk
     * @return
     */
    private PAppSession initSession(AppSessionData session){

        try {
            AppSessionImpl.current = PAppSession.parseFrom(session.getData());
            PAppSession.Builder builder = getBuilder();
            wrapDevice(builder);
            wrapTestUser(builder);
            builder.setSessionId(System.currentTimeMillis() / 1000 + "");

            AppSessionImpl.current = builder.build();
            save();
        } catch (InvalidProtocolBufferException e) {
            Timber.e(e, e.getMessage());
            PAppSession.Builder builder = getBuilder();
            wrapDevice(builder);
            wrapTestUser(builder);
            newAnonymousUser(builder);
            AppSessionImpl.current = builder.build();
            save();
        } catch (Exception e) {
            Timber.e(e, e.getMessage());
        }

        markVars(false);

        return current;
    }

    private void wrapTestUser(PAppSession.Builder builder){
//        builder.setUserId(1399);
//        builder.setUserKind(1);
//        builder.setUserDemo(0);
//        builder.setRealName("林小明2");
//        builder.setUserName("ocw01");
    }

    /**
     * wrap device info into Session
     */
    private void wrapDevice(PAppSession.Builder builder){
        builder.setDeviceId(getIMEI());
        builder.setAppName(this.appName);
        builder.setPackageVersion(packageInfo.versionName);
        builder.setPackageName(UserAgentProvider.APP_NAME);
        builder.setOsName(String.format("Android %s", Build.VERSION.SDK_INT));
        builder.setOsVersion(Build.VERSION.RELEASE);
        builder.setLocaleIdentifier(context.getResources().getConfiguration().locale.getLanguage());
        builder.setDeviceToken(HMAC.hmacSHA256(builder.getDeviceId(), builder.getAppName()));
    }

    /**
     * 获取设备ID
     * @return
     */
    public String getIMEI(){
        //获取imei
        String imei_id = telephonyManager.getDeviceId();
        if(Strings.isEmpty(imei_id)){
            //获取smi
            imei_id = telephonyManager.getSimSerialNumber();
            //如果无法获取，用默认
            if(Strings.isEmpty(imei_id)){
                imei_id = "default-imei";
            }
        }
        return imei_id;
    }

    private String checkValue(String str) {
        if ((str == null) || (str.length() == 0)) {
            return "'TM.ERROR'";
        }

        return "\"" + str + "\"";
    }

    /**
     * load config from disk.
     */
    private void initConfig(AppSessionData item){

        byte[] value = item.getData();
        String str = new String(value, UTF_8);

        if ("AppUMengEnable".equalsIgnoreCase(item.getKey()) ||
                "AppApns".equalsIgnoreCase(item.getKey())){

            config.put(item.getKey(), Boolean.valueOf(str));

        }else{

            config.put(item.getKey(), str);

        }

        if (BuildConfig.DEBUG) {
            Timber.d("Config Item, %s = %s", item.getKey(), str);
        }
    }

    /**
     * 获取Session Builder
     * @return
     */
    private PAppSession.Builder getBuilder(){
        if (current == null){
            PAppSession.Builder builder = PAppSession.newBuilder();
            wrapDevice(builder);
            return builder;
        }else{
            return PAppSession.newBuilder(AppSessionImpl.current);
        }
    }

    /**
     * 注销时调用
     * r session from disk
     */
    @Override
    public void clear(){
        newAnonymousUser(getBuilder());
        initHeaders();

        markVars(true);

        if (APIClientProvider.instance != null){
            APIClientProvider.instance.resetHeaders();
        }
    }

    /**
     * 新建匿名用户
     * @param builder
     */
    private void newAnonymousUser(PAppSession.Builder builder){
        builder.setUserId(0);
        builder.setUserName(GUEST);
        builder.setUserKind(0);
        builder.setRealName(GUEST);
        builder.setProfileImageUrl("");
        builder.setSessionId(System.currentTimeMillis() / 1000 + "");
        wrapTestUser(builder);
        AppSessionImpl.current = builder.build();
        save();
    }

    /**
     * is Anonymous user.
     * @return
     */
    @Override
    public boolean isAnonymous(){
        return current.getUserId()  == 0;
    }

    /**
     * does User sign in.
     * @return
     */
    @Override
    public boolean isSignIn(){
        return current.getUserId()  > 0;
    }

    /**
     * to Header Map for Http Request.
     * @return
     */
    private Map<String, String> initHeaders(){
        String auth = authValue();
        header = new HashMap<String, String>();
        header.put("X-sessionid", current.getSessionId());
        header.put("X-app", current.getAppName());
        header.put("X-cid", current.getDeviceId());
        header.put("X-client", current.getDeviceName());
        header.put("X-ver", current.getPackageVersion());
        header.put("X-auth", auth);
        header.put("X-lang", current.getLocaleIdentifier());
        header.put("X-ctype", D_ANDROID);
        return header;
    }


    /**
     * 对用户数据做签名，通过HTTP传回服务端
     * @return
     */
    private String authValue(){
        String cookieId = config.get("CookieId") + "";
        String cookieSecret = config.get("CookieSecret") + "";

        cookieSecret = HMAC.md5(cookieSecret);

        long timestamp = System.currentTimeMillis() / 1000;
        String uv = current.getUserId() + "";

        uv = ByteString.of(uv.getBytes(UTF_8)).base64Url();

        String sign = String.format("%s|%s|%s|%s", timestamp, cookieSecret, cookieId, uv);

        sign = HMAC.sha256(sign);

        sign = String.format("%s|%s|%s", uv, timestamp, sign);

        return sign;
    }

    /**
     *
     * @param key
     * @param defaultValue
     * @param <T>
     * @return
     */
    @Override
    public <T> T getConfigValue(String key, T defaultValue){
        Object val = config.get(key);
        if (val == null){
            return defaultValue;
        }
        return (T)val;
    }

    
    @Override
    public PAppSession get() {
        return current;
    }

    public ApplicationInfo getAppInfo() {
        return appInfo;
    }

    @Override
    public String toString() {
        if (current == null){
            return "NULL";
        }

        return "AppSessionImpl{" + current.getUserId() + ", " + current.getUserName() + "}";
    }

    @Override
    public void close() throws IOException {
        this.sessionDb.close();
    }
}
