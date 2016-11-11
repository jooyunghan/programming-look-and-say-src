package ch7.cont;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class Cont<A> {
  public static <A> Cont<A> read(Function<Optional<A>, Cont<A>> next) {
    return new Read<>(next);
  }

  public static <A> Cont<A> write(A v, Supplier<Cont<A>> next) {
    return new Write<>(v, next);
  }

  public static <A> Cont<A> done() {
    return new Done<>();
  }

  public static <A> Cont<A> pipe(Supplier<Cont<A>> pre, Supplier<Cont<A>> post) {
    return new Pipe<>(pre, post);
  }

  public static <A> void run(Cont<A> process, Consumer<A> consumer) {
    Deque<Cont<A>> processes = new ArrayDeque<>();
    Deque<Cont<A>> readers = new ArrayDeque<>();

    processes.add(process);
    while (!processes.isEmpty() || !readers.isEmpty()) {
      if (processes.isEmpty()) { // 실행할 프로세스가 없으므로
                                 // 읽기 대기 중인 프로세스를 깨워준다
        Read<A> r = (Read<A>) readers.pop();
        processes.push(r.next.apply(Optional.empty()));
      }
      Cont<A> c = processes.pop();
      if (c instanceof Done) {
        processes.clear(); // 실행 대기 중인 프로세스를 함께 종료한다
      } else if (c instanceof Read) {
        readers.push(c);
      } else if (c instanceof Write) {
        Write<A> w = (Write<A>) c;
        if (readers.isEmpty()) {
          consumer.accept(w.value);
          processes.push(w.next.get());
        } else {
          processes.push(w.next.get());
          Read<A> r = (Read<A>) readers.pop();
          processes.push(r.next.apply(Optional.of(w.value)));
        }
      } else {
        Pipe<A> pipe = (Pipe<A>) c;
        processes.push(pipe.pre.get());
        processes.push(pipe.post.get());
      }
    }
  }
}

class Read<A> extends Cont<A> {
  final Function<Optional<A>, Cont<A>> next;

  Read(Function<Optional<A>, Cont<A>> next) {
    this.next = next;
  }
}

class Write<A> extends Cont<A> {
  final A value;
  final Supplier<Cont<A>> next;

  Write(A value, Supplier<Cont<A>> next) {
    this.value = value;
    this.next = next;
  }
}

class Done<A> extends Cont<A> {
}

class Pipe<A> extends Cont<A> {
  final Supplier<Cont<A>> pre;
  final Supplier<Cont<A>> post;

  Pipe(Supplier<Cont<A>> pre, Supplier<Cont<A>> post) {
    this.pre = pre;
    this.post = post;
  }
}