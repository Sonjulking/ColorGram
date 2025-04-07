package modules.board.comment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import database.ConnectionProvider;

public class CommentDAO {

    // 댓글 추가
    public int insert(CommentVO comment) {
        int result = -1;
        try {
            String sql = "INSERT INTO comments(comment_num, comment_board_num, comment_writer_num, " +
                    "comment_content, comment_create_time, comment_update_time, " +
                    "comment_is_deleted, comment_deleted_time) " +
                    "VALUES(comments_seq.nextval, ?, ?, ?, sysdate, null, 'N', null)";

            Connection conn = ConnectionProvider.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setInt(1, comment.getCommentBoardNum());
            pstmt.setInt(2, comment.getCommentWriterNum());
            pstmt.setString(3, comment.getCommentContent());

            result = pstmt.executeUpdate();

            ConnectionProvider.close(conn, pstmt);

        } catch (Exception e) {
            System.out.println("예외발생: " + e.getMessage());
        }
        return result;
    }

    // 특정 게시글의 댓글 목록 조회 (삭제되지 않은 댓글만)
    public ArrayList<CommentVO> findByBoardNum(int boardNum) {
        ArrayList<CommentVO> list = new ArrayList<CommentVO>();
        try {
            String sql = "SELECT c.comment_num, c.comment_board_num, c.comment_writer_num, " +
                    "c.comment_content, c.comment_create_time, c.comment_update_time, " +
                    "c.comment_like_cnt, u.user_nickname " +
                    "FROM comments c " +
                    "JOIN users u ON c.comment_writer_num = u.user_no " +
                    "WHERE c.comment_board_num = ? AND c.comment_is_deleted = 'N' " +
                    "ORDER BY c.comment_like_cnt DESC, c.comment_create_time ASC";

            Connection conn = ConnectionProvider.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, boardNum);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                CommentVO comment = new CommentVO();
                comment.setCommentNum(rs.getInt("comment_num"));
                comment.setCommentBoardNum(rs.getInt("comment_board_num"));
                comment.setCommentWriterNum(rs.getInt("comment_writer_num"));
                comment.setCommentContent(rs.getString("comment_content"));
                comment.setCommentCreateTime(rs.getDate("comment_create_time"));
                comment.setCommentUpdateTime(rs.getDate("comment_update_time"));
                comment.setCommentLikeCnt(rs.getInt("comment_like_cnt"));
                comment.setWriterNickname(rs.getString("user_nickname"));
                comment.setCommentIsDeleted("N");

                list.add(comment);
            }

            ConnectionProvider.close(conn, pstmt, rs);

        } catch (Exception e) {
            System.out.println("예외발생: " + e.getMessage());
        }
        return list;
    }

    // 댓글 한 개 조회
    public CommentVO findById(int commentNum) {
        CommentVO comment = null;
        try {
            String sql = "SELECT c.comment_num, c.comment_board_num, c.comment_writer_num, " +
                    "c.comment_content, c.comment_create_time, c.comment_update_time, " +
                    "c.comment_is_deleted, c.comment_deleted_time, c.comment_like_cnt, " +
                    "u.user_nickname " +
                    "FROM comments c " +
                    "JOIN users u ON c.comment_writer_num = u.user_no " +
                    "WHERE c.comment_num = ? AND c.comment_is_deleted = 'N'";

            Connection conn = ConnectionProvider.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, commentNum);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                comment = new CommentVO();
                comment.setCommentNum(rs.getInt("comment_num"));
                comment.setCommentBoardNum(rs.getInt("comment_board_num"));
                comment.setCommentWriterNum(rs.getInt("comment_writer_num"));
                comment.setCommentContent(rs.getString("comment_content"));
                comment.setCommentCreateTime(rs.getDate("comment_create_time"));
                comment.setCommentUpdateTime(rs.getDate("comment_update_time"));
                comment.setCommentIsDeleted(rs.getString("comment_is_deleted"));
                comment.setCommentDeletedTime(rs.getDate("comment_deleted_time"));
                comment.setCommentLikeCnt(rs.getInt("comment_like_cnt"));
                comment.setWriterNickname(rs.getString("user_nickname"));
            }

            ConnectionProvider.close(conn, pstmt, rs);

        } catch (Exception e) {
            System.out.println("예외발생: " + e.getMessage());
        }
        return comment;
    }

    // 댓글 수정
    public int update(CommentVO comment) {
        int result = -1;
        try {
            String sql = "UPDATE comments SET " +
                    "comment_content = ?, " +
                    "comment_update_time = sysdate " +
                    "WHERE comment_num = ? AND comment_is_deleted = 'N'";

            Connection conn = ConnectionProvider.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, comment.getCommentContent());
            pstmt.setInt(2, comment.getCommentNum());

            result = pstmt.executeUpdate();

            ConnectionProvider.close(conn, pstmt);

        } catch (Exception e) {
            System.out.println("예외발생: " + e.getMessage());
        }
        return result;
    }

    // 댓글 삭제 (논리적 삭제)
    public int delete(int commentNum) {
        int result = -1;
        try {
            String sql = "UPDATE comments SET " +
                    "comment_is_deleted = 'Y', " +
                    "comment_deleted_time = sysdate " +
                    "WHERE comment_num = ? AND comment_is_deleted = 'N'";

            Connection conn = ConnectionProvider.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, commentNum);

            result = pstmt.executeUpdate();

            ConnectionProvider.close(conn, pstmt);

        } catch (Exception e) {
            System.out.println("예외발생: " + e.getMessage());
        }
        return result;
    }

    // 댓글 추천 증가
    public int increaseLikeCount(int commentNum) {
        int result = -1;
        try {
            String sql = "UPDATE comments SET comment_like_cnt = comment_like_cnt + 1 " +
                    "WHERE comment_num = ? AND comment_is_deleted = 'N'";

            Connection conn = ConnectionProvider.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, commentNum);

            result = pstmt.executeUpdate();

            ConnectionProvider.close(conn, pstmt);

        } catch (Exception e) {
            System.out.println("예외발생: " + e.getMessage());
        }
        return result;
    }
    
    // 사용자가 댓글에 추천했는지 확인
    public boolean hasUserLikedComment(int userNo, int commentNum) {
        boolean hasLiked = false;
        try {
            String sql = "SELECT COUNT(*) FROM comment_likes WHERE user_no = ? AND comment_num = ?";
            
            Connection conn = ConnectionProvider.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userNo);
            pstmt.setInt(2, commentNum);
            
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                hasLiked = rs.getInt(1) > 0;
            }
            
            ConnectionProvider.close(conn, pstmt, rs);
        } catch (Exception e) {
            System.out.println("예외발생: " + e.getMessage());
        }
        return hasLiked;
    }

    // 댓글 추천 추가
    public boolean addCommentLike(int userNo, int commentNum) {
        int result = -1;
        try {
            // 1. 추천 이력 추가
            String insertSql = "INSERT INTO comment_likes (user_no, comment_num) VALUES (?, ?)";
            
            Connection conn = ConnectionProvider.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(insertSql);
            pstmt.setInt(1, userNo);
            pstmt.setInt(2, commentNum);
            
            result = pstmt.executeUpdate();
            
            if (result > 0) {
                // 2. 댓글의 추천 수 증가
                String updateSql = "UPDATE comments SET comment_like_cnt = comment_like_cnt + 1 WHERE comment_num = ?";
                pstmt = conn.prepareStatement(updateSql);
                pstmt.setInt(1, commentNum);
                pstmt.executeUpdate();
            }
            
            ConnectionProvider.close(conn, pstmt);
        } catch (Exception e) {
            System.out.println("예외발생: " + e.getMessage());
            return false;
        }
        return result > 0;
    }

    // 댓글 추천 제거
    public boolean removeCommentLike(int userNo, int commentNum) {
        int result = -1;
        try {
            // 1. 추천 이력 제거
            String deleteSql = "DELETE FROM comment_likes WHERE user_no = ? AND comment_num = ?";
            
            Connection conn = ConnectionProvider.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(deleteSql);
            pstmt.setInt(1, userNo);
            pstmt.setInt(2, commentNum);
            
            result = pstmt.executeUpdate();
            
            if (result > 0) {
                // 2. 댓글의 추천 수 감소
                String updateSql = "UPDATE comments SET comment_like_cnt = comment_like_cnt - 1 WHERE comment_num = ?";
                pstmt = conn.prepareStatement(updateSql);
                pstmt.setInt(1, commentNum);
                pstmt.executeUpdate();
            }
            
            ConnectionProvider.close(conn, pstmt);
        } catch (Exception e) {
            System.out.println("예외발생: " + e.getMessage());
            return false;
        }
        return result > 0;
    }

    // 게시글에 대한 댓글 개수 조회
    public int getCommentCount(int boardNum) {
        int count = 0;
        try {
            String sql = "SELECT COUNT(*) FROM comments " +
                    "WHERE comment_board_num = ? AND comment_is_deleted = 'N'";

            Connection conn = ConnectionProvider.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, boardNum);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt(1);
            }

            ConnectionProvider.close(conn, pstmt, rs);

        } catch (Exception e) {
            System.out.println("예외발생: " + e.getMessage());
        }
        return count;
    }

    // 게시글 삭제 시 해당 게시글의 모든 댓글 삭제 (논리적 삭제)
    public int deleteByBoardNum(int boardNum) {
        int result = -1;
        try {
            String sql = "UPDATE comments SET " +
                    "comment_is_deleted = 'Y', " +
                    "comment_deleted_time = sysdate " +
                    "WHERE comment_board_num = ? AND comment_is_deleted = 'N'";

            Connection conn = ConnectionProvider.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, boardNum);

            result = pstmt.executeUpdate();

            ConnectionProvider.close(conn, pstmt);

        } catch (Exception e) {
            System.out.println("예외발생: " + e.getMessage());
        }
        return result;
    }
    
    // 탈퇴한 사용자의 댓글 작성자명 변경
    public int updateCommentsForDeletedUser(int userNo) {
        int result = -1;
        try {
            String sql = "UPDATE comments SET comment_writer_num = 0 " +
                    "WHERE comment_writer_num = ?";

            Connection conn = ConnectionProvider.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userNo);

            result = pstmt.executeUpdate();

            ConnectionProvider.close(conn, pstmt);
        } catch (Exception e) {
            System.out.println("예외발생: " + e.getMessage());
        }
        return result;
    }
    
    // 탈퇴 한 사용자의 좋아요 삭제
    public int deleteCommentLikesByUser(int userNo) {
        int result = -1;
        try {
            String sql = "DELETE FROM comment_likes WHERE user_no = ?";
            
            Connection conn = ConnectionProvider.getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userNo);
            
            result = pstmt.executeUpdate();
            
            ConnectionProvider.close(conn, pstmt);
        } catch (Exception e) {
            System.out.println("예외발생: " + e.getMessage());
        }
        return result;
    }
}