package ch8.concurrency.patterns;

import ch8.go.Command;

import java.util.function.Function;

interface Search extends Function<String, Command<String>> {
}
