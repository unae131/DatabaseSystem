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
			if(findUser(ssn)){ // 존재하면
				menuUser(ssn);
			}
			
			break;
		default :
			makeUser();
			break;
		}
	}
	private static void menuUser(String ssn){ // 한 사용자가 메뉴를 고름
		int cmd;
		long amount;
		String anum, canum, cnum;
		ResultSet crs;
		try {
		while(true){
		System.out.println("메뉴를 고르세요."
				+ "\n0. 보유한 계좌 목록 보기"
				+ "\n1. 특정 계좌 상세 보기"
				+ "\n2. 계좌 개설/해지하기"
				+ "\n3. 계좌 이체하기"
				+ "\n4. 출금/입금하기"
				+ "\n5. 보유한 카드 목록 보기"
				+ "\n6. 카드 발급/해지"
				+ "\n7. 카드 사용하기"
				+ "\n8. 신용등급 알아보기"
				+ "\n9. 개인정보 수정하기"
				+ "\n#. 모드메뉴로 돌아가기");
			cmd = sc.nextInt();
			sc.nextLine();
			switch(cmd) {
			case 0 :
				showAccounts(ssn);
				break;
			case 1 : // 특정 계좌 상세 보기
				do {
					System.out.println("14자리 계좌번호를 '-'없이 숫자만 입력하세요.");
					anum = sc.nextLine();
				} while(!checkAnum(anum));
				showAccount(ssn, anum);
				break;
			case 2 : // 개설/해지
				System.out.println("메뉴를 고르세요.\n( 0.계좌개설 / 1.계좌해지 / #.취소 )");
				cmd = sc.nextInt();
				sc.nextLine();
				switch(cmd) {
				case 0 :
					makeAccount(ssn);
					break;
				case 1 :
					do {
						System.out.println("삭제할 계좌의 14자리 계좌번호를 '-'없이 숫자만 입력하세요.");
						anum = sc.nextLine();
					} while(!checkAnum(anum));
					deleteAccount(ssn, anum);
					break;
				default :
					break;
				}
				break;
			case 3: // 계좌이체하기
				do {
					System.out.println("출금할 계좌의 14자리 계좌번호를 '-'없이 숫자만 입력하세요.");
					anum = sc.nextLine();
				} while(!checkAnum(anum));
				rs = stmt.executeQuery("select * from account where ssn = '" + ssn + "'"
						+ " and anum = '" + anum + "'");
				if(!rs.next()){
					System.out.println("해당 계좌가 존재하지 않습니다.");
					break;
				}
				do {
					System.out.println("입금할 계좌의 14자리 계좌번호를 '-'없이 숫자만 입력하세요.");
					canum = sc.nextLine();
				} while(!checkAnum(canum));
				if((crs = selectAccount(canum)) == null ) break;
				System.out.println("금액을 입력하세요.");
				checkTransfer(rs, crs, sc.nextLong());
				sc.nextLine();
				break;
			case 4: // 입금/출금하기
				System.out.println("( 0.출금, #.입금 )");
				cmd = sc.nextInt();
				sc.nextLine();
				
				if(cmd == 0) do {
					System.out.println("출금할 계좌의 14자리 계좌번호를 '-'없이 숫자만 입력하세요.");
					anum = sc.nextLine();
				} while(!checkAnum(anum));
				else do {
					System.out.println("입금할 계좌의 14자리 계좌번호를 '-'없이 숫자만 입력하세요.");
					anum = sc.nextLine();
				} while(!checkAnum(anum));
				
				rs = stmt.executeQuery("select * from account where ssn = '" + ssn + "'"
						+ " and anum = '" + anum + "'");
				if(!rs.next()){
					System.out.println("해당 계좌가 존재하지 않습니다.");
					break;
				}
				
				System.out.println("금액을 입력하세요.");
				amount = sc.nextLong();
				sc.nextLine();
				if(cmd == 0) withdraw(rs, amount, "ATM");
				else putMoney(rs, amount);
				break;
			case 5: // 보유한 카드 목록 보여주기
				int check = 0;
				rs = stmt.executeQuery("select cnum, mbank, card.anum "
						+ "from account, card, admin "
						+ "where ssn = '" + ssn + "' and card.anum = account.anum "
								+ "and account.mnum = admin.mnum");
				System.out.println("카드번호 / 은행 / 연결된 계좌번호");
				while(rs.next()){
					System.out.println(rs.getString("cnum") + " " + rs.getString("mbank") 
						+ " " + rs.getString("anum"));
					check += 1;
				}
				if(check == 0)
					System.out.println("보유하고 계신 카드가 없습니다.");
				break;
			case 6: // 카드 발급/해지
				Long limit, tmp;
				System.out.println("( 0.카드발급 / #.카드해지 )");
				cmd = sc.nextInt();
				sc.nextLine();
				if(cmd == 0){
					do {
						System.out.println("카드와 연결할 계좌의 14자리 계좌번호를 '-'없이 숫자만 입력하세요.");
						anum = sc.nextLine();
					} while(!checkAnum(anum));
					
					rs = stmt.executeQuery("select * from account where ssn = '" + ssn + "'"
							+ " and anum = '" + anum + "'");
					
					if(!rs.next()){
						System.out.println("해당 계좌가 존재하지 않습니다.");
						break;
					}
					
					Date today = new Date();
					if(rs.getString("kind").equals("inst")){
						System.out.println("적금계좌는 카드를 발급받을 수 없습니다.");
						break;
					}
					else if(compareDate(rs.getDate("enddate"), today) <= 0){
						System.out.println("만기된 계좌는 카드를 발급받을 수 없습니다.");
						break;
					}
	
					limit = rs.getLong("account.limit");
					if(rs.getString("kind").equals("dpst") && rs.getObject("account.limit") != null) {
						do {
							System.out.println("한도를 정하세요. (10000 <= " + limit + ")");
							tmp = sc.nextLong();
							sc.nextLine();
						} while (tmp > limit && tmp < 10000);
						limit = tmp;
					}
					else if(rs.getString("kind").equals("dpst")){
						System.out.println("한도를 정하세요. ('0'입력시 한도 없음.)");
						tmp = sc.nextLong();
						sc.nextLine();
						if(tmp == 0) limit = null;
					}
					
					stmt.executeUpdate("insert into card values('" + (cnum = makeAorCnum(16)) + "',"
							+ limit + ",'" + rs.getString("anum") + "')");
					System.out.println("카드가 발급되었습니다. 카드번호는 " + cnum + "입니다.");
					break;
				}
				else {
					int flag = 0;
					rs = stmt.executeQuery("select cnum from account, card, user "
							+ "where user.ssn = '" + ssn + "' and "
							+ "user.ssn = account.ssn and account.anum = card.anum");
					if(!rs.next()){
						System.out.println("보유하고 계신 카드가 없습니다.");
						break;
					}
					do {
						System.out.println("해지할 카드번호 16자리를 '-'없이 숫자만 입력하세요.");
						cnum = sc.nextLine();
					} while(!checkCnum(cnum));
					do{
						if(rs.getString("cnum").equals(cnum)){
							stmt.executeUpdate("delete from card where cnum = '" + cnum + "'");							
							System.out.println("카드를 성공적으로 해지하였습니다.");
							flag = 1;
							break;
						}
					} while(flag == 0 && rs.next());
					if(flag == 0) System.out.println("보유하고 계신 카드 중 해당 카드가 존재하지 않습니다.");
					break;
				}
			case 7 : // 카드 사용하기
				rs = stmt.executeQuery("select cnum, climit, account.* from account, card, user "
						+ "where user.ssn = '" + ssn + "' and "
						+ "user.ssn = account.ssn and account.anum = card.anum");
				if(!rs.next()){
					System.out.println("보유하고 계신 카드가 없습니다.");
					break;
				}
				do {
					System.out.println("사용할 카드번호 16자리를 '-'없이 숫자만 입력하세요.");
					cnum = sc.nextLine();
				} while(!checkCnum(cnum));
				int flag = 0;
				do{
					if(rs.getString("cnum").equals(cnum)){
						System.out.println("결제할 금액을 입력하세요.");
						amount = sc.nextLong();
						sc.nextLine();
						withdraw(rs, amount, "카드" + rs.getString("cnum").substring(0,4));
						flag = 1;
						break;
					}
				} while(flag == 0 && rs.next());
				if(flag == 0) System.out.println("보유하고 계신 카드 중 해당 카드가 존재하지 않습니다.");
				break;
			case 8 :
				System.out.println("현재 이용 중인 은행 :");
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
					System.out.println("이용 중인 은행이 없어 신용평가를 할 수 없습니다.");
					break;
				}
				System.out.println("신용을 확인할 은행 이름을 '정확히' 입력하세요.");
				System.out.println(calcCredit(ssn, sc.nextLine()));
				break;
			case 9 : // 개인정보 수정하기
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
	private static int calcCredit(String ssn, String mbank) throws SQLException{ // ssn은 존재 보장, mbank는 보장x
		int rate = 5;
		boolean overdue = false;
		Date today = new Date();
		rs = stmt.executeQuery("select salary, account.* "
				+ "from user, account, admin "
				+ "where user.ssn = account.ssn and account.mnum = admin.mnum "
				+ "and user.ssn = '" + ssn + "' and mbank = '" + mbank +"'");
		if(!rs.next()){
			System.out.println("사용 중인 은행 중에 " + mbank + "은행이 존재하지 않아 신용정보를 알 수 없습니다.");
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
		
		// credit table 수정하기
		rs = stmt.executeQuery("select credit.* "
				+ "from credit, admin "
				+ "where ssn = '" + ssn + "' and credit.mnum = admin.mnum "
						+ "and admin.mbank = '" + mbank + "'");
		if(rs.next()) { // 이미 존재
			stmt.executeUpdate("update credit "
					+ "set credit = " + rate + ", overdue = " + overdue 
					+ " where mnum = '" + rs.getString("credit.mnum") + "' "
					+ "and ssn = '" + ssn + "'");
		}
		else { // 존재하지 않을 경우
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
				System.out.println("수정할 정보를 선택하세요.");
				System.out.println("( 0.전화번호 / 1.연소득 / 2.직업 / 3.이름 / 4.이메일 / 5.거주지 / #.수정취소 )");
				cmd = sc.nextInt();
				sc.nextLine();
				switch(cmd){
				case 0 :
					do{
						System.out.println("바뀐 전화번호를 입력하세요. 취소를 원하시면 'q'를 입력해주세요.");
						input = sc.nextLine();
						if(input.equals("q")) return;
					} while(!checkPh(input));
					stmt.executeUpdate("update user set phonenum = '" + input + "' "
							+ "where ssn = '" + ssn + "'");
					break;
				case 1 :
					System.out.println("바뀐 연소득을 입력하세요. 취소를 원하시면 -1을 입력해주세요.");
					salary = sc.nextLong();
					sc.nextLine();
					if(salary < 0) return;
					if(salary <= 0){
						System.out.println("양수의 수를 입력해주세요.");
						break;
					}
					stmt.executeUpdate("update user set salary = " + salary + " "
							+ "where ssn = '" + ssn + "'");
					break;
				case 2 :
					System.out.println("바뀐 직업을 입력하세요. 취소를 원하시면 'q'를 입력해주세요.");
					input = sc.nextLine();
					if(input.equals("q")) return;
					stmt.executeUpdate("update user set job = '" + input + "' "
							+ "where ssn = '" + ssn + "'");
					break;
				case 3 :
					System.out.println("바뀐 이름을 입력하세요. 취소를 원하시면 'q'를 입력해주세요.");
					input = sc.nextLine();
					if(input.equals("q")) return;
					stmt.executeUpdate("update user set name = '" + input + "' "
							+ "where ssn = '" + ssn + "'");
					break;
				case 4 :
					System.out.println("바뀐 이메일 주소를 입력하세요. 취소를 원하시면 'q'를 입력해주세요.");
					input = sc.nextLine();
					if(input.equals("q")) return;
					stmt.executeUpdate("update user set email = '" + input + "' "
							+ "where ssn = '" + ssn + "'");
					break;
				case 5 :
					System.out.println("바뀐 거주지를 입력하세요. 취소를 원하시면 'q'를 입력해주세요.");
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
			System.out.println("중복되는  전화번호입니다. 다시 확인해보세요.");
			return;
		} catch (SQLException e) {
			e.printStackTrace();
			return;
		}
	}
	@SuppressWarnings("deprecation")
	private static void putMoney(ResultSet account, long amount){ // 입금만
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date end = new Date();
		Date start = new Date();
		String date = "", anum = "";
		int error = 0;
		try {
			switch(account.getString("kind")){
			case "inst" :
				// 받는 계좌 한도 확인
				start.setDate(1);
				start.setHours(0);
				start.setMinutes(0);
				start.setSeconds(0);
				if(account.getObject("account.limit") != null
					&& account.getLong("account.limit") - sumMoney(account.getString("anum"), (byte) 1, start,end) < amount){
					System.out.print("!!적립금초과!! 금월 해당 계좌에 적립할 수 있는 금액을 초과하였습니다.");
					return;
				}
				break;
			case "loan" :
				// 받는 계좌 한도 확인
				start.setDate(1);
				start.setHours(0);
				start.setMinutes(0);
				start.setSeconds(0);
				if(account.getObject("account.limit") != null
					&& (-account.getLong("sum")) < amount){
					System.out.print("!!상환금초과!! 해당 계좌에 상환할 수 있는 금액을 초과하였습니다.");
					return;
				}
				break;
			default :
				break;
			}
			// 입금하기
			dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			date = "'" + dateFormat.format(new Date()) + "'";
			anum = "\'" + account.getString("anum") + "\'";
			// money에 입금 tuple 추가
			stmt.executeUpdate("insert into money(money.when, dir, amount, anum, cname)"
					+ " values("+date+", 1, "+amount+", "+anum +", 'ATM')");
			error = 1;
			// from account에서 금액 더하기
			stmt.executeUpdate("update account set sum = sum + " + amount + " where anum = " + anum);
			error = 0;
			System.out.println("현금 " + amount + "원을 입금하였습니다.");
			
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
	private static void withdraw(ResultSet account, long amount, String cname){ // 출금만
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date end = new Date();
		Date start = new Date();
		String date = "", anum = "", fin;
		Long sum, save = null, limit;
		int error = 0;
		try {
			switch(account.getString("kind")){
			case "dpst" :
				// 잔액 확인
				if(account.getLong("sum") < amount) {
					System.out.println("잔액이 부족합니다.");
					return;
				}
				// 한도 확인
				start.setHours(0);
				start.setMinutes(0);
				start.setSeconds(0);
				if(account.getObject("account.limit") != null
						&& (limit = account.getLong("account.limit") - sumMoney(account.getString("anum"), (byte) 0, start,end)) < amount){
					System.out.print("!!한도초과!! 일일 " + account.getLong("account.limit") + "원 출금 가능합니다.");
					System.out.println(" " + limit + "원 남았습니다.");
					return;
				}
				break;
			case "inst" :
				// 적금 이용 종료 전
				if(!cname.equals("ATM")){
					System.out.println("적금 계좌는 카드를 발급받을 수 없습니다.");
					return;
				}
				if(compareDate(account.getDate("enddate"), end) > 0){
					sum = calcInterest(account); // 이자 계산하기
					// 보낼 수 있는 금액인지 확인
					if(sum < amount){
						System.out.println("금액이 부족합니다.");
						return;
					}
					
					// 송금할 것인지
					System.out.println("출금시 적금 이용이 종료됩니다."
							+ "\n적금 이용 종료시 이자가 합산되고, 오늘부터 출금만 이용 가능합니다.\n 계속하시겠습니까?( 0.계속 / #.종료 )");
					if(sc.nextInt() != 0) {
						System.out.println("출금을 취소합니다.");
						break;
					}
					
					// 적금 이용 종료
					System.out.println("적금 이용을 종료합니다...");
					error = 1;
					start = account.getDate("enddate"); // rollback 대비해, 해지날짜 등록
					stmt.executeUpdate("update account set enddate = '" + dateFormat.format(end) 
						+ "' where anum = '" + account.getString("anum") + "'");
					error = 2;
					save = account.getLong("sum"); // rollback대비
					stmt.executeUpdate("update account set sum = " + sum + " where anum = '" + account.getString("anum") + "'");
					error = 0;
					System.out.println("이자 합산 후 계좌에 " + sum +"원이 남았습니다.\n빠른 시일 내에 잔액을 출금/이체 후 해지해주세요.");
				}
				// 적금 이용 종료 후
				else if(account.getLong("sum") < amount){
					System.out.println("금액이 부족합니다.");
					return;
				}
				break;
		
			case "loan" :
				// 보내는 계좌 만기인지 확인
				if(!checkEnddate(account)) return;
				// 한도 확인
				if(account.getLong("account.limit") + account.getLong("sum") < amount) {
					System.out.println("!!한도초과!! 대출 가능한 금액을 초과하였습니다.");
					return;
				}
				// 대출가능한 한도 낮추기
				stmt.executeUpdate("update account set account.limit = account.limit - " + amount 
						+ " where anum = '" + account.getString("anum") + "'");
				stmt.executeUpdate("update card set climit = climit - " + amount
						+ " where anum = '" + account.getString("anum") + "'");
				error = 3;
				break;
			}
			// 출금하기
			dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			date = "'" + dateFormat.format(new Date()) + "'";
			anum = "\'" + account.getString("anum") + "\'";
			
			if(cname.equals("ATM")) fin = "현금";
			else fin = "카드에서";
			
			// money에 tuple 추가 from의 계좌 먼저
			stmt.executeUpdate("insert into money(money.when, dir, amount, anum, cname)"
					+ " values("+date+", 0, "+amount+", "+anum +", '" + cname + "')");
			error = 4;
			// from account에서 금액 뺴기
			stmt.executeUpdate("update account set sum = sum - " + amount + " where anum = " + anum);
			error = 0;
			System.out.println(fin + amount + "원을 출금하였습니다.");
			
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
			if(rs.next()){ // 반드시 하나만 있어야함.
				System.out.println("은행 : "+ rs.getString("mbank") + 
						"\n계좌번호 : " + rs.getString("anum") + 
						"\n개설날짜 : " + rs.getDate("startdate") + 
						"\n해지/만기 날짜 : " + rs.getDate("enddate") + 
						"\n총액 : " + rs.getLong("sum"));
				switch(rs.getString("kind")) {
				case "dpst" :
					System.out.print("종류 : 예금\n한도 : ");
					if(rs.getObject("account.limit")==null) System.out.println("없음");
					else System.out.println("하루에 " + rs.getLong("account.limit") + " 원까지 출금 가능");
					break;
				case "inst" :
					System.out.println("종류 : 적금\n매월 최대 " + rs.getLong("account.limit") + " 원 이내 적립 가능");
					break;
				case "loan" :
					System.out.println("종류 : 대출\n최대 " + rs.getLong("account.limit") + " 원까지 사용 가능");
					break;
				default :
					System.out.println("DB ERR : Wrong Data!");
					return false;
				}
				System.out.println("이자율 : " + rs.getDouble("interestR") + "\n거래내역 :");
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
	private static void showAccounts(String ssn) { // ssn 유효하고, 존재한다 가정
		int check = 0;
		try {
			rs = stmt.executeQuery("Select mbank, anum, sum, enddate, kind "
					+ "From account, admin "
					+ "Where ssn = \'" + ssn + "\' and admin.mnum = account.mnum");
			System.out.println("( 은행 / 계좌번호 / 종류 / 잔액 / 만료날짜 )");
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
	private static void transaction(String anum) { // anum 존재한다는 가정 하
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
	private static boolean findUser(String ssn) { // ssn 형식 유효하다고 가정
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
			System.out.println("0. 13자리의 주민등록번호를 (숫자만) 입력하세요.");
			if(checkSsn((ssn = sc.nextLine()))) break;
		}
		
		// ph
		while(true) {
			System.out.println("1. 전화번호를 입력하세요.(숫자만 입력하시오.)");
			if(checkPh((ph = sc.nextLine()))) break;
		}
		
		// salary
		System.out.println("2. 연소득을 입력하세요.");
		salary = sc.nextLong();
		sc.nextLine();
		// job
		System.out.println("3. 직업을 입력하세요.");
		job = sc.nextLine();
		// name
		System.out.println("4. 이름을 입력하세요.");
		name = sc.nextLine();
		// email
		System.out.println("5. 이메일을 입력하세요.");
		email = sc.nextLine();
		// address
		System.out.println("6. 주소를 입력하세요.");
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
	private static long sumMoney(String anum, byte dir, Date start, Date end) throws SQLException{ // 존재하는 계좌 번호라고 가정, date는 yyyy-MM-dd HH:mm:ss형식
		// start포함 end미포함
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ResultSet r = stmt.executeQuery("select sum(amount) from money where anum = \'" + anum + "\' and "
				+ "dir = " + dir + " and money.when >= \'" + df.format(start) + "\' and money.when < \'" + df.format(end) + "\'"); // start포함, end미포함
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
	private static Date nextMonth(Date date){ // date가 28일 보다 작은 것을 전제
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
	private static long calcInterest(ResultSet account) throws SQLException{ // 존재하는 계좌를 가리키고 있음을 전제, 오늘 기준
		long sum = 0, minusSum = 0;
		double intrst = account.getDouble("interestR");
		ResultSet r, s;
		
		Date start = account.getDate("startdate");
		Date end = account.getDate("enddate");
		Date tmpDate = new Date(), tmpNext = new Date(), today = new Date();
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		
		tmpDate = cloneDate(start);
		tmpDate.setDate(1);
		
		if(account.getString("kind").equals("inst")){ // 월 복리
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
		else{ // 연 복리
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
	private static boolean transfer(ResultSet from, ResultSet to, long amount){ // 두 계좌가 존재하고 유효한 것을 가정
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
		
		// money에 tuple 추가 from의 계좌 먼저
		stmt.executeUpdate("insert into money values("+date+", 0, "+amount+", " + anum + ", "
				+ canum + ", " + cname + ", " + cmnum + ")");
		flag = 1;
		// money에 to의 계좌 tuple 추가
		stmt.executeUpdate("insert into money values("+date+", 1, "+amount+", " + canum + ", " 
				+ anum + ", " + name + ", " + mnum + ")");
		flag = 2;
		// from account에서 금액 뺴기
		stmt.executeUpdate("update account set sum = sum - " + amount + " where anum = " + anum);
		flag = 3;
		// to account에서 금액 더하기
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
		// 적금계좌는 상대 계좌 해지된 것만 넣을것을 전제 적금계좌의 경우 상대계좌는 예금만
		Date end = new Date();
		Date start = new Date();
		Long limit, sum, save = null;
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		int error = 0;
		try {
			// 받는 계좌 만기인지 확인
			if(!checkEnddate(to) && !to.getString("kind").equals("loan")) return false;
			switch(from.getString("kind")){
			case "dpst" :
				// 잔액 확인
				if(from.getLong("sum") < amount) {
					System.out.println("잔액이 부족합니다.");
					return false;
				}
				// 보내는 계좌 한도 확인
				start.setHours(0);
				start.setMinutes(0);
				start.setSeconds(0);
				if(from.getObject("account.limit") != null
						&& (limit = from.getLong("account.limit") - sumMoney(from.getString("anum"), (byte) 0, start,end)) < amount){
					
					System.out.print("!!한도초과!! 일일 " + from.getLong("account.limit") + "원 출금 가능합니다.");
					System.out.println(" " + limit + "원 남았습니다.");
					return false;
				}
				switch(to.getString("kind")){
				case "inst" :
					// 받는 계좌 한도 확인
					start.setDate(1);
					start.setHours(0);
					start.setMinutes(0);
					start.setSeconds(0);
					if(to.getObject("account.limit") != null
						&& (limit = to.getLong("account.limit") - sumMoney(to.getString("anum"), (byte) 1, start,end)) < amount){
						System.out.println("!!적립금초과!! 금월 해당 계좌에 적립할 수 있는 금액을 초과하였습니다.");
						return false;
					}
					break;
				case "loan" :
					// 받는 계좌 한도 확인
					start.setDate(1);
					start.setHours(0);
					start.setMinutes(0);
					start.setSeconds(0);
					if(to.getObject("account.limit") != null
						&& (-to.getLong("sum")) < amount){
						System.out.print("!!상환금초과!! 해당 계좌에 상환할 수 있는 금액을 초과하였습니다.");
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
					System.out.println("예금계좌로만 송금 가능합니다. 이체를 취소합니다.");
					return false;
				}
				// 적금 이용 종료 전
				if(compareDate(from.getDate("enddate"), end) > 0){
					sum = calcInterest(from); // 이자 계산하기
					// 보낼 수 있는 금액인지 확인
					if(sum < amount){
						System.out.println("금액이 부족합니다.");
						return false;
					}
					
					// 송금할 것인지
					System.out.println("출금시 적금 이용이 종료됩니다. 계속하시겠습니까?( 0.계속 / #.종료 )");
					if(sc.nextInt() != 0) {
						System.out.println("계좌이체를 취소합니다.");
						break;
					}
					
					// 적금 이용 종료
					System.out.println("적금 이용을 종료합니다...");
					error = 1;
					start = from.getDate("enddate"); // rollback 대비해, 해지날짜 등록
					stmt.executeUpdate("update account set enddate = '" + dateFormat.format(end) 
						+ "' where anum = '" + from.getString("anum") + "'");
					error = 2;
					save = from.getLong("sum"); // rollback대비
					stmt.executeUpdate("update account set sum = " + sum + " where anum = '" + from.getString("anum") + "'");
					error = 0;
					System.out.println("이자 합산 후 계좌에 " + sum +"원이 남았습니다.\n빠른 시일 내에 잔액을 출금/이체 후 해지해주세요.");
				}
				// 적금 이용 종료 후
				else if(from.getLong("sum") < amount){
					System.out.println("금액이 부족합니다.");
					return false;
				}
				
				// 송금
				System.out.println(amount+"원을 송금합니다...");
				from = stmt.executeQuery("select * from account where anum = '" + from.getString("anum") + "'");
				from.next();
				if(!transfer(from, to, amount)) return false;
				return true;
				
			case "loan" :
				// 보내는 계좌 만기인지 확인
				if(!checkEnddate(from)) return false;
				// 잔액 확인
				if(from.getLong("account.limit") + from.getLong("sum") < amount) {
					System.out.println("!!한도초과!! 대출 가능한 금액을 초과하였습니다.");
					return false;
				}
				switch(to.getString("kind")){
				case "inst" :
					// 받는 계좌 한도 확인
					start.setDate(1);
					start.setHours(0);
					start.setMinutes(0);
					start.setSeconds(0);
					if(to.getObject("account.limit") != null
						&& (limit = to.getLong("account.limit") - sumMoney(to.getString("anum"), (byte) 1, start,end)) < amount){
						System.out.print("!!적립금초과!! 금월 해당 계좌에 적립할 수 있는 금액을 초과하였습니다.");
						return false;
					}
					break;
				case "loan" :
					// 받는 계좌 한도 확인
					start.setDate(1);
					start.setHours(0);
					start.setMinutes(0);
					start.setSeconds(0);
					if(to.getObject("account.limit") != null
						&& (-to.getLong("sum")) < amount){
						System.out.print("!!상환금초과!! 해당 계좌에 상환할 수 있는 금액을 초과하였습니다.");
						return false;
					}
					break;
				default :
					break;
				}
				if(!transfer(from, to, amount)) return false;
				// 대출가능한 한도 낮추기
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
	
	private static ResultSet selectAccount(String anum) { // 존재하는 첫 번째 row를 반환
		ResultSet nr = null;
		try {
			nr = stmt.executeQuery("Select * from account where anum = \'" + anum + "\'");
			if(!nr.next()){
				System.out.println("해당 계좌는 존재하지 않습니다.");
				return null;
			}
			return nr;
		} catch (SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
	// date하고 anum은 꼭 있어야함
	private static boolean checkEnddate(ResultSet r) { // 해당 계좌 가리키고 있는 거, 만료면 false
		Date date = new Date();
		try {
			if(compareDate(r.getDate("enddate"),date) <= 0){
				if(!r.getString("kind").equals("loan"))
					System.out.println(r.getString("anum") + "은 만료된 계좌입니다.");
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
		// 은행리스트 보여주고 은행명 입력받기
		System.out.println("Choose a bank.");
		try {
			rs = stmt.executeQuery("Select mbank, mnum from admin");
		while(rs.next())
			System.out.println(check++ + ". " + rs.getString("mbank"));
		while(!flag){			
			System.out.println("정확한 \'은행명\'을 입력하세요.");
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
		// 계좌 종류
		while(!flag) {
			System.out.println("어떤 계좌를 만드시겠습니까? ( 0.예금 / 1.적금 / 2.대출 / #.취소 )");
			cmd = sc.nextInt();
			sc.nextLine();
			switch(cmd){
			case 0 :
				kind = "dpst";
				while(true){
					System.out.println("다음 범위 내에서 연이율을 결정하세요.(0.10 ~ 2.00)%");
					interest = sc.nextDouble();
					sc.nextLine();
					if(interest >= 0.1 && interest <= 2.00) break;
				}
				do {
				System.out.println("1일 출금 가능한 한도를 결정하세요.(최소 1만원 이상 / '0' 입력시 한도 없음)");
				limit = sc.nextLong();
				sc.nextLine();
				} while(limit != 0 && limit < 10000);
				if(limit == 0) limit = null;
				flag = true;
				break;
			case 1 :
				kind = "inst";
				while(true){
					System.out.println("다음 범위 내에서 월이율을 결정하세요.(2.00 ~ 2.87)%");
					interest = sc.nextDouble();
					sc.nextLine();
					if(interest >= 2 && interest <= 2.87) break;
				}
				while(true){
					System.out.println("다음 범위 내에서 매월 적립 가능한 한도를 결정하세요.( 10000 ~ 20000000 )원");
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
						System.out.println("타행에서 연체기록이 있습니다. 대출이 불가합니다.");
						return;
					}
					if(calcCredit(ssn,mbank) > 5) {
						System.out.println("신용이 낮아 대출이 불가합니다.");
						return;
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
				while(true){
					System.out.println("다음 범위 내에서 연이율을 결정하세요.( 3.5 ~ 4.2 )%");
					interest = sc.nextDouble();
					sc.nextLine();
					if(interest >= 3.5 && interest <= 4.2) break;
				}
				while(true){
					System.out.println("다음 범위 내에서 대출 한도를 결정하세요. ( 1000000 ~ 500000000 )원");
					limit = sc.nextLong();
					sc.nextLine();
					if(limit <= 500000000 && limit >= 1000000) break;
				}
				flag = true;
				break;
			default :
				System.out.println("계좌 생성을 취소합니다.");
				return;
			}
		}
		
		// 가입 기간
		while(true){
			System.out.println("가입할 기간을 입력하세요.( 1 ~ 5 )년");
			year = sc.nextInt();
			sc.nextLine();
			if(year > 0 && year < 6) break;
		}
		// insert
		
		// 오늘 날짜
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
	private static void deleteAccount(String ssn, String anum) { // ssn은 존재하는 ssn이라 전제, anum은 올바른 계좌번호 형식이라고 가정
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
				System.out.println("해당 계좌는 존재하지 않습니다.");
				return;
			}
			sum = rs.getLong("sum");
			
			System.out.println("현재 잔액은 " + sum + "원 입니다.");
			if(compareDate(rs.getDate("enddate"), end) > 0){ // 만기 전 이용 종료
				System.out.println("계좌가 만료되기 전에는 이용을 먼저 종료하고 해지를 진행합니다.");
				System.out.println("이용을 종료하면 오늘부터 예적금 계좌는 '입금 불가' '출금 가능'하며, 대출 계좌는 '출금 불가' '입금 가능'합니다."
						+ "\n이자는 이용종료 날짜를 기준으로 계산됩니다.");
				System.out.println("계좌와 연결된 카드는 모두 해지됩니다.");
				System.out.println("계좌 이용을 종료하겠습니까? ( 0.아니오 / #.예 )");
				cmd = sc.nextInt();
				sc.nextLine();
				if(cmd == 0) return;
				// 이용 종료 진행
				stmt.executeUpdate("update account set enddate = '" + dateFormat.format(end)
						+"' where anum = '" + anum + "'");
				sum = calcInterest(rs); // 이자 계산하기
				stmt.executeUpdate("update account set sum = " + sum 
						+ " where anum = '" + anum + "'");
				System.out.println("계좌 이용을 종료하였습니다.\n이자 합산 후 계좌에 " + sum +"원이 남았습니다. ");
				// 카드 해지
				stmt.executeQuery("delete from card where anum = '" + anum + "'");
				// 한도 바꿔주기
				Date start = new Date();
				start.setHours(0);
				start.setMinutes(0);
				start.setSeconds(0);
				if(rs.getString("kind").equals("dpst"))
					stmt.executeUpdate("update account set account.limit = " + Long.MAX_VALUE
							+ " where anum = '" + anum + "'");
				if(rs.getString("kind").equals("loan") && sum < 0){
					System.out.println("빠른 시일 내에 잔액을 상환 후 해지해주세요.");
					return;
				}
				else if(rs.getString("kind").equals("loan")) del = true;
				else if(!rs.getString("kind").equals("loan") && sum > 0) {
					System.out.println("빠른 시일 내에 잔액을 출금/이체 후 해지해주세요.");
					return;
				}
				else del = true;
			}				
			else del = true;
			if(del) {
				switch(rs.getString("kind")){
				case "loan" :
					if(sum < 0)	{				
						System.out.println((-sum) + "원 상환을 먼저해주세요.\n계좌 해지를 취소합니다.");
						return;
					}
					break;
				default :
					if(sum > 0){
						System.out.println("잔액을 먼저 다른 계좌로 이체해주세요.\n계좌 해지를 취소합니다.");
						return;
					}
					break;
				}
				// 삭제
				System.out.println("계좌 해지를 진행합니다. 해당 계좌 내역은 모두 삭제됩니다.");
				stmt.executeQuery("delete from money where anum = '" + anum + "'");
				stmt.executeQuery("delete from account where anum = '" + anum + "'");
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	private static boolean checkCnum (String cnum) {
		if(cnum.length() != 16){
			System.out.println("잘못된 카드번호 형식입니다!(16자리, 숫자만)");
			return false;
		}
		char c;
		for(int i = 0; i < 16; i++){
			c = cnum.charAt(i);
			if(!Character.isDigit(c)){
				System.out.println("잘못된 계좌번호 형식입니다!(16자리, 숫자만)");
				return false;
			}
		}
		return true;
	}
	private static boolean checkAnum (String anum) {
		if(anum.length() != 14){
			System.out.println("잘못된 계좌번호 형식입니다!(14자리, 숫자만)");
			return false;
		}
		char c;
		for(int i = 0; i < 14; i++){
			c = anum.charAt(i);
			if(!Character.isDigit(c)){
				System.out.println("잘못된 계좌번호 형식입니다!(14자리, 숫자만)");
				return false;
			}
		}
		return true;
	}
	private static boolean checkPh(String ph) {
		if(ph.length() != 11 && ph.length() != 10){
			System.out.println("잘못된 전화번호 형식입니다!(10 or 11자리, 숫자만)");
			return false;
		}
		char c;
		for(int i = 0; i < ph.length(); i++){
			c = ph.charAt(i);
			if(!Character.isDigit(c)){
				System.out.println("잘못된 전화번호 형식입니다!(10 or 11자리, 숫자만)");
				return false;
			}
		}
		return true;
	}
	private static boolean checkSsn(String ssn) {
		if(ssn.length() != 13){
			System.out.println("잘못된 주민등록번호 형식입니다!(13자리, 숫자만)");
			return false;
		}
		char c;
		for(int i = 0; i < 13; i++){
			c = ssn.charAt(i);
			if(!Character.isDigit(c)){
				System.out.println("잘못된 주민등록번호 형식입니다!(13자리, 숫자만)");
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
		int check = 0; // Empty인지 확인
		int credit;
		// Admin 리스트 보여주기 : (count(*), 은행이름, admin 이름) 관리자 번호 순으로 보여주기
		rs = stmt.executeQuery("SELECT Mnum, Mbank, Mname FROM Admin");
		System.out.println("AdminID / Bank Name / Admin Name");
		while(rs.next()) {
			String Mnum = rs.getString("Mnum");
			String Mbank = rs.getString("Mbank");
			String Mname = rs.getString("Mname");
			System.out.println(Mnum + "  " + Mbank + "  " + Mname);
			check += 1;
		}
		
		// admin 아무도 없으면 생성할건지 물어보고 만들거나/안만들거나 후 menu로 back
		if(check == 0){
			System.out.println("!It's empty! Establish a new bank( 0.yes / #(other num).no )");
			if(sc.nextInt() == 0){
				// 새로운 admin 추가하기
				do{
				System.out.println("6자의 AdminID를 입력하세요.(a-z,A-Z,0-9)");
				adminID = sc.next();
				sc.nextLine();
				} while(adminID.length() != 6);
				System.out.println("은행 이름을 입력하세요.");
				String bankName = sc.next();
				sc.nextLine();
				System.out.println("관리자 이름을 입력하세요.");
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
			
			// manage에 해당하는게 없을 경우
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
