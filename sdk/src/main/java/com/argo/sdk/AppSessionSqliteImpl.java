package com.argo.sdk;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.telephony.TelephonyManager;

import com.argo.sqlite.SqliteBlock;
import com.argo.sqlite.SqliteContext;
import com.argo.sdk.core.AppSecurity;
import com.argo.sdk.core.AppSessionAbstractImpl;
import com.argo.sdk.core.AppSessionData;
import com.argo.sdk.util.AssetsUtil;

import net.sqlcipher.Cursor;
import net.sqlcipher.database.SQLiteDatabase;

import java.io.File;
import java.io.IOException;

import javax.inject.Inject;

import timber.log.Timber;


/**
 * Created by user on 11/6/14.
 */
public class AppSessionSqliteImpl extends AppSessionAbstractImpl {

    public static final String UPDATE_APP_SESSION_SET_DATA_WHERE_KEY = "replace into app_session(key, data)values(?, ?)";
    public static final String SELECT_ALL = "select key, data from app_session";
    public static final String insert = "insert into app_session(key, data)values(?, ?)";
    public static final String init = "create table if not exists app_session(key text primary key, data blob) without rowid";

    private SqliteContext sessionDb;

    /**
     * 构造函数
     *
     * @param context
     * @param appInfo
     * @param packageInfo
     * @param telephonyManager
     * @param appSecurity
     */
    @Inject
    public AppSessionSqliteImpl(Context context, ApplicationInfo appInfo, PackageInfo packageInfo, TelephonyManager telephonyManager, AppSecurity appSecurity) {
        super(context, appInfo, packageInfo, telephonyManager, appSecurity);
        instance = this;
    }

    @Override
    protected void initAppSessionDb() {
        initDbFrom();
    }

    private void initDbFrom() {

        sessionDb = new SqliteContext(context, DB_BASE, appSecurity.getSalt());
        sessionDb.setTag("base");

        loadDbTemplate();

        final AppSessionData[] sessionData = new AppSessionData[]{null};

        sessionDb.query("AppSession-loadall", new SqliteBlock<SQLiteDatabase>() {

            @Override
            public void execute(SQLiteDatabase engine) {

                Cursor cursor = engine.rawQuery(SELECT_ALL, null);

                int total = cursor.getCount();

                cursor.moveToFirst();

                try {

                    AppSessionData item = new AppSessionData();
                    for (int i = 0; i < total; i++) {

                        item.setKey(cursor.getString(0));
                        item.setData(cursor.getBlob(1));

                        if (item.getKey().equalsIgnoreCase(KEY_SESSION)){
                            sessionData[0] = item;
                        }else{
                            initConfig(item);
                        }

                        cursor.moveToNext();
                    }
                } catch (Exception e) {
                    Timber.e(e, "init Session DbFrom");
                }finally {
                    cursor.close();
                }

            }
        });

        if (sessionData[0] != null) {
            initSession(sessionData[0]);
        }
    }

    private void loadDbTemplate(){

        File file = sessionDb.getDbFolder(DB_BASE);
        if (file.exists()) {
            Timber.i("AppSession Db file exists: %s", file);
            return;
        }

        boolean ok = AssetsUtil.copyFromAssets(context, file, DB_BASE);
        Timber.i("Copy Done: %s, %s", ok, file);

    }

    @Override
    public void save() {

        sessionDb.update(new SqliteBlock<SQLiteDatabase>() {
            @Override
            public void execute(SQLiteDatabase engine) {
                engine.execSQL(UPDATE_APP_SESSION_SET_DATA_WHERE_KEY, new Object[]{KEY_SESSION, current.toByteArray()});
            }
        });

    }

    @Override
    public void remember(long userId, String userName, String realName, int userKind, String profileImageUrl, boolean demo) {
        super.remember(userId, userName, realName, userKind, profileImageUrl, demo);
    }

    @Override
    public void clear() {
        super.clear();
    }

    @Override
    public void close() throws IOException {
        if (this.sessionDb != null) {
            this.sessionDb.close();
        }
    }

}
