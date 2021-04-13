package BPT;

public abstract class Node {
	int degree;
	int numKey;
	NonLeaf parent; // nullÀÌ¸é root
	int idx = 0;
	
	Node(int degree, NonLeaf parent){
		this.degree = degree;
		this.parent = parent;
		numKey = 0;
	}
	
	Boolean isRoot(){
		if(parent == null)
			return true;
		return false;
	}
	
	Boolean isFull(){
		if(numKey >= degree-1)
			return true;
		return false;
	}
	
	abstract Boolean isFew();
	
	int getNumKey(){
		return numKey;
	}
	void setNumKey(int num){
		numKey = num;
	}
	void setParent(NonLeaf parent){
		this.parent = parent;
	}


}
