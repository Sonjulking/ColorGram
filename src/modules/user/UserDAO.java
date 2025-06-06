package modules.user;

import database.ConnectionProvider;

import modules.board.BoardWriteView;

import database.dao.BoardDAO;
import modules.board.comment.CommentDAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class UserDAO {

    // 로그인 확인
    public boolean selectUserLogin(String id, String password) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionProvider.getConnection("c##colorgram", "colorgram");
            if (conn == null) return false;

            String sql = "SELECT * FROM users WHERE user_id = ? AND user_password = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);
            pstmt.setString(2, password);

            rs = pstmt.executeQuery();
            return rs.next();
        } catch (Exception e) {
            System.out.println("예외 발생: " + e.getMessage());
            return false;
        } finally {
            System.out.println("user view curr user : " + id);
            BoardWriteView.setCurrentUserId(id);
            ConnectionProvider.close(conn, pstmt, rs);
        }
    }


    // 이 부분 추가했어요 -승원
    // 사용자 번호로 사용자 정보 조회 메소드
    public UserVO selectUserByNo(int userNo) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionProvider.getConnection("c##colorgram", "colorgram");

            // 연결 실패 시 처리
            if (conn == null) {
                System.out.println("데이터베이스 연결 실패");
                return null;
            }

            String sql = "SELECT * FROM users WHERE user_no = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userNo);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                UserVO user = new UserVO();
                user.setUserNo(rs.getInt("user_no"));
                user.setUserNickname(rs.getString("user_nickname"));
                user.setUserId(rs.getString("user_id"));
                user.setUserPassword(rs.getString("user_password"));
                user.setUserEmail(rs.getString("user_email"));
                return user;
            }
            return null;
        } catch (Exception e) {
            System.out.println("예외 발생: " + e.getMessage());
            return null;
        } finally {
            ConnectionProvider.close(conn, pstmt, rs);
        }
    }

    // 사용자 닉네임 가져오기
    public String getNicknameById(String id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        String nickname = null;

        try {
            conn = ConnectionProvider.getConnection("c##colorgram", "colorgram");
            if (conn == null) return null;

            String sql = "SELECT user_nickname FROM users WHERE user_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);

            rs = pstmt.executeQuery();
            if (rs.next()) {
                nickname = rs.getString("user_nickname");
            }
        } catch (Exception e) {
            System.out.println("예외 발생: " + e.getMessage());
        } finally {
            ConnectionProvider.close(conn, pstmt, rs);
        }
        return nickname;
    }

    // 회원가입 메서드
    public boolean insertUser(UserVO user) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ConnectionProvider.getConnection("c##colorgram", "colorgram");

            // 연결 실패 시 처리
            if (conn == null) {
                System.out.println("데이터베이스 연결 실패");
                return false;
            }

            // 이메일이 null이거나 빈 문자열인 경우 이메일 값 없이 쿼리 작성
            String sql;
            if (user.getUserEmail() == null || user.getUserEmail().isEmpty()) {
                sql = "INSERT INTO users (user_no, user_id, user_nickname, user_password) VALUES (userno_seq.NEXTVAL, ?, ?, ?)";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, user.getUserId());
                pstmt.setString(2, user.getUserNickname());
                pstmt.setString(3, user.getUserPassword());
            } else {
                sql = "INSERT INTO users (user_no, user_id, user_email, user_nickname, user_password) VALUES (userno_seq.NEXTVAL, ?, ?, ?, ?)";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, user.getUserId());
                pstmt.setString(2, user.getUserEmail());
                pstmt.setString(3, user.getUserNickname());
                pstmt.setString(4, user.getUserPassword());
            }

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            System.out.println("예외 발생: " + e.getMessage());
            return false;
        } finally {
            ConnectionProvider.close(conn, pstmt);
        }
    }

    public boolean deleteUser(String id) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            // 사용자 정보 조회
            UserVO user = selectUser(id);
            if (user == null) return false;

            int userNo = user.getUserNo();

            // 1. 댓글 좋아요 기록 삭제
            CommentDAO commentDAO = new CommentDAO();
            commentDAO.deleteCommentLikesByUser(userNo);

            // 2. 댓글 작성자 업데이트
            commentDAO.updateCommentsForDeletedUser(userNo);

            // 3. 게시글 작성자 업데이트
            BoardDAO boardDAO = new BoardDAO();
            boardDAO.updateBoardsForDeletedUser(userNo);

            // 4. 사용자 삭제
            conn = ConnectionProvider.getConnection("c##colorgram", "colorgram");
            if (conn == null) return false;

            String sql = "DELETE FROM users WHERE user_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            System.out.println("예외 발생: " + e.getMessage());
            return false;
        } finally {
            ConnectionProvider.close(conn, pstmt);
        }
    }

    // 사용자 정보 조회 메소드
    public UserVO selectUser(String id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionProvider.getConnection("c##colorgram", "colorgram");

            // 연결 실패 시 처리
            if (conn == null) {
                System.out.println("데이터베이스 연결 실패");
                return null;
            }

            String sql = "SELECT * FROM users WHERE user_id = ?";
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, id);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                UserVO user = new UserVO();
                user.setUserNo(rs.getInt("user_no"));
                user.setUserNickname(rs.getString("user_nickname"));
                user.setUserId(rs.getString("user_id"));
                user.setUserPassword(rs.getString("user_password"));
                user.setUserEmail(rs.getString("user_email"));
                return user;
            }
            return null;
        } catch (Exception e) {
            System.out.println("예외 발생: " + e.getMessage());
            return null;
        } finally {
            ConnectionProvider.close(conn, pstmt, rs);
        }
    }

    // 닉네임 업데이트
    public boolean updateUserNickname(String id, String newNickname) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ConnectionProvider.getConnection("c##colorgram", "colorgram");

            // 연결 실패 시 처리
            if (conn == null) {
                System.out.println("데이터베이스 연결 실패");
                return false;
            }

            String sql = "UPDATE users SET user_nickname = ? WHERE user_id = ?";
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, newNickname);
            pstmt.setString(2, id);

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            System.out.println("예외 발생: " + e.getMessage());
            return false;
        } finally {
            ConnectionProvider.close(conn, pstmt);
        }
    }

    // 이메일 업데이트
    public boolean updateUserEmail(String id, String newEmail) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ConnectionProvider.getConnection("c##colorgram", "colorgram");

            // 연결 실패 시 처리
            if (conn == null) {
                System.out.println("데이터베이스 연결 실패");
                return false;
            }

            String sql;
            if (newEmail == null || newEmail.isEmpty()) {
                // 이메일을 NULL로 업데이트
                sql = "UPDATE users SET user_email = NULL WHERE user_id = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, id);
            } else {
                // 새 이메일로 업데이트
                sql = "UPDATE users SET user_email = ? WHERE user_id = ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, newEmail);
                pstmt.setString(2, id);
            }

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            System.out.println("예외 발생: " + e.getMessage());
            return false;
        } finally {
            ConnectionProvider.close(conn, pstmt);
        }
    }

    // 비밀번호 업데이트
    public boolean updateUserPassword(String id, String newPassword) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ConnectionProvider.getConnection("c##colorgram", "colorgram");

            // 연결 실패 시 처리
            if (conn == null) {
                System.out.println("데이터베이스 연결 실패");
                return false;
            }

            String sql = "UPDATE users SET user_password = ? WHERE user_id = ?";
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, newPassword);
            pstmt.setString(2, id);

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            System.out.println("예외 발생: " + e.getMessage());
            return false;
        } finally {
            ConnectionProvider.close(conn, pstmt);
        }
    }

    // 아이디 업데이트
    public boolean updateUserId(String oldId, String newId) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionProvider.getConnection("c##colorgram", "colorgram");

            // 연결 실패 시 처리
            if (conn == null) {
                System.out.println("데이터베이스 연결 실패");
                return false;
            }

            // 먼저 새 아이디가 이미 사용 중인지 확인
            String checkSql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
            pstmt = conn.prepareStatement(checkSql);
            pstmt.setString(1, newId);
            rs = pstmt.executeQuery();

            if (rs.next() && rs.getInt(1) > 0) {
                // 이미 사용 중인 아이디인 경우
                System.out.println("이미 사용 중인 아이디입니다: " + newId);
                return false;
            }

            // 아이디 업데이트 진행
            ConnectionProvider.close(null, pstmt, rs);
            String sql = "UPDATE users SET user_id = ? WHERE user_id = ?";
            pstmt = conn.prepareStatement(sql);

            pstmt.setString(1, newId);
            pstmt.setString(2, oldId);

            int result = pstmt.executeUpdate();
            return result > 0;
        } catch (Exception e) {
            System.out.println("예외 발생: " + e.getMessage());
            return false;
        } finally {
            ConnectionProvider.close(conn, pstmt, rs);
        }
    }

    // 닉네임 중복 검사
    public boolean IsUserNicknameDupe(String nickname) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionProvider.getConnection("c##colorgram", "colorgram");

            // 연결 실패 시 처리
            if (conn == null) {
                System.out.println("데이터베이스 연결 실패");
                return false;
            }

            String sql = "SELECT COUNT(*) FROM users WHERE user_nickname = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nickname);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                // 0이면 사용 가능, 0보다 크면 이미 사용중
                return rs.getInt(1) == 0;
            }
            return false;
        } catch (Exception e) {
            System.out.println("예외 발생: " + e.getMessage());
            return false;
        } finally {
            ConnectionProvider.close(conn, pstmt, rs);
        }
    }

    // 아이디 중복 검사
    public boolean isUserIdDupe(String id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionProvider.getConnection("c##colorgram", "colorgram");

            // 연결 실패 시 처리
            if (conn == null) {
                System.out.println("데이터베이스 연결 실패");
                return false;
            }

            String sql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, id);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                // 0이면 사용 가능, 0보다 크면 이미 사용중
                return rs.getInt(1) == 0;
            }
            return false;
        } catch (Exception e) {
            System.out.println("예외 발생: " + e.getMessage());
            return false;
        } finally {
            ConnectionProvider.close(conn, pstmt, rs);
        }
    }

    // 이메일 중복 검사
    public boolean isUserEmailDupe(String email) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionProvider.getConnection("c##colorgram", "colorgram");

            // 연결 실패 시 처리
            if (conn == null) {
                System.out.println("데이터베이스 연결 실패");
                return false;
            }

            String sql = "SELECT COUNT(*) FROM users WHERE user_email = ?";
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                // 0이면 사용 가능, 0보다 크면 이미 사용중
                return rs.getInt(1) == 0;
            }
            return false;
        } catch (Exception e) {
            System.out.println("예외 발생: " + e.getMessage());
            return false;
        } finally {
            ConnectionProvider.close(conn, pstmt, rs);
        }
    }
}