package com.argo.sdk.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import timber.log.Timber;

/**
 * Originally from RoboGuice:
 * https://github.com/roboguice/roboguice/blob/master/roboguice/src/main/java/roboguice/util/SafeAsyncTask.java
 * <p/>
 * A class similar but unrelated to android's {@link android.os.AsyncTask}.
 * <p/>
 * Unlike AsyncTask, this class properly propagates exceptions.
 * <p/>
 * If you're familiar with AsyncTask and are looking for {@link android.os.AsyncTask#doInBackground(Object[])},
 * we've named it {@link #call()} here to conform with java 1.5's {@link Callable} interface.
 * <p/>
 * Current limitations: does not yet handle progress, although it shouldn't be
 * hard to add.
 * <p/>
 * If using your own executor, you must call future() to get a runnable you can execute.
 *
 * @param <ResultT>
 */
public abstract class BackgroundAsyncTask<ResultT> implements Callable<ResultT> {
    public static final int DEFAULT_POOL_SIZE = 25;
    protected static final Executor DEFAULT_EXECUTOR = Executors.newFixedThreadPool(DEFAULT_POOL_SIZE);

    protected Executor executor;
    protected StackTraceElement[] launchLocation;
    protected FutureTask<Void> future;
    protected String name;

    /**
     * Sets executor to Executors.newFixedThreadPool(DEFAULT_POOL_SIZE) and
     * Handler to new Handler()
     */
    public BackgroundAsyncTask(String name) {
        this.executor = DEFAULT_EXECUTOR;
        this.name = name;
    }

    public FutureTask<Void> future() {
        future = new FutureTask<Void>(newTask());
        return future;
    }

    public BackgroundAsyncTask<ResultT> executor(Executor executor) {
        this.executor = executor;
        return this;
    }

    public String getName() {
        return name;
    }

    public Executor executor() {
        return executor;
    }

    public void execute() {
        execute(Thread.currentThread().getStackTrace());
    }

    protected void execute(StackTraceElement[] launchLocation) {
        this.launchLocation = launchLocation;
        executor.execute(future());
    }

    public boolean cancel(boolean mayInterruptIfRunning) {
        if (future == null)
            throw new UnsupportedOperationException("You cannot cancel this task before calling future()");

        return future.cancel(mayInterruptIfRunning);
    }


    /**
     * @throws Exception, captured on passed to onException() if present.
     */
    protected void onPreExecute() throws Exception {
    }

    /**
     * @param t the result of {@link #call()}
     * @throws Exception, captured on passed to onException() if present.
     */
    @SuppressWarnings({"UnusedDeclaration"})
    protected void onSuccess(ResultT t) throws Exception {
    }

    /**
     * Called when the thread has been interrupted, likely because
     * the task was canceled.
     * <p/>
     * By default, calls {@link #onException(Exception)}, but this method
     * may be overridden to handle interruptions differently than other
     * exceptions.
     *
     * @param e an InterruptedException or InterruptedIOException
     */
    protected void onInterrupted(Exception e) {
        onException(e);
    }

    /**
     * Logs the exception as an Error by default, but this method may
     * be overridden by subclasses.
     *
     * @param e the exception thrown from {@link #onPreExecute()}, {@link #call()}, or {@link #onSuccess(Object)}
     * @throws RuntimeException, ignored
     */
    protected void onException(Exception e) throws RuntimeException {
        onThrowable(e);
    }

    protected void onThrowable(Throwable t) throws RuntimeException {
        Timber.e(t, "Throwable caught during background processing");
    }

    /**
     * @throws RuntimeException, ignored
     */
    protected void onFinally() throws RuntimeException {
    }


    protected Task<ResultT> newTask() {
        return new Task<ResultT>(this);
    }


    public static class Task<ResultT> implements Callable<Void> {
        protected final BackgroundAsyncTask<ResultT> parent;

        public Task(BackgroundAsyncTask<ResultT> parent) {
            this.parent = parent;
        }

        public Void call() throws Exception {
            long ts = System.currentTimeMillis();
            try {
                doPreExecute();
                doSuccess(doCall());

            } catch (final Exception e) {
                try {
                    doException(e);
                } catch (Exception f) {
                    // logged but ignored
                    Timber.e(f, f.getMessage());
                }

            } catch (final Throwable t) {
                try {
                    doThrowable(t);
                } catch (Exception f) {
                    // logged but ignored
                    Timber.e(f, f.getMessage());
                }
            } finally {
                doFinally();
            }
            ts = System.currentTimeMillis() - ts;
            Timber.i("task %s complete with duration %s ms", this.parent.getName(), ts);
            return null;
        }

        protected void doPreExecute() throws Exception {

        }

        protected ResultT doCall() throws Exception {
            return parent.call();
        }

        protected void doSuccess(final ResultT r) throws Exception {
            //TODO: call for callback
        }

        protected void doException(final Exception e) throws Exception {
            if (parent.launchLocation != null) {
                final ArrayList<StackTraceElement> stack = new ArrayList<StackTraceElement>(Arrays.asList(e.getStackTrace()));
                stack.addAll(Arrays.asList(parent.launchLocation));
                e.setStackTrace(stack.toArray(new StackTraceElement[stack.size()]));
            }
            Timber.e(e, e.getMessage() +", taskName=" + parent.getName());
        }

        protected void doThrowable(final Throwable e) throws Exception {
            if (parent.launchLocation != null) {
                final ArrayList<StackTraceElement> stack = new ArrayList<StackTraceElement>(Arrays.asList(e.getStackTrace()));
                stack.addAll(Arrays.asList(parent.launchLocation));
                e.setStackTrace(stack.toArray(new StackTraceElement[stack.size()]));
            }
            Timber.e(e, e.getMessage() +", taskName=" + parent.getName());
        }

        protected void doFinally() throws Exception {

        }


    }

}