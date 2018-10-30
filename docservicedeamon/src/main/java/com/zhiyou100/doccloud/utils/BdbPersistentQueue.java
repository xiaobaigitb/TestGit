package com.zhiyou100.doccloud.utils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.AbstractQueue;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.io.FileUtils;

import com.sleepycat.bind.EntryBinding;
import com.sleepycat.bind.serial.SerialBinding;
import com.sleepycat.bind.serial.StoredClassCatalog;
import com.sleepycat.bind.tuple.TupleBinding;
import com.sleepycat.collections.StoredMap;
import com.sleepycat.collections.StoredSortedMap;
import com.sleepycat.je.Database;
import com.sleepycat.je.DatabaseConfig;
import com.sleepycat.je.DatabaseException;
import com.sleepycat.je.DatabaseExistsException;
import com.sleepycat.je.DatabaseNotFoundException;
import com.sleepycat.je.EnvironmentConfig;
/**
 * 持久化队列,基于BDB实现,也继承Queue,以及可以序列化.但不等同于Queue的时,不再使用后需要关闭
 * 相比一般的内存Queue,插入和获取值需要多消耗一定的时间
 * 这里为什么是继承AbstractQueue而不是实现Queue接口,是因为只要实现offer,peek,poll几个方法即可,
 * 其他如remove,addAll,AbstractQueue会基于这几个方法去实现
 *
 * @contributor guoyun
 * @param <E>
 */
public class BdbPersistentQueue<E extends Serializable> extends AbstractQueue<E> implements
        Serializable {
    private static final long serialVersionUID = 3427799316155220967L;
    private transient BdbEnvironment dbEnv;            // 数据库环境,无需序列化
    private transient Database queueDb;             // 数据库,用于保存值,使得支持队列持久化,无需序列化
    private transient StoredMap<Long,E> queueMap;   // 持久化Map,Key为指针位置,Value为值,无需序列化
    private transient String dbDir;                 // 数据库所在目录
    private transient String dbName;                // 数据库名字
    //AtomicLong:元子类型，线程安全
    //i++线程不安全
    private AtomicLong headIndex;                   // 头部指针
    private AtomicLong tailIndex;                   // 尾部指针
    private transient E peekItem=null;              // 当前获取的值

    /**
     * 构造函数,传入BDB数据库
     *
     * @param db
     * @param valueClass
     * @param classCatalog
     */
    public BdbPersistentQueue(Database db,Class<E> valueClass,StoredClassCatalog classCatalog){
        this.queueDb=db;
        this.dbName=db.getDatabaseName();
        headIndex=new AtomicLong(0);
        tailIndex=new AtomicLong(0);
        bindDatabase(queueDb,valueClass,classCatalog);
    }
    /**
     * 构造函数,传入BDB数据库位置和名字,自己创建数据库
     *
     * @param dbDir
     * @param dbName
     * @param valueClass
     */
    public BdbPersistentQueue(String dbDir,String dbName,Class<E> valueClass){
        //headIndex=new AtomicLong(0);
        //tailIndex=new AtomicLong(0);
        this.dbDir=dbDir;
        this.dbName=dbName;
        createAndBindDatabase(dbDir,dbName,valueClass);
    }
    /**
     * 绑定数据库
     *
     * @param db
     * @param valueClass
     * @param classCatalog
     */
    public void bindDatabase(Database db, Class<E> valueClass, StoredClassCatalog classCatalog){
        EntryBinding<E> valueBinding = TupleBinding.getPrimitiveBinding(valueClass);
        if(valueBinding == null) {
            valueBinding = new SerialBinding<E>(classCatalog, valueClass);   // 序列化绑定
        }
        queueDb = db;
        queueMap = new StoredSortedMap<Long,E>(
                db,                                             // db
                TupleBinding.getPrimitiveBinding(Long.class),   //Key 序列化类型
                valueBinding,                                   // Value
                true);                                          // allow write
        //todo
        Long firstKey = ((StoredSortedMap<Long, E>) queueMap).firstKey();
        Long lastKey = ((StoredSortedMap<Long, E>) queueMap).lastKey();

        headIndex=new AtomicLong(firstKey == null ? 0 : firstKey);
        tailIndex=new AtomicLong(lastKey==null?0:lastKey+1);
    }
    /**
     * 创建以及绑定数据库
     *
     * @param dbDir
     * @param dbName
     * @param valueClass
     * @throws DatabaseNotFoundException
     * @throws DatabaseExistsException
     * @throws DatabaseException
     * @throws IllegalArgumentException
     */
    private void createAndBindDatabase(String dbDir, String dbName,Class<E> valueClass) throws DatabaseNotFoundException,
            DatabaseExistsException,DatabaseException,IllegalArgumentException{
        File envFile = null;
        EnvironmentConfig envConfig = null;
        DatabaseConfig dbConfig = null;
        Database db=null;

        try {
            // 数据库位置
            envFile = new File(dbDir);

            // 数据库环境配置
            envConfig = new EnvironmentConfig();
            envConfig.setAllowCreate(true);
            //不支持事务
            envConfig.setTransactional(false);

            // 数据库配置
            dbConfig = new DatabaseConfig();
            dbConfig.setAllowCreate(true);
            dbConfig.setTransactional(false);
            //是否要延迟写
            dbConfig.setDeferredWrite(true);

            // 创建环境
            dbEnv = new BdbEnvironment(envFile, envConfig);
            // 打开数据库
            db = dbEnv.openDatabase(null, dbName, dbConfig);
            // 绑定数据库
            bindDatabase(db,valueClass,dbEnv.getClassCatalog());

        } catch (DatabaseNotFoundException e) {
            throw e;
        } catch (DatabaseExistsException e) {
            throw e;
        } catch (DatabaseException e) {
            throw e;
        } catch (IllegalArgumentException e) {
            throw e;
        }


    }

    /**
     * 值遍历器
     */
    @Override
    public Iterator<E> iterator() {
        return queueMap.values().iterator();
    }
    /**
     * 大小
     */
    @Override
    public int size() {
        synchronized(tailIndex){
            synchronized(headIndex){
                return (int)(tailIndex.get()-headIndex.get());
            }
        }
    }

    /**
     * 插入值
     */
    @Override
    public boolean offer(E e) {
        synchronized(tailIndex){
            queueMap.put(tailIndex.getAndIncrement(), e);// 从尾部插入
//            if (tailIndex.get()==0){
//                //i++:先将值赋给再加1
//                queueMap.put(tailIndex.get(), e);// 从0插入
//            }else {
//                //增加并获取++i;先增加再返回
//                queueMap.put(tailIndex.incrementAndGet(), e);// 从尾部插入
//            }
            //将数据不保存在缓冲区，直接存入磁盘
            dbEnv.sync();
        }
        return true;
    }

    /**
     * 获取值,从头部获取
     */
    @Override
    public E peek() {
        synchronized(headIndex){
            if(peekItem!=null){
                return peekItem;
            }
            E headItem=null;
            while(headItem==null&&headIndex.get()<tailIndex.get()){ // 没有超出范围
                headItem=queueMap.get(headIndex.get());
                if(headItem!=null){
                    peekItem=headItem;
                    continue;
                }
                headIndex.incrementAndGet();    // 头部指针后移
            }
            return headItem;
        }
    }

    /**
     * 移出元素,移出头部元素
     */
    @Override
    public E poll() {
        synchronized(headIndex){
            E headItem=peek();
            if(headItem!=null){
                queueMap.remove(headIndex.getAndIncrement());
                //从磁盘上移除
                dbEnv.sync();
                peekItem=null;
                return headItem;
            }
        }
        return null;
    }

    /**
     * 关闭,也就是关闭所是用的BDB数据库但不关闭数据库环境
     */
    public void close(){
        try {
            if(queueDb!=null){
                queueDb.sync();
                queueDb.close();
            }
        } catch (DatabaseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedOperationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * 清理,会清空数据库,并且删掉数据库所在目录,慎用.如果想保留数据,请调用close()
     */
    @Override
    public void clear() {
        try {
            close();
            if(dbEnv!=null&&queueDb!=null){
                dbEnv.removeDatabase(null, dbName==null?queueDb.getDatabaseName():dbName);
                dbEnv.close();
            }
        } catch (DatabaseNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (DatabaseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } finally{
            try {
                if(this.dbDir!=null){
                    FileUtils.deleteDirectory(new File(this.dbDir));
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

}