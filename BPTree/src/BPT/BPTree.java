package BPT;

import java.io.*;
import java.util.ArrayList;

public class BPTree implements Serializable{
	private static final long serialVersionUID = 1L;
	int degree = 0; // max num of pointer
	Node root;
	ArrayList<Node> nodes;

	public BPTree(int degree) { // create a b+tree
		this.degree = degree;
		root = new LeafNode(this.degree, null, null);
	}

	@SuppressWarnings("unchecked")
	public void insert(int key, int value) {
		Node inserted = travarse(key); // ���ԵǴ� ��� ó������ LeafNode
		@SuppressWarnings("rawtypes")
		Pair put = new Pair<Integer>(key, value);
		// split ���ص� �ǰų� root�� �������� ������ �ݺ��ؼ� split
		while (inserted.isFull()) {
			Node newNode; // newRoot ������ rMst �������ֱ� ����
			if (inserted instanceof LeafNode) { // split���ְ� �θ� Ȯ���ϱ� ���� put�̶� inserted �缳��
				newNode = new LeafNode(degree, inserted.parent, ((LeafNode) inserted).right);
				((LeafNode) inserted).putSplit((LeafNode) newNode, new Pair<Integer>(key, value));
				put = new Pair<Node>(((LeafNode) newNode).lfpair[0].key, newNode); // �ϴ� value�� ���ο� ��� ������ ��
			} else { // (inserted instanceof NonLeaf)
				newNode = new NonLeaf(degree, inserted.parent, ((NonLeaf) inserted).rMstChild); // �ʱ�ȭ �ƹ����Գ� ���൵ ��
				put = ((NonLeaf) inserted).putSplit((NonLeaf) newNode, put);
			}
			if (inserted.isRoot()) {
				// full�ε� root�� ��� -> ���ο� ��Ʈ ���� �� return
				NonLeaf newRoot = new NonLeaf(degree, null, newNode);
				newRoot.npair[0] = new Pair<Node>(put.key, inserted);
				newRoot.setNumKey(1);
				inserted.setParent(newRoot);
				newNode.setParent(newRoot);
				newRoot.setRMstChild(newNode);
				root = newRoot;
				return;
			}
			inserted = newNode.parent;
		}
		// full�� �ƴ� ��� or �ݺ� �� ������ ���(child)�� ������ ���� �� �־��ֱ�
		if (inserted instanceof LeafNode)
			((LeafNode) inserted).put(put);
		else // inserted instanceof NonLeaf
			((NonLeaf) inserted).put(put);
	}

	public void delete(int key) { // �ش� key�� ����
		Node delNode = travarse(key); // ������ key�� ���� ���ɼ��� �ִ� ���
		int del = key; // ���� or �ٲ�����ϴ� key ��
		int changeTo = 0; // del�� �̰ɷ� �ٲ���
		Boolean delete = true; // true�� ����, false�� change
		Boolean doubleDelete = false;
		int doubleChange = 0;
		if (((LeafNode) delNode).findKey(key) == -1) // �ش� key�� �������� ���� ���
			return;
		while (!delNode.isRoot()) {
			if (!delete) { // �ٲ��ֱ�, �� ��� ������ nonLeaf, changeTo �Ʒ����� �������� ���޵Ǿ�� ��
				NonLeaf changed = (NonLeaf) delNode;
				int idx = changed.findKey(del);
				// �ٲ��� ���� ã�Ҵ�
				if (idx != -1) { // key�� ����
					changed.npair[idx].key = changeTo;
					if(!doubleDelete) return;
					break;
				}
				else {// �ٲ��� ���� ã�� ���ߴ�
					delNode = delNode.parent;
					continue;
				}
			}
			// �����ؾ���
			if (delNode instanceof NonLeaf) { // delNode�� nonLeaf
				// �ش� key ���� ����
				NonLeaf deleted = (NonLeaf) delNode;
				int idx = deleted.findKey(del);
				int tmp = 0;
				if(deleted.getNumKey() != 0) tmp = deleted.npair[0].key;
				//����
				deleted.delete(del, idx);
				if (deleted.isFew()) { // few��
					// sibling ��� 1���� ����
					NonLeaf leftN = deleted.sibling(), rightN; // �� ��尡 �ʿ�
					if(deleted.getNumKey() == 0 && leftN.npair[0].key < tmp){ // ���� ��� �� ��� �Ǿ�����
						rightN = deleted; 
						del = deleted.leftMstLeaf();
						//redistribute�� �ϰ� �����ϸ� ��
						leftN.redistribute(rightN, del);
						return;
						
					}					
					else if (leftN.npair[0].key < deleted.npair[0].key) { // ������ sibling ����
						rightN = deleted;
					}
					else { // ������ sibling �ִ�
						rightN = leftN;
						leftN = deleted;
					}
					del = rightN.leftMstLeaf(); // ���� ������ ����� ù ��° key��
					delNode = rightN.parent; // �� key���� ������ ���
					if (leftN.merge(rightN)) { // merge�� ������ ���, �θ𿡼� delete���ֱ� ���� �ݺ�
						
						continue;
					}
					else { // redistribute
						leftN.redistribute(rightN, del);
						changeTo = rightN.leftMstLeaf();
						delete = false;
						continue;
					}
				}
				else {
					if(idx == -1){
						delete = false;
						changeTo = ((NonLeaf) delNode).leftMstLeaf();
						delNode = delNode.parent;
						continue;
					}
					return;
				}
			}
			else { // delNode�� Leaf, �ִ� 1���� ����
				// ���� �ϸ鼭 ���� �� ����� ù record-key���� ��ȯ
				// del �� key
				int tmp = ((LeafNode) delNode).lfpair[0].key;
				LeafNode leftTmp = ((LeafNode) delNode).getLeft();
				//System.out.println(delNode);
				changeTo = ((LeafNode) delNode).deletePair(key);
				// ��忡 record ���� ���
				if(delNode.getNumKey() == 0){ // �� ��尡 �Ǿ��� ��� �ȿ��� right �ٲ���
					delNode = delNode.parent;
					changeTo = tmp;
					continue;
				}
				if (delNode.isFew()) { // few�̸� merge or redistribute
					// ���� ���� delNode�� root�ϸ� ���� ��� sibling ��� 1���� ����
					// �ִ� �� �� ����� �� ����
					LeafNode leftN, rightN; // �� ��尡 �ʿ�
					if (((LeafNode) delNode).right == null) { // ������ leaf ����
						leftN = leftTmp;
						rightN = (LeafNode) delNode;
					} else { // ������ leaf �ִ�
						leftN = (LeafNode) delNode;
						rightN = ((LeafNode) delNode).right;
					}
					del = rightN.lfpair[0].key; // ������ ����� ù record���� �ٲ�ٸ� �θ� ��忡�� �� key�� ����
					delNode = rightN.parent; // �� del�� ������ ���\
					if (leftN.merge(rightN)) { // merge�� ������ ���, �θ𿡼� delete���ֱ� ���� �ݺ�
						if(tmp != changeTo){ // ù ��° key�� �����ǰ� merge�� ���
							doubleDelete =true; // merge�Ǹ鼭 ������ ù key�� ����, ù ��° record �����Ȱ� �ٲ������
							doubleChange = changeTo;
						}
						continue;
					}
					else { // redistribute
						leftN.redistribute(rightN);
						if(tmp != changeTo){ // ù ��° key�� �����ǰ� merge�� ���
							doubleDelete =true; // redistribute�Ǹ鼭 ������ ù key�� ��ü, ù ��° record �����Ȱ� �ٲ������
							doubleChange = changeTo;
						}
						changeTo = rightN.lfpair[0].key;
						delete = false;
						continue;
					}
				}
				else if (!delNode.isFew() && ((LeafNode) delNode).lfpair[0].key == changeTo) { // not few & 1st record changed
					delete = false;
					delNode = delNode.parent;
					continue;
				}
				else if (!delNode.isFew()) { // not few & 1st record not changed
					return;
				}
				else {
					System.out.println("ERROR");
					return;
				}
			}
		}
		// root ó��
		if (delNode.isRoot()&&delete && delNode instanceof LeafNode){
			((LeafNode) delNode).deletePair(del);
		}
		else if (delNode.isRoot()&&delete) { // nonLeaf�� ���
			int idx = ((NonLeaf) delNode).findKey(del);
			if(idx == -1 && del >= ((NonLeaf) delNode).npair[0].key){
				idx = ((NonLeaf) delNode).findKey(key);
				((NonLeaf) delNode).npair[idx].key = doubleChange;
				idx = ((NonLeaf) delNode).findKey(del);
			}
			((NonLeaf) delNode).delete(del, idx);
			if(delNode.getNumKey() == 0){ // �ڽ��� 1���� ��� �ڽ��� root��
				((NonLeaf) delNode).rMstChild.setParent(null);
				root = ((NonLeaf) delNode).rMstChild;
			}
		}
		else if(delNode.isRoot()){ // ������ �Ѵ� == root�� nonLeaf��
			int idx = ((NonLeaf) delNode).findKey(del);
			if(idx != -1)
				((NonLeaf) delNode).npair[idx].key = changeTo;
		}
		if(doubleDelete){ // key ã�Ƽ� doubleChange�� �ٲ��ֱ�
			while(delNode instanceof NonLeaf){
				int idx = ((NonLeaf) delNode).findKey(key);
				if(idx != -1) {
					((NonLeaf) delNode).npair[idx].key = doubleChange;
					break;
				}
				idx = ((NonLeaf) delNode).beforeKey(key);
				if(idx + 1 == delNode.getNumKey())
					delNode = ((NonLeaf) delNode).rMstChild;
				else delNode = ((NonLeaf) delNode).npair[idx+1].value;
			}
		}
		
	}

	public Pair<Integer> search(int key) { // key�� search
		Node check = root;
		LeafNode leaf; // �������� ������ leaf node

		while (check instanceof NonLeaf) { // nonleaf�� ��
			Pair<Node>[] np = ((NonLeaf) check).getPairs(); // �迭 ���� ���ϴϱ� getPairs
			int i;
			for (i = 0; i < check.getNumKey() - 1; i++) // �������� node�� key�� ��� ���
				System.out.print(np[i].key + ",");
			System.out.println(np[i].key);	
			i = check.getNumKey() - 1;
			// � ���� �Ѿ�� ����
			if (np[i].key <= key) {
				check = ((NonLeaf) check).rMstChild;
				continue;
			}
			while (i > -1 && np[i].key > key) {
				i--;
			}
			check = np[i + 1].value; // ���� level�� node�� �̵�
		}
		leaf = (LeafNode) check; // ���������� ������ �������
		int find = leaf.findKey(key); // �������� search key�� ã�ƺ�
		if (find == -1) {
			System.out.println("NOT FOUND"); // ������ �̷��� ���
			return null;
		}
		System.out.println(leaf.lfpair[find].value); // ������ value ���
		return leaf.lfpair[find];
	}

	public ArrayList<Pair<Integer>> rangeSearch(int start, int end) { // start�̻� end������ key�� search�ؼ� ���
		// start <= end���� ��
		ArrayList<Pair<Integer>> searched = new ArrayList<Pair<Integer>>();
		LeafNode leaf = travarse(start); // start key�� ������ �� �ִ� leaf node, leaf��  �� á���� ��á���� ��
		int idx = leaf.beforeKey(start); // �־���ϴ� idx �� idx
		// Pair<Integer> search; // ���ϴ� key��
		// idx�� leaf�� ���� ���, ���� ���� �Ѿ
		if (idx == leaf.getNumKey() - 1) {
			leaf = leaf.right;
			idx = 0;
		} else
			idx++; // idx�� start���� ũ�ų� ���� key �� ���� ù ��° key�� ����Ű�� �ε���
		while (leaf.lfpair[idx].key <= end) {
			searched.add(leaf.lfpair[idx]);
			idx++;
			if (idx == leaf.getNumKey()) { // �� ��忡�� ������ Ȯ���ϸ� ���� ���� �Ѿ
				leaf = leaf.right;
				idx = 0;
			}
		}
		if (searched.size() == 0) { // ������
			System.out.println("NOT FOUND");
			return null;
		}
		// ������ ��� ���
		for (Pair<Integer> p : searched)
			System.out.println(p.key + "," + p.value);
		return searched;
	}

	public LeafNode travarse(int key) { // key�� ������ �� �ִ� leafnode�� return
		Node check = root;
		while (check instanceof NonLeaf) { // nonleaf�̸�
			Pair<Node>[] np = ((NonLeaf) check).getPairs(); // �迭 ���� ���ϴϱ�
															// getPairs
			int i = check.getNumKey() - 1;
			check = ((NonLeaf) check).rMstChild;
			while (i > -1 && np[i].key > key) {
				i--;
				check = np[i + 1].value; // ���� level�� node�� �̵�
			}
		} // leaf�� �����ϸ� ����
		return (LeafNode) check; // �����Ѱ� �ƴ϶� ���� leaf ����
	}

	public void writeFile(String idxFile){
		nodes = new ArrayList<Node>();
		nodes.add(null);
		nodes(root); // root���� �Է�
		// idx, n/l(0, 1), parent idx, Ű ��, rMstChild/right idx,  records...
		DataOutputStream writer = null;
		try {
			writer = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(idxFile)));
			writer.writeInt(nodes.size()-1); // node ��;
			writer.writeInt(degree);
			for (int i = 1; i <= nodes.size() - 1; i++){
				writer.writeInt(i); // idx
				Node tmp = nodes.get(i);
				if(tmp instanceof LeafNode) // nonleaf?leaf?
					writer.writeInt(1);
				else writer.writeInt(0);
				if(tmp.parent == null) writer.writeInt(0); // parent idx
				else writer.writeInt(nodes.indexOf(tmp.parent));
				writer.writeInt(tmp.getNumKey());
				if(tmp instanceof LeafNode){ // leaf : right + lfpair
					if(((LeafNode) tmp).right == null) writer.writeInt(0); // right
					else writer.writeInt(nodes.indexOf(((LeafNode) tmp).right));
					for(int j = 0; j < tmp.getNumKey(); j++){ // lfpair
						writer.writeInt(((LeafNode) tmp).lfpair[j].key);
						writer.writeInt(((LeafNode) tmp).lfpair[j].value);
					}
				}
				else{ // nonLeaf : rMstChild + npair
					writer.writeInt(nodes.indexOf(((NonLeaf) tmp).rMstChild)); // rMstChild
					for(int j = 0; j < tmp.getNumKey(); j++){ // npair
						writer.writeInt(((NonLeaf) tmp).npair[j].key);
						writer.writeInt(nodes.indexOf(((NonLeaf) tmp).npair[j].value));
					}
				}
			}

		} catch (IOException ioe) {
			System.out.println("���Ϸ� ����� �� �����ϴ�.");
		} finally {
			try {
				writer.close();
			} catch (Exception e) {
			}
		}
	}
	
	public void nodes(Node node){
		nodes.add(node);
		if(node instanceof LeafNode) return;
		for(int i = 0; i < node.getNumKey(); i++){
			nodes(((NonLeaf) node).npair[i].value);
		}
		nodes(((NonLeaf) node).rMstChild);
	}
	public void print() {
		System.out.println(root);
	}
}
