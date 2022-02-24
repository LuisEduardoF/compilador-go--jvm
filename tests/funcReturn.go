package main


func teste1(a int, b int) (int,int){
	var i int = 3;
	return a*b,a/b
}

func teste2() (int){
	var i int = 4;
	return 3;
}

var d float64 = 3.2 //variavel global

func main() {
	//blockScope
	var a,b int;
	a,b = teste1(3,3);
	b = teste2();

}