package io.lundie.michael.viewcue.utilities;

import android.util.Log;

/**
 * Class modified from : https://stackoverflow.com/a/826283
 */
public class CallbackRunnable implements Runnable {

    private final Runnable task;
    private final RunnableInterface runnableInterface;

    public CallbackRunnable(Runnable task, RunnableInterface runnableInterface) {
        this.task = task;
        this.runnableInterface = runnableInterface;
    }

    @Override
    public void run() {
        Log.i("CALLBACK", "TEST: called complete.");
        task.run();
        runnableInterface.complete();
    }
}
