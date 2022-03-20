package main


func teste1(a int, b int) (int){
	var i int = 3;
	return a*b
}

func teste2() (int){
	var i int = 4;
	return 3;
}

func main() {
	//blockScope
	var a,b int;
	a = teste1(3,3);
	b = teste2();
	
	fmt.Println(a);
	fmt.Println(b);
}