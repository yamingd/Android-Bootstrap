package com.argo.sdk.core;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.support.v4.util.ArrayMap;
import android.telephony.TelephonyManager;

import com.argo.sdk.AppSession;
import com.argo.sdk.BootConstants;
import com.argo.sdk.http.APIClientProvider;
import com.argo.sdk.protobuf.PAppSession;
import com.argo.sdk.util.HMAC;
import com.google.protobuf.InvalidProtocolBufferException;
import com.argo.sdk.util.Strings;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

import timber.log.Timber;


/**
 * Created by user on 11/6/14.
 */
public abstract class AppSessionAbstractImpl implements AppSession {

    public static final String KEY_SESSION = "session";
    public static final String GUEST = "Guest";
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    public static final String DB_BASE = "base";

    protected ApplicationInfo appInfo;

    protected PackageInfo packageInfo;

    protected TelephonyManager telephonyManager;

    protected Context context;

    protected AppSecurity appSecurity;

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
     * 生成签名的Http Header值
     */
    public Map<String, String> header = null;

    private String cookieId = null;
    private String cookieSecret = null;

    /**
     * 构造函数
     * @param context
     * @param appInfo
     * @param packageInfo
     * @param telephonyManager
     */
    public AppSessionAbstractImpl(Context context, ApplicationInfo appInfo, PackageInfo packageInfo, TelephonyManager telephonyManager, AppSecurity appSecurity){
        this.context = context;
        this.appInfo = appInfo;
        this.packageInfo = packageInfo;
        this.telephonyManager = telephonyManager;
        this.appSecurity = appSecurity;

        this.initAppSessionDb();

        if (current == null){
            newAnonymousUser(getBuilder());
        }

        cookieId = config.get("CookieId") + "";
        cookieSecret = config.get("CookieSecret") + "";

        initHeaders();

        // Timber.d("Session: %s", current.toString());

    }

    protected abstract void initAppSessionDb();

    @Override
    public Map<String, String> getHttpHeader() {
        return header;
    }

    @Override
    public byte[] getSalt() {
        return appSecurity.getSalt();
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
        current = builder.build();
        save();
        initHeaders();

        if (APIClientProvider.instance != null){
            APIClientProvider.instance.resetHeaders();
        }
    }

    /**
     * load session from disk
     * @return
     */
    protected PAppSession initSession(AppSessionData session){

        try {

            current = PAppSession.parseFrom(session.getData());
            PAppSession.Builder builder = getBuilder();
            wrapDevice(builder);
            wrapTestUser(builder);
            builder.setSessionId(System.currentTimeMillis() / 1000 + "");

            current = builder.build();
            save();

        } catch (InvalidProtocolBufferException e) {
            Timber.e(e, e.getMessage());
            PAppSession.Builder builder = getBuilder();
            wrapDevice(builder);
            wrapTestUser(builder);
            newAnonymousUser(builder);
            current = builder.build();
            save();
        } catch (Exception e) {
            Timber.e(e, e.getMessage());
        }

        return current;
    }

    private void wrapTestUser(PAppSession.Builder builder){

    }

    /**
     * wrap device info into Session
     */
    private void wrapDevice(PAppSession.Builder builder){
        builder.setDeviceId(getIMEI());
        builder.setAppName(BootConstants.APP_NAME);
        builder.setPackageVersion(packageInfo.versionName);
        builder.setPackageName(BootConstants.APP_NAME);
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
    protected void initConfig(AppSessionData item){

        byte[] value = item.getData();
        String str = new String(value, UTF_8);

        if ("AppUMengEnable".equalsIgnoreCase(item.getKey()) ||
                "AppApns".equalsIgnoreCase(item.getKey())){

            config.put(item.getKey(), Boolean.valueOf(str));

        }else{

            config.put(item.getKey(), str);

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
            return PAppSession.newBuilder(current);
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
        current = builder.build();
        save();
    }

    /**
     * is Anonymous user.
     * @return
     */
    @Override
    public boolean isAnonymous(){
        Timber.i("session: %s", this);
        return current.getUserId()  == 0;
    }

    @Override
    public String signRequest(String url) {
        String uv = current.getUserId() + "";
        if (url.contains("?")){
            url = url.substring(0, url.indexOf("?"));
        }
        //Timber.d("signRequest start: %s", url);
        String sign = appSecurity.signRequest(cookieId, cookieSecret, url, uv);
        return sign;
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
        header = new ArrayMap<String, String>();
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
        String uv = current.getUserId() + "";
        String sign = appSecurity.authHeader(cookieId, cookieSecret, uv);
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
    public String dump() {
        StringBuilder s = new StringBuilder("session={");
        if (current != null) {
            s.append("userId=").append(current.getUserId()).append(", ");
            s.append("userName=").append(current.getUserName()).append(", ");
        }
        s.append("}");
        return s.toString();
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
        return dump();
    }

    @Override
    public void close() throws IOException {

    }
}
