package database.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import database.ConnectionProvider;
import database.vo.BoardVO;

public class BoardDAO {
	public int insert(BoardVO b) {
		int re = -1;
		try {
			String sql = "insert into board(board_num,board_type,board_writer_num, "
					+ "board_title,board_content,board_create_time,board_update_time, "
					+ "board_view_cnt,board_like_cnt,board_is_deleted,board_deleted_time) "
					+ "values(board_seq.nextval,?,?,?,?,sysdate,null,?,?,?,null)";
			Connection conn = ConnectionProvider.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, b.getBoardType());
			pstmt.setInt(2, b.getBoardWriterNum());
			pstmt.setString(3, b.getBoardTitle());
			pstmt.setString(4, b.getBoardContent());
			pstmt.setInt(5, b.getBoardViewCnt());
			pstmt.setInt(6, b.getBoardLikeCnt());
			pstmt.setString(7, b.getBoardIsDeleted());

			re = pstmt.executeUpdate();

			ConnectionProvider.close(conn, pstmt);

		} catch (Exception e) {
			System.out.println("예외발생: " + e.getMessage());
		}
		return re;
	}

	// 게시물 목록을 리스트로 반환 (삭제되지 않은 게시물만)
	public ArrayList<BoardVO> findAll() {
		ArrayList<BoardVO> list = new ArrayList<BoardVO>();
		String sql = "select board_num, board_type, board_writer_num, board_title, " 
				+ "board_content, board_create_time, board_view_cnt, board_like_cnt "
				+ "from board where board_is_deleted = 'N' "
				+ "order by board_num desc";
		try {
			Connection conn = ConnectionProvider.getConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				BoardVO vo = new BoardVO();
				vo.setBoardNum(rs.getInt(1));
				vo.setBoardType(rs.getString(2));
				vo.setBoardWriterNum(rs.getInt(3));
				vo.setBoardTitle(rs.getString(4));
				vo.setBoardContent(rs.getString(5));
				vo.setBoardCreateTime(rs.getDate(6));
				vo.setBoardViewCnt(rs.getInt(7));
				vo.setBoardLikeCnt(rs.getInt(8));
				vo.setBoardIsDeleted("N");  // 명시적으로 설정

				list.add(vo);
			}
			ConnectionProvider.close(conn, stmt, rs);

		} catch (Exception e) {
			System.out.println("예외발생: " + e.getMessage());
		}
		return list;
	}

	// 게시물 하나 조회 (삭제되지 않은 게시물만)
	public BoardVO findById(int boardNum) {
		BoardVO vo = null;
		String sql = "select board_num, board_type, board_writer_num, board_title, "
				+ "board_content, board_create_time, board_update_time, "
				+ "board_view_cnt, board_like_cnt, board_is_deleted, board_deleted_time "
				+ "from board where board_num = ? and board_is_deleted = 'N'";
		try {
			Connection conn = ConnectionProvider.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, boardNum);
			ResultSet rs = pstmt.executeQuery();
			
			if (rs.next()) {
				vo = new BoardVO();
				vo.setBoardNum(rs.getInt("board_num"));
				vo.setBoardType(rs.getString("board_type"));
				vo.setBoardWriterNum(rs.getInt("board_writer_num"));
				vo.setBoardTitle(rs.getString("board_title"));
				vo.setBoardContent(rs.getString("board_content"));
				vo.setBoardCreateTime(rs.getDate("board_create_time"));
				vo.setBoardUpdateTime(rs.getDate("board_update_time"));
				vo.setBoardViewCnt(rs.getInt("board_view_cnt"));
				vo.setBoardLikeCnt(rs.getInt("board_like_cnt"));
				vo.setBoardIsDeleted(rs.getString("board_is_deleted"));
				vo.setBoardDeletedTime(rs.getDate("board_deleted_time"));
			}
			
			ConnectionProvider.close(conn, pstmt, rs);
		} catch (Exception e) {
			System.out.println("예외발생: " + e.getMessage());
		}
		return vo;
	}
	
	// 게시물 수정
	public int update(BoardVO b) {
		int re = -1;
		try {
			String sql = "update board set "
					+ "board_type = ?, "
					+ "board_title = ?, "
					+ "board_content = ?, "
					+ "board_update_time = sysdate "
					+ "where board_num = ? and board_is_deleted = 'N'";
			
			Connection conn = ConnectionProvider.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setString(1, b.getBoardType());
			pstmt.setString(2, b.getBoardTitle());
			pstmt.setString(3, b.getBoardContent());
			pstmt.setInt(4, b.getBoardNum());
			
			re = pstmt.executeUpdate();
			
			ConnectionProvider.close(conn, pstmt);
		} catch (Exception e) {
			System.out.println("예외발생: " + e.getMessage());
		}
		return re;
	}
	
	// 게시물 삭제 (실제로 삭제하지 않고 is_deleted 플래그 설정)
	public int delete(int boardNum) {
		int re = -1;
		try {
			String sql = "update board set "
					+ "board_is_deleted = 'Y', "
					+ "board_deleted_time = sysdate "
					+ "where board_num = ? and board_is_deleted = 'N'";
			
			Connection conn = ConnectionProvider.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, boardNum);
			
			re = pstmt.executeUpdate();
			
			ConnectionProvider.close(conn, pstmt);
		} catch (Exception e) {
			System.out.println("예외발생: " + e.getMessage());
		}
		return re;
	}
	
	// 게시물 영구 삭제 (DB에서 실제로 삭제)
	public int permanentDelete(int boardNum) {
		int re = -1;
		try {
			String sql = "delete from board where board_num = ?";
			
			Connection conn = ConnectionProvider.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, boardNum);
			
			re = pstmt.executeUpdate();
			
			ConnectionProvider.close(conn, pstmt);
		} catch (Exception e) {
			System.out.println("예외발생: " + e.getMessage());
		}
		return re;
	}
	
	// 조회수 증가
	public int increaseViewCount(int boardNum) {
		int re = -1;
		try {
			String sql = "update board set board_view_cnt = board_view_cnt + 1 "
					+ "where board_num = ? and board_is_deleted = 'N'";
			
			Connection conn = ConnectionProvider.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, boardNum);
			
			re = pstmt.executeUpdate();
			
			ConnectionProvider.close(conn, pstmt);
		} catch (Exception e) {
			System.out.println("예외발생: " + e.getMessage());
		}
		return re;
	}
	
	// 좋아요 증가
	public int increaseLikeCount(int boardNum) {
		int re = -1;
		try {
			String sql = "update board set board_like_cnt = board_like_cnt + 1 "
					+ "where board_num = ? and board_is_deleted = 'N'";
			
			Connection conn = ConnectionProvider.getConnection();
			PreparedStatement pstmt = conn.prepareStatement(sql);
			pstmt.setInt(1, boardNum);
			
			re = pstmt.executeUpdate();
			
			ConnectionProvider.close(conn, pstmt);
		} catch (Exception e) {
			System.out.println("예외발생: " + e.getMessage());
		}
		return re;
	}
}