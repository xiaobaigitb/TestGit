package com.zhiyou100.doccloud.utils;

import java.io.File;

import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
/**
 * BDB数据库环境,可以缓存StoredClassCatalog并共享
 *
 * @contributor guoyun
 */
public class BdbEnvironment extends Environment {
    StoredClassCatalog classCatalog;
    Database classCatalogDB;

    /**
     * Constructor
     *
     * @param envHome 数据库环境目录
     * @param envConfig config options  数据库换纪念馆配置
     * @throws DatabaseException
     */
    public BdbEnvironment(File envHome, EnvironmentConfig envConfig) throws DatabaseException {
        super(envHome, envConfig);
    }

    /**
     * 返回StoredClassCatalog
     * @return the cached class catalog
     */
    public StoredClassCatalog getClassCatalog() {
        if(classCatalog == null) {
            DatabaseConfig dbConfig = new DatabaseConfig();
            dbConfig.setAllowCreate(true);
            try {
                //事务、数据库名、配置项
                classCatalogDB = openDatabase(null, "classCatalog", dbConfig);
                classCatalog = new StoredClassCatalog(classCatalogDB);
            } catch (DatabaseException e) {
                // TODO Auto-generated catch block
                throw new RuntimeException(e);
            }
        }
        return classCatalog;
    }

    @Override
    public synchronized void close() throws DatabaseException {
        if(classCatalogDB!=null) {
            classCatalogDB.close();
        }
        super.close();
    }

}
