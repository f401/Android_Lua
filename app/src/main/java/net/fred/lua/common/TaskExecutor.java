package net.fred.lua.common;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TaskExecutor {
    private final ArrayList<Runnable> tasks;
    private final int totalTaskCount;

    public TaskExecutor(ArrayList<Runnable> tasks, int count) {
        this.tasks = tasks;
        this.totalTaskCount = count;
    }

    public ExecutorService executeTasks() {
        ExecutorService executor = Executors.newCachedThreadPool();
        for (Runnable task : tasks) {
            executor.execute(task);
        }
        return executor;
    }

    public int getTotalTaskCount() {
        return totalTaskCount;
    }

    public static final class Builder {
        private final ArrayList<Runnable> tasks;

        public Builder() {
            tasks = new ArrayList<>();
        }

        public Builder addTask(Runnable task) {
            tasks.add(task);
            return this;
        }

        public TaskExecutor build() {
            return new TaskExecutor(tasks, tasks.size());
        }
    }
}
