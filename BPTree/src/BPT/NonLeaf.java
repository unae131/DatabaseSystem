package BPT;

public class NonLeaf extends Node{
	Node rMstChild; // null일 수 없음
	Pair<Node>[] npair; // pairs of <key, pointer>
	@SuppressWarnings("unchecked")
	NonLeaf(int degree, NonLeaf parent, Node rMst){
		super(degree, parent);
		rMstChild = rMst;
		npair = new Pair[degree-1];
	}
	
	Pair<Node>[] getPairs(){
		return npair.clone();
	}
	void setRMstChild(Node rMst){ 
		rMstChild = rMst;
	}
	Boolean isFew(){ // 최소 ceil(n/2) -1 개는 꼭 가져야함
		if(numKey < Math.ceil((double)degree/2) - 1)
			return true;
		return false;
	}
	
	int findKey(int key){ //key 존재 확인, 없으면 -1, 있으면 idx 반환
		for(int i = 0; i < numKey; i++)
			if(npair[i].key == key)
				return i;
		return -1;
	}
	int beforeKey(int key){ // key가 들어갈 수 있는 자리 전 index, 같아도 그 전까지
		int max = -1;
		for(int i = 0; i < this.numKey && npair[i].key < key; i++)
			max = i;
		return max;
	}
	
	NonLeaf sibling(){ // innernode 에서의 sibling을 반환
		if(isRoot()) return null; // root면 없음
		int key = parent.findKey(leftMstLeaf()); // 현 노드의 부모에서 key 존재 확인
		if(key == -1 && parent.numKey == 1) // 현 부모의 맨 왼쪽 자식이고 자식이 총 2개임
			return (NonLeaf) parent.rMstChild;
		else if(key == -1 && parent.numKey > 1) // 맨 왼쪽 자식이고 자식 3개 이상
			return (NonLeaf) parent.npair[1].value;
		return (NonLeaf) parent.npair[key].value; // 맨 왼쪽 자식 아니면 현 노드의 왼쪽 sibling을 return
	}
	
	void delete(int key, int idx){ // key는 삭제되어야하는 key, node 삭제는 이미 수행되었다고 가정
		// 삭제 key를 idx에서 찾은 것을 가정, idx == -1 일 때 여기선 삭제된 node가 현 노드의 첫 번째 자식이었음을 의미
		// 자식 노드가 merge 되었을 때만 사용
		if(idx == 0 && numKey == 1){ // 남은 key가 한 개일 때
			rMstChild = npair[0].value;
			numKey--;
			return;
		}
		else if(idx == numKey - 1){ // 맨 오른쪽 노드가 삭제된 경우
			rMstChild = npair[numKey - 1].value;
			npair[numKey - 1] = null;
			if(rMstChild instanceof NonLeaf)
				npair[idx - 1].key = ((NonLeaf)rMstChild).leftMstLeaf();
			else npair[idx -1].key = ((LeafNode) rMstChild).lfpair[0].key;
			numKey--;
			return;
		}
		if(idx != -1) // 제인 왼쪽 record가 아닌 경우만
			npair[idx].key = npair[idx+1].key;
		idx++;
		for(;idx < numKey - 1; idx++){ // 왼쪽으로 한 칸씩 당김
			npair[idx] = npair[idx+1];
		}
		numKey--;
	}
	
	Boolean merge(NonLeaf rightN){ // return으로는 merge 가능성(null인 경우 merge할 수 없)
		// few일 때만 사용, root일 때는 사용할 수 없음
		if(this == null || rightN == null){
			System.out.println("MERGE() null pointer ERROR");
			return false;
		}
		if(getNumKey() + rightN.getNumKey() + 1 >= degree) // merge 불가
			return false;
		npair[numKey] = new Pair<Node>(rightN.leftMstLeaf(), rMstChild); // leftMstLeaf는 parent에 반드시 존재
		for(int i = 0; i < rightN.getNumKey(); i++) {// 왼쪽으로 다 넣고, 부모도 재설정
			rightN.npair[i].value.setParent(this);
			npair[numKey + 1 + i] = rightN.npair[i];
		}
		rightN.rMstChild.setParent(this);
		rMstChild = rightN.rMstChild;
		setNumKey(getNumKey() + rightN.getNumKey()+1); // numKey 재설정
		return true;
		
	}
	
	void redistribute(NonLeaf rightN, int del){ // rightN과 redistribute
		// few일 때만 사용, root일 때는 사용할 수 없음, merge 검사를 마친 후라 가정
		if(this == null || rightN == null){
			System.out.println("NonLeaf REDISTRIBUTE() null point ERROR");
			return;
		}
		int total = getNumKey() + rightN.getNumKey(); // 총 키수
		// total - total/2개가 오른쪽에 있어야함
		int i, j;
		if(numKey < rightN.getNumKey()){ // 오른쪽에서 왼쪽으로 record 몇 개 이동
			npair[numKey] = new Pair<Node>(del, rMstChild);
			for(i = numKey + 1, j = 0; i< total/2; i++, j++)
				npair[i] = rightN.npair[j];
			rMstChild = rightN.npair[j++].value;
			for( i = 0; j < rightN.getNumKey(); i++, j++)
				rightN.npair[i] = rightN.npair[j];
			for( ; i < rightN.getNumKey(); i++)
				rightN.npair[i] = null;
		}
		else{ // 왼쪽에서 오른쪽으로 이동 
			for(i = total - total/2 - 1, j = rightN.getNumKey() - 1; j >= 0; i--, j--)
				rightN.npair[i] = rightN.npair[j];
			rightN.npair[i--] = new Pair<Node>(del, rMstChild);
			for(j = getNumKey() - 1; i >= 0; i--, j--)
				rightN.npair[i] = npair[j];
			rMstChild = npair[j].value;
			for(; j < getNumKey(); j++)
				npair[j] = null;
			
		}
		// numKey 재설정
		setNumKey(total/2);
		rightN.setNumKey(total - total/2);
		// 부모들 재설정
		for(i = 0; i < numKey; i++)
			npair[i].value.setParent(this);
		rMstChild.setParent(this);
		for(i = 0; i < rightN.getNumKey(); i++)
			rightN.npair[i].value.setParent(rightN);
		rightN.rMstChild.setParent(rightN);
	}
	
	int leftMstLeaf(){ // 해당 노드를 타고 내려갔을 때 나오는 leaf의 key들 중 가장 작은 key값 반환
		if(numKey == 0 && rMstChild instanceof NonLeaf){ // 노드에 오른쪽 자식 가리키는 포인터만 존재
			return ((NonLeaf) rMstChild).leftMstLeaf(); // nonleaf인 경우  자식에서 함수 다시 사용
		}
		else if(numKey == 0){ // 노드에 오른쪽 child pointer만 존재, 자식은 leaf
			return ((LeafNode) rMstChild).lfpair[0].key;
		}
		Node tmp = this.npair[0].value;
		while(tmp instanceof NonLeaf){ // 제일 왼쪽 child 타고 내려감
			tmp = ((NonLeaf) tmp).npair[0].value;
		}
		return ((LeafNode) tmp).lfpair[0].key;
	}
	
	Boolean put(Pair<Node> put){ // 노드에 put하고 성공 여부 return
		// 꽉 안차있어야 가능, key는 중복 no
		if(isFull()) return false;
		int idx = beforeKey(put.key) + 1; // put 될 자리
		for(int i = numKey - 1; i >= idx; i--){
			npair[i+1] = npair[i];
		}
		if(idx == numKey){
			npair[idx] = new Pair<Node>(put.key, rMstChild);
			setRMstChild(put.value);
			numKey++;
			return true;
		}
		npair[idx] = new Pair<Node>(put.key, put.value);
		npair[idx].value = npair[idx+1].value; // 포인터 재설정
		npair[idx+1].value = put.value;
		numKey++;
		return true;
	}
	
	Node[] childs(Pair<Node> put){ // 새 노드를 포함한 childs를 정렬해서 배열로 return
		// put은 현 노드에 추가되어야하는 pair로서 value(pointer)로 새로만들어진 노드를 일단 가져온다
		// 각 노드의 첫 key값을 비교
		Node[] childs = new Node[numKey+2];
		for(int i = 0; i < numKey; i++) // 일단 다 넣기
			childs[i] = npair[i].value;
		childs[numKey] = rMstChild;
		childs[numKey+1] = put.value;
		int i;
		// 자식의 첫 키 값으로 insertion sort
		for(int j = 1; j < childs.length; j++){
			if(this.rMstChild instanceof NonLeaf){
				NonLeaf key = (NonLeaf) childs[j];
				i = j - 1;
				while (j > 0 && ((NonLeaf)childs[i]).npair[0].key > key.npair[0].key){
					childs[i+1] = childs[i];
					i -= 1;
				}
				childs[i+1] = key;
			}
			else{
				LeafNode key = (LeafNode) childs[j];
				i = j - 1;
				while (j > 0 && ((LeafNode)childs[i]).lfpair[0].key > key.lfpair[0].key){
					childs[i+1] = childs[i];
					i -= 1;
				}
				childs[i+1] = key;
			}
		}
		return childs;
	}
	
	@SuppressWarnings("unchecked")
	Pair<Node> putSplit(NonLeaf newNonLeaf, Pair<Node> put){ // 해당 노드에 put을 삽입하면서 newNonLeaf로 split하고 이를 반환
		// old는 this 고 full이라는 가정 하, 삽입하는 key 중복안된다는 가정
		// 일단 put.value는 newNode로 설정 돼서 들어옴
		// 자식이 leaf인 경우엔 못씀
		Pair<Node>[] tmp = new Pair[degree];
		Pair<Node> result; // return해줄 pair
		Node[] sortedC = childs(put);
		
		int half = (int) Math.ceil((double)degree/2) - 1; // nonleaf에서 split할 때 남길 수
		int idx = beforeKey(put.key) + 1; // 넣어줄 곳의 index
		
		for(int i = 0; i < idx; i++) // 원래 노드에 
			tmp[i] = this.npair[i];
		tmp[idx] = put; // 받은 data tmp에 넣어주기
		for(int i = idx; i < numKey; i++) //나머지도 넣어줌, numkey == degree -1
			tmp[i+1] = this.npair[i];
		// 여기까지 새로운 data와 함께 크기순으로 tmp에 저장했음	
		
		for(int i = 0; i < half; i++)
			this.npair[i] = tmp[i];
		result = tmp[half];
		for(int i = half+1, n = 0; i < degree; i++, n++)
			newNonLeaf.npair[n] = tmp[i];
		// 여기까지 key값 왼쪽 ceil(n/2)-1, 결과 한 개, 오른쪽 나머지로 정리

		setNumKey(half); // ceil(d/2)-1개 남김, 왼쪽 numKey 줄이고
		newNonLeaf.setNumKey(degree - half - 1); // 오른쪽 numKey 설정해주고
		
		for(int i = 0 ; i < this.numKey; i++){ // 왼쪽 npair의 부모 자식 관계 정리
			sortedC[i].setParent(this);
			this.npair[i].value = sortedC[i];
		}
		sortedC[this.numKey].setParent(this);
		this.setRMstChild(sortedC[this.numKey]);
		
		for(int i = 0; i < newNonLeaf.numKey; i++){ // 오른쪽 npair의 부모 자식 관계 정리
			sortedC[this.numKey + i + 1].setParent(newNonLeaf);
			newNonLeaf.npair[i].value = sortedC[this.numKey + i + 1];
		}
		sortedC[sortedC.length - 1].setParent(newNonLeaf);
		newNonLeaf.setRMstChild(sortedC[sortedC.length - 1]);
		// 여기까지 현 노드와 새 노드 전부 정리
		
		for(int i = numKey; i < degree - 1; i++) // 기존 node에서 옮긴 것들 삭제
			this.npair[i] = null;
		
		result.value = newNonLeaf; // 마지막에 put으로 보낼 때 새노드 가리키게 하고 return
		return result;
	}
	
	public String toString(){
		String str = "[NonLeaf";
		for(int i = 0; i < numKey; i++)
		str += npair[i].toString() + "";
		str += "r"+rMstChild;
		str += "]";
		return str;
	}
}
