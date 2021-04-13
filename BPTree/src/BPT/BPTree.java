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
		Node inserted = travarse(key); // 삽입되는 노드 처음에는 LeafNode
		@SuppressWarnings("rawtypes")
		Pair put = new Pair<Integer>(key, value);
		// split 안해도 되거나 root에 도달했을 때까지 반복해서 split
		while (inserted.isFull()) {
			Node newNode; // newRoot 생성시 rMst 지정해주기 위해
			if (inserted instanceof LeafNode) { // split해주고 부모 확인하기 위해 put이랑 inserted 재설정
				newNode = new LeafNode(degree, inserted.parent, ((LeafNode) inserted).right);
				((LeafNode) inserted).putSplit((LeafNode) newNode, new Pair<Integer>(key, value));
				put = new Pair<Node>(((LeafNode) newNode).lfpair[0].key, newNode); // 일단 value에 새로운 노드 가지고 감
			} else { // (inserted instanceof NonLeaf)
				newNode = new NonLeaf(degree, inserted.parent, ((NonLeaf) inserted).rMstChild); // 초기화 아무렇게나 해줘도 됨
				put = ((NonLeaf) inserted).putSplit((NonLeaf) newNode, put);
			}
			if (inserted.isRoot()) {
				// full인데 root인 경우 -> 새로운 루트 생성 후 return
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
		// full이 아닌 경우 or 반복 후 마지막 노드(child)에 마지막 넣을 것 넣어주기
		if (inserted instanceof LeafNode)
			((LeafNode) inserted).put(put);
		else // inserted instanceof NonLeaf
			((NonLeaf) inserted).put(put);
	}

	public void delete(int key) { // 해당 key를 삭제
		Node delNode = travarse(key); // 삭제할 key를 가질 가능성이 있는 노드
		int del = key; // 삭제 or 바꿔줘야하는 key 값
		int changeTo = 0; // del을 이걸로 바꿔줌
		Boolean delete = true; // true면 삭제, false면 change
		Boolean doubleDelete = false;
		int doubleChange = 0;
		if (((LeafNode) delNode).findKey(key) == -1) // 해당 key가 존재하지 않을 경우
			return;
		while (!delNode.isRoot()) {
			if (!delete) { // 바꿔주기, 이 경우 무조건 nonLeaf, changeTo 아래에서 정해져서 전달되어야 함
				NonLeaf changed = (NonLeaf) delNode;
				int idx = changed.findKey(del);
				// 바꿔줄 것을 찾았다
				if (idx != -1) { // key값 존재
					changed.npair[idx].key = changeTo;
					if(!doubleDelete) return;
					break;
				}
				else {// 바꿔줄 것을 찾지 못했다
					delNode = delNode.parent;
					continue;
				}
			}
			// 삭제해야함
			if (delNode instanceof NonLeaf) { // delNode가 nonLeaf
				// 해당 key 삭제 먼저
				NonLeaf deleted = (NonLeaf) delNode;
				int idx = deleted.findKey(del);
				int tmp = 0;
				if(deleted.getNumKey() != 0) tmp = deleted.npair[0].key;
				//삭제
				deleted.delete(del, idx);
				if (deleted.isFew()) { // few다
					// sibling 적어도 1개는 있음
					NonLeaf leftN = deleted.sibling(), rightN; // 두 노드가 필요
					if(deleted.getNumKey() == 0 && leftN.npair[0].key < tmp){ // 삭제 결과 빈 노드 되었으면
						rightN = deleted; 
						del = deleted.leftMstLeaf();
						//redistribute만 하고 종료하면 됨
						leftN.redistribute(rightN, del);
						return;
						
					}					
					else if (leftN.npair[0].key < deleted.npair[0].key) { // 오른쪽 sibling 없다
						rightN = deleted;
					}
					else { // 오른쪽 sibling 있다
						rightN = leftN;
						leftN = deleted;
					}
					del = rightN.leftMstLeaf(); // 기존 오른쪽 노드의 첫 번째 key값
					delNode = rightN.parent; // 위 key값을 삭제할 노드
					if (leftN.merge(rightN)) { // merge가 가능할 경우, 부모에서 delete해주기 위해 반복
						
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
			else { // delNode가 Leaf, 최대 1번만 실행
				// 삭제 하면서 삭제 후 노드의 첫 record-key값을 반환
				// del 은 key
				int tmp = ((LeafNode) delNode).lfpair[0].key;
				LeafNode leftTmp = ((LeafNode) delNode).getLeft();
				//System.out.println(delNode);
				changeTo = ((LeafNode) delNode).deletePair(key);
				// 노드에 record 없을 경우
				if(delNode.getNumKey() == 0){ // 빈 노드가 되었을 경우 안에서 right 바꿔줌
					delNode = delNode.parent;
					changeTo = tmp;
					continue;
				}
				if (delNode.isFew()) { // few이면 merge or redistribute
					// 여기 들어온 delNode는 root일리 없음 고로 sibling 적어도 1개는 있음
					// 최대 한 번 실행될 수 있음
					LeafNode leftN, rightN; // 두 노드가 필요
					if (((LeafNode) delNode).right == null) { // 오른쪽 leaf 없다
						leftN = leftTmp;
						rightN = (LeafNode) delNode;
					} else { // 오른쪽 leaf 있다
						leftN = (LeafNode) delNode;
						rightN = ((LeafNode) delNode).right;
					}
					del = rightN.lfpair[0].key; // 오른쪽 노드의 첫 record값이 바뀐다면 부모 노드에서 이 key값 삭제
					delNode = rightN.parent; // 위 del을 삭제할 노드\
					if (leftN.merge(rightN)) { // merge가 가능할 경우, 부모에서 delete해주기 위해 반복
						if(tmp != changeTo){ // 첫 번째 key가 삭제되고 merge된 경우
							doubleDelete =true; // merge되면서 오른쪽 첫 key는 삭제, 첫 번째 record 삭제된건 바꿔줘야함
							doubleChange = changeTo;
						}
						continue;
					}
					else { // redistribute
						leftN.redistribute(rightN);
						if(tmp != changeTo){ // 첫 번째 key가 삭제되고 merge된 경우
							doubleDelete =true; // redistribute되면서 오른쪽 첫 key는 교체, 첫 번째 record 삭제된건 바꿔줘야함
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
		// root 처리
		if (delNode.isRoot()&&delete && delNode instanceof LeafNode){
			((LeafNode) delNode).deletePair(del);
		}
		else if (delNode.isRoot()&&delete) { // nonLeaf일 경우
			int idx = ((NonLeaf) delNode).findKey(del);
			if(idx == -1 && del >= ((NonLeaf) delNode).npair[0].key){
				idx = ((NonLeaf) delNode).findKey(key);
				((NonLeaf) delNode).npair[idx].key = doubleChange;
				idx = ((NonLeaf) delNode).findKey(del);
			}
			((NonLeaf) delNode).delete(del, idx);
			if(delNode.getNumKey() == 0){ // 자식이 1개일 경우 자식을 root로
				((NonLeaf) delNode).rMstChild.setParent(null);
				root = ((NonLeaf) delNode).rMstChild;
			}
		}
		else if(delNode.isRoot()){ // 변경을 한다 == root가 nonLeaf다
			int idx = ((NonLeaf) delNode).findKey(del);
			if(idx != -1)
				((NonLeaf) delNode).npair[idx].key = changeTo;
		}
		if(doubleDelete){ // key 찾아서 doubleChange로 바꿔주기
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

	public Pair<Integer> search(int key) { // key를 search
		Node check = root;
		LeafNode leaf; // 마지막에 도달한 leaf node

		while (check instanceof NonLeaf) { // nonleaf일 때
			Pair<Node>[] np = ((NonLeaf) check).getPairs(); // 배열 수정 안하니까 getPairs
			int i;
			for (i = 0; i < check.getNumKey() - 1; i++) // 지나가는 node의 key들 모두 출력
				System.out.print(np[i].key + ",");
			System.out.println(np[i].key);	
			i = check.getNumKey() - 1;
			// 어떤 노드로 넘어갈지 결정
			if (np[i].key <= key) {
				check = ((NonLeaf) check).rMstChild;
				continue;
			}
			while (i > -1 && np[i].key > key) {
				i--;
			}
			check = np[i + 1].value; // 다음 level의 node로 이동
		}
		leaf = (LeafNode) check; // 마지막으로 도달한 리프노드
		int find = leaf.findKey(key); // 리프에서 search key를 찾아봄
		if (find == -1) {
			System.out.println("NOT FOUND"); // 없으면 이렇게 출력
			return null;
		}
		System.out.println(leaf.lfpair[find].value); // 있으면 value 출력
		return leaf.lfpair[find];
	}

	public ArrayList<Pair<Integer>> rangeSearch(int start, int end) { // start이상 end이하의 key를 search해서 출력
		// start <= end여야 함
		ArrayList<Pair<Integer>> searched = new ArrayList<Pair<Integer>>();
		LeafNode leaf = travarse(start); // start key가 존재할 수 있는 leaf node, leaf가  꽉 찼는지 안찼는지 모름
		int idx = leaf.beforeKey(start); // 넣어야하는 idx 전 idx
		// Pair<Integer> search; // 비교하는 key값
		// idx가 leaf의 끝인 경우, 다음 노드로 넘어감
		if (idx == leaf.getNumKey() - 1) {
			leaf = leaf.right;
			idx = 0;
		} else
			idx++; // idx는 start보다 크거나 같은 key 중 가장 첫 번째 key를 가리키는 인덱스
		while (leaf.lfpair[idx].key <= end) {
			searched.add(leaf.lfpair[idx]);
			idx++;
			if (idx == leaf.getNumKey()) { // 현 노드에서 끝까지 확인하면 다음 노드로 넘어감
				leaf = leaf.right;
				idx = 0;
			}
		}
		if (searched.size() == 0) { // 없으면
			System.out.println("NOT FOUND");
			return null;
		}
		// 있으면 모두 출력
		for (Pair<Integer> p : searched)
			System.out.println(p.key + "," + p.value);
		return searched;
	}

	public LeafNode travarse(int key) { // key가 존재할 수 있는 leafnode를 return
		Node check = root;
		while (check instanceof NonLeaf) { // nonleaf이면
			Pair<Node>[] np = ((NonLeaf) check).getPairs(); // 배열 수정 안하니까
															// getPairs
			int i = check.getNumKey() - 1;
			check = ((NonLeaf) check).rMstChild;
			while (i > -1 && np[i].key > key) {
				i--;
				check = np[i + 1].value; // 다음 level의 node로 이동
			}
		} // leaf에 도달하면 멈춤
		return (LeafNode) check; // 복사한게 아니라 실제 leaf 전달
	}

	public void writeFile(String idxFile){
		nodes = new ArrayList<Node>();
		nodes.add(null);
		nodes(root); // root부터 입력
		// idx, n/l(0, 1), parent idx, 키 수, rMstChild/right idx,  records...
		DataOutputStream writer = null;
		try {
			writer = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(idxFile)));
			writer.writeInt(nodes.size()-1); // node 수;
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
			System.out.println("파일로 출력할 수 없습니다.");
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
