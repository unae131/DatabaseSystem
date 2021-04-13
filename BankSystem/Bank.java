package bank_p4;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.InputMismatchException;
import java.util.Random;
import java.util.Scanner;
import java.util.Date;

public class Bank {
	static Connection con;
	static Statement stmt;
	static ResultSet rs;
	static Scanner sc;
	public static void main(String[] args) {
		sc = new Scanner(System.in);
		int mode;
		try {
			con = DriverManager.getConnection("jdbc:mariadb://localhost:3306/bank?user=root&password=unae131");
			stmt = con.createStatement();
			
			System.out.println("***************************************");
			System.out.println("           Bank System Start           ");
			System.out.println("***************************************");
			
			do {
				System.out.println("Choose Mode( 0.Admin / 1.User / 2.Quit )");
				
				try{
					mode = sc.nextInt();
				} catch (InputMismatchException ime) {
					System.out.println("Input can be only int type.");
					mode = 3;
					sc.nextLine();
				}
				
				switch(mode){
				case 0 :
					startAdmin();
					break;
				case 1 :
					startUser();
					break;
				case 2 :
					System.out.println("***************************************");
					System.out.println("                Ok, BYE!               ");
					System.out.println("***************************************");
					break;
				default :
					System.out.println("Choose among 0, 1, 2\n");
				}
			} while (mode != 2);
			sc.close();
			if(rs != null) rs.close();
			stmt.close();
			con.close();
			
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
	}
	private static void startUser() {
		String ssn;
		int cmd;
		
		System.out.println("Choose( 0.Sign In / #.Sign Up )");
		cmd = sc.nextInt();
		sc.nextLine();
		switch(cmd){
		case 0 :
			System.out.println("Type your ssn.");
			while(!checkSsn(ssn =  sc.nextLine())){
				System.out.println("( 0.Retry / #.Back to menu )");
				int tmp = sc.nextInt();
				sc.nextLine();
				if(tmp != 0) 
					return;
				else System.out.println("Type your ssn.");
			}
			if(findUser(ssn)){ // �����ϸ�
				menuUser(ssn);
			}
			
			break;
		default :
			makeUser();
			break;
		}
	}
	private static void menuUser(String ssn){ // �� ����ڰ� �޴��� ��
		int cmd;
		long amount;
		String anum, canum, cnum;
		ResultSet crs;
		try {
		while(true){
		System.out.println("�޴��� ������."
				+ "\n0. ������ ���� ��� ����"
				+ "\n1. Ư�� ���� �� ����"
				+ "\n2. ���� ����/�����ϱ�"
				+ "\n3. ���� ��ü�ϱ�"
				+ "\n4. ���/�Ա��ϱ�"
				+ "\n5. ������ ī�� ��� ����"
				+ "\n6. ī�� �߱�/����"
				+ "\n7. ī�� ����ϱ�"
				+ "\n8. �ſ��� �˾ƺ���"
				+ "\n9. �������� �����ϱ�"
				+ "\n#. ���޴��� ���ư���");
			cmd = sc.nextInt();
			sc.nextLine();
			switch(cmd) {
			case 0 :
				showAccounts(ssn);
				break;
			case 1 : // Ư�� ���� �� ����
				do {
					System.out.println("14�ڸ� ���¹�ȣ�� '-'���� ���ڸ� �Է��ϼ���.");
					anum = sc.nextLine();
				} while(!checkAnum(anum));
				showAccount(ssn, anum);
				break;
			case 2 : // ����/����
				System.out.println("�޴��� ������.\n( 0.���°��� / 1.�������� / #.��� )");
				cmd = sc.nextInt();
				sc.nextLine();
				switch(cmd) {
				case 0 :
					makeAccount(ssn);
					break;
				case 1 :
					do {
						System.out.println("������ ������ 14�ڸ� ���¹�ȣ�� '-'���� ���ڸ� �Է��ϼ���.");
						anum = sc.nextLine();
					} while(!checkAnum(anum));
					deleteAccount(ssn, anum);
					break;
				default :
					break;
				}
				break;
			case 3: // ������ü�ϱ�
				do {
					System.out.println("����� ������ 14�ڸ� ���¹�ȣ�� '-'���� ���ڸ� �Է��ϼ���.");
					anum = sc.nextLine();
				} while(!checkAnum(anum));
				rs = stmt.executeQuery("select * from account where ssn = '" + ssn + "'"
						+ " and anum = '" + anum + "'");
				if(!rs.next()){
					System.out.println("�ش� ���°� �������� �ʽ��ϴ�.");
					break;
				}
				do {
					System.out.println("�Ա��� ������ 14�ڸ� ���¹�ȣ�� '-'���� ���ڸ� �Է��ϼ���.");
					canum = sc.nextLine();
				} while(!checkAnum(canum));
				if((crs = selectAccount(canum)) == null ) break;
				System.out.println("�ݾ��� �Է��ϼ���.");
				checkTransfer(rs, crs, sc.nextLong());
				sc.nextLine();
				break;
			case 4: // �Ա�/����ϱ�
				System.out.println("( 0.���, #.�Ա� )");
				cmd = sc.nextInt();
				sc.nextLine();
				
				if(cmd == 0) do {
					System.out.println("����� ������ 14�ڸ� ���¹�ȣ�� '-'���� ���ڸ� �Է��ϼ���.");
					anum = sc.nextLine();
				} while(!checkAnum(anum));
				else do {
					System.out.println("�Ա��� ������ 14�ڸ� ���¹�ȣ�� '-'���� ���ڸ� �Է��ϼ���.");
					anum = sc.nextLine();
				} while(!checkAnum(anum));
				
				rs = stmt.executeQuery("select * from account where ssn = '" + ssn + "'"
						+ " and anum = '" + anum + "'");
				if(!rs.next()){
					System.out.println("�ش� ���°� �������� �ʽ��ϴ�.");
					break;
				}
				
				System.out.println("�ݾ��� �Է��ϼ���.");
				amount = sc.nextLong();
				sc.nextLine();
				if(cmd == 0) withdraw(rs, amount, "ATM");
				else putMoney(rs, amount);
				break;
			case 5: // ������ ī�� ��� �����ֱ�
				int check = 0;
				rs = stmt.executeQuery("select cnum, mbank, card.anum "
						+ "from account, card, admin "
						+ "where ssn = '" + ssn + "' and card.anum = account.anum "
								+ "and account.mnum = admin.mnum");
				System.out.println("ī���ȣ / ���� / ����� ���¹�ȣ");
				while(rs.next()){
					System.out.println(rs.getString("cnum") + " " + rs.getString("mbank") 
						+ " " + rs.getString("anum"));
					check += 1;
				}
				if(check == 0)
					System.out.println("�����ϰ� ��� ī�尡 �����ϴ�.");
				break;
			case 6: // ī�� �߱�/����
				Long limit, tmp;
				System.out.println("( 0.ī��߱� / #.ī������ )");
				cmd = sc.nextInt();
				sc.nextLine();
				if(cmd == 0){
					do {
						System.out.println("ī��� ������ ������ 14�ڸ� ���¹�ȣ�� '-'���� ���ڸ� �Է��ϼ���.");
						anum = sc.nextLine();
					} while(!checkAnum(anum));
					
					rs = stmt.executeQuery("select * from account where ssn = '" + ssn + "'"
							+ " and anum = '" + anum + "'");
					
					if(!rs.next()){
						System.out.println("�ش� ���°� �������� �ʽ��ϴ�.");
						break;
					}
					
					Date today = new Date();
					if(rs.getString("kind").equals("inst")){
						System.out.println("���ݰ��´� ī�带 �߱޹��� �� �����ϴ�.");
						break;
					}
					else if(compareDate(rs.getDate("enddate"), today) <= 0){
						System.out.println("����� ���´� ī�带 �߱޹��� �� �����ϴ�.");
						break;
					}
	
					limit = rs.getLong("account.limit");
					if(rs.getString("kind").equals("dpst") && rs.getObject("account.limit") != null) {
						do {
							System.out.println("�ѵ��� ���ϼ���. (10000 <= " + limit + ")");
							tmp = sc.nextLong();
							sc.nextLine();
						} while (tmp > limit && tmp < 10000);
						limit = tmp;
					}
					else if(rs.getString("kind").equals("dpst")){
						System.out.println("�ѵ��� ���ϼ���. ('0'�Է½� �ѵ� ����.)");
						tmp = sc.nextLong();
						sc.nextLine();
						if(tmp == 0) limit = null;
					}
					
					stmt.executeUpdate("insert into card values('" + (cnum = makeAorCnum(16)) + "',"
							+ limit + ",'" + rs.getString("anum") + "')");
					System.out.println("ī�尡 �߱޵Ǿ����ϴ�. ī���ȣ�� " + cnum + "�Դϴ�.");
					break;
				}
				else {
					int flag = 0;
					rs = stmt.executeQuery("select cnum from account, card, user "
							+ "where user.ssn = '" + ssn + "' and "
							+ "user.ssn = account.ssn and account.anum = card.anum");
					if(!rs.next()){
						System.out.println("�����ϰ� ��� ī�尡 �����ϴ�.");
						break;
					}
					do {
						System.out.println("������ ī���ȣ 16�ڸ��� '-'���� ���ڸ� �Է��ϼ���.");
						cnum = sc.nextLine();
					} while(!checkCnum(cnum));
					do{
						if(rs.getString("cnum").equals(cnum)){
							stmt.executeUpdate("delete from card where cnum = '" + cnum + "'");							
							System.out.println("ī�带 ���������� �����Ͽ����ϴ�.");
							flag = 1;
							break;
						}
					} while(flag == 0 && rs.next());
					if(flag == 0) System.out.println("�����ϰ� ��� ī�� �� �ش� ī�尡 �������� �ʽ��ϴ�.");
					break;
				}
			case 7 : // ī�� ����ϱ�
				rs = stmt.executeQuery("select cnum, climit, account.* from account, card, user "
						+ "where user.ssn = '" + ssn + "' and "
						+ "user.ssn = account.ssn and account.anum = card.anum");
				if(!rs.next()){
					System.out.println("�����ϰ� ��� ī�尡 �����ϴ�.");
					break;
				}
				do {
					System.out.println("����� ī���ȣ 16�ڸ��� '-'���� ���ڸ� �Է��ϼ���.");
					cnum = sc.nextLine();
				} while(!checkCnum(cnum));
				int flag = 0;
				do{
					if(rs.getString("cnum").equals(cnum)){
						System.out.println("������ �ݾ��� �Է��ϼ���.");
						amount = sc.nextLong();
						sc.nextLine();
						withdraw(rs, amount, "ī��" + rs.getString("cnum").substring(0,4));
						flag = 1;
						break;
					}
				} while(flag == 0 && rs.next());
				if(flag == 0) System.out.println("�����ϰ� ��� ī�� �� �ش� ī�尡 �������� �ʽ��ϴ�.");
				break;
			case 8 :
				System.out.println("���� �̿� ���� ���� :");
				rs = stmt.executeQuery("select distinct mbank"
						+ " from user, account, admin"
						+ " where user.ssn = '" + ssn + "' and"
								+ " user.ssn = account.ssn and account.mnum = admin.mnum");
				check = 0;
				while(rs.next()){
					System.out.println(rs.getString("mbank"));
					check += 1;
				}
				if(check == 0){
					System.out.println("�̿� ���� ������ ���� �ſ��򰡸� �� �� �����ϴ�.");
					break;
				}
				System.out.println("�ſ��� Ȯ���� ���� �̸��� '��Ȯ��' �Է��ϼ���.");
				System.out.println(calcCredit(ssn, sc.nextLine()));
				break;
			case 9 : // �������� �����ϱ�
				editUserInfo(ssn);
				break;
			default :
				return;
			}
		}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	private static int calcCredit(String ssn, String mbank) throws SQLException{ // ssn�� ���� ����, mbank�� ����x
		int rate = 5;
		boolean overdue = false;
		Date today = new Date();
		rs = stmt.executeQuery("select salary, account.* "
				+ "from user, account, admin "
				+ "where user.ssn = account.ssn and account.mnum = admin.mnum "
				+ "and user.ssn = '" + ssn + "' and mbank = '" + mbank +"'");
		if(!rs.next()){
			System.out.println("��� ���� ���� �߿� " + mbank + "������ �������� �ʾ� �ſ������� �� �� �����ϴ�.");
			return -1;
		}
		
		if(rs.getLong("salary") > 200000000 ) rate -= 4;
		else if(rs.getLong("salary") > 100000000 ) rate -= 3;
		else if(rs.getLong("salary") > 80000000) rate -= 2;
		else if(rs.getLong("salary") > 50000000) rate -= 1;
		else if(rs.getLong("salary") < 20000000) rate += 1;
		do{
			if(rs.getString("kind").equals("loan")) {
				rate += 1;
				if(compareDate(rs.getDate("enddate"), today) <= 0){
					rate += 1;
					if(difMonth(today, rs.getDate("enddate")) > 12 ) rate += 5;
					if(rs.getLong("sum") < -50000000) rate += 2;
					overdue = true;
				}
			}
			else {
				if(rs.getLong("sum") > 50000000) rate -= 1;
				if(difMonth(today, rs.getDate("startdate")) > 48) rate -= 2;
				else if(difMonth(today, rs.getDate("startdate")) > 30) rate -=1;
			}
		} while(rs.next());
		if(rate < 1) rate = 1;
		else if(rate > 10) rate = 10;
		
		// credit table �����ϱ�
		rs = stmt.executeQuery("select credit.* "
				+ "from credit, admin "
				+ "where ssn = '" + ssn + "' and credit.mnum = admin.mnum "
						+ "and admin.mbank = '" + mbank + "'");
		if(rs.next()) { // �̹� ����
			stmt.executeUpdate("update credit "
					+ "set credit = " + rate + ", overdue = " + overdue 
					+ " where mnum = '" + rs.getString("credit.mnum") + "' "
					+ "and ssn = '" + ssn + "'");
		}
		else { // �������� ���� ���
			rs = stmt.executeQuery("select mnum from admin where mbank = '" + mbank + "'");
			rs.next();
			stmt.executeUpdate("insert into credit values('" + rs.getString("mnum") + "','" + ssn
					+ "'," + rate + "," + overdue + ")");
		}
		return rate;
	}
	private static void editUserInfo(String ssn){
		int cmd;
		long salary;
		String input;
		try {
			rs = stmt.executeQuery("select * from user where ssn = '" + ssn + "'");
			if(!rs.next()){
				System.out.println("Something is wrong!");
				return;
			}
			while(true){
				System.out.println("������ ������ �����ϼ���.");
				System.out.println("( 0.��ȭ��ȣ / 1.���ҵ� / 2.���� / 3.�̸� / 4.�̸��� / 5.������ / #.������� )");
				cmd = sc.nextInt();
				sc.nextLine();
				switch(cmd){
				case 0 :
					do{
						System.out.println("�ٲ� ��ȭ��ȣ�� �Է��ϼ���. ��Ҹ� ���Ͻø� 'q'�� �Է����ּ���.");
						input = sc.nextLine();
						if(input.equals("q")) return;
					} while(!checkPh(input));
					stmt.executeUpdate("update user set phonenum = '" + input + "' "
							+ "where ssn = '" + ssn + "'");
					break;
				case 1 :
					System.out.println("�ٲ� ���ҵ��� �Է��ϼ���. ��Ҹ� ���Ͻø� -1�� �Է����ּ���.");
					salary = sc.nextLong();
					sc.nextLine();
					if(salary < 0) return;
					if(salary <= 0){
						System.out.println("����� ���� �Է����ּ���.");
						break;
					}
					stmt.executeUpdate("update user set salary = " + salary + " "
							+ "where ssn = '" + ssn + "'");
					break;
				case 2 :
					System.out.println("�ٲ� ������ �Է��ϼ���. ��Ҹ� ���Ͻø� 'q'�� �Է����ּ���.");
					input = sc.nextLine();
					if(input.equals("q")) return;
					stmt.executeUpdate("update user set job = '" + input + "' "
							+ "where ssn = '" + ssn + "'");
					break;
				case 3 :
					System.out.println("�ٲ� �̸��� �Է��ϼ���. ��Ҹ� ���Ͻø� 'q'�� �Է����ּ���.");
					input = sc.nextLine();
					if(input.equals("q")) return;
					stmt.executeUpdate("update user set name = '" + input + "' "
							+ "where ssn = '" + ssn + "'");
					break;
				case 4 :
					System.out.println("�ٲ� �̸��� �ּҸ� �Է��ϼ���. ��Ҹ� ���Ͻø� 'q'�� �Է����ּ���.");
					input = sc.nextLine();
					if(input.equals("q")) return;
					stmt.executeUpdate("update user set email = '" + input + "' "
							+ "where ssn = '" + ssn + "'");
					break;
				case 5 :
					System.out.println("�ٲ� �������� �Է��ϼ���. ��Ҹ� ���Ͻø� 'q'�� �Է����ּ���.");
					input = sc.nextLine();
					if(input.equals("q")) return;
					stmt.executeUpdate("update user set address = '" + input + "' "
							+ "where ssn = '" + ssn + "'");
					break;
				default :
					return;
				}
			}
		} catch (SQLIntegrityConstraintViolationException e1){
			System.out.println("�ߺ��Ǵ�  ��ȭ��ȣ�Դϴ�. �ٽ� Ȯ���غ�����.");
			return;
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
	}
	@SuppressWarnings("deprecation")
	private static void putMoney(ResultSet account, long amount){ // �Աݸ�
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date end = new Date();
		Date start = new Date();
		String date = "", anum = "";
		int error = 0;
		try {
			switch(account.getString("kind")){
			case "inst" :
				// �޴� ���� �ѵ� Ȯ��
				start.setDate(1);
				start.setHours(0);
				start.setMinutes(0);
				start.setSeconds(0);
				if(account.getObject("account.limit") != null
					&& account.getLong("account.limit") - sumMoney(account.getString("anum"), (byte) 1, start,end) < amount){
					System.out.print("!!�������ʰ�!! �ݿ� �ش� ���¿� ������ �� �ִ� �ݾ��� �ʰ��Ͽ����ϴ�.");
					return;
				}
				break;
			case "loan" :
				// �޴� ���� �ѵ� Ȯ��
				start.setDate(1);
				start.setHours(0);
				start.setMinutes(0);
				start.setSeconds(0);
				if(account.getObject("account.limit") != null
					&& (-account.getLong("sum")) < amount){
					System.out.print("!!��ȯ���ʰ�!! �ش� ���¿� ��ȯ�� �� �ִ� �ݾ��� �ʰ��Ͽ����ϴ�.");
					return;
				}
				break;
			default :
				break;
			}
			// �Ա��ϱ�
			dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			date = "'" + dateFormat.format(new Date()) + "'";
			anum = "\'" + account.getString("anum") + "\'";
			// money�� �Ա� tuple �߰�
			stmt.executeUpdate("insert into money(money.when, dir, amount, anum, cname)"
					+ " values("+date+", 1, "+amount+", "+anum +", 'ATM')");
			error = 1;
			// from account���� �ݾ� ���ϱ�
			stmt.executeUpdate("update account set sum = sum + " + amount + " where anum = " + anum);
			error = 0;
			System.out.println("���� " + amount + "���� �Ա��Ͽ����ϴ�.");
			
		} catch (SQLException e) {
			e.printStackTrace();
			try {
			if(error == 1)
				stmt.executeUpdate("delete from money where money.when = "+date+" and anum = " + anum);
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}
	@SuppressWarnings("deprecation")
	private static void withdraw(ResultSet account, long amount, String cname){ // ��ݸ�
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date end = new Date();
		Date start = new Date();
		String date = "", anum = "", fin;
		Long sum, save = null, limit;
		int error = 0;
		try {
			switch(account.getString("kind")){
			case "dpst" :
				// �ܾ� Ȯ��
				if(account.getLong("sum") < amount) {
					System.out.println("�ܾ��� �����մϴ�.");
					return;
				}
				// �ѵ� Ȯ��
				start.setHours(0);
				start.setMinutes(0);
				start.setSeconds(0);
				if(account.getObject("account.limit") != null
						&& (limit = account.getLong("account.limit") - sumMoney(account.getString("anum"), (byte) 0, start,end)) < amount){
					System.out.print("!!�ѵ��ʰ�!! ���� " + account.getLong("account.limit") + "�� ��� �����մϴ�.");
					System.out.println(" " + limit + "�� ���ҽ��ϴ�.");
					return;
				}
				break;
			case "inst" :
				// ���� �̿� ���� ��
				if(!cname.equals("ATM")){
					System.out.println("���� ���´� ī�带 �߱޹��� �� �����ϴ�.");
					return;
				}
				if(compareDate(account.getDate("enddate"), end) > 0){
					sum = calcInterest(account); // ���� ����ϱ�
					// ���� �� �ִ� �ݾ����� Ȯ��
					if(sum < amount){
						System.out.println("�ݾ��� �����մϴ�.");
						return;
					}
					
					// �۱��� ������
					System.out.println("��ݽ� ���� �̿��� ����˴ϴ�."
							+ "\n���� �̿� ����� ���ڰ� �ջ�ǰ�, ���ú��� ��ݸ� �̿� �����մϴ�.\n ����Ͻðڽ��ϱ�?( 0.��� / #.���� )");
					if(sc.nextInt() != 0) {
						System.out.println("����� ����մϴ�.");
						break;
					}
					
					// ���� �̿� ����
					System.out.println("���� �̿��� �����մϴ�...");
					error = 1;
					start = account.getDate("enddate"); // rollback �����, ������¥ ���
					stmt.executeUpdate("update account set enddate = '" + dateFormat.format(end) 
						+ "' where anum = '" + account.getString("anum") + "'");
					error = 2;
					save = account.getLong("sum"); // rollback���
					stmt.executeUpdate("update account set sum = " + sum + " where anum = '" + account.getString("anum") + "'");
					error = 0;
					System.out.println("���� �ջ� �� ���¿� " + sum +"���� ���ҽ��ϴ�.\n���� ���� ���� �ܾ��� ���/��ü �� �������ּ���.");
				}
				// ���� �̿� ���� ��
				else if(account.getLong("sum") < amount){
					System.out.println("�ݾ��� �����մϴ�.");
					return;
				}
				break;
		
			case "loan" :
				// ������ ���� �������� Ȯ��
				if(!checkEnddate(account)) return;
				// �ѵ� Ȯ��
				if(account.getLong("account.limit") + account.getLong("sum") < amount) {
					System.out.println("!!�ѵ��ʰ�!! ���� ������ �ݾ��� �ʰ��Ͽ����ϴ�.");
					return;
				}
				// ���Ⱑ���� �ѵ� ���߱�
				stmt.executeUpdate("update account set account.limit = account.limit - " + amount 
						+ " where anum = '" + account.getString("anum") + "'");
				stmt.executeUpdate("update card set climit = climit - " + amount
						+ " where anum = '" + account.getString("anum") + "'");
				error = 3;
				break;
			}
			// ����ϱ�
			dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			date = "'" + dateFormat.format(new Date()) + "'";
			anum = "\'" + account.getString("anum") + "\'";
			
			if(cname.equals("ATM")) fin = "����";
			else fin = "ī�忡��";
			
			// money�� tuple �߰� from�� ���� ����
			stmt.executeUpdate("insert into money(money.when, dir, amount, anum, cname)"
					+ " values("+date+", 0, "+amount+", "+anum +", '" + cname + "')");
			error = 4;
			// from account���� �ݾ� ����
			stmt.executeUpdate("update account set sum = sum - " + amount + " where anum = " + anum);
			error = 0;
			System.out.println(fin + amount + "���� ����Ͽ����ϴ�.");
			
		} catch (SQLException e) {
			e.printStackTrace();
			try {
			if(error == 1 || error == 2)
				stmt.executeUpdate("update account set enddate = '" + dateFormat.format(start) 
					+ "' where anum = '" + account.getString("anum") + "'");
			if(error == 2)
				stmt.executeUpdate("update account set sum = " + save
					+ " where anum = '" + account.getString("anum") + "'");
			if(error == 3)
				stmt.executeUpdate("update account set account.limit = account.limit + " + amount 
						+ " where anum = '" + account.getString("anum") + "'");
			if(error == 4 && account.getString("kind").equals("loan")) {
				stmt.executeUpdate("update account set account.limit = account.limit + " + amount 
						+ " where anum = '" + account.getString("anum") + "'");
				stmt.executeUpdate("delete from money where money.when = "+date+" and anum = " + anum);
			}
			else if(error == 4){
				stmt.executeUpdate("delete from money where money.when = "+date+" and anum = " + anum);
			}
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
	}
	private static boolean showAccount(String ssn, String anum) {
		try {
			rs = stmt.executeQuery("Select mbank, anum, startdate, enddate, sum, kind, account.limit, interestR"
					+ " From account, admin"
					+ " Where ssn = \'" + ssn + "\' and anum = \'" + anum + "\' and account.mnum = admin.mnum");
			if(rs.next()){ // �ݵ�� �ϳ��� �־����.
				System.out.println("���� : "+ rs.getString("mbank") + 
						"\n���¹�ȣ : " + rs.getString("anum") + 
						"\n������¥ : " + rs.getDate("startdate") + 
						"\n����/���� ��¥ : " + rs.getDate("enddate") + 
						"\n�Ѿ� : " + rs.getLong("sum"));
				switch(rs.getString("kind")) {
				case "dpst" :
					System.out.print("���� : ����\n�ѵ� : ");
					if(rs.getObject("account.limit")==null) System.out.println("����");
					else System.out.println("�Ϸ翡 " + rs.getLong("account.limit") + " ������ ��� ����");
					break;
				case "inst" :
					System.out.println("���� : ����\n�ſ� �ִ� " + rs.getLong("account.limit") + " �� �̳� ���� ����");
					break;
				case "loan" :
					System.out.println("���� : ����\n�ִ� " + rs.getLong("account.limit") + " ������ ��� ����");
					break;
				default :
					System.out.println("DB ERR : Wrong Data!");
					return false;
				}
				System.out.println("������ : " + rs.getDouble("interestR") + "\n�ŷ����� :");
				transaction(anum);
				return true;
			}
			else {
				System.out.println("You don't have that account.");
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	private static void showAccounts(String ssn) { // ssn ��ȿ�ϰ�, �����Ѵ� ����
		int check = 0;
		try {
			rs = stmt.executeQuery("Select mbank, anum, sum, enddate, kind "
					+ "From account, admin "
					+ "Where ssn = \'" + ssn + "\' and admin.mnum = account.mnum");
			System.out.println("( ���� / ���¹�ȣ / ���� / �ܾ� / ���ᳯ¥ )");
			while(rs.next()){
				System.out.println(rs.getString("mbank") + " " + rs.getString("anum")
					+ " " + rs.getString("kind") + " " + rs.getLong("sum") + " " + rs.getDate("enddate"));
				check += 1;
			}
			if(check == 0) System.out.println("Empty.");
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	private static boolean showAccounts(String ssn, String mnum) {
		int check = 0;
		try {
			rs = stmt.executeQuery("Select mbank, anum, sum, enddate "
					+ "From account, admin "
					+ "Where ssn = \'" + ssn + "\' and account.mnum = \'" + mnum + "\' and "
							+ "admin.mnum = account.mnum");
			while(rs.next()){
				System.out.println(rs.getString("mbank") + " " + rs.getString("anum")
					+ " " + rs.getLong("sum") + " " + rs.getDate("enddate"));
				check += 1;
			}
			if(check == 0) {
				System.out.println("Empty.");
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	private static void transaction(String anum) { // anum �����Ѵٴ� ���� ��
		String output;
		try {
			rs = stmt.executeQuery("Select money.when, dir, amount, cname, cmnum, canum "
					+ "from money "
					+ "where anum = \'" + anum + "\'");
			System.out.println("( When / Dir / Amount / Name / Bank Info )");
			while(rs.next()){
				output = rs.getTimestamp("money.when") + " ";
				if(rs.getByte("dir") == 1) output += "IN ";
				else output += "OUT ";
				output += rs.getLong("amount") + " " + rs.getString("cname") + " ";
				if(rs.getString("cmnum") != null){
					ResultSet tmp = stmt.executeQuery("select mnum, mbank from admin");
					while(tmp.next()){
						if(rs.getString("cmnum").equals(tmp.getString("mnum")))
							output += tmp.getString("mbank") + rs.getString("canum").substring(0, 4);
					}
				}
				else output += ".";
				System.out.println(output);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	private static boolean findUser(String ssn) { // ssn ���� ��ȿ�ϴٰ� ����
		try {
			rs = stmt.executeQuery("Select ssn from user where ssn = \'" + ssn + "\'");
			if(rs.next()) return true;
			else {
				System.out.println("There is no such a user");
				return false;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	private static void makeUser() {
		String ssn, ph, job, name, email, address;
		long salary;
		System.out.println("Add a new user...");
		// ssn
		while(true){
			System.out.println("0. 13�ڸ��� �ֹε�Ϲ�ȣ�� (���ڸ�) �Է��ϼ���.");
			if(checkSsn((ssn = sc.nextLine()))) break;
		}
		
		// ph
		while(true) {
			System.out.println("1. ��ȭ��ȣ�� �Է��ϼ���.(���ڸ� �Է��Ͻÿ�.)");
			if(checkPh((ph = sc.nextLine()))) break;
		}
		
		// salary
		System.out.println("2. ���ҵ��� �Է��ϼ���.");
		salary = sc.nextLong();
		sc.nextLine();
		// job
		System.out.println("3. ������ �Է��ϼ���.");
		job = sc.nextLine();
		// name
		System.out.println("4. �̸��� �Է��ϼ���.");
		name = sc.nextLine();
		// email
		System.out.println("5. �̸����� �Է��ϼ���.");
		email = sc.nextLine();
		// address
		System.out.println("6. �ּҸ� �Է��ϼ���.");
		address = sc.nextLine();
		
		// insert
		try {
			stmt.executeUpdate("INSERT INTO user values (\'" + ssn + "\', \'" + ph + "\', " + salary + ", \'"
					+ job + "\', \'" + name + "\', \'" + email + "\', \'" + address + "\')");
		} catch (SQLException e) {
			System.out.println("DUPLICATE!! Check your Ssn and PhoneNumber again!");
			return;
		}
		
	}
	private static String makeAorCnum(int size){
		String result = "";
		Random r = new Random();
		for(int i = 0; i < size; i++)
			result += r.nextInt(10);
		return result;
	}
	private static long sumMoney(String anum, byte dir, Date start, Date end) throws SQLException{ // �����ϴ� ���� ��ȣ��� ����, date�� yyyy-MM-dd HH:mm:ss����
		// start���� end������
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ResultSet r = stmt.executeQuery("select sum(amount) from money where anum = \'" + anum + "\' and "
				+ "dir = " + dir + " and money.when >= \'" + df.format(start) + "\' and money.when < \'" + df.format(end) + "\'"); // start����, end������
		if(!r.next()) return 0;
		return r.getLong("sum(amount)");
	}
	@SuppressWarnings("deprecation")
	private static Date cloneDate(Date date){
		Date newDate = new Date();
		newDate.setYear(date.getYear());
		newDate.setMonth(date.getMonth());
		newDate.setDate(date.getDate());
		return newDate;
	}
	@SuppressWarnings("deprecation")
	private static Date nextMonth(Date date){ // date�� 28�� ���� ���� ���� ����
		Date newDate = new Date();
		if(date.getMonth() == 12) {
			newDate.setYear(date.getYear() + 1);
			newDate.setMonth(1);
		}
		else newDate.setMonth(date.getMonth() + 1);
		return newDate;
	}
	@SuppressWarnings("deprecation")
	private static int difMonth(Date d1, Date d2){
		int dif = 0, day1, day2;
		day1 = (d1.getYear() - 1) * 365 + (d1.getMonth() - 1) * 30 + d1.getDate();
		day2 = (d2.getYear() - 1) * 365 + (d2.getMonth() - 1) * 30 + d2.getDate();
		dif = day1 - day2;
		return dif/30;
	}
	@SuppressWarnings("deprecation")
	private static long calcInterest(ResultSet account) throws SQLException{ // �����ϴ� ���¸� ����Ű�� ������ ����, ���� ����
		long sum = 0, minusSum = 0;
		double intrst = account.getDouble("interestR");
		ResultSet r, s;
		
		Date start = account.getDate("startdate");
		Date end = account.getDate("enddate");
		Date tmpDate = new Date(), tmpNext = new Date(), today = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		tmpDate = cloneDate(start);
		tmpDate.setDate(1);
		
		if(account.getString("kind").equals("inst")){ // �� ����
			tmpNext = nextMonth(tmpDate);
			while(compareDate(tmpDate, end) < 0 && compareDate(tmpDate, today) < 0){
				r = stmt.executeQuery("select sum(amount) from money"
						+ " where dir = 1 and anum = '" + account.getString("anum") + "'"
						+ " and money.when >= '" + dateFormat.format(tmpDate) + "'"
						+ " and money.when < '" + dateFormat.format(tmpNext) + "'");
				s = stmt.executeQuery("select sum(amount) from money"
						+ " where dir = 0 and anum = '" + account.getString("anum") + "'"
						+ " and money.when >= '" + dateFormat.format(tmpDate) + "'"
						+ " and money.when < '" + dateFormat.format(tmpNext) + "'");
				if(!s.next()) minusSum = 0;
				else minusSum = s.getLong("sum(amount)");
				if(r.next())
					sum += (r.getLong("sum(amount)") - minusSum)* Math.pow(1+0.01*intrst,difMonth(today, tmpDate));
				tmpDate = nextMonth(tmpDate);
				tmpNext = nextMonth(tmpNext);
			}
			return sum;
		}
		else{ // �� ����
			tmpNext.setYear(tmpDate.getYear() + 1);
			while(compareDate(tmpDate, end) < 0 && compareDate(tmpDate, today) < 0){
				r = stmt.executeQuery("select sum(amount) from money"
						+ " where dir = 1 and anum = '" + account.getString("anum") + "'"
						+ " and money.when >= '" + dateFormat.format(tmpDate) + "'"
						+ " and money.when < '" + dateFormat.format(tmpNext) + "'");
				s = stmt.executeQuery("select sum(amount) from money"
						+ " where dir = 0 and anum = '" + account.getString("anum") + "'"
						+ " and money.when >= '" + dateFormat.format(tmpDate) + "'"
						+ " and money.when < '" + dateFormat.format(tmpNext) + "'");
				if(!s.next()) minusSum = 0;
				else minusSum = s.getLong("sum(amount)");
				if(r.next())
					sum += (r.getLong("sum(amount)") - minusSum) * Math.pow(1+0.01*intrst,difMonth(today, tmpDate) / 12);
				tmpDate = cloneDate(tmpNext);
				tmpNext.setYear(tmpNext.getYear() + 1);;
			}
			return sum;
		}
	}
	private static boolean transfer(ResultSet from, ResultSet to, long amount){ // �� ���°� �����ϰ� ��ȿ�� ���� ����
		String anum = "", canum = "", mnum = "", cmnum = "";
		int flag = 0;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String date = "\'" + dateFormat.format(new Date()) + "\'";
		
		try {
		anum = "\'" + from.getString("anum") + "\'";
		canum = "\'" + to.getString("anum") + "\'";
		mnum = "\'" + from.getString("mnum") + "\'";
		cmnum = "\'" + to.getString("mnum") + "\'";
		
		ResultSet tmp = stmt.executeQuery("select name from user where ssn = \'" + from.getString("ssn") +"\'");
		tmp.next();
		String name = "\'" + tmp.getString("name") + "\'";
		tmp = stmt.executeQuery("select name from user where ssn = \'" + to.getString("ssn") + "\'");
		tmp.next();
		String cname = "\'" + tmp.getString("name") + "\'";
		
		// money�� tuple �߰� from�� ���� ����
		stmt.executeUpdate("insert into money values("+date+", 0, "+amount+", " + anum + ", "
				+ canum + ", " + cname + ", " + cmnum + ")");
		flag = 1;
		// money�� to�� ���� tuple �߰�
		stmt.executeUpdate("insert into money values("+date+", 1, "+amount+", " + canum + ", " 
				+ anum + ", " + name + ", " + mnum + ")");
		flag = 2;
		// from account���� �ݾ� ����
		stmt.executeUpdate("update account set sum = sum - " + amount + " where anum = " + anum);
		flag = 3;
		// to account���� �ݾ� ���ϱ�
		stmt.executeUpdate("update account set sum = sum + " + amount + " where anum = " + canum);
		flag = 4;
		return true;
		} catch (SQLException e){
			e.printStackTrace();
			System.out.println("Error Code : " + flag);
			if(flag == 0) return false;
			
			System.out.println("Rollback...");
			try {
				stmt.executeUpdate("delete from money where money.when = " + date + " and anum = " + anum );
			System.out.println("1 Done...");
			if(flag == 1) return false;
			
			stmt.executeUpdate("delete from money where money.when = " + date + ", anum = " + canum);
			System.out.println("2 Done...");
			if(flag == 2) return false;
			
			stmt.executeUpdate("update account set sum = sum + " + amount + " where anum = " + anum);
			System.out.println("3 Done...");
			return false;
			} catch (SQLException e1) {
				e1.printStackTrace();
				return false;
			}
		}
	}
	@SuppressWarnings("deprecation")
	private static boolean checkTransfer(ResultSet from, ResultSet to, long amount){
		// ���ݰ��´� ��� ���� ������ �͸� �������� ���� ���ݰ����� ��� �����´� ���ݸ�
		Date end = new Date();
		Date start = new Date();
		Long limit, sum, save = null;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		int error = 0;
		try {
			// �޴� ���� �������� Ȯ��
			if(!checkEnddate(to) && !to.getString("kind").equals("loan")) return false;
			switch(from.getString("kind")){
			case "dpst" :
				// �ܾ� Ȯ��
				if(from.getLong("sum") < amount) {
					System.out.println("�ܾ��� �����մϴ�.");
					return false;
				}
				// ������ ���� �ѵ� Ȯ��
				start.setHours(0);
				start.setMinutes(0);
				start.setSeconds(0);
				if(from.getObject("account.limit") != null
						&& (limit = from.getLong("account.limit") - sumMoney(from.getString("anum"), (byte) 0, start,end)) < amount){
					
					System.out.print("!!�ѵ��ʰ�!! ���� " + from.getLong("account.limit") + "�� ��� �����մϴ�.");
					System.out.println(" " + limit + "�� ���ҽ��ϴ�.");
					return false;
				}
				switch(to.getString("kind")){
				case "inst" :
					// �޴� ���� �ѵ� Ȯ��
					start.setDate(1);
					start.setHours(0);
					start.setMinutes(0);
					start.setSeconds(0);
					if(to.getObject("account.limit") != null
						&& (limit = to.getLong("account.limit") - sumMoney(to.getString("anum"), (byte) 1, start,end)) < amount){
						System.out.println("!!�������ʰ�!! �ݿ� �ش� ���¿� ������ �� �ִ� �ݾ��� �ʰ��Ͽ����ϴ�.");
						return false;
					}
					break;
				case "loan" :
					// �޴� ���� �ѵ� Ȯ��
					start.setDate(1);
					start.setHours(0);
					start.setMinutes(0);
					start.setSeconds(0);
					if(to.getObject("account.limit") != null
						&& (-to.getLong("sum")) < amount){
						System.out.print("!!��ȯ���ʰ�!! �ش� ���¿� ��ȯ�� �� �ִ� �ݾ��� �ʰ��Ͽ����ϴ�.");
						return false;
					}
					break;
				default :
					break;
				}
				if(!transfer(from, to, amount)) return false;
				return true;
			case "inst" :
				if(!to.getString("kind").equals("dpst")){
					System.out.println("���ݰ��·θ� �۱� �����մϴ�. ��ü�� ����մϴ�.");
					return false;
				}
				// ���� �̿� ���� ��
				if(compareDate(from.getDate("enddate"), end) > 0){
					sum = calcInterest(from); // ���� ����ϱ�
					// ���� �� �ִ� �ݾ����� Ȯ��
					if(sum < amount){
						System.out.println("�ݾ��� �����մϴ�.");
						return false;
					}
					
					// �۱��� ������
					System.out.println("��ݽ� ���� �̿��� ����˴ϴ�. ����Ͻðڽ��ϱ�?( 0.��� / #.���� )");
					if(sc.nextInt() != 0) {
						System.out.println("������ü�� ����մϴ�.");
						break;
					}
					
					// ���� �̿� ����
					System.out.println("���� �̿��� �����մϴ�...");
					error = 1;
					start = from.getDate("enddate"); // rollback �����, ������¥ ���
					stmt.executeUpdate("update account set enddate = '" + dateFormat.format(end) 
						+ "' where anum = '" + from.getString("anum") + "'");
					error = 2;
					save = from.getLong("sum"); // rollback���
					stmt.executeUpdate("update account set sum = " + sum + " where anum = '" + from.getString("anum") + "'");
					error = 0;
					System.out.println("���� �ջ� �� ���¿� " + sum +"���� ���ҽ��ϴ�.\n���� ���� ���� �ܾ��� ���/��ü �� �������ּ���.");
				}
				// ���� �̿� ���� ��
				else if(from.getLong("sum") < amount){
					System.out.println("�ݾ��� �����մϴ�.");
					return false;
				}
				
				// �۱�
				System.out.println(amount+"���� �۱��մϴ�...");
				from = stmt.executeQuery("select * from account where anum = '" + from.getString("anum") + "'");
				from.next();
				if(!transfer(from, to, amount)) return false;
				return true;
				
			case "loan" :
				// ������ ���� �������� Ȯ��
				if(!checkEnddate(from)) return false;
				// �ܾ� Ȯ��
				if(from.getLong("account.limit") + from.getLong("sum") < amount) {
					System.out.println("!!�ѵ��ʰ�!! ���� ������ �ݾ��� �ʰ��Ͽ����ϴ�.");
					return false;
				}
				switch(to.getString("kind")){
				case "inst" :
					// �޴� ���� �ѵ� Ȯ��
					start.setDate(1);
					start.setHours(0);
					start.setMinutes(0);
					start.setSeconds(0);
					if(to.getObject("account.limit") != null
						&& (limit = to.getLong("account.limit") - sumMoney(to.getString("anum"), (byte) 1, start,end)) < amount){
						System.out.print("!!�������ʰ�!! �ݿ� �ش� ���¿� ������ �� �ִ� �ݾ��� �ʰ��Ͽ����ϴ�.");
						return false;
					}
					break;
				case "loan" :
					// �޴� ���� �ѵ� Ȯ��
					start.setDate(1);
					start.setHours(0);
					start.setMinutes(0);
					start.setSeconds(0);
					if(to.getObject("account.limit") != null
						&& (-to.getLong("sum")) < amount){
						System.out.print("!!��ȯ���ʰ�!! �ش� ���¿� ��ȯ�� �� �ִ� �ݾ��� �ʰ��Ͽ����ϴ�.");
						return false;
					}
					break;
				default :
					break;
				}
				if(!transfer(from, to, amount)) return false;
				// ���Ⱑ���� �ѵ� ���߱�
				stmt.executeUpdate("update account set account.limit = account.limit - " + amount 
						+ " where anum = '" + from.getString("anum") + "'");
				stmt.executeUpdate("update card set climit = climit - " + amount 
						+ " where anum = '" + from.getString("anum") + "'");
				return true;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			try {
			if(error == 1 || error == 2)
				stmt.executeUpdate("update account set enddate = '" + dateFormat.format(start) 
					+ "' where anum = '" + from.getString("anum") + "'");
			if(error == 2)
				stmt.executeUpdate("update account set sum = " + save
					+ " where anum = '" + from.getString("anum") + "'");
			} catch (SQLException e1) {
				e1.printStackTrace();
			}
		}
		return true;
	}
	
	private static ResultSet selectAccount(String anum) { // �����ϴ� ù ��° row�� ��ȯ
		ResultSet nr = null;
		try {
			nr = stmt.executeQuery("Select * from account where anum = \'" + anum + "\'");
			if(!nr.next()){
				System.out.println("�ش� ���´� �������� �ʽ��ϴ�.");
				return null;
			}
			return nr;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	// date�ϰ� anum�� �� �־����
	private static boolean checkEnddate(ResultSet r) { // �ش� ���� ����Ű�� �ִ� ��, ����� false
		Date date = new Date();
		try {
			if(compareDate(r.getDate("enddate"),date) <= 0){
				if(!r.getString("kind").equals("loan"))
					System.out.println(r.getString("anum") + "�� ����� �����Դϴ�.");
				return false;
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	@SuppressWarnings("deprecation")
	private static void makeAccount(String ssn) {
		String kind = "dpst", mnum = "", mbank = "";
		double interest = 0;
		Long limit = null;
		int year, check = 0, cmd;
		boolean flag = false;
		System.out.println("Add a new account...");
		// ���ฮ��Ʈ �����ְ� ����� �Է¹ޱ�
		System.out.println("Choose a bank.");
		try {
			rs = stmt.executeQuery("Select mbank, mnum from admin");
		while(rs.next())
			System.out.println(check++ + ". " + rs.getString("mbank"));
		while(!flag){			
			System.out.println("��Ȯ�� \'�����\'�� �Է��ϼ���.");
			mbank = sc.nextLine();
			rs.beforeFirst();
			while(rs.next()) {
				if((rs.getString("mbank")).equals(mbank)) {
					mnum = rs.getString("mnum");
					flag = true;
					break;
				}
			}
		}
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		
		flag = false;
		// ���� ����
		while(!flag) {
			System.out.println("� ���¸� ����ðڽ��ϱ�? ( 0.���� / 1.���� / 2.���� / #.��� )");
			cmd = sc.nextInt();
			sc.nextLine();
			switch(cmd){
			case 0 :
				kind = "dpst";
				while(true){
					System.out.println("���� ���� ������ �������� �����ϼ���.(0.10 ~ 2.00)%");
					interest = sc.nextDouble();
					sc.nextLine();
					if(interest >= 0.1 && interest <= 2.00) break;
				}
				do {
				System.out.println("1�� ��� ������ �ѵ��� �����ϼ���.(�ּ� 1���� �̻� / '0' �Է½� �ѵ� ����)");
				limit = sc.nextLong();
				sc.nextLine();
				} while(limit != 0 && limit < 10000);
				if(limit == 0) limit = null;
				flag = true;
				break;
			case 1 :
				kind = "inst";
				while(true){
					System.out.println("���� ���� ������ �������� �����ϼ���.(2.00 ~ 2.87)%");
					interest = sc.nextDouble();
					sc.nextLine();
					if(interest >= 2 && interest <= 2.87) break;
				}
				while(true){
					System.out.println("���� ���� ������ �ſ� ���� ������ �ѵ��� �����ϼ���.( 10000 ~ 20000000 )��");
					limit = sc.nextLong();
					sc.nextLine();
					if(limit >= 10000 && limit <= 20000000) break;
				}
				flag = true;
				break;
			case 2 :
				kind = "loan";
				try {
					ResultSet result = stmt.executeQuery("select count(*) from credit "
							+ "where ssn = '" + ssn + "' and credit > 5");
					result.next();
					if(result.getInt("count(*)") > 0){
						System.out.println("Ÿ�࿡�� ��ü����� �ֽ��ϴ�. ������ �Ұ��մϴ�.");
						return;
					}
					if(calcCredit(ssn,mbank) > 5) {
						System.out.println("�ſ��� ���� ������ �Ұ��մϴ�.");
						return;
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				while(true){
					System.out.println("���� ���� ������ �������� �����ϼ���.( 3.5 ~ 4.2 )%");
					interest = sc.nextDouble();
					sc.nextLine();
					if(interest >= 3.5 && interest <= 4.2) break;
				}
				while(true){
					System.out.println("���� ���� ������ ���� �ѵ��� �����ϼ���. ( 1000000 ~ 500000000 )��");
					limit = sc.nextLong();
					sc.nextLine();
					if(limit <= 500000000 && limit >= 1000000) break;
				}
				flag = true;
				break;
			default :
				System.out.println("���� ������ ����մϴ�.");
				return;
			}
		}
		
		// ���� �Ⱓ
		while(true){
			System.out.println("������ �Ⱓ�� �Է��ϼ���.( 1 ~ 5 )��");
			year = sc.nextInt();
			sc.nextLine();
			if(year > 0 && year < 6) break;
		}
		// insert
		
		// ���� ��¥
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date startdate = new Date(), enddate = new Date();
		enddate.setYear(startdate.getYear() + year);
		
		String test = makeAorCnum(14);
		try {
			stmt.executeUpdate("INSERT INTO account values (\'" + test + "\', 0, " 
					+ interest + ", \'" + kind + "\', " + limit + ", \'" + dateFormat.format(startdate) + "\', \'"
					+ dateFormat.format(enddate) + "\', \'" + mnum + "\', " + "\'" + ssn + "\')");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("DUPLICATE!! Check your anum again!");
			System.out.println(test);
			return;
		}
		System.out.println("Done!");
	}
	@SuppressWarnings("deprecation")
	private static void deleteAccount(String ssn, String anum) { // ssn�� �����ϴ� ssn�̶� ����, anum�� �ùٸ� ���¹�ȣ �����̶�� ����
		try {
			long sum;
			int cmd;
			boolean del = false;
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date end = new Date();
			if((rs = selectAccount(anum)) == null){
				return;
			}
			else if(!rs.getString("ssn").equals(ssn)){
				System.out.println("�ش� ���´� �������� �ʽ��ϴ�.");
				return;
			}
			sum = rs.getLong("sum");
			
			System.out.println("���� �ܾ��� " + sum + "�� �Դϴ�.");
			if(compareDate(rs.getDate("enddate"), end) > 0){ // ���� �� �̿� ����
				System.out.println("���°� ����Ǳ� ������ �̿��� ���� �����ϰ� ������ �����մϴ�.");
				System.out.println("�̿��� �����ϸ� ���ú��� ������ ���´� '�Ա� �Ұ�' '��� ����'�ϸ�, ���� ���´� '��� �Ұ�' '�Ա� ����'�մϴ�."
						+ "\n���ڴ� �̿����� ��¥�� �������� ���˴ϴ�.");
				System.out.println("���¿� ����� ī��� ��� �����˴ϴ�.");
				System.out.println("���� �̿��� �����ϰڽ��ϱ�? ( 0.�ƴϿ� / #.�� )");
				cmd = sc.nextInt();
				sc.nextLine();
				if(cmd == 0) return;
				// �̿� ���� ����
				stmt.executeUpdate("update account set enddate = '" + dateFormat.format(end)
						+"' where anum = '" + anum + "'");
				sum = calcInterest(rs); // ���� ����ϱ�
				stmt.executeUpdate("update account set sum = " + sum 
						+ " where anum = '" + anum + "'");
				System.out.println("���� �̿��� �����Ͽ����ϴ�.\n���� �ջ� �� ���¿� " + sum +"���� ���ҽ��ϴ�. ");
				// ī�� ����
				stmt.executeQuery("delete from card where anum = '" + anum + "'");
				// �ѵ� �ٲ��ֱ�
				Date start = new Date();
				start.setHours(0);
				start.setMinutes(0);
				start.setSeconds(0);
				if(rs.getString("kind").equals("dpst"))
					stmt.executeUpdate("update account set account.limit = " + Long.MAX_VALUE
							+ " where anum = '" + anum + "'");
				if(rs.getString("kind").equals("loan") && sum < 0){
					System.out.println("���� ���� ���� �ܾ��� ��ȯ �� �������ּ���.");
					return;
				}
				else if(rs.getString("kind").equals("loan")) del = true;
				else if(!rs.getString("kind").equals("loan") && sum > 0) {
					System.out.println("���� ���� ���� �ܾ��� ���/��ü �� �������ּ���.");
					return;
				}
				else del = true;
			}				
			else del = true;
			if(del) {
				switch(rs.getString("kind")){
				case "loan" :
					if(sum < 0)	{				
						System.out.println((-sum) + "�� ��ȯ�� �������ּ���.\n���� ������ ����մϴ�.");
						return;
					}
					break;
				default :
					if(sum > 0){
						System.out.println("�ܾ��� ���� �ٸ� ���·� ��ü���ּ���.\n���� ������ ����մϴ�.");
						return;
					}
					break;
				}
				// ����
				System.out.println("���� ������ �����մϴ�. �ش� ���� ������ ��� �����˴ϴ�.");
				stmt.executeQuery("delete from money where anum = '" + anum + "'");
				stmt.executeQuery("delete from account where anum = '" + anum + "'");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	private static boolean checkCnum (String cnum) {
		if(cnum.length() != 16){
			System.out.println("�߸��� ī���ȣ �����Դϴ�!(16�ڸ�, ���ڸ�)");
			return false;
		}
		char c;
		for(int i = 0; i < 16; i++){
			c = cnum.charAt(i);
			if(!Character.isDigit(c)){
				System.out.println("�߸��� ���¹�ȣ �����Դϴ�!(16�ڸ�, ���ڸ�)");
				return false;
			}
		}
		return true;
	}
	private static boolean checkAnum (String anum) {
		if(anum.length() != 14){
			System.out.println("�߸��� ���¹�ȣ �����Դϴ�!(14�ڸ�, ���ڸ�)");
			return false;
		}
		char c;
		for(int i = 0; i < 14; i++){
			c = anum.charAt(i);
			if(!Character.isDigit(c)){
				System.out.println("�߸��� ���¹�ȣ �����Դϴ�!(14�ڸ�, ���ڸ�)");
				return false;
			}
		}
		return true;
	}
	private static boolean checkPh(String ph) {
		if(ph.length() != 11 && ph.length() != 10){
			System.out.println("�߸��� ��ȭ��ȣ �����Դϴ�!(10 or 11�ڸ�, ���ڸ�)");
			return false;
		}
		char c;
		for(int i = 0; i < ph.length(); i++){
			c = ph.charAt(i);
			if(!Character.isDigit(c)){
				System.out.println("�߸��� ��ȭ��ȣ �����Դϴ�!(10 or 11�ڸ�, ���ڸ�)");
				return false;
			}
		}
		return true;
	}
	private static boolean checkSsn(String ssn) {
		if(ssn.length() != 13){
			System.out.println("�߸��� �ֹε�Ϲ�ȣ �����Դϴ�!(13�ڸ�, ���ڸ�)");
			return false;
		}
		char c;
		for(int i = 0; i < 13; i++){
			c = ssn.charAt(i);
			if(!Character.isDigit(c)){
				System.out.println("�߸��� �ֹε�Ϲ�ȣ �����Դϴ�!(13�ڸ�, ���ڸ�)");
				return false;
			}
		}
		return true;
	}
	@SuppressWarnings("deprecation")
	private static int compareDate(Date a, Date b) {
		if(a.getYear() < b.getYear()) return -1;
		else if(a.getYear() > b.getYear()) return 1;
		else {
			if(a.getMonth()<b.getMonth()) return -1;
			else if(a.getMonth() > b.getMonth()) return 1;
			else {
				if(a.getDate() < b.getDate()) return -1;
				else if(a.getDate() > b.getDate()) return 1;
				else return 0;
			}
		}
	}
	private static void startAdmin() throws SQLException{
		String adminID;
		int check = 0; // Empty���� Ȯ��
		int credit;
		// Admin ����Ʈ �����ֱ� : (count(*), �����̸�, admin �̸�) ������ ��ȣ ������ �����ֱ�
		rs = stmt.executeQuery("SELECT Mnum, Mbank, Mname FROM Admin");
		System.out.println("AdminID / Bank Name / Admin Name");
		while(rs.next()) {
			String Mnum = rs.getString("Mnum");
			String Mbank = rs.getString("Mbank");
			String Mname = rs.getString("Mname");
			System.out.println(Mnum + "  " + Mbank + "  " + Mname);
			check += 1;
		}
		
		// admin �ƹ��� ������ �����Ұ��� ����� ����ų�/�ȸ���ų� �� menu�� back
		if(check == 0){
			System.out.println("!It's empty! Establish a new bank( 0.yes / #(other num).no )");
			if(sc.nextInt() == 0){
				// ���ο� admin �߰��ϱ�
				do{
				System.out.println("6���� AdminID�� �Է��ϼ���.(a-z,A-Z,0-9)");
				adminID = sc.next();
				sc.nextLine();
				} while(adminID.length() != 6);
				System.out.println("���� �̸��� �Է��ϼ���.");
				String bankName = sc.next();
				sc.nextLine();
				System.out.println("������ �̸��� �Է��ϼ���.");
				String mname = sc.nextLine();
				stmt.executeUpdate("INSERT INTO admin values (\'" + adminID + "\', \'" + bankName + 
						"\', \'" + mname + "\')");
			}
			return;
		}
		
		// back? manage?
		System.out.println("Go back to menu? or Start managing?( 0.menu / #(other num).manage )");
		if(sc.nextInt() == 0) return;
		
		// choose specific admin and list users
		check = 0;
		while(true) {
			System.out.println("Type the AdminID.");
			adminID = sc.next();
			
			// find the admin
			rs = stmt.executeQuery("SELECT mnum FROM admin WHERE mnum = \'" + adminID + "\'");
			if(!rs.next()){
				System.out.println("There is no such a admin. Check your AdminId again."
						+ "\n( 0.back to menu / #(other num).retry )");
				if(sc.nextInt() == 0) return;
				continue;
			}
			
			// retreive users
			rs = stmt.executeQuery("SELECT DISTINCT User.ssn, User.name "
					+ "FROM Account, User "
					+ "WHERE Account.Mnum = \'" + adminID + "\' and Account.ssn = User.ssn");
			check += 1;
			
			// manage�� �ش��ϴ°� ���� ���
			if(rs.next()) break;
			else {
				System.out.println("There are no any users."
						+ "\n( 0.back to menu / #(other num).retry )");
				if(sc.nextInt() == 0) return;
			}
		}
		
		// user list
		System.out.println("User ssn / User name");
		do {
			System.out.println(rs.getString("User.ssn") + " " + rs.getString("User.name"));
		} while(rs.next());
		
		// choose menu
		int cmd = 0;
		while(cmd == 0 || cmd == 1){
			System.out.println("Choose menu!"
					+ "\n0. look at specific user"
					+ "\n1. show list of users being overdue"
					+ "\n#(other num). back to menu");
			cmd = sc.nextInt();
			sc.nextLine();
			String userSsn;
			switch(cmd){
			case 0 :
				System.out.println("Please type the user's ssn");
				userSsn = sc.nextLine();

				// retreive user & find & show user info
				rs = stmt.executeQuery("SELECT DISTINCT User.*, mbank FROM User, Account, admin"
						+ " WHERE Account.ssn = \'"+userSsn+"\' and Account.mnum = \'" + adminID 
						+ "\' and Account.ssn = User.ssn and account.mnum = admin.mnum");
				if(rs.next())
					System.out.println("Ssn : " + rs.getString("User.ssn") + 
							"\nPhoneNum : " + rs.getString("PhoneNum") +
							"\nSalary : " + rs.getInt("Salary") +
							"\nJob : " + rs.getString("Job") +
							"\nName : " + rs.getString("Name") +
							"\nEmail : " + rs.getString("Email"));
				else {
					System.out.println("No such a user in the list.");
					break;
				}
				// credit
				credit = calcCredit(rs.getString("user.ssn"), rs.getString("mbank"));
				if(credit > 0)
					System.out.println("Credit : " + credit);
				else System.out.println("Credit : Don't Know");
				
				// account list
				if(!showAccounts(userSsn, adminID)){
					System.out.println("No account");
					break;
				}
				else{
					String anum;
					while(true){
						System.out.println("Type the ACCOUNT NUMBER, if you want to see info of an account."
								+"\nType \"BACK\", if you want to go back.");
						anum = sc.nextLine();
						if(anum.equalsIgnoreCase("Back")) break;
						if(!checkAnum(anum)) continue;
						showAccount(userSsn,anum);
					}
				}
				break;
				
			case 1 :
				rs = stmt.executeQuery("Select user.ssn, name from credit, user"
					+ " where credit.mnum = \'" + adminID + "\' and credit.ssn = user.ssn "
					+ "and overdue = true");
				System.out.println("User_Ssn      Name");
				while(rs.next())
					System.out.println(rs.getString("user.ssn") + " " + rs.getString("name"));
				
				break;
			default :
				return;
			}
		}	
	}

}
