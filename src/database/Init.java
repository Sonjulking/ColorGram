package database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class Init {

    public static void init() {
        createUser();
        grant();
        createTable();
        insertData();
    }

    public static void createUser() {
        try {
            Connection conn = ConnectionProvider.getConnection("system", "manager");
            String sql = "create user c##colorgram identified by colorgram";
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            ConnectionProvider.close(conn, stmt);
        } catch (Exception e) {
            System.out.println("예외발생:" + e.getMessage());
        }
    }

    public static void grant() {
        try {
            Connection conn = ConnectionProvider.getConnection("system", "manager");
            String sql = "grant connect, resource, dba to c##colorgram";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.executeUpdate();
            ConnectionProvider.close(conn, pstmt);
        } catch (Exception e) {
            System.out.println("예외발생:" + e.getMessage());
        }
    }

    public static void createTable() {
        String[] sqlStatements = {
                // users 테이블
                "CREATE TABLE users ( user_no NUMBER PRIMARY KEY, user_nickname VARCHAR2(100) NOT NULL, user_id VARCHAR2(100) NOT NULL UNIQUE, user_password VARCHAR2(100) NOT NULL, user_email VARCHAR2(100) UNIQUE )",

                // board 테이블
                "CREATE TABLE board ( board_num NUMBER PRIMARY KEY, board_type VARCHAR2(50), board_writer_num NUMBER, board_title VARCHAR2(100), board_content VARCHAR2(3000), board_create_time DATE DEFAULT SYSDATE, board_update_time DATE, board_view_cnt NUMBER DEFAULT 0, board_like_cnt NUMBER DEFAULT 0, board_is_deleted CHAR(1) DEFAULT 'N', board_deleted_time DATE, CONSTRAINT fk_board_writer FOREIGN KEY (board_writer_num) REFERENCES users(user_no) )",

                // comments 테이블
                "CREATE TABLE comments ( comment_num NUMBER PRIMARY KEY, comment_like_cnt NUMBER DEFAULT 0 NOT NULL, comment_board_num NUMBER NOT NULL, comment_writer_num NUMBER NOT NULL, comment_content VARCHAR2(2000) NOT NULL, comment_create_time DATE DEFAULT SYSDATE NOT NULL, comment_update_time DATE, comment_is_deleted CHAR(1) DEFAULT 'N' NOT NULL, comment_deleted_time DATE, CONSTRAINT fk_comment_board FOREIGN KEY (comment_board_num) REFERENCES board(board_num), CONSTRAINT fk_comment_writer FOREIGN KEY (comment_writer_num) REFERENCES users(user_no) )",

                // comment_likes 테이블
                "CREATE TABLE comment_likes ( user_no NUMBER NOT NULL, comment_num NUMBER NOT NULL, like_time DATE DEFAULT SYSDATE, PRIMARY KEY (user_no, comment_num), CONSTRAINT fk_comment_likes_user FOREIGN KEY (user_no) REFERENCES users(user_no), CONSTRAINT fk_comment_likes_comment FOREIGN KEY (comment_num) REFERENCES comments(comment_num) )",

                // 시퀀스들
                "CREATE SEQUENCE board_seq START WITH 1 INCREMENT BY 1",
                "CREATE SEQUENCE userno_seq START WITH 1 INCREMENT BY 1",
                "CREATE SEQUENCE comments_seq START WITH 1 INCREMENT BY 1"
        };


        try {
            Connection conn = ConnectionProvider.getConnection("c##colorgram", "colorgram");
            Statement stmt = conn.createStatement();

            for (String sql : sqlStatements) {
                stmt.executeUpdate(sql);
                System.out.println("Executed: " + sql);
            }
            ConnectionProvider.close(conn, stmt);
        } catch (Exception e) {
            System.out.println("예외발생:" + e.getMessage());
        }
    }


    public static void insertData() {

        String[] insertStatements = {
                //삭제를 위한 특별계정
                "INSERT INTO users (user_no, user_id, user_nickname, user_password) VALUES (0, 'deleted', '(탈퇴한 유저)', 'deleted')",
        };

        try {
            Connection conn = ConnectionProvider.getConnection("c##colorgram", "colorgram");
            Statement stmt = conn.createStatement();
            for (String sql : insertStatements) {
                stmt.executeUpdate(sql);
                System.out.println("Inserted: " + sql);
            }
            ConnectionProvider.close(conn, stmt);
        } catch (Exception e) {
            System.out.println("예외발생 inserted:" + e.getMessage());
        }
    }
}




















