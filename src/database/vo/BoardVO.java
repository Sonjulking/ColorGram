package database.vo;

import java.sql.Date;

public class BoardVO {
    private int boardNum;
    private String boardType;
    private int boardWriterNum;
    private String boardTitle;
    private String boardContent;
    private Date boardCreateTime;
    private Date boardUpdateTime;
    private int boardViewCnt;
    private int boardLikeCnt;
    private String boardIsDeleted;
    private Date boardDeletedTime;

    public BoardVO() {
        super();
        // TODO Auto-generated constructor stub
    }

    public BoardVO(
            int boardNum,
            String boardType,
            int boardWriterNum,
            String boardTitle,
            String boardContent,
            Date boardCreateTime,
            Date boardUpdateTime,
            int boardViewCnt,
            int boardLikeCnt,
            String boardIsDeleted,
            Date boardDeletedTime
    ) {
        super();
        this.boardNum = boardNum;
        this.boardType = boardType;
        this.boardWriterNum = boardWriterNum;
        this.boardTitle = boardTitle;
        this.boardContent = boardContent;
        this.boardCreateTime = boardCreateTime;
        this.boardUpdateTime = boardUpdateTime;
        this.boardViewCnt = boardViewCnt;
        this.boardLikeCnt = boardLikeCnt;
        this.boardIsDeleted = boardIsDeleted;
        this.boardDeletedTime = boardDeletedTime;
    }

    public int getBoardNum() {
        return boardNum;
    }

    public void setBoardNum(int boardNum) {
        this.boardNum = boardNum;
    }

    public String getBoardType() {
        return boardType;
    }

    public void setBoardType(String boardType) {
        this.boardType = boardType;
    }

    public int getBoardWriterNum() {
        return boardWriterNum;
    }

    public void setBoardWriterNum(int boardWriterNum) {
        this.boardWriterNum = boardWriterNum;
    }

    public String getBoardTitle() {
        return boardTitle;
    }

    public void setBoardTitle(String boardTitle) {
        this.boardTitle = boardTitle;
    }

    public String getBoardContent() {
        return boardContent;
    }

    public void setBoardContent(String boardContent) {
        this.boardContent = boardContent;
    }

    public Date getBoardCreateTime() {
        return boardCreateTime;
    }

    public void setBoardCreateTime(Date boardCreateTime) {
        this.boardCreateTime = boardCreateTime;
    }

    public Date getBoardUpdateTime() {
        return boardUpdateTime;
    }

    public void setBoardUpdateTime(Date boardUpdateTime) {
        this.boardUpdateTime = boardUpdateTime;
    }

    public int getBoardViewCnt() {
        return boardViewCnt;
    }

    public void setBoardViewCnt(int boardViewCnt) {
        this.boardViewCnt = boardViewCnt;
    }

    public int getBoardLikeCnt() {
        return boardLikeCnt;
    }

    public void setBoardLikeCnt(int boardLikeCnt) {
        this.boardLikeCnt = boardLikeCnt;
    }

    public String getBoardIsDeleted() {
        return boardIsDeleted;
    }

    public void setBoardIsDeleted(String boardIsDeleted) {
        this.boardIsDeleted = boardIsDeleted;
    }

    public Date getBoardDeletedTime() {
        return boardDeletedTime;
    }

    public void setBoardDeletedTime(Date boardDeletedTime) {
        this.boardDeletedTime = boardDeletedTime;
    }

}