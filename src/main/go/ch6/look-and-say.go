package main

func main() {
	c := ant(10)
	for value := range c {
		print(value)
	}
	println()
}

func ant(n int) <-chan int {
	c := gen(1)
	for i := 0; i < n; i++ {
		c = next(c)
	}
	return c
}

func gen(n int) <-chan int {
	c := make(chan int)
	go func() {
		c <- n
		close(c)
	}()
	return c
}

func next(in <-chan int) <-chan int {
	out := make(chan int)
	go func() {
		prev, count := <-in, 1
		for value := range in {
			if value == prev {
				count++
			} else {
				out <- count
				out <- prev
				prev, count = value, 1
			}
		}
		out <- count
		out <- prev
		close(out)
	}()
	return out
}
