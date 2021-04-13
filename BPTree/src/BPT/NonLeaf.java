package BPT;

public class NonLeaf extends Node{
	Node rMstChild; // null�� �� ����
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
	Boolean isFew(){ // �ּ� ceil(n/2) -1 ���� �� ��������
		if(numKey < Math.ceil((double)degree/2) - 1)
			return true;
		return false;
	}
	
	int findKey(int key){ //key ���� Ȯ��, ������ -1, ������ idx ��ȯ
		for(int i = 0; i < numKey; i++)
			if(npair[i].key == key)
				return i;
		return -1;
	}
	int beforeKey(int key){ // key�� �� �� �ִ� �ڸ� �� index, ���Ƶ� �� ������
		int max = -1;
		for(int i = 0; i < this.numKey && npair[i].key < key; i++)
			max = i;
		return max;
	}
	
	NonLeaf sibling(){ // innernode ������ sibling�� ��ȯ
		if(isRoot()) return null; // root�� ����
		int key = parent.findKey(leftMstLeaf()); // �� ����� �θ𿡼� key ���� Ȯ��
		if(key == -1 && parent.numKey == 1) // �� �θ��� �� ���� �ڽ��̰� �ڽ��� �� 2����
			return (NonLeaf) parent.rMstChild;
		else if(key == -1 && parent.numKey > 1) // �� ���� �ڽ��̰� �ڽ� 3�� �̻�
			return (NonLeaf) parent.npair[1].value;
		return (NonLeaf) parent.npair[key].value; // �� ���� �ڽ� �ƴϸ� �� ����� ���� sibling�� return
	}
	
	void delete(int key, int idx){ // key�� �����Ǿ���ϴ� key, node ������ �̹� ����Ǿ��ٰ� ����
		// ���� key�� idx���� ã�� ���� ����, idx == -1 �� �� ���⼱ ������ node�� �� ����� ù ��° �ڽ��̾����� �ǹ�
		// �ڽ� ��尡 merge �Ǿ��� ���� ���
		if(idx == 0 && numKey == 1){ // ���� key�� �� ���� ��
			rMstChild = npair[0].value;
			numKey--;
			return;
		}
		else if(idx == numKey - 1){ // �� ������ ��尡 ������ ���
			rMstChild = npair[numKey - 1].value;
			npair[numKey - 1] = null;
			if(rMstChild instanceof NonLeaf)
				npair[idx - 1].key = ((NonLeaf)rMstChild).leftMstLeaf();
			else npair[idx -1].key = ((LeafNode) rMstChild).lfpair[0].key;
			numKey--;
			return;
		}
		if(idx != -1) // ���� ���� record�� �ƴ� ��츸
			npair[idx].key = npair[idx+1].key;
		idx++;
		for(;idx < numKey - 1; idx++){ // �������� �� ĭ�� ���
			npair[idx] = npair[idx+1];
		}
		numKey--;
	}
	
	Boolean merge(NonLeaf rightN){ // return���δ� merge ���ɼ�(null�� ��� merge�� �� ��)
		// few�� ���� ���, root�� ���� ����� �� ����
		if(this == null || rightN == null){
			System.out.println("MERGE() null pointer ERROR");
			return false;
		}
		if(getNumKey() + rightN.getNumKey() + 1 >= degree) // merge �Ұ�
			return false;
		npair[numKey] = new Pair<Node>(rightN.leftMstLeaf(), rMstChild); // leftMstLeaf�� parent�� �ݵ�� ����
		for(int i = 0; i < rightN.getNumKey(); i++) {// �������� �� �ְ�, �θ� �缳��
			rightN.npair[i].value.setParent(this);
			npair[numKey + 1 + i] = rightN.npair[i];
		}
		rightN.rMstChild.setParent(this);
		rMstChild = rightN.rMstChild;
		setNumKey(getNumKey() + rightN.getNumKey()+1); // numKey �缳��
		return true;
		
	}
	
	void redistribute(NonLeaf rightN, int del){ // rightN�� redistribute
		// few�� ���� ���, root�� ���� ����� �� ����, merge �˻縦 ��ģ �Ķ� ����
		if(this == null || rightN == null){
			System.out.println("NonLeaf REDISTRIBUTE() null point ERROR");
			return;
		}
		int total = getNumKey() + rightN.getNumKey(); // �� Ű��
		// total - total/2���� �����ʿ� �־����
		int i, j;
		if(numKey < rightN.getNumKey()){ // �����ʿ��� �������� record �� �� �̵�
			npair[numKey] = new Pair<Node>(del, rMstChild);
			for(i = numKey + 1, j = 0; i< total/2; i++, j++)
				npair[i] = rightN.npair[j];
			rMstChild = rightN.npair[j++].value;
			for( i = 0; j < rightN.getNumKey(); i++, j++)
				rightN.npair[i] = rightN.npair[j];
			for( ; i < rightN.getNumKey(); i++)
				rightN.npair[i] = null;
		}
		else{ // ���ʿ��� ���������� �̵� 
			for(i = total - total/2 - 1, j = rightN.getNumKey() - 1; j >= 0; i--, j--)
				rightN.npair[i] = rightN.npair[j];
			rightN.npair[i--] = new Pair<Node>(del, rMstChild);
			for(j = getNumKey() - 1; i >= 0; i--, j--)
				rightN.npair[i] = npair[j];
			rMstChild = npair[j].value;
			for(; j < getNumKey(); j++)
				npair[j] = null;
			
		}
		// numKey �缳��
		setNumKey(total/2);
		rightN.setNumKey(total - total/2);
		// �θ�� �缳��
		for(i = 0; i < numKey; i++)
			npair[i].value.setParent(this);
		rMstChild.setParent(this);
		for(i = 0; i < rightN.getNumKey(); i++)
			rightN.npair[i].value.setParent(rightN);
		rightN.rMstChild.setParent(rightN);
	}
	
	int leftMstLeaf(){ // �ش� ��带 Ÿ�� �������� �� ������ leaf�� key�� �� ���� ���� key�� ��ȯ
		if(numKey == 0 && rMstChild instanceof NonLeaf){ // ��忡 ������ �ڽ� ����Ű�� �����͸� ����
			return ((NonLeaf) rMstChild).leftMstLeaf(); // nonleaf�� ���  �ڽĿ��� �Լ� �ٽ� ���
		}
		else if(numKey == 0){ // ��忡 ������ child pointer�� ����, �ڽ��� leaf
			return ((LeafNode) rMstChild).lfpair[0].key;
		}
		Node tmp = this.npair[0].value;
		while(tmp instanceof NonLeaf){ // ���� ���� child Ÿ�� ������
			tmp = ((NonLeaf) tmp).npair[0].value;
		}
		return ((LeafNode) tmp).lfpair[0].key;
	}
	
	Boolean put(Pair<Node> put){ // ��忡 put�ϰ� ���� ���� return
		// �� �����־�� ����, key�� �ߺ� no
		if(isFull()) return false;
		int idx = beforeKey(put.key) + 1; // put �� �ڸ�
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
		npair[idx].value = npair[idx+1].value; // ������ �缳��
		npair[idx+1].value = put.value;
		numKey++;
		return true;
	}
	
	Node[] childs(Pair<Node> put){ // �� ��带 ������ childs�� �����ؼ� �迭�� return
		// put�� �� ��忡 �߰��Ǿ���ϴ� pair�μ� value(pointer)�� ���θ������ ��带 �ϴ� �����´�
		// �� ����� ù key���� ��
		Node[] childs = new Node[numKey+2];
		for(int i = 0; i < numKey; i++) // �ϴ� �� �ֱ�
			childs[i] = npair[i].value;
		childs[numKey] = rMstChild;
		childs[numKey+1] = put.value;
		int i;
		// �ڽ��� ù Ű ������ insertion sort
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
	Pair<Node> putSplit(NonLeaf newNonLeaf, Pair<Node> put){ // �ش� ��忡 put�� �����ϸ鼭 newNonLeaf�� split�ϰ� �̸� ��ȯ
		// old�� this �� full�̶�� ���� ��, �����ϴ� key �ߺ��ȵȴٴ� ����
		// �ϴ� put.value�� newNode�� ���� �ż� ����
		// �ڽ��� leaf�� ��쿣 ����
		Pair<Node>[] tmp = new Pair[degree];
		Pair<Node> result; // return���� pair
		Node[] sortedC = childs(put);
		
		int half = (int) Math.ceil((double)degree/2) - 1; // nonleaf���� split�� �� ���� ��
		int idx = beforeKey(put.key) + 1; // �־��� ���� index
		
		for(int i = 0; i < idx; i++) // ���� ��忡 
			tmp[i] = this.npair[i];
		tmp[idx] = put; // ���� data tmp�� �־��ֱ�
		for(int i = idx; i < numKey; i++) //�������� �־���, numkey == degree -1
			tmp[i+1] = this.npair[i];
		// ������� ���ο� data�� �Բ� ũ������� tmp�� ��������	
		
		for(int i = 0; i < half; i++)
			this.npair[i] = tmp[i];
		result = tmp[half];
		for(int i = half+1, n = 0; i < degree; i++, n++)
			newNonLeaf.npair[n] = tmp[i];
		// ������� key�� ���� ceil(n/2)-1, ��� �� ��, ������ �������� ����

		setNumKey(half); // ceil(d/2)-1�� ����, ���� numKey ���̰�
		newNonLeaf.setNumKey(degree - half - 1); // ������ numKey �������ְ�
		
		for(int i = 0 ; i < this.numKey; i++){ // ���� npair�� �θ� �ڽ� ���� ����
			sortedC[i].setParent(this);
			this.npair[i].value = sortedC[i];
		}
		sortedC[this.numKey].setParent(this);
		this.setRMstChild(sortedC[this.numKey]);
		
		for(int i = 0; i < newNonLeaf.numKey; i++){ // ������ npair�� �θ� �ڽ� ���� ����
			sortedC[this.numKey + i + 1].setParent(newNonLeaf);
			newNonLeaf.npair[i].value = sortedC[this.numKey + i + 1];
		}
		sortedC[sortedC.length - 1].setParent(newNonLeaf);
		newNonLeaf.setRMstChild(sortedC[sortedC.length - 1]);
		// ������� �� ���� �� ��� ���� ����
		
		for(int i = numKey; i < degree - 1; i++) // ���� node���� �ű� �͵� ����
			this.npair[i] = null;
		
		result.value = newNonLeaf; // �������� put���� ���� �� ����� ����Ű�� �ϰ� return
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
