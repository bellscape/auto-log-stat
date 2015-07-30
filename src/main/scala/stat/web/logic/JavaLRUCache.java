package stat.web.logic;

import java.util.LinkedHashMap;
import java.util.Map;

// ref: http://tomyz0223.iteye.com/blog/1035686
public class JavaLRUCache<K, V> extends LinkedHashMap<K, V> {

	private LinkedHashMap<K, V> cache = null;
	private int cacheSize = 0;

	public JavaLRUCache(int cacheSize) {
		this.cacheSize = cacheSize;
		int hashTableCapacity = (int) Math.ceil(cacheSize / 0.75f) + 1;
		cache = new LinkedHashMap<K, V>(hashTableCapacity, 0.75f, true) {
			// (an anonymous inner class)
			private static final long serialVersionUID = 1;

			@Override
			protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
				System.out.println("size=" + size());
				return size() > JavaLRUCache.this.cacheSize;
			}
		};
	}

	public V put(K key, V value) {
		return cache.put(key, value);
	}
	public V get(Object key) {
		return cache.get(key);
	}

	public static void main(String[] args) {
		JavaLRUCache<String, String> lruCache = new JavaLRUCache<String, String>(5);
		lruCache.put("1", "1");
		lruCache.put("2", "2");
		lruCache.put("3", "3");
		lruCache.put("4", "4");

		System.out.println(lruCache.get("2"));
		lruCache.get("2");
		lruCache.put("6", "6");
		lruCache.put("5", "5");
		System.out.println(lruCache.get("1"));
	}

}
