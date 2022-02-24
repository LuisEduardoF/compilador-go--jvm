package main


func teste1(a int, b int) (int,int){

	return a*b,a/b
}

func teste2() (int){
	return 3;
}

func main() {
	//atrComands
	var a,b int;
	a,b = teste1(3,4); 
	
	//e,f := teste1(3,4) isto nao fuciona =(

	c := teste2()
	d := teste2()
	c = 34
	d = 43

}