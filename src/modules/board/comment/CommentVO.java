package modules.board.comment;

import java.sql.Date;

public class CommentVO {
    private int commentNum;             // 댓글 번호
    private int commentBoardNum;        // 게시글 번호
    private int commentWriterNum;       // 작성자 번호
    private String commentContent;      // 댓글 내용
    private Date commentCreateTime;     // 작성 시간
    private Date commentUpdateTime;     // 수정 시간
    private String commentIsDeleted;    // 삭제 여부(Y/N)
    private Date commentDeletedTime;    // 삭제 시간
    private int commentLikeCnt;         // 추천수

    // 추가 필드: 작성자 닉네임 (DB 조인 결과를 위한 필드)
    private String writerNickname;

    // 기본 생성자
    public CommentVO() {
        this.commentIsDeleted = "N";
        this.commentLikeCnt = 0;
    }

    // Getters and Setters
    public int getCommentNum() {
        return commentNum;
    }

    public void setCommentNum(int commentNum) {
        this.commentNum = commentNum;
    }

    public int getCommentBoardNum() {
        return commentBoardNum;
    }

    public void setCommentBoardNum(int commentBoardNum) {
        this.commentBoardNum = commentBoardNum;
    }

    public int getCommentWriterNum() {
        return commentWriterNum;
    }

    public void setCommentWriterNum(int commentWriterNum) {
        this.commentWriterNum = commentWriterNum;
    }

    public String getCommentContent() {
        return commentContent;
    }

    public void setCommentContent(String commentContent) {
        this.commentContent = commentContent;
    }

    public Date getCommentCreateTime() {
        return commentCreateTime;
    }

    public void setCommentCreateTime(Date commentCreateTime) {
        this.commentCreateTime = commentCreateTime;
    }

    public Date getCommentUpdateTime() {
        return commentUpdateTime;
    }

    public void setCommentUpdateTime(Date commentUpdateTime) {
        this.commentUpdateTime = commentUpdateTime;
    }

    public String getCommentIsDeleted() {
        return commentIsDeleted;
    }

    public void setCommentIsDeleted(String commentIsDeleted) {
        this.commentIsDeleted = commentIsDeleted;
    }

    public Date getCommentDeletedTime() {
        return commentDeletedTime;
    }

    public void setCommentDeletedTime(Date commentDeletedTime) {
        this.commentDeletedTime = commentDeletedTime;
    }

    public String getWriterNickname() {
        return writerNickname;
    }

    public void setWriterNickname(String writerNickname) {
        this.writerNickname = writerNickname;
    }

    public int getCommentLikeCnt() {
        return commentLikeCnt;
    }

    public void setCommentLikeCnt(int commentLikeCnt) {
        this.commentLikeCnt = commentLikeCnt;
    }

    @Override
    public String toString() {
        return "CommentVO [commentNum=" + commentNum +
                ", commentBoardNum=" + commentBoardNum +
                ", commentWriterNum=" + commentWriterNum +
                ", commentContent=" + commentContent +
                ", commentCreateTime=" + commentCreateTime +
                ", commentLikeCnt=" + commentLikeCnt +
                ", writerNickname=" + writerNickname + "]";
    }
}