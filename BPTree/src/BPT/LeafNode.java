package BPT;

public class LeafNode extends Node{
	LeafNode right; // null �� ����
	Pair<Integer>[] lfpair; //pairs <key, value> int/int������ ��� �迭
	
	@SuppressWarnings("unchecked")
	LeafNode(int degree, NonLeaf parent, LeafNode right){
		super(degree, parent);
		this.right = right;
		lfpair = new Pair[degree-1];
	}
	Pair<Integer>[] getPairs(){ // �� ����� pair�� ���� �迭 ���� �� ��ȯ
		return lfpair.clone();
	}
	
	void setRight(LeafNode right){
		this.right = right;
	}
	
	Boolean isFew(){ // �ּ� ceil(n-1/2) ���� �� ��������
		if(numKey < Math.ceil((double)((degree-1)/2)))
			return true;
		return false;
	}
	
	int findKey(int key){ //key ���� Ȯ��, ������ -1 ������ idx ��ȯ
		for(int i = 0; i < numKey; i++)
			if(lfpair[i].key == key)
				return i;
		return -1;
	}
	
	int beforeKey(int key){ // key�� �� �� �ִ� �ڸ� �� index return
		int max = -1;
		for(int i = 0; i < numKey && lfpair[i].key < key; i++)
			max = i;
		return max;
	}
	Boolean put(Pair<Integer> put){ // leaf�� put�� �߰��ϰ� �������� return
		// �� �����־�� ����, key�� �ߺ� no
		if(isFull()) return false;
		int idx = beforeKey(put.key) + 1; // put �� �ڸ�
		for(int i = numKey - 1; i >= idx; i--){
			lfpair[i+1] = lfpair[i];
		}
		lfpair[idx] = put;
		numKey++;
		return true;
	}
	
	LeafNode getLeft(){ // ���� ���� ��带 return, leaf�� �ݵ�� 2�� �̻� ����
		// numKey 1�� �̻��̶� ����
		if(this.isRoot()) return null; // root�� ���
		
		NonLeaf check = this.parent;
		int findKey = this.lfpair[0].key;
		int idx = -1;
		while(!check.isRoot()){ // Ÿ�� �ö�
			idx = check.findKey(findKey);
			if(idx == -1){
				check = check.parent;
				continue;
			}
			break; // ã���� break
		}
		// root �̰ų� ã���� break
		idx = check.findKey(findKey);
		if(check.isRoot() && check.findKey(findKey) == -1)
			return null; // ���� ���� �����
		if(check.npair[0].value instanceof NonLeaf){ // �ڽĳ�尡 nonleaf�� ���
			check = (NonLeaf)check.npair[idx].value;
			while(check.npair[0].value instanceof NonLeaf) // Ÿ�� ������
				check = (NonLeaf) check.rMstChild;
			return (LeafNode)check.rMstChild;
		}
		else { // �ڽ� ��尡 leaf�� ��� �ٷ� return
			return (LeafNode)check.npair[idx].value;
		}
	}
	Boolean merge(LeafNode rightN){ // return���δ� merge ���ɼ�(null�� ��� merge�� �� ����), �� ��带 merge ����
		// ���� : few�� ���� ���, root�� ���� ����� �� ����
		if(this == null || rightN == null){
			System.out.println("MERGE() null pointer ERROR");
			return false;
		}
		if(getNumKey() + rightN.getNumKey() >= degree) // �� �� ��ġ�� key������ degree�� �̻�
			return false;
		for(int i = getNumKey(), n = 0; i < getNumKey() + rightN.getNumKey(); i++,n++) {// ���� ��忡 ������
			lfpair[i] = rightN.lfpair[n];
		}
		right = rightN.right; // ������ leaf �缳��
		setNumKey(getNumKey() + rightN.getNumKey());
		return true;
		
	}
	@SuppressWarnings("unchecked")
	void redistribute(LeafNode rightN){ // �� ��带 rightN�� redistribute
		// few�� ���� ���
		// root�� ���� ����� �� ����, merge �˻縦 ��ģ �Ķ� ����
		if(this == null || rightN == null){
			System.out.println("REDISTRIBUTE() null pointer ERROR");
			return;
		}
		int total = getNumKey() + rightN.getNumKey(); // �� Ű��
		Pair<Integer>[] tmp = new Pair[total]; // tmp�� �� ��� record ��� ����
		for(int i = 0; i < numKey; i++)
			tmp[i] = lfpair[i];
		for(int i = 0; i < rightN.getNumKey(); i++)
			tmp[numKey + i] = rightN.lfpair[i];
		// total/2���� �� ���(����), total - total/2���� ������(rightN)�� �־����
		// �������� null��
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
		setNumKey(total/2); // numKey �缳��
		rightN.setNumKey(total - total/2);
	}
	
	int deletePair(int key){ // �ش� Ű�� record�� ����, return�� ���� �� �� ����� ù ��° record�� key
		int find = findKey(key); // key ������� ã��
		if(find != -1){ //�����Ѵٸ�
			if(numKey == 1){ // �� ��尡 �Ǿ���� ��� , ������ key�� ��ȯ
				if(getLeft() != null)
					getLeft().right = right;
				numKey--;
				return key;
			}
			for(int i = find; i < numKey-1; i++){ // key �����ߴٸ� �� ĭ�� �������� ���
				lfpair[i] = lfpair[i+1];
			}
			lfpair[numKey-1] = null; // �� ������ record null��
			numKey--;
			return lfpair[0].key;
		}
		System.out.println("\nERR) There are No pair with the key in Leaf");
		return -1;
	}
	
	@SuppressWarnings("unchecked")
	LeafNode putSplit(LeafNode newLeaf, Pair<Integer> put){ // put�� �����鼭 �� ���� split �� ���� ������� ��带 ��ȯ
		// full�̶�� ���� ��, �����ϴ� key �ߺ��ȵȴٴ� ����
		Pair<Integer>[] tmp = new Pair[degree];
		int idx = beforeKey(put.key) + 1; // �־��� ���� index
		for(int i = 0; i < idx; i++)
			tmp[i] = lfpair[i];
		tmp[idx] = put; // ���� data tmp�� �־��ְ�
		for(int i = idx; i < numKey; i++) //�������� �־���
			tmp[i+1] = lfpair[i];
		// ������� ���ο� data�� �Բ� ũ������� tmp�� ��������

		setNumKey((int) Math.ceil((double)((degree-1)/2))); // ceil(d-1/2)�� ����
		for(int i = numKey, n = 0; i < degree; i++,n++)
			newLeaf.lfpair[n] = tmp[i]; // ���ο� leaf�� ������� �־���
		for(int i = 0; i < numKey; i++) // ����node �������ְ�
			lfpair[i] = tmp[i];
		for(int i = numKey; i < degree - 1; i++) // ���� node���� �ű� �͵� ����
			lfpair[i] = null;
		newLeaf.setNumKey(degree - numKey);
		// ������ �缳��
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
