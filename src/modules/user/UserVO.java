package modules.user;

public class UserVO {
    private int userNo;
    private String userNickname;
    private String userId;
    private String userPassword;
    private String userEmail;




    public UserVO() {
        super();
    }

    public UserVO(String userId, String userPassword) {
        this.userId = userId;
        this.userPassword = userPassword;
    }



    //닉네임 가져오기
    public UserVO(String userNickname, String userId, String userPassword) {
        super();
        this.userNickname = userNickname;
        this.userId = userId;
        this.userPassword = userPassword;
    }

    public int getUserNo() {
        return userNo;
    }

    public void setUserNo(int userNo) {
        this.userNo = userNo;
    }

    public String getUserNickname() {
        return userNickname;
    }

    public void setUserNickname(String userNickname) {
        this.userNickname = userNickname;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
}