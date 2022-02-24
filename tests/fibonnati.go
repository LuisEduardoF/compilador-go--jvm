package main



func main() {

	n1 , n2 := 0, 1;

	var qtd int;

	fmt.Scanln(qtd); //qtd de termos do fibonati

	for qtd > 0{
		n3 := n1 + n2
		fmt.Println(n3);
		n1 = n2
		n2 = n3;
		qtd = qtd-1;
	}
	fmt.Println("Fim");
}
