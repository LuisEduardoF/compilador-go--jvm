package main

func somaNum(a int, b int) (int){
	return a+b;
}

func main() {
	
	var a , b int;
	fmt.Scanln(a,b);

	var soma int;

	soma = somaNum(a,b)

	fmt.Println(soma);
}