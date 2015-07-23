package com.argo.sdk.realm;

import android.content.Context;
import android.os.Looper;

import com.argo.sdk.AppSession;

import java.io.File;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmMigration;
import timber.log.Timber;

/**
 * https://realm.io/docs/java/latest/
 *
 * Created by user on 6/15/15.
 */
public class RealmContext{

    private String name;
    private AppSession appSession;
    private Context context;
    private byte[] salt;
    private Object module;
    private int version;
    private RealmMigration realmMigration;

    public RealmContext(Context context, AppSession appSession, String name, byte[] salt, int version, Object module, RealmMigration realmMigration) {
        this.name = name;
        this.context = context;
        this.salt = salt;
        this.appSession = appSession;
        this.module = module;
        this.version = version;
        this.realmMigration = realmMigration == null ? new RealmMigratory.MigrationImpl(version) : realmMigration;
    }

    /**
     *
     * @param name
     * @return
     */
    public Realm open(String name){
        File path = getDbFolder(context, name);
        RealmConfiguration.Builder builder = new RealmConfiguration.Builder(path.getParentFile())
                                                            .encryptionKey(salt)
                                                            .name(name)
                                                            .migration(this.realmMigration)
                                                            .schemaVersion(version);
        if (this.module != null){
            builder.setModules(this.module);
        }
        return Realm.getInstance(builder.build());
    }

    public static File getDbFolder(Context context, String name){
        File path = context.getDatabasePath(name);
        Timber.d("getDbFolder: %s", path);

        try {
            if (path.exists()){
                return path;
            }
        } finally {

        }
        File folder = path.getParentFile();
        if (!folder.exists()){
            folder.mkdirs();
            Timber.d("Create getDbFolder: %s", path);
        }
        return path;
    }

    public boolean exists(){
        if (name != null){
            File file = getDbFolder(context, name);
            return file.exists();
        }else{
            return true;
        }
    }

    public Realm open(){
        Realm realm = getInstance();
        return realm;
    }

    /**
     * 在主线程查询
     * @param block
     */
    public Realm query(RealmBlock block){
        Realm realm = getInstance();
        try {
            block.execute(realm);
        } catch (RuntimeException e) {
            Timber.e(e, e.getMessage());
        }finally {
            return realm;
        }
    }

    /**
     *
     * 在子线程查询完后关闭数据库，并且数据仅在block内读取
     *
     * @param block
     */
    public void queryAndClose(RealmBlock block){
        Realm realm = getInstance();
        try {
            block.execute(realm);
        } catch (RuntimeException e) {
            Timber.e(e, e.getMessage());
        }finally {
            realm.close();
            realm = null;
        }
    }

    /**
     *
     * @param realm
     */
    public void tryClose(Realm realm){
        if (Looper.myLooper().equals(Looper.getMainLooper())){
            return;
        }
        realm.close();
    }

    /**
     * 获取当前线程下的实例
     * @return
     */
    private Realm getInstance(){
        if (null == name){
            name = "user_" + appSession.get().getUserId();
        }

        Timber.d("get Realm Instance. name=%s", name);
        Realm realm = null;

        if (realm == null) {
            realm = open(name);
        }else{
            realm.refresh();
        }

        return realm;
    }

    /**
     * 更新、删除
     * @param block
     */
    public void update(final RealmBlock block){

        Realm realm = getInstance();
        try {

            realm.executeTransaction(new Realm.Transaction() {
                @Override
                public void execute(Realm realm) {
                    block.execute(realm);
                }
            });

        } catch (Exception e) {
            Timber.e(e, e.getMessage());
        }finally {
            realm.close();
            realm = null;
        }

    }

    @Override
    public String toString() {
        return "RealmContext{" +
                "name='" + name + '\'' +
                '}';
    }

    public void close() {

    }
}
