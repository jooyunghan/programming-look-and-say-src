package ch8.go;

import ch8.go.Channel.BlockedRecv;
import ch8.go.Channel.BlockedSend;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Function;

@SuppressWarnings("unchecked")
public class Interpreter<A> {
  private Scheduler scheduler = new Scheduler();

  public Interpreter(Command<A> mainCommand) {
    scheduler.schedule(new Process(true, mainCommand));
  }

  public static <A> void run(Command<A> mainCommand) {
    new Interpreter(mainCommand).run();
  }

  public void run() {
    while (scheduler.hasNext()) {
      Process p = scheduler.next();
      Command<A> c = next(p.command);

      if (c instanceof Command.Done) {
        if (p.isMain) {
          break;
        }
      } else if (c instanceof Command.Chan) {
        Command.Chan<?, A> chan = (Command.Chan) c;
        Channel ch = new Channel<>(chan.cls, chan.length);
        scheduler.schedule(p.update(chan.next.apply(ch)));
      } else if (c instanceof Command.Close) {
        Command.Close<?, A> close = (Command.Close) c;
        Channel<?> ch = close.ch;
        if (ch.isClosed) {
          throw new RuntimeException("Close a closed channel");
        } else if (!ch.senders.isEmpty()) {
          throw new RuntimeException("Close a channel with senders");
        } else {
          for (BlockedRecv<?> blocked : ch.receivers) {
            scheduler.schedule(blocked.resume.apply(Optional.empty()));
          }
          ch.receivers.clear();
          ch.isClosed = true;
          scheduler.schedule(p.update(close.next));
        }
      } else if (c instanceof Command.Send) {
        Command.Send<?, A> send = (Command.Send) c;
        Command<A> next = runSend(send);
        if (next != null) {
          scheduler.schedule(p.update(next));
        } else {
          BlockedSend blocked = new BlockedSend(send.value, () -> p.update(send.next));
          send.ch.senders.add(blocked);
        }
      } else if (c instanceof Command.Recv) {
        Command.Recv<?, A> recv = (Command.Recv) c;
        Command<A> next = runRecv(recv);
        if (next != null) {
          scheduler.schedule(p.update(next));
        } else {
          BlockedRecv blocked = new BlockedRecv(v -> p.update(recv.next.apply((Optional) v)));
          recv.ch.receivers.add(blocked);
        }
      } else if (c instanceof Command.Go) {
        Command.Go<?, A> go = (Command.Go) c;
        scheduler.schedule(new Process(false, go.forked));
        scheduler.schedule(p.update(go.next));
      } else if (c instanceof Command.Print) {
        Command.Print<A> print = (Command.Print) c;
        System.out.print(print.value);
        scheduler.schedule(p.update(print.next));
      } else if (c instanceof Command.Sleep) {
        Command.Sleep<A> sleep = (Command.Sleep) c;
        Instant due = scheduler.now().plusMillis(sleep.ms);
        scheduler.scheduleAt(due, p.update(sleep.next));
      } else if (c instanceof Command.Select) {
        Command.Select<A> select = (Command.Select<A>) c;

        // TODO tree transform? if each branches' first command is neither recv/send except default
        List<Command<A>> branches = map(select.branches, br -> next(br));
        Command next = proceedToAvailableBranch(branches);
        if (next != null) {
          scheduler.schedule(p.update(next));
        } else {
          blockOnAllBranches(p, branches);
        }
      } else if (c instanceof Command.Runtime) {
        Command.Runtime runtime = (Command.Runtime) c;
        Object result = runtime.f.apply(scheduler);
        scheduler.schedule(p.update((Command) runtime.next.apply(result)));
      } else {
        throw new RuntimeException("Unknown command:" + c.getClass().getName());
      }
    }
  }

  private void blockOnAllBranches(Process p, List<Command<A>> branches) {
    final List waiters = new ArrayList();
    for (Command<A> branch : branches) {
      if (branch instanceof Command.Recv) {
        Command.Recv recv = (Command.Recv) branch;
        BlockedRecv<?> blockedRecv = new BlockedRecv<>(value -> {
          removeWaiters(branches, waiters);
          return p.update((Command) recv.next.apply((Optional) value));
        });
        waiters.add(blockedRecv);
        recv.ch.receivers.add(blockedRecv);
      } else {
        Command.Send send = (Command.Send) branch;
        BlockedSend<?> blockedSend = new BlockedSend<>(send.value, () -> {
          removeWaiters(branches, waiters);
          return p.update(send.next);
        });
        waiters.add(blockedSend);
        send.ch.senders.add(blockedSend);
      }
    }
  }

  private <B> Command<B> proceedToAvailableBranch(List<Command<B>> branches) {
    for (Command<B> branch : branches) {
      if (branch instanceof Command.Recv) {
        Command<B> next = runRecv((Command.Recv<?, B>) branch);
        if (next != null) return next;
      } else if (branch instanceof Command.Send) {
        Command<B> next = runSend((Command.Send) branch);
        if (next != null) return next;
      } else { // default
        return branch;
      }
    }
    return null;
  }

  private <C, B> Command<B> runSend(Command.Send<C, B> send) {
    final Channel<C> channel = send.ch;
    if (channel.isClosed) {
      throw new RuntimeException("send to a closed channel");
    } else if (!channel.receivers.isEmpty()) {
      BlockedRecv<C> blockedRecv = removeRandom(channel.receivers);
      scheduler.schedule(blockedRecv.resume.apply(Optional.of(send.value)));
      return send.next;
    } else if (channel.buffer.size() < channel.length) {
      channel.buffer.add(send.value);
      return send.next;
    }
    return null;
  }

  private <C, B> Command<B> runRecv(Command.Recv<C, B> recv) {
    final Channel<C> channel = recv.ch;
    if (channel.isClosed) {
      return recv.next.apply(Optional.empty());
    } else if (!channel.senders.isEmpty()) {
      BlockedSend<C> blockedSend = removeRandom(channel.senders);
      scheduler.schedule(blockedSend.resume.get());
      return recv.next.apply(Optional.of(blockedSend.value));
    } else if (!channel.buffer.isEmpty()) {
      return recv.next.apply(Optional.of(channel.buffer.remove()));
    }
    return null;
  }

  private void removeWaiters(List<Command<A>> branches, List waiters) {
    for (int i = 0; i < branches.size(); i++) {
      Command<A> first = branches.get(i);
      if (first instanceof Command.Recv) {
        Command.Recv recv = (Command.Recv) first;
        recv.ch.receivers.remove(waiters.get(i));
      } else {
        Command.Send send = (Command.Send) first;
        send.ch.senders.remove(waiters.get(i));
      }
    }
  }

  private static <A> A removeRandom(List<A> list) {
    return list.remove(new Random().nextInt(list.size()));
  }

  private static <A> Command<A> next(Command<A> command) {
    Command<A> c = command;
    while (c instanceof Command.Then) { // skip `then`
      Command.Then then = (Command.Then) c;
      if (then.sub instanceof Command.Done) {
        Command.Done done = (Command.Done) then.sub;
        c = (Command<A>) then.k.apply(done.value);
      } else if (then.sub instanceof Command.Then) { // nested Then
        Command.Then nested = (Command.Then) then.sub;
        c = nested.sub.then(a -> ((Command) nested.k.apply(a)).then(then.k));
      } else if (then.sub instanceof Command.Chan) {
        Command.Chan chan = (Command.Chan) then.sub;
        c = new Command.Chan(chan.cls, chan.length, ch -> ((Command) chan.next.apply(ch)).then(then.k));
      } else if (then.sub instanceof Command.Close) {
        Command.Close close = (Command.Close) then.sub;
        c = new Command.Close(close.ch, close.next.then(then.k));
      } else if (then.sub instanceof Command.Send) {
        Command.Send send = (Command.Send) then.sub;
        c = new Command.Send(send.ch, send.value, send.next.then(then.k));
      } else if (then.sub instanceof Command.Recv) {
        Command.Recv recv = (Command.Recv) then.sub;
        c = new Command.Recv(recv.ch, v -> ((Command) recv.next.apply(v)).then(then.k));
      } else if (then.sub instanceof Command.Go) {
        Command.Go go = (Command.Go) then.sub;
        c = new Command.Go(go.forked, go.next.then(then.k));
      } else if (then.sub instanceof Command.Print) {
        Command.Print print = (Command.Print) then.sub;
        c = new Command.Print(print.value, print.next.then(then.k));
      } else if (then.sub instanceof Command.Sleep) {
        Command.Sleep sleep = (Command.Sleep) then.sub;
        c = new Command.Sleep(sleep.ms, sleep.next.then(then.k));
      } else if (then.sub instanceof Command.Select) {
        Command.Select select = (Command.Select) then.sub;
        c = new Command.Select(map(select.branches, (Command br) -> br.then(then.k)));
      } else if (then.sub instanceof Command.Runtime) {
        Command.Runtime runtime = (Command.Runtime) then.sub;
        c = new Command.Runtime(runtime.f, a -> ((Command) runtime.next.apply(a)).then(then.k));
      } else {
        throw new RuntimeException("Unknown command: " + then.sub.getClass().getName());
      }
    }
    return c;
  }

  private static <A, B> List<B> map(List<A> as, Function<A, B> f) {
    List<B> bs = new ArrayList<>();
    for (A a : as) {
      bs.add(f.apply(a));
    }
    return bs;
  }

}