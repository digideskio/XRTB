package com.xrtb.db;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CountDownLatch;

import org.redisson.Config;
import org.redisson.Redisson;
import org.redisson.RedissonClient;

public enum DataBaseObject  {

	INSTANCE;

	public static final String USERS_DATABASE = "users-database";
	public static final String MASTER_BLACKLIST = "master-blacklist";
	
	static RedissonClient redisson;
	static ConcurrentMap<String, User> map;
	static Set<String> set;

	static String DBNAME = USERS_DATABASE;

	public static DataBaseObject getInstance() {
		return INSTANCE;
	}

	public static DataBaseObject getInstance(Config cfg) {
		redisson = Redisson.create(cfg);
		map = redisson.getMap(USERS_DATABASE);
		set = redisson.getSet(MASTER_BLACKLIST);
		return INSTANCE;
	}

	public static DataBaseObject getInstance(String name) throws Exception{
		Config cfg = new Config();
		cfg.useSingleServer()
    	.setAddress("localhost:6379")
    	.setConnectionPoolSize(128);
		redisson = Redisson.create(cfg);
		map = redisson.getMap(USERS_DATABASE);
		set = redisson.getSet(MASTER_BLACKLIST);
		return INSTANCE;
	}
	
	public static boolean isBlackListed(String test) {
		if (set == null)
			return false;
		return set.contains(test);
	}

	public User get(String userName) throws Exception {
		synchronized (INSTANCE) {
			return map.get(userName);
		}

	}

	public Set keySet() throws Exception {
		synchronized (INSTANCE) {
			return map.keySet();
		}
	}

	public void put(User u) throws Exception {

	
		synchronized (INSTANCE) {
			map.put(u.name,u);
		}
	}
	
	public void addToBlackList(String domain) {

		synchronized (INSTANCE) {
			set.add(domain);
		}
	}
	
	public void addToBlackList(List<String> list) {
		synchronized (INSTANCE) {
			set.addAll(list);
		}
	}
	
	public void removeFromBlackList(String domain) {
		synchronized (INSTANCE) {
			set.remove(domain);
		}
	}
	
	public List<String> getBlackList() {
		List<String> blackList = new ArrayList();
		Iterator<String> iter = set.iterator();
		while(iter.hasNext()) {
			try {
				
				blackList.add(iter.next());
			} catch (Exception error) {
				error.printStackTrace();
			}
		}
		Collections.sort(blackList);;
		return blackList;
	}
	
	public void clearBlackList() {
		if (set == null)
			return;
		
		synchronized(INSTANCE) {
			set.clear();
		}
	}
	
	public synchronized void clear() throws Exception {
		
		synchronized(INSTANCE) {
			map.clear();
		}
	}

	public void remove(String who) throws Exception {
		synchronized (INSTANCE) {
			map.remove(who);
		}
	}
}
