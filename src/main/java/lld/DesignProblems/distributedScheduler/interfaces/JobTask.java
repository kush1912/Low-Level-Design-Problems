package lld.DesignProblems.distributedScheduler.interfaces;

@FunctionalInterface
public interface JobTask {
    void execute() throws Exception;
}
