package com.zhiyou100.doccloud.demo.bdbdemo;

import java.io.File;

import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseEntry;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.Environment;
import com.sleepycat.je.EnvironmentConfig;
import com.sleepycat.je.LockMode;
import com.sleepycat.je.OperationStatus;

public class MyBerkeleyDB {

    private Environment env;
    private Database db;

    public MyBerkeleyDB() {

    }

    public void setUp(String path, long cacheSize) {
        //进行数据库配置
        EnvironmentConfig envConfig = new EnvironmentConfig();
        //如果数据库不存在，则允许创建
        envConfig.setAllowCreate(true);
        //设置缓冲区大小
        envConfig.setCacheSize(cacheSize);
        try {
            //创建一个数据库环境--相当于一个目录
            //env：通过env创建数据库
            env = new Environment(new File(path), envConfig);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    /**
     * 数据库自身的配置
     * @param dbName
     */
    public void open(String dbName) {
        //创建一个数据库配置对象
        DatabaseConfig dbConfig = new DatabaseConfig();
        //如果数据库不存在，则创建
        dbConfig.setAllowCreate(true);
        try {
            //通过env打开数据库，dbName数据库名-dbConfig数据库配置
            db = env.openDatabase(null, dbName, dbConfig);
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (db != null) {
                db.close();
            }
            if (env != null) {
                env.close();
            }
        } catch (DatabaseException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取
     * @param key
     * @return
     * @throws Exception
     */
    public String get(String key) throws Exception {
        DatabaseEntry queryKey = new DatabaseEntry();
        DatabaseEntry value = new DatabaseEntry();
        queryKey.setData(key.getBytes("UTF-8"));

        OperationStatus status = db.get(null, queryKey, value, LockMode.DEFAULT);
        if (status == OperationStatus.SUCCESS) {
            return new String(value.getData());
        }
        return null;
    }

    /**
     * 上传berklydb
     * @param key
     * @param value
     * @return
     * @throws Exception
     */
    public boolean put(String key, String value) throws Exception {
        //将key变成字节数组
        byte[] theKey = key.getBytes("UTF-8");
        byte[] theValue = value.getBytes("UTF-8");
        //一个k-v是一个DatabaseEntry（磁条）
        OperationStatus status = db.put(null, new DatabaseEntry(theKey), new DatabaseEntry(theValue));
        if (status == OperationStatus.SUCCESS) {
            return true;
        }
        return false;
    }

    public static void main(String[] args) {
        MyBerkeleyDB mbdb = new MyBerkeleyDB();
        // 需要先建E://Test//bdb文件夹
        mbdb.setUp("D://bdb", 1000000);
        //打开或创建一个数据库
        mbdb.open("myDB");
        System.out.println("开始向Berkeley DB中存入数据...");
//        for (int i = 0; i < 20; i++) {
//            try {
//                String key = "myKey" + i;
//                String value = "myValue" + i;
//                System.out.println("[" + key + ":" + value + "]");
//                //向数据库插入k-v键值对
//                mbdb.put(key, value);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }

        for (int i = 0; i < 20; i++) {
            try {
                String key = "myKey" + i;
                String value = mbdb.get(key);
                System.out.println("[" + key + ":" + value + "]");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mbdb.close();
    }
}

