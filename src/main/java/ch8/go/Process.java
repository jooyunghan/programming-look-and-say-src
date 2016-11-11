package ch8.go;

class Process<A> {
  final boolean isMain;
  final Command<A> command;

  Process(boolean isMain, Command<A> command) {
    this.isMain = isMain;
    this.command = command;
  }

  @Override
  public String toString() {
    return "Process" + (isMain ? "(M):" : ":") + command.getClass().getName();
  }

  public Process<A> update(Command<A> next) {
    return new Process<>(isMain, next);
  }
}
