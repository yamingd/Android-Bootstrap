package com.argo.sdk.tools;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.telephony.TelephonyManager;

import com.argo.sdk.AppSessionSqliteImpl;
import com.argo.sdk.core.AppSecurity;
import com.argo.sqlite.SqliteBlock;
import com.argo.sqlite.SqliteContext;

import net.sqlcipher.database.SQLiteDatabase;

/**
 * Created by user on 10/18/15.
 */
public class AppSessionDbBuilder extends AppSessionSqliteImpl {

    /**
     * 构造函数
     *
     * @param context
     * @param appInfo
     * @param packageInfo
     * @param telephonyManager
     * @param appSecurity
     */
    public AppSessionDbBuilder(Context context, ApplicationInfo appInfo, PackageInfo packageInfo, TelephonyManager telephonyManager, AppSecurity appSecurity) {
        super(context, appInfo, packageInfo, telephonyManager, appSecurity);
    }

    @Override
    protected void initAppSessionDb() {
        initBaseDevDbTemplate("dev");
        initBaseDevDbTemplate("test");

        initBaseProdDbTemplate("qqstore");
        initBaseProdDbTemplate("baidustore");
        initBaseProdDbTemplate("360store");
        initBaseProdDbTemplate("home");

        super.initAppSessionDb();
    }

    /**/
    void initBaseDevDbTemplate(final String channel){

        final SqliteContext db = new SqliteContext(this.context, DB_BASE +"_" + channel, appSecurity.getSalt());
        db.setTag("base");
        db.deleteFile();

        db.update(new SqliteBlock<SQLiteDatabase>() {

            @Override
            public void execute(SQLiteDatabase realm) {

                db.createTable(init);

                addItem(realm, "ApiBaseUrl", "http://localhost:8080");
                addItem(realm, "AppUMengId", "AppUMengId");
                addItem(realm, "AppUMengEnable", "false");
                addItem(realm, "CookieId", "x-auth");
                addItem(realm, "CookieSecret", "CookieSecret");
                addItem(realm, "AppTitle", "AppTitle");
                addItem(realm, "PushServer.host", "dev.test.com");
                addItem(realm, "PushServer.port", "9080");
                addItem(realm, "ShareDomain", "http://dev.test.com");
                addItem(realm, "wxId", "wxId");
                addItem(realm, "wxSecret", "wxSecret");
                addItem(realm, "AppName", "test");
                addItem(realm, "Channel", channel);
            }
        });

        db.close();

    }

    void initBaseProdDbTemplate(final String channel){

        final SqliteContext db = new SqliteContext(this.context, DB_BASE + "_" + channel, appSecurity.getSalt());
        db.setTag("base");
        db.deleteFile();

        db.update(new SqliteBlock<SQLiteDatabase>() {

            @Override
            public void execute(SQLiteDatabase realm) {

                db.createTable(init);

                addItem(realm, "ApiBaseUrl", "http://api.test.com");
                addItem(realm, "AppUMengId", "AppUmengId");
                addItem(realm, "AppUMengEnable", "true");
                addItem(realm, "CookieId", "x-auth");
                addItem(realm, "CookieSecret", "CookieSecret");
                addItem(realm, "PushServer.host", "api.test.com");
                addItem(realm, "PushServer.port", "9080");
                addItem(realm, "ShareDomain", "http://www.test.com");
                addItem(realm, "wxId", "wxId");
                addItem(realm, "wxSecret", "wxSecret");
                addItem(realm, "AppName", "test");
                addItem(realm, "Channel", channel);
            }
        });

        db.close();

    }

    void addItem(SQLiteDatabase engine, String key, String val){
        engine.execSQL(insert, new Object[]{key, val.getBytes(UTF_8)});
    }

}
