package modules.user;

import database.ConnectionProvider;
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
            conn = ConnectionProvider.getConnection();
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
            conn = ConnectionProvider.getConnection();
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










    // 회원가입 메소드

//    public boolean insertUser(UserVO user) {
//        Connection conn = null;
//        PreparedStatement pstmt = null;
//
//        try {
//            conn = ConnectionProvider.getConnection();
//            if (conn == null) {
//                System.out.println("[디버깅] DB 연결 실패");
//                return false;
//            }
//
//            String sql = "INSERT INTO users (user_nickname, user_id, user_password, user_email) VALUES (?, ?, ?, ?)";
//            pstmt = conn.prepareStatement(sql);
//            pstmt.setString(1, user.getUserNickname());
//            pstmt.setString(2, user.getUserId());
//            pstmt.setString(3, user.getUserPassword());
//            pstmt.setString(4, user.getUserEmail());
//
//            int rowsInserted = pstmt.executeUpdate();
//            System.out.println("[디버깅] 회원가입 SQL 실행 완료, 삽입된 행 수: " + rowsInserted);
//
//            return rowsInserted > 0;
//        } catch (Exception e) {
//            System.out.println("[예외 발생] " + e.getMessage());
//            e.printStackTrace();
//            return false;
//        } finally {
//            ConnectionProvider.close(conn, pstmt, null);
//        }
//    }

    public boolean insertUser(UserVO user) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ConnectionProvider.getConnection();

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

    // 회원 삭제 메소드
    public boolean deleteUser(String id) {
        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = ConnectionProvider.getConnection();

            // 연결 실패 시 처리
            if (conn == null) {
                System.out.println("데이터베이스 연결 실패");
                return false;
            }

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

    // 로그인 확인
//    public boolean selectUserLogin(String id, String password) {
//        Connection conn = null;
//        PreparedStatement pstmt = null;
//        ResultSet rs = null;
//
//        try {
//            conn = ConnectionProvider.getConnection();
//
//            // 연결 실패 시 처리
//            if (conn == null) {
//                System.out.println("데이터베이스 연결 실패");
//                return false;
//            }
//
//            String sql = "SELECT * FROM users WHERE user_id = ? AND user_password = ?";
//            pstmt = conn.prepareStatement(sql);
//
//            pstmt.setString(1, id);
//            pstmt.setString(2, password);
//
//            rs = pstmt.executeQuery();
//
//            return rs.next(); // 결과가 있으면 true, 없으면 false
//        } catch (Exception e) {
//            System.out.println("예외 발생: " + e.getMessage());
//            return false;
//        } finally {
//            ConnectionProvider.close(conn, pstmt, rs);
//        }
//    }

    // 사용자 정보 조회 메소드
    public UserVO selectUser(String id) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionProvider.getConnection();

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
            conn = ConnectionProvider.getConnection();

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
            conn = ConnectionProvider.getConnection();

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
            conn = ConnectionProvider.getConnection();

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
            conn = ConnectionProvider.getConnection();

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
    public boolean selectIsUserNicknameDupe(String nickname) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = ConnectionProvider.getConnection();

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
            conn = ConnectionProvider.getConnection();

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
            conn = ConnectionProvider.getConnection();

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