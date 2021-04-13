package BPT;
import java.io.*;
import java.util.ArrayList;
public class Main {

	public static void main(String[] args) {
		String cmd, idxFile; // command, name of index file
		
		if (args.length < 3){
			System.out.println("Wrong Command! You should enter as follows"
					+ "\ncreate : -c index_file degree(Integer type)"
					+ "\ninsert : -i index_file data_file"
					+ "\ndelete : -d index_file data_file"
					+ "\nsearch : -s index_file key(Integer type)"
					+ "\nrange search : -r index_file start_key end_key");
			return;
		}
		cmd = args[0]; // command
		idxFile = args[1]; // name of index file

		if (cmd.equals("-c")) { // create : program -c index_file b
			BPTree bptree = new BPTree(Integer.parseInt(args[2]));
			bptree.writeFile(idxFile);
			return;
		}

		BPTree bptree = readFile(idxFile);
		switch (cmd) {
		case "-i": // insertion : -i index_file data_file
			String insertF = args[2];
			ArrayList<Pair<Integer>> indat = readInsert(insertF);
			for (Pair<Integer> p : indat)
				bptree.insert(p.key, p.value);
			bptree.writeFile(idxFile);
			break;
		case "-d": // deletion : -d index_file data_file
			String deleteF = args[2];
			ArrayList<Integer> deldat = readDelete(deleteF);
			for (Integer i : deldat)
				bptree.delete(i);
			bptree.writeFile(idxFile);
			break;
		case "-s": // single search :-s index_file key
			int key = Integer.parseInt(args[2]);
			bptree.search(key);
			break;
		case "-r": // range search : -r index_file start_key end_key
			int start = Integer.parseInt(args[2]);
			int end = Integer.parseInt(args[3]);
			bptree.rangeSearch(start, end);
			break;
		}
	}

	public static BPTree readFile(String idxFile){ // ������ int������ �о bptree���� return
		BPTree bptree = null;
		Node[] allNodes;
		int[][] pointer;
		// num of nodes, degree
		// idx, n/l(0, 1), parent idx, Ű ��, rMstChild/right idx,  records...
		DataInputStream reader = null;
		try {
			reader = new DataInputStream(new BufferedInputStream(new FileInputStream(idxFile)));
			allNodes = new Node[reader.readInt()+1]; // num of nodes
			allNodes[0] = null;
			int degree = reader.readInt(); // degree
			pointer = new int[allNodes.length][degree + 2]; // ���� node ��, ���� �ڴ� (�θ� idx + r idx, records...)
			for(int i = 0; i < degree +2; i++)
				pointer[0][i] = 0;
			
			for(int i = 0 ; i < allNodes.length - 1;i++){ // ��� ���� �б�
				int idx = reader.readInt(); // idx
				if(reader.readInt() == 0){ // nonleaf
					allNodes[idx] = new NonLeaf(degree, null, null);
					pointer[idx][0] = reader.readInt(); // parent idx
					allNodes[idx].setNumKey(reader.readInt()); // Ű ��
					pointer[idx][1] = reader.readInt(); // rMstChild idx
					for(int j = 0; j < allNodes[idx].getNumKey(); j++){ // records...
						((NonLeaf) allNodes[idx]).npair[j] = new Pair<Node>(reader.readInt(),null); // key
						pointer[idx][2+j] = reader.readInt(); // pointer
					}
				}
				else { // leaf
					allNodes[idx] = new LeafNode(degree, null, null);
					pointer[idx][0] = reader.readInt(); // parent idx
					allNodes[idx].setNumKey(reader.readInt()); // Ű ��
					pointer[idx][1] = reader.readInt(); // right idx
					for(int j = 0; j < allNodes[idx].getNumKey(); j++){ // records...
						((LeafNode) allNodes[idx]).lfpair[j] = new Pair<Integer>(reader.readInt(),reader.readInt()); // key, value
					}
				}
			}
			// ������� nodes ���� �Ϸ� ���� ������ �������ֱ�
			for(int i = 1; i <= allNodes.length - 1; i++){
				allNodes[i].setParent((NonLeaf) allNodes[pointer[i][0]]); // parent
				if(allNodes[i] instanceof NonLeaf){
					NonLeaf tmp = (NonLeaf) allNodes[i];
					tmp.rMstChild = allNodes[pointer[i][1]]; // rMstChild
					for(int j = 0; j < tmp.getNumKey(); j++)
						tmp.npair[j].value = allNodes[pointer[i][2+j]]; // record�� pointer ����
				}
			}
			// ���������� BPTree �����ؼ� root �������ְ� BPTree root�� �����ϱ�
			bptree = new BPTree(degree);
			bptree.root = allNodes[1];
		} catch (IOException ioe) {
			System.out.println("������ ���� �� �����ϴ�.");
		} finally {
			try {
				reader.close();
			} catch (Exception e) {
			}
		}
		return bptree;
	}

	public static ArrayList<Pair<Integer>> readInsert(String insertF) {
		// insertF��� �̸��� ������ �о� key, value�� ��� pair�� �迭�� ����� ��ȯ
		ArrayList<Pair<Integer>> indat = new ArrayList<Pair<Integer>>();
		try {
			// csv ������ ����
			File csv = new File(insertF);
			BufferedReader br = new BufferedReader(new FileReader(csv));
			String line = "";

			while ((line = br.readLine()) != null) { // key, value ����
				String[] token = line.split(",");
				indat.add(new Pair<Integer>(Integer.parseInt(token[0]), Integer.parseInt(token[1])));
			}
			br.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return indat;
	}

	public static ArrayList<Integer> readDelete(String deleteF) {
		// deleteF��� �̸��� ���Ͽ��� delete-key�� ��� �迭�� ����� ��ȯ
		ArrayList<Integer> deldat = new ArrayList<Integer>();
		try {
			// csv ������ ����
			File csv = new File(deleteF);
			BufferedReader br = new BufferedReader(new FileReader(csv));
			String line = "";

			while ((line = br.readLine()) != null) // key �迭�� �߰�
				deldat.add(Integer.parseInt(line));
			br.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return deldat;
	}

}