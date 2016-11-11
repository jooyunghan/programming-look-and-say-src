package ch8.go;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Supplier;

public class Channel<A> {
  boolean isClosed = false;

  @SuppressWarnings("WeakerAccess")
  final Class<A> cls;
  final int length;
  final Queue<A> buffer = new LinkedList<>();
  final List<BlockedSend<A>> senders = new LinkedList<>();
  final List<BlockedRecv<A>> receivers = new LinkedList<>();

  public Channel(Class<A> cls, int length) {
    this.cls = cls;
    this.length = length;
  }

  static class BlockedSend<A> {
    final A value;
    final Supplier<Process> resume;

    BlockedSend(A value, Supplier<Process> resume) {
      this.value = value;
      this.resume = resume;
    }
  }

  static class BlockedRecv<A> {
    final Function<Optional<A>, Process> resume;

    BlockedRecv(Function<Optional<A>, Process> resume) {
      this.resume = resume;
    }
  }
}

