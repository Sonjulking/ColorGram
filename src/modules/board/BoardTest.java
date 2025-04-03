package modules.board;

import java.util.ArrayList;
import java.util.Scanner;

import database.dao.BoardDAO;
import database.vo.BoardVO;

public class BoardTest {
	public static Scanner sc;
	
	public static void printMenu() {
		System.out.println("***게시판***");
		System.out.println("1.글읽기	2.글쓰기	0.나가기");
		System.out.print("메뉴를 선택하세요. ");

	}
	public static void main(String[] args) {
		sc = new Scanner(System.in);
		int menu;
		
		while(true) {
			printMenu();
			menu = sc.nextInt();
			if(menu==0) {
				return;
			}
		
			switch(menu) {
			case 1: printBoard(); break;
			case 2: insertBoard(); break;
			} //end switch
		} //end while
	}
	public static void printBoard() {
		BoardDAO dao = new BoardDAO();
		ArrayList<BoardVO> list =dao.findAll();
		System.out.println("=================");
		for(BoardVO b:list) {
			System.out.print(b.getBoardNum()+"\t");
			System.out.print(b.getBoardType()+"\t");
			System.out.print(b.getBoardWriterNum()+"\t");
			System.out.print(b.getBoardTitle()+"\t");
			System.out.print(b.getBoardContent()+"\t");
			System.out.print(b.getBoardCreateTime()+"\t");
			System.out.print(b.getBoardUpdateTime()+"\t");
			System.out.print(b.getBoardViewCnt()+"\t");
			System.out.print(b.getBoardLikeCnt()+"\t");
			System.out.println();
		}
	}
	
	public static void insertBoard() {
		String boardType, boardTitle, boardContent;
		int boardWriterNum;
		System.out.println("글 타입을 입력하세요. [music/free]");
		boardType = sc.next();
		System.out.println("글쓴이를 입력하세요. ");
		boardWriterNum = sc.nextInt();
		sc.nextLine();
		System.out.println("글 제목을 입력하세요.");
		boardTitle = sc.nextLine();
		System.out.println("글 내용을 입력하세요.");
		boardContent = sc.nextLine();
		BoardDAO dao= new BoardDAO();
		BoardVO vo = new BoardVO();
		vo.setBoardType(boardType);
		vo.setBoardWriterNum(boardWriterNum);
		vo.setBoardTitle(boardTitle);
		vo.setBoardContent(boardContent);
		int re = dao.insert(vo);
		if(re == 1) {
			System.out.println("게시물이 등록되었습니다.");
		}else {
			System.out.println("게시물 등록에 실패하였습니다.");
		}
	}
}
