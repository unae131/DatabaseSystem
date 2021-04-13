package BPT;

public class LeafNode extends Node{
	LeafNode right; // null 일 수도
	Pair<Integer>[] lfpair; //pairs <key, value> int/int형식을 담는 배열
	
	@SuppressWarnings("unchecked")
	LeafNode(int degree, NonLeaf parent, LeafNode right){
		super(degree, parent);
		this.right = right;
		lfpair = new Pair[degree-1];
	}
	Pair<Integer>[] getPairs(){ // 현 노드의 pair를 담은 배열 복사 후 반환
		return lfpair.clone();
	}
	
	void setRight(LeafNode right){
		this.right = right;
	}
	
	Boolean isFew(){ // 최소 ceil(n-1/2) 개는 꼭 가져야함
		if(numKey < Math.ceil((double)((degree-1)/2)))
			return true;
		return false;
	}
	
	int findKey(int key){ //key 존재 확인, 없으면 -1 있으면 idx 반환
		for(int i = 0; i < numKey; i++)
			if(lfpair[i].key == key)
				return i;
		return -1;
	}
	
	int beforeKey(int key){ // key가 들어갈 수 있는 자리 전 index return
		int max = -1;
		for(int i = 0; i < numKey && lfpair[i].key < key; i++)
			max = i;
		return max;
	}
	Boolean put(Pair<Integer> put){ // leaf에 put을 추가하고 성공여부 return
		// 꽉 안차있어야 가능, key는 중복 no
		if(isFull()) return false;
		int idx = beforeKey(put.key) + 1; // put 될 자리
		for(int i = numKey - 1; i >= idx; i--){
			lfpair[i+1] = lfpair[i];
		}
		lfpair[idx] = put;
		numKey++;
		return true;
	}
	
	LeafNode getLeft(){ // 왼쪽 리프 노드를 return, leaf는 반드시 2개 이상 존재
		// numKey 1개 이상이라 가정
		if(this.isRoot()) return null; // root일 경우
		
		NonLeaf check = this.parent;
		int findKey = this.lfpair[0].key;
		int idx = -1;
		while(!check.isRoot()){ // 타고 올라감
			idx = check.findKey(findKey);
			if(idx == -1){
				check = check.parent;
				continue;
			}
			break; // 찾으면 break
		}
		// root 이거나 찾으면 break
		idx = check.findKey(findKey);
		if(check.isRoot() && check.findKey(findKey) == -1)
			return null; // 제일 왼쪽 노드임
		if(check.npair[0].value instanceof NonLeaf){ // 자식노드가 nonleaf일 경우
			check = (NonLeaf)check.npair[idx].value;
			while(check.npair[0].value instanceof NonLeaf) // 타고 내려감
				check = (NonLeaf) check.rMstChild;
			return (LeafNode)check.rMstChild;
		}
		else { // 자식 노드가 leaf일 경우 바로 return
			return (LeafNode)check.npair[idx].value;
		}
	}
	Boolean merge(LeafNode rightN){ // return으로는 merge 가능성(null인 경우 merge할 수 없다), 두 노드를 merge 해줌
		// 조건 : few일 때만 사용, root일 때는 사용할 수 없음
		if(this == null || rightN == null){
			System.out.println("MERGE() null pointer ERROR");
			return false;
		}
		if(getNumKey() + rightN.getNumKey() >= degree) // 두 개 합치면 key값들이 degree개 이상
			return false;
		for(int i = getNumKey(), n = 0; i < getNumKey() + rightN.getNumKey(); i++,n++) {// 왼쪽 노드에 합쳐줌
			lfpair[i] = rightN.lfpair[n];
		}
		right = rightN.right; // 오른쪽 leaf 재설정
		setNumKey(getNumKey() + rightN.getNumKey());
		return true;
		
	}
	@SuppressWarnings("unchecked")
	void redistribute(LeafNode rightN){ // 현 노드를 rightN과 redistribute
		// few일 때만 사용
		// root일 때는 사용할 수 없음, merge 검사를 마친 후라 가정
		if(this == null || rightN == null){
			System.out.println("REDISTRIBUTE() null pointer ERROR");
			return;
		}
		int total = getNumKey() + rightN.getNumKey(); // 총 키수
		Pair<Integer>[] tmp = new Pair[total]; // tmp에 두 노드 record 모두 담음
		for(int i = 0; i < numKey; i++)
			tmp[i] = lfpair[i];
		for(int i = 0; i < rightN.getNumKey(); i++)
			tmp[numKey + i] = rightN.lfpair[i];
		// total/2개는 현 노드(왼쪽), total - total/2개가 오른쪽(rightN)에 있어야함
		// 나머지는 null로
		for(int i = 0; i < degree - 1; i++){
			if(i < total/2)
				lfpair[i] = tmp[i];
			else
				lfpair[i] = null;
		}
		for(int i = 0; i < degree - 1; i++){
			if(i < total - total/2)
				rightN.lfpair[i] = tmp[total/2+i];
			else
				rightN.lfpair[i] = null;
		}
		setNumKey(total/2); // numKey 재설정
		rightN.setNumKey(total - total/2);
	}
	
	int deletePair(int key){ // 해당 키의 record를 삭제, return은 삭제 후 현 노드의 첫 번째 record의 key
		int find = findKey(key); // key 어딨는지 찾음
		if(find != -1){ //존재한다면
			if(numKey == 1){ // 빈 노드가 되어야할 경우 , 삭제된 key를 반환
				if(getLeft() != null)
					getLeft().right = right;
				numKey--;
				return key;
			}
			for(int i = find; i < numKey-1; i++){ // key 삭제했다면 한 칸씩 왼쪽으로 당김
				lfpair[i] = lfpair[i+1];
			}
			lfpair[numKey-1] = null; // 맨 오른쪽 record null로
			numKey--;
			return lfpair[0].key;
		}
		System.out.println("\nERR) There are No pair with the key in Leaf");
		return -1;
	}
	
	@SuppressWarnings("unchecked")
	LeafNode putSplit(LeafNode newLeaf, Pair<Integer> put){ // put을 넣으면서 두 노드로 split 후 새로 만들어진 노드를 반환
		// full이라는 가정 하, 삽입하는 key 중복안된다는 가정
		Pair<Integer>[] tmp = new Pair[degree];
		int idx = beforeKey(put.key) + 1; // 넣어줄 곳의 index
		for(int i = 0; i < idx; i++)
			tmp[i] = lfpair[i];
		tmp[idx] = put; // 받은 data tmp에 넣어주고
		for(int i = idx; i < numKey; i++) //나머지도 넣어줌
			tmp[i+1] = lfpair[i];
		// 여기까지 새로운 data와 함꼐 크기순으로 tmp에 저장했음

		setNumKey((int) Math.ceil((double)((degree-1)/2))); // ceil(d-1/2)개 남김
		for(int i = numKey, n = 0; i < degree; i++,n++)
			newLeaf.lfpair[n] = tmp[i]; // 새로운 leaf에 순서대로 넣어줌
		for(int i = 0; i < numKey; i++) // 기존node 수정해주고
			lfpair[i] = tmp[i];
		for(int i = numKey; i < degree - 1; i++) // 기존 node에서 옮긴 것들 삭제
			lfpair[i] = null;
		newLeaf.setNumKey(degree - numKey);
		// 포인터 재설정
		this.setRight(newLeaf);
		return newLeaf;
	}
	
	public String toString(){
		String str = "[LeafNode";
		for(int i = 0; i < numKey; i++)
			str += lfpair[i].toString() + "";
		str += "]";
		return str;
	}
}
