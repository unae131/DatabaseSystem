package BPT;

public class Pair <T> {
	int key;
	T value;
	
	Pair(int key, T value){
		this.key = key;
		this.value = value;
	}
	
	public String toString(){
		return "("+key+","+value+")";
	}
}
