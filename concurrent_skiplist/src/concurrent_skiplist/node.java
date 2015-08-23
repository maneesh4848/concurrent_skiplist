package concurrent_skiplist;

import java.util.concurrent.locks.*;

public final class node
{
	private final Lock lock = new ReentrantLock();
	final int data;
	final node[] next;
	volatile boolean mark = false;
	volatile boolean fulllink = false;
	int toplevel;
	
	//Constructor
	public node(int data, int height)
	{
		this.data = data;
		this.toplevel = height;
		this.next = new node[height+1];
	}
	
	public void lock()
	{
		lock.lock();
	}
	public void unlock()
	{
		lock.unlock();
	}
}
