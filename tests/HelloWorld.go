package main


func main2(a int, b float64) (int, int) {
	return 1, 2
}

func main3(a int) (int) {
	return 2
}

var a int        // +
var b, c float64 // + strange extra levels
var d = 1        // + doesn't show zero value
var e, f float32 = -1, -2  // +
var (
	g       int
	h, i, j = 2.0, 3.0, "bar"
	)

func main() {
	var dudu [43]int;
	dudu[3] = 2;
}
