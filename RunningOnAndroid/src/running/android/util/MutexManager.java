package running.android.util;

import android.util.Log;

public class MutexManager {
	private boolean isModifying;
	private String tag;
	
	
	public MutexManager(String tag) {
		super();
		this.isModifying = false;
		this.tag = tag;		
	}

	public synchronized void startToReadOrModify() {
		while(isModifying) {
			Log.i("MUTEX-" + this.tag, "wait");
			try {
				wait();
			} catch (InterruptedException e) {
				Log.i("MUTEX", "Mutex interruption");
			}
		}
		Log.i("MUTEX-" + this.tag, "awake");
		isModifying = true;
	}
	
	public synchronized void endReadingOrModifying() {
		Log.i("MUTEX-" + this.tag, "notify");
		isModifying = false;
		notifyAll();
	}
}
