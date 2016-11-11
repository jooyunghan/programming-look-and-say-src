package ch8.go;

import ch8.go.clocks.VirtualClock;

import java.time.Instant;
import java.util.Deque;
import java.util.LinkedList;
import java.util.PriorityQueue;

public class Scheduler {

  private Deque<Process> processes = new LinkedList<>();
  private PriorityQueue<Event> events = new PriorityQueue<>();
  private Clock clock = new VirtualClock();

  public Instant now() {
    return clock.instant();
  }

  public void advanceTo(Instant when) {
    clock.advanceTo(when);
  }

  public void scheduleAt(Instant instant, Process process) {
    events.add(new Event(instant, process));
  }

  public void schedule(Process process) {
    processes.addLast(process);
  }

  public boolean hasNext() {
    return !processes.isEmpty() || !events.isEmpty();
  }

  public Process next() {
    // all processes are blocked, but there are sleeping processes
    if (processes.isEmpty()) {
      advanceTo(events.element().due);
      processes.addLast(events.remove().process);
    }
    while (!events.isEmpty() && expired(events.element().due)) {
      processes.addLast(events.remove().process);
    }
    return processes.remove();
  }

  private boolean expired(Instant test) {
    return now().isAfter(test);
  }

  static class Event implements Comparable<Event> {
    final Instant due;
    final Process process;

    Event(Instant due, Process process) {
      this.due = due;
      this.process = process;
    }

    @Override
    public int compareTo(Event o) {
      return due.compareTo(o.due);
    }

    @Override
    public String toString() {
      return "Event{" +
          "due=" + due +
          ", process=" + process +
          '}';
    }
  }
}
