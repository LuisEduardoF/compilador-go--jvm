package main

func main() {
	
	var a  = 3;
	if(a == 3 || a == 2 && a == 5){
		a = 1;
	}else{
		a = 4;
	}

	var n int = 40;

	if(a > n){
		n = a;
	}else if(a >= n){
		a = n+n
	}else{
		n = a*3;
	}

	fmt.Println(a)
	fmt.Println(n)
}