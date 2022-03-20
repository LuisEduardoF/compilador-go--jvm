package main

func leAluno(a int, b bool) (int){ 
	var x int;
	fmt.Println("Digite um número qualquer:")
	fmt.Scanln(x);
	return x;
}

func main() {
	
	var aluno int;
	aluno = leAluno(3,true);
	fmt.Println("ALUNO:")
	fmt.Println(aluno);
}