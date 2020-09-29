package raytracing.multithread;

import java.util.LinkedList;

public class WorkQueue {
	private final PoolWorker[] threads;
	private final LinkedList<Runnable> queue;

	public WorkQueue(int nThreads) {
		queue = new LinkedList<Runnable>();
		threads = new PoolWorker[nThreads];

		for (int i = 0; i < nThreads; i++) {
			threads[i] = new PoolWorker();
			threads[i].start();
		}
	}

	public void clear() {
		synchronized (queue) {
			queue.clear();
		}
	}

	public boolean IsFinished() {
		for (int i = 0; i < threads.length; i++)
			if (threads[i].working)
				return false;
		return queue.isEmpty();
	}

	public void execute(Runnable r) {
		synchronized (queue) {
			queue.addLast(r);
			queue.notify();
		}
	}

	private class PoolWorker extends Thread {
		boolean working;

		public void run() {
			working = true;
			Runnable r;

			while (true) {
				synchronized (queue) {
					while (queue.isEmpty()) {
						working = false;
						try {
							queue.wait();
						} catch (InterruptedException ignored) {
						}
					}
					working = true;
					r = (Runnable) queue.removeFirst();
				}
				// If we don't catch RuntimeException,
				// the pool could leak threads
				try {
					r.run();
				} catch (RuntimeException e) {
					// You might want to log something here
					System.out.println(e);
				}
			}
		}
	}
}
