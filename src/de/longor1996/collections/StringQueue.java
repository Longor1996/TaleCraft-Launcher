package de.longor1996.collections;

import java.util.Vector;

/**
 * This is a simple N-elements String Queue.
 **/
public class StringQueue {
	private final int QueueSize;
	private final Vector<String> Queue;
	
	public StringQueue(int size){
		this.QueueSize = size;
		this.Queue = new Vector<String>();
	}
	
	/**
	 * Pushes a new element into this queue, and removes the last one if the limit is reached.
	 **/
	public void push(String string){
		this.Queue.add(string);
		
		if(this.Queue.size() > this.QueueSize)
			this.Queue.remove(0);
	}
	
	/**
	 * Looks back into the string queue.<br>
	 * If the calculated look-back index is smaller than zero,<br>
	 * this method will return null.
	 **/
	public String lookBackFromEnd(int n){
		int index = this.Queue.size() - 1;
		
		if(index < 0)
			return null;
		
		return this.Queue.get(index);
	}
	
	/**
	 * @return The last element being put into the queue, or null if the queue is empty.
	 **/
	public String peek(){
		return this.Queue.lastElement();
	}
	
	@Override
	public String toString(){
		return "StringQueue[Size: "+this.Queue.size()+", MaxSize: "+this.QueueSize+", Elements:"+this.asString()+"]";
	}
	
	private String asString() {
		StringBuffer buf = new StringBuffer("[");
		
		for(String str : this.Queue){
			buf.append(str);
			buf.append(',');
			buf.append(' ');
		}
		
		return buf.toString();
	}

	public String peekSecond() {
		return this.Queue.get(this.Queue.size()-2);
	}
}
