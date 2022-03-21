package main

func main() {
	
	var a = 0
	for(a < 10){
		if(a/2 == 5){
			a = a+2
		}else{
			a = a+1
		}
		fmt.Println(a)
		a = a+1;
	}
}