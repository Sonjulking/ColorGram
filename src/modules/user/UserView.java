package modules.user;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import modules.board.BoardWriteView;
import modules.chat.ChatClient;
public class UserView extends VBox {

    private TextField idField;
    private PasswordField passwordField;
    private Button loginButton;
    private Button registerButton;
    private ChatClient chatClient;
    
    // 로그인 상태
    private static boolean isLogIn = false;
    
    // 로그인 성공 시 실행할 콜백 인터페이스
    private static Runnable onLoginSuccess;
    
    // 로그아웃 성공 시 실행할 콜백 인터페이스
    private static Runnable onLogoutSuccess;

    // 현재 로그인 중인 사용자 ID 저장
    private static String currentUserId = null;
    
    // 기본 생성자
    public UserView() {
        if (isLogIn && currentUserId != null) {
            selectUserInfo();
     
        } else {
            setupLoginView();
        }
    }
    
    //채팅을 위한 로그아웃
    public UserView(ChatClient chatClient) {
        this.chatClient = chatClient;

        if (isLogIn && currentUserId != null) {
            selectUserInfo();
        } else {
            setupLoginView();
        }
    }
    
    // 사용자 ID 설정 메서드
    public static void setCurrentUserId(String userId) {
        currentUserId = userId;
    }
    
    // 현재 사용자 ID 반환 메서드
    public static String getCurrentUserId() {
        return currentUserId;
    }
    
    // 로그인 상태 확인 메서드
    public static boolean isLogIn() {
        return isLogIn;
    }
    
    // 로그아웃 메서드 - 콜백은 유지하면서 로그아웃처리
    public void logout() {
        setLogIn(false);
        setCurrentUserId(null);
        getChildren().clear();
        setupLoginView();
    }
    
    // 로그인 성공 콜백 설정 메서드
    public void setOnLoginSuccess(Runnable callback) {
        UserView.onLoginSuccess = callback;
    }

    // 로그아웃 성공 콜백 설정 메서드
    public void setOnLogoutSuccess(Runnable callback) {
        UserView.onLogoutSuccess = callback;
    }

    // 로그인 화면 UI 구성 메서드
    private void setupLoginView() {
        setPadding(new Insets(20));
        setAlignment(Pos.CENTER);
        setSpacing(20);

        // 타이틀
        Label titleLabel = new Label("ColorGram 로그인");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // 로그인 폼
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setAlignment(Pos.CENTER);

        Label idLabel = new Label("아이디:");
        Label passwordLabel = new Label("비밀번호:");

        idField = new TextField();
        idField.setPromptText("아이디를 입력하세요");

        passwordField = new PasswordField();
        passwordField.setPromptText("비밀번호를 입력하세요");

        formGrid.add(idLabel, 0, 0);
        formGrid.add(idField, 1, 0);
        formGrid.add(passwordLabel, 0, 1);
        formGrid.add(passwordField, 1, 1);

        // 버튼 영역
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        loginButton = new Button("로그인");
        registerButton = new Button("회원가입");

        // 로그인 버튼 이벤트
        loginButton.setOnAction(e -> {
            String id = idField.getText();
            String password = passwordField.getText();

            if (id.isEmpty() || password.isEmpty()) {
                showAlert("오류", "아이디와 비밀번호를 모두 입력하세요.");
                return;
            }

            UserDAO userDAO = new UserDAO();
            if (userDAO.selectUserLogin(id, password)) {
                showAlert("로그인 성공", "로그인에 성공했습니다.");
                // 로그인 상태 변경
                setLogIn(true);
                // 현재 사용자 ID 저장
                setCurrentUserId(id);
                
                // 로그인 성공 콜백 실행
                if (onLoginSuccess != null) {
                    onLoginSuccess.run();
                }
            } else {
                showAlert("로그인 실패", "아이디 또는 비밀번호가 일치하지 않습니다.");
            }
        });

        // 회원가입 버튼 이벤트
        registerButton.setOnAction(e -> {
            // VBox의 내용을 회원가입 폼으로 변경
            getChildren().clear();
            insertUserDataForm();
        });

        buttonBox.getChildren().addAll(loginButton, registerButton);

        // 화면에 추가
        getChildren().addAll(titleLabel, formGrid, buttonBox);
    }

    // 회원가입 화면 UI 구성 메소드
    private void insertUserDataForm() {
        setPadding(new Insets(20));
        setAlignment(Pos.CENTER);
        setSpacing(20);

        // 타이틀
        Label titleLabel = new Label("ColorGram 회원가입");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // 회원가입 폼
        GridPane formGrid = new GridPane();
        formGrid.setHgap(10);
        formGrid.setVgap(10);
        formGrid.setAlignment(Pos.CENTER);

        // 중복 상태 추적 변수
        final boolean[] isIdChecked = {false};
        final boolean[] isNicknameChecked = {false};
        
        // 1. 닉네임 필드
        Label nickLabel = new Label("닉네임:");
        TextField nickField = new TextField();
        nickField.setPromptText("닉네임을 입력하세요");
        
        // 닉네임 중복 확인 버튼
        Button checkNickButton = new Button("중복확인");
        HBox nickBox = new HBox(10, nickField, checkNickButton);
        
        // 닉네임 중복 확인 버튼 이벤트
        checkNickButton.setOnAction(event -> {
            String nickname = nickField.getText().trim();
            
            // 입력 검증
            if (nickname.isEmpty()) {
                showAlert("입력 오류", "닉네임을 입력해주세요.");
                isNicknameChecked[0] = false;
                return;
            }
            
            // 중복 검사 - DAO를 통해 실제 데이터베이스 체크
            UserDAO userDAO = new UserDAO();
            if (userDAO.IsUserNicknameDupe(nickname)) {
                showAlert("확인", "사용 가능한 닉네임입니다.");
                isNicknameChecked[0] = true;
            } else {
                showAlert("중복 오류", "이미 사용 중인 닉네임입니다. 다른 닉네임을 입력해주세요.");
                isNicknameChecked[0] = false;
            }
        });
        
        // 닉네임 필드 변경 시 체크 상태 초기화
        nickField.textProperty().addListener((observable, oldValue, newValue) -> {
            isNicknameChecked[0] = false;
        });
        
        // 2. 아이디 필드
        Label idLabel = new Label("아이디:");
        TextField idField = new TextField();
        idField.setPromptText("아이디를 입력하세요");
        
        // 아이디 중복 확인 버튼
        Button checkIdButton = new Button("중복확인");
        HBox idBox = new HBox(10, idField, checkIdButton);
        
        // 중복 확인 버튼 이벤트
        checkIdButton.setOnAction(event -> {
            String id = idField.getText().trim();
            
            // 입력 검증
            if (id.isEmpty()) {
                showAlert("입력 오류", "아이디를 입력해주세요.");
                isIdChecked[0] = false;
                return;
            }
            
            // 중복 검사
            UserDAO userDAO = new UserDAO();
            if (userDAO.isUserIdDupe(id)) {
                showAlert("확인", "사용 가능한 아이디입니다.");
                isIdChecked[0] = true;
            } else {
                showAlert("중복 오류", "이미 사용 중인 아이디입니다. 다른 아이디를 입력해주세요.");
                isIdChecked[0] = false;
            }
        });
        
        // 아이디 필드 변경 시 체크 상태 초기화
        idField.textProperty().addListener((observable, oldValue, newValue) -> {
            isIdChecked[0] = false;
        });

        // 3. 비밀번호 필드
        Label passwordLabel = new Label("비밀번호:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("비밀번호를 입력하세요");

        // 4. 이메일 필드 - 선택 사항임을 표시
        Label emailLabel = new Label("이메일 (선택사항):");
        TextField emailField = new TextField();
        emailField.setPromptText("이메일을 입력하세요 (선택사항)");

        // 폼에 필드 추가 - 순서 변경됨
        formGrid.add(nickLabel, 0, 0);
        formGrid.add(nickBox, 1, 0);
        formGrid.add(idLabel, 0, 1);
        formGrid.add(idBox, 1, 1);
        formGrid.add(passwordLabel, 0, 2);
        formGrid.add(passwordField, 1, 2);
        formGrid.add(emailLabel, 0, 3);
        formGrid.add(emailField, 1, 3);

        // 버튼 영역
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);

        Button registerBtn = new Button("가입하기");
        Button cancelBtn = new Button("취소");

        // 회원가입 버튼 이벤트
        registerBtn.setOnAction(e -> {
            String nickname = nickField.getText();
            String id = idField.getText();
            String password = passwordField.getText();
            String email = emailField.getText();

            // 필수 입력 검증 (이메일 제외)
            if (nickname.isEmpty() || id.isEmpty() || password.isEmpty()) {
                showAlert("입력 오류", "닉네임, 아이디, 비밀번호는 필수 입력 항목입니다.");
                return;
            }
            
            // 닉네임 중복 확인 검사
            if (!isNicknameChecked[0]) {
                showAlert("닉네임 확인 필요", "닉네임 중복 확인을 먼저 해주세요.");
                return;
            }
            
            // 아이디 중복 확인 검사
            if (!isIdChecked[0]) {
                showAlert("아이디 확인 필요", "아이디 중복 확인을 먼저 해주세요.");
                return;
            }
            
            // 이메일이 입력된 경우에만 검증
            if (!email.isEmpty()) {
                // 이메일 형식 검증 - 정규식 사용
                String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
                if (!email.matches(emailRegex)) {
                    showAlert("이메일 형식 오류", "올바른 이메일 형식이 아닙니다. (예: example@domain.com)");
                    return;
                }
                
                // 이메일 중복 검사
                UserDAO checkDao = new UserDAO();
                if (!checkDao.isUserIdDupe(email)) {
                    showAlert("이메일 중복", "이미 사용 중인 이메일입니다. 다른 이메일을 사용해주세요.");
                    return;
                }
            }

            // VO 객체 생성
            UserVO user = new UserVO();
            user.setUserNickname(nickname);
            user.setUserId(id);
            user.setUserPassword(password);
            user.setUserEmail(email); // 빈 문자열이면 null로 저장될 것임

            // 데이터베이스에 저장
            UserDAO userDAO = new UserDAO();
            boolean result = userDAO.insertUser(user);

            if (result) {
                showAlert("회원가입 성공", "회원가입이 완료되었습니다. 로그인해주세요.");
                // 로그인 화면으로 돌아가기
                getChildren().clear();
                setupLoginView();
            } else {
                showAlert("회원가입 실패", "회원가입 중 오류가 발생했습니다. 다시 시도해주세요.");
            }
        });

        // 취소 버튼 이벤트
        cancelBtn.setOnAction(e -> {
            // 로그인 화면으로 돌아가기
            getChildren().clear();
            setupLoginView();
        });

        buttonBox.getChildren().addAll(registerBtn, cancelBtn);

        // 화면에 추가
        getChildren().addAll(titleLabel, formGrid, buttonBox);
    }

    // 알림창 표시
    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
    
    // 사용자 정보 화면 구성 메소드
    private void selectUserInfo() {
        setPadding(new Insets(20));
        setAlignment(Pos.CENTER);
        setSpacing(20);

        // 타이틀
        Label titleLabel = new Label("유저 정보");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // 사용자 정보 가져오기
        UserDAO userDAO = new UserDAO();
        UserVO userInfo = userDAO.selectUser(currentUserId);
        
        if (userInfo == null) {
            // 사용자 정보를 가져오지 못한 경우 에러 메시지 표시
            Label errorLabel = new Label("유저 정보를 불러올 수 없습니다.");
            errorLabel.setStyle("-fx-text-fill: red;");
            
            Button backButton = new Button("돌아가기");
            backButton.setOnAction(e -> {
                // 로그인 화면으로 돌아가기
                getChildren().clear();
                setupLoginView();
            });
            
            getChildren().addAll(titleLabel, errorLabel, backButton);
            return;
        }
        
        // 사용자 정보 표시 영역
        GridPane infoGrid = new GridPane();
        infoGrid.setHgap(15);
        infoGrid.setVgap(15);
        infoGrid.setAlignment(Pos.CENTER);
        
        // 정보 레이블들 - 순서 변경: 닉네임, 아이디 (이메일 제거)
        Label nicknameLabel = new Label("닉네임:");
        Label nicknameValueLabel = new Label(userInfo.getUserNickname());
        nicknameValueLabel.setStyle("-fx-font-weight: bold;");
        
        Label idLabel = new Label("아이디:");
        Label idValueLabel = new Label(userInfo.getUserId());
        idValueLabel.setStyle("-fx-font-weight: bold;");
        
        // 그리드에 정보 추가 - 순서 변경됨, 이메일 제거
        infoGrid.add(nicknameLabel, 0, 0);
        infoGrid.add(nicknameValueLabel, 1, 0);
        infoGrid.add(idLabel, 0, 1);
        infoGrid.add(idValueLabel, 1, 1);
        
        // 버튼 영역
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button logoutButton = new Button("로그아웃");
        Button editButton = new Button("정보 수정");
        Button deleteAccountButton = new Button("회원탈퇴");
        deleteAccountButton.setStyle("-fx-text-fill: red;"); // 회원탈퇴 버튼을 붉은색으로 표시
        
        // 로그아웃 버튼 이벤트
        logoutButton.setOnAction(e -> {
            // 로그아웃 확인 다이얼로그 표시
            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
            confirmAlert.setTitle("로그아웃 확인");
            confirmAlert.setHeaderText("정말 로그아웃하시겠습니까?");
            confirmAlert.setContentText("로그아웃하면 다시 로그인해야 합니다.");
            
            // 버튼 텍스트 변경
            ((Button) confirmAlert.getDialogPane().lookupButton(ButtonType.OK)).setText("로그아웃");
            ((Button) confirmAlert.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("취소");
            
            confirmAlert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
               	 // 여기서 채팅 중일 때 logout 메시지 보내기
                    if (chatClient != null) {
                        chatClient.logout();
                        System.out.println("UserView: chatClient.logout() 호출됨");
                    }
                	
                	
                    // 로그아웃 처리 - 콜백은 유지
                    setLogIn(false);
                    setCurrentUserId(null);
                    
                    // 로그아웃 성공 콜백 실행
                    if (onLogoutSuccess != null) {
                        onLogoutSuccess.run();
                    }
                    
                    // 로그인 화면으로 전환
                    getChildren().clear();
                    setupLoginView();
                    
                    showAlert("로그아웃", "로그아웃되었습니다.");
                }
                // 취소 버튼을 누른 경우에는 아무 작업도 하지 않음
            });
        });
        
        // 정보 수정 버튼 이벤트
        editButton.setOnAction(e -> {
            // 비밀번호 확인 화면으로 전환
            getChildren().clear();
            setupPasswordConfirmationForm(userInfo);
        });
        
        // 회원탈퇴 버튼 이벤트
        deleteAccountButton.setOnAction(e -> {
            // 회원탈퇴 확인을 위한 비밀번호 확인 화면으로 전환
            getChildren().clear();
            setupDeleteAccountConfirmation(userInfo);
        });
        
        buttonBox.getChildren().addAll(editButton, logoutButton, deleteAccountButton);
        
        // 화면에 추가
        getChildren().addAll(titleLabel, infoGrid, buttonBox);
    }
    
    // 비밀번호 확인 화면 구성 메서드
    private void setupPasswordConfirmationForm(UserVO userInfo) {
        setPadding(new Insets(20));
        setAlignment(Pos.CENTER);
        setSpacing(20);

        // 타이틀
        Label titleLabel = new Label("본인 확인");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        // 안내 메시지
        Label infoLabel = new Label("정보 수정을 위해 현재 비밀번호를 입력해주세요.");
        infoLabel.setStyle("-fx-font-size: 14px;");
        
        // 비밀번호 입력 영역
        VBox passwordBox = new VBox(15);
        passwordBox.setAlignment(Pos.CENTER);
        
        Label passwordLabel = new Label("현재 비밀번호:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("현재 비밀번호를 입력하세요");
        passwordField.setPrefWidth(250);
        
        passwordBox.getChildren().addAll(passwordLabel, passwordField);
        
        // 버튼 영역
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button confirmButton = new Button("확인");
        Button cancelButton = new Button("취소");
        
        // 버튼 생성 후 엔터 키 이벤트 설정
        passwordField.setOnAction(e -> {
            // 엔터 키를 누르면 확인 버튼 클릭과 동일한 효과
            confirmButton.fire();
        });
        
        // 확인 버튼 이벤트
        confirmButton.setOnAction(e -> {
            String inputPassword = passwordField.getText();
            
            if (inputPassword.isEmpty()) {
                showAlert("입력 오류", "비밀번호를 입력해주세요.");
                return;
            }
            
            UserDAO userDAO = new UserDAO();
            // 입력한 비밀번호와 현재 사용자의 비밀번호 비교
            if (userDAO.selectUserLogin(userInfo.getUserId(), inputPassword)) {
                // 비밀번호 일치, 정보 수정 화면으로 이동
                getChildren().clear();
                setupEditProfileForm(userInfo);
            } else {
                // 비밀번호 불일치
                showAlert("인증 실패", "비밀번호가 일치하지 않습니다.");
            }
        });
        
        // 취소 버튼 이벤트
        cancelButton.setOnAction(e -> {
            // 사용자 정보 화면으로 돌아가기
            getChildren().clear();
            selectUserInfo();
        });
        
        buttonBox.getChildren().addAll(confirmButton, cancelButton);
        
        // 화면에 추가
        getChildren().addAll(titleLabel, infoLabel, passwordBox, buttonBox);
    }
    
    // 회원탈퇴 비밀번호 확인 화면 구성 메소드
    private void setupDeleteAccountConfirmation(UserVO userInfo) {
        setPadding(new Insets(20));
        setAlignment(Pos.CENTER);
        setSpacing(20);

        // 타이틀
        Label titleLabel = new Label("회원탈퇴 확인");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");
        
        // 경고 메시지
        Label warningLabel = new Label("주의: 회원탈퇴 시 모든 정보가 영구적으로 삭제됩니다.");
        warningLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
        
        // 안내 메시지
        Label infoLabel = new Label("정확한 본인 확인을 위해 현재 비밀번호를 입력해주세요.");
        infoLabel.setStyle("-fx-font-size: 14px;");
        
        // 비밀번호 입력 영역
        VBox passwordBox = new VBox(15);
        passwordBox.setAlignment(Pos.CENTER);
        
        Label passwordLabel = new Label("현재 비밀번호:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("비밀번호를 입력하세요");
        passwordField.setPrefWidth(250);
        
        passwordBox.getChildren().addAll(passwordLabel, passwordField);
        
        // 버튼 영역
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        
        Button confirmButton = new Button("탈퇴하기");
        confirmButton.setStyle("-fx-text-fill: red;");
        Button cancelButton = new Button("취소");
        
        // 엔터 키 이벤트 설정
        passwordField.setOnAction(e -> confirmButton.fire());
        
        // 확인 버튼 이벤트
        confirmButton.setOnAction(e -> {
            String inputPassword = passwordField.getText();
            
            if (inputPassword.isEmpty()) {
                showAlert("입력 오류", "비밀번호를 입력해주세요.");
                return;
            }
            
            UserDAO userDAO = new UserDAO();
            // 입력한 비밀번호와 현재 사용자의 비밀번호 비교
            if (userDAO.selectUserLogin(userInfo.getUserId(), inputPassword)) {
                // 비밀번호 일치, 최종 확인 다이얼로그 표시
                showFinalConfirmationDialog(userInfo.getUserId());
            } else {
                // 비밀번호 불일치
                showAlert("인증 실패", "비밀번호가 일치하지 않습니다.");
            }
        });
        
        // 취소 버튼 이벤트
        cancelButton.setOnAction(e -> {
            // 사용자 정보 화면으로 돌아가기
            getChildren().clear();
            selectUserInfo();
        });
        
        buttonBox.getChildren().addAll(confirmButton, cancelButton);
        
        // 화면에 추가
        getChildren().addAll(titleLabel, warningLabel, infoLabel, passwordBox, buttonBox);
    }

    // 회원탈퇴 최종 확인 다이얼로그
    private void showFinalConfirmationDialog(String userId) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("탈퇴 확인");
        confirmAlert.setHeaderText("정말로 탈퇴하시겠습니까?");
        confirmAlert.setContentText("이 작업은 취소할 수 없으며, 모든 사용자 정보가 영구적으로 삭제됩니다.");
        
        // 버튼 텍스트 변경
        ((Button) confirmAlert.getDialogPane().lookupButton(ButtonType.OK)).setText("탈퇴");
        ((Button) confirmAlert.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("취소");
        
        confirmAlert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                // 진행 중 다이얼로그 표시
                Alert progressAlert = new Alert(Alert.AlertType.INFORMATION);
                progressAlert.setTitle("처리 중");
                progressAlert.setHeaderText(null);
                progressAlert.setContentText("회원 탈퇴를 처리 중입니다. 잠시만 기다려주세요...");
                progressAlert.show();
                
                // 백그라운드 스레드에서 실행
                new Thread(() -> {
                    // 사용자 삭제 실행
                    UserDAO userDAO = new UserDAO();
                    boolean result = userDAO.deleteUser(userId);
                    
                    // UI 스레드에서 결과 처리
                    javafx.application.Platform.runLater(() -> {
                        // 진행 중 다이얼로그 닫기
                        progressAlert.close();
                        
                        if (result) {
                            showAlert("탈퇴 완료", "회원탈퇴가 완료되었습니다.");
                            
                            // 로그아웃 처리
                            setLogIn(false);
                            setCurrentUserId(null);
                            
                            // 로그아웃 성공 콜백 실행
                            if (onLogoutSuccess != null) {
                                onLogoutSuccess.run();
                            }
                            
                            // 로그인 화면으로 전환
                            getChildren().clear();
                            setupLoginView();
                        } else {
                            showAlert("탈퇴 실패", "회원탈퇴 처리 중 오류가 발생했습니다. 다시 시도해주세요.");
                            
                            // 사용자 정보 화면으로 돌아가기
                            getChildren().clear();
                            selectUserInfo();
                        }
                    });
                }).start();
            } else {
                // 취소 시 사용자 정보 화면으로 돌아가기
                getChildren().clear();
                selectUserInfo();
            }
        });
    }
    
    // 정보 수정 화면 구성 메소드
    private void setupEditProfileForm(UserVO userInfo) {
        setPadding(new Insets(20));
        setAlignment(Pos.CENTER);
        setSpacing(20);

        // 타이틀
        Label titleLabel = new Label("회원 정보 수정");
        titleLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold;");

        // 수정 폼
        GridPane formGrid = new GridPane();
        formGrid.setHgap(15);
        formGrid.setVgap(15);
        formGrid.setAlignment(Pos.CENTER);

        // 중복 확인 상태 변수
        final boolean[] isNicknameAvailable = {true}; // 현재 닉네임은 이미 사용 중이므로 true로 초기화
        final boolean[] isIdAvailable = {true}; // 현재 아이디는 이미 사용 중이므로 true로 초기화
        final String[] originalId = {userInfo.getUserId()}; // 원래 아이디 저장
        final String[] originalNickname = {userInfo.getUserNickname()}; // 원래 닉네임 저장

        // 1. 닉네임 수정 필드
        Label nicknameLabel = new Label("닉네임:");
        TextField nicknameField = new TextField(userInfo.getUserNickname());
        
        // 닉네임 중복 확인 버튼
        Button checkNickButton = new Button("중복확인");
        HBox nickBox = new HBox(10, nicknameField, checkNickButton);
        
        // 닉네임 중복 확인 버튼 이벤트
        checkNickButton.setOnAction(event -> {
            String newNickname = nicknameField.getText().trim();
            
            // 입력 검증
            if (newNickname.isEmpty()) {
                showAlert("입력 오류", "닉네임을 입력해주세요.");
                isNicknameAvailable[0] = false;
                return;
            }
            
            // 현재 닉네임과 동일하면 중복 체크 불필요
            if (newNickname.equals(originalNickname[0])) {
                showAlert("확인", "현재 사용 중인 닉네임입니다.");
                isNicknameAvailable[0] = true;
                return;
            }
            
            // 중복 검사 - DAO를 통해 실제 데이터베이스 체크
            UserDAO userDAO = new UserDAO();
            if (userDAO.IsUserNicknameDupe(newNickname)) {
                showAlert("확인", "사용 가능한 닉네임입니다.");
                isNicknameAvailable[0] = true;
            } else {
                showAlert("중복 오류", "이미 사용 중인 닉네임입니다. 다른 닉네임을 입력해주세요.");
                isNicknameAvailable[0] = false;
            }
        });
        
        // 닉네임 필드 변경 시 체크 상태 초기화
        nicknameField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(originalNickname[0])) {
                isNicknameAvailable[0] = false;
            } else {
                isNicknameAvailable[0] = true;
            }
        });
        
        // 2. 아이디 수정 필드
        Label idLabel = new Label("아이디:");
        TextField idField = new TextField(userInfo.getUserId());
        
        // 아이디 중복 확인 버튼
        Button checkIdButton = new Button("중복확인");
        HBox idBox = new HBox(10, idField, checkIdButton);
        
        // 아이디 중복 확인 버튼 이벤트
        checkIdButton.setOnAction(event -> {
            String newId = idField.getText().trim();
            
            // 입력 검증
            if (newId.isEmpty()) {
                showAlert("입력 오류", "아이디를 입력해주세요.");
                isIdAvailable[0] = false;
                return;
            }
            
            // 현재 아이디와 동일하면 중복 체크 불필요
            if (newId.equals(originalId[0])) {
                showAlert("확인", "현재 사용 중인 아이디입니다.");
                isIdAvailable[0] = true;
                return;
            }
            
            // 중복 검사
            UserDAO userDAO = new UserDAO();
            if (userDAO.isUserIdDupe(newId)) {
                showAlert("확인", "사용 가능한 아이디입니다.");
                isIdAvailable[0] = true;
            } else {
                showAlert("중복 오류", "이미 사용 중인 아이디입니다. 다른 아이디를 입력해주세요.");
                isIdAvailable[0] = false;
            }
        });
        
        // 아이디 필드 변경 시 체크 상태 초기화
        idField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(originalId[0])) {
                isIdAvailable[0] = false;
            } else {
                isIdAvailable[0] = true;
            }
        });

        // 3. 이메일 필드 - 이미 있으면 읽기 전용으로 표시, 없으면 입력 가능
        Label emailLabel = new Label("이메일:");
        
        // 4. 새 비밀번호 필드
        Label passwordLabel = new Label("새 비밀번호:");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("변경할 비밀번호 입력");

        // 5. 새 비밀번호 확인 필드
        Label passwordConfirmLabel = new Label("비밀번호 확인:");
        PasswordField passwordConfirmField = new PasswordField();
        passwordConfirmField.setPromptText("비밀번호 확인");

        // 버튼 영역
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);

        Button saveButton = new Button("저장");
        Button cancelButton = new Button("취소");

        // 이메일 필드 처리 - 이미 있으면 읽기 전용, 없으면 입력 가능
        if (userInfo.getUserEmail() != null && !userInfo.getUserEmail().isEmpty()) {
            // 이메일이 이미 있는 경우 - 읽기 전용 레이블로 표시
            Label emailValueLabel = new Label(userInfo.getUserEmail());
            emailValueLabel.setStyle("-fx-font-weight: bold;");
            
            // 그리드에 정보 추가 - 순서 변경됨
            formGrid.add(nicknameLabel, 0, 0);
            formGrid.add(nickBox, 1, 0);
            formGrid.add(idLabel, 0, 1);
            formGrid.add(idBox, 1, 1);
            formGrid.add(emailLabel, 0, 2);
            formGrid.add(emailValueLabel, 1, 2);
            formGrid.add(passwordLabel, 0, 3);
            formGrid.add(passwordField, 1, 3);
            formGrid.add(passwordConfirmLabel, 0, 4);
            formGrid.add(passwordConfirmField, 1, 4);
            
            // 이메일이 이미 있는 경우, 이메일 수정 없이 다른 정보만 변경 가능한 이벤트 핸들러 사용
            saveButton.setOnAction(e -> {
                UserDAO userDAO = new UserDAO();
                boolean isUpdated = false;
                boolean hasErrors = false;
                
                // 닉네임 업데이트 시도
                String newNickname = nicknameField.getText().trim();
                if (!newNickname.isEmpty() && !newNickname.equals(userInfo.getUserNickname())) {
                    // 중복 확인했는지 체크
                    if (!isNicknameAvailable[0]) {
                        hasErrors = true;
                        showAlert("닉네임 변경 오류", "닉네임 중복 확인을 먼저 해주세요.");
                    } else {
                        // 닉네임 업데이트 (현재 아이디 사용)
                        if (userDAO.updateUserNickname(userInfo.getUserId(), newNickname)) {
                            isUpdated = true;
                        } else {
                            hasErrors = true;
                            showAlert("닉네임 변경 실패", "닉네임 변경 중 오류가 발생했습니다.");
                        }
                    }
                }
                
                // 아이디 업데이트 시도
                String newId = idField.getText().trim();
                if (!newId.isEmpty() && !newId.equals(originalId[0])) {
                    // 중복 확인했는지 체크
                    if (!isIdAvailable[0]) {
                        hasErrors = true;
                        showAlert("아이디 변경 오류", "아이디 중복 확인을 먼저 해주세요.");
                    } else {
                        // 아이디 업데이트
                        if (userDAO.updateUserId(originalId[0], newId)) {
                            isUpdated = true;
                            // 현재 세션의 사용자 ID 업데이트
                            setCurrentUserId(newId);
                        } else {
                            hasErrors = true;
                            showAlert("아이디 변경 실패", "아이디 변경 중 오류가 발생했습니다.");
                        }
                    }
                }

                // 비밀번호 업데이트 시도
                String newPassword = passwordField.getText();
                String confirmPassword = passwordConfirmField.getText();
                
                if (!newPassword.isEmpty()) {
                    if (newPassword.equals(confirmPassword)) {
                        String currentId = getCurrentUserId(); // 현재 ID는 업데이트 됐을 수 있음
                        if (userDAO.updateUserPassword(currentId, newPassword)) {
                            isUpdated = true;
                        } else {
                            hasErrors = true;
                            showAlert("비밀번호 변경 실패", "비밀번호 변경 중 오류가 발생했습니다.");
                        }
                    } else {
                        hasErrors = true;
                        showAlert("비밀번호 불일치", "입력한 비밀번호가 일치하지 않습니다.");
                    }
                }

                // 업데이트 결과 처리
                if (isUpdated && !hasErrors) {
                    showAlert("업데이트 성공", "회원 정보가 성공적으로 업데이트되었습니다.");
                    
                    // 유저 정보 갱신된 화면으로 돌아가기
                    getChildren().clear();
                    selectUserInfo();
                } else if (!isUpdated && !hasErrors) {
                    showAlert("변경 사항 없음", "변경된 정보가 없습니다.");
                    
                    // 유저 정보 화면으로 돌아가기
                    getChildren().clear();
                    selectUserInfo();
                }
            });
            
        } else {
            // 이메일이 없는 경우 - 입력 필드 표시
            TextField emailField = new TextField();
            emailField.setPromptText("이메일을 입력하세요 (선택사항)");
            
            // 그리드에 정보 추가 - 순서 변경됨
            formGrid.add(nicknameLabel, 0, 0);
            formGrid.add(nickBox, 1, 0);
            formGrid.add(idLabel, 0, 1);
            formGrid.add(idBox, 1, 1);
            formGrid.add(emailLabel, 0, 2);
            formGrid.add(emailField, 1, 2);
            formGrid.add(passwordLabel, 0, 3);
            formGrid.add(passwordField, 1, 3);
            formGrid.add(passwordConfirmLabel, 0, 4);
            formGrid.add(passwordConfirmField, 1, 4);
            
            // 이메일이 없는 경우, 이메일 추가 기능이 있는 이벤트 핸들러 사용
            saveButton.setOnAction(e -> {
                UserDAO userDAO = new UserDAO();
                // 람다 내에서 수정 가능하도록 배열로 변경
                final boolean[] isUpdated = {false};
                final boolean[] hasErrors = {false};
                
                // 닉네임 업데이트 시도
                String newNickname = nicknameField.getText().trim();
                if (!newNickname.isEmpty() && !newNickname.equals(userInfo.getUserNickname())) {
                    // 중복 확인했는지 체크
                    if (!isNicknameAvailable[0]) {
                        hasErrors[0] = true;
                        showAlert("닉네임 변경 오류", "닉네임 중복 확인을 먼저 해주세요.");
                    } else {
                        // 닉네임 업데이트 (현재 아이디 사용)
                        if (userDAO.updateUserNickname(userInfo.getUserId(), newNickname)) {
                            isUpdated[0] = true;
                        } else {
                            hasErrors[0] = true;
                            showAlert("닉네임 변경 실패", "닉네임 변경 중 오류가 발생했습니다.");
                        }
                    }
                }
                
                // 아이디 업데이트 시도
                String newId = idField.getText().trim();
                if (!newId.isEmpty() && !newId.equals(originalId[0])) {
                    // 중복 확인했는지 체크
                    if (!isIdAvailable[0]) {
                        hasErrors[0] = true;
                        showAlert("아이디 변경 오류", "아이디 중복 확인을 먼저 해주세요.");
                    } else {
                        // 아이디 업데이트
                        if (userDAO.updateUserId(originalId[0], newId)) {
                            isUpdated[0] = true;
                            // 현재 세션의 사용자 ID 업데이트
                            setCurrentUserId(newId);
                        } else {
                            hasErrors[0] = true;
                            showAlert("아이디 변경 실패", "아이디 변경 중 오류가 발생했습니다.");
                        }
                    }
                }
                    
                // 이메일 업데이트 시도 - 이메일이 없는 경우에만 실행
                String newEmail = emailField.getText().trim();
                if (!newEmail.isEmpty()) {
                    // 이메일 형식 검증
                    String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
                    if (!newEmail.matches(emailRegex)) {
                        hasErrors[0] = true;
                        showAlert("이메일 형식 오류", "올바른 이메일 형식이 아닙니다. (예: example@domain.com)");
                    } else {
                        // 이메일 중복 확인
                        if (!userDAO.isUserEmailDupe(newEmail)) {
                            hasErrors[0] = true;
                            showAlert("이메일 중복", "이미 사용 중인 이메일입니다.");
                        } else {
                            // 이메일 추가 전 확인 다이얼로그 표시
                            Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
                            confirmAlert.setTitle("이메일 추가 확인");
                            confirmAlert.setHeaderText("한 번 추가한 이메일은 다시는 변경 할 수 없습니다.");
                            confirmAlert.setContentText("이메일 (" + newEmail + ")을(를) 추가하시겠습니까?");
                            
                            // 버튼 텍스트 변경
                            ((Button) confirmAlert.getDialogPane().lookupButton(ButtonType.OK)).setText("추가");
                            ((Button) confirmAlert.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("취소");
                            
                            final String emailToAdd = newEmail;
                            
                            confirmAlert.showAndWait().ifPresent(response -> {
                                if (response == ButtonType.OK) {
                                    // 사용자가 확인한 경우에만 이메일 업데이트 진행
                                    String currentId = getCurrentUserId(); // 현재 ID는 업데이트 됐을 수 있음
                                    if (userDAO.updateUserEmail(currentId, emailToAdd)) {
                                        isUpdated[0] = true;
                                        showAlert("이메일 추가 완료", "이메일이 추가되었습니다.");
                                    } else {
                                        hasErrors[0] = true;
                                        showAlert("이메일 추가 실패", "이메일 추가 중 오류가 발생했습니다.");
                                    }
                                }
                                // 취소한 경우 아무것도 하지 않음
                            });
                        }
                    }
                }

                // 비밀번호 업데이트 시도
                String newPassword = passwordField.getText();
                String confirmPassword = passwordConfirmField.getText();
                
                if (!newPassword.isEmpty()) {
                    if (newPassword.equals(confirmPassword)) {
                        String currentId = getCurrentUserId(); // 현재 ID는 업데이트 됐을 수 있음
                        if (userDAO.updateUserPassword(currentId, newPassword)) {
                            isUpdated[0] = true;
                        } else {
                            hasErrors[0] = true;
                            showAlert("비밀번호 변경 실패", "비밀번호 변경 중 오류가 발생했습니다.");
                        }
                    } else {
                        hasErrors[0] = true;
                        showAlert("비밀번호 불일치", "입력한 비밀번호가 일치하지 않습니다.");
                    }
                }

                // 업데이트 결과 처리
                if (isUpdated[0] && !hasErrors[0]) {
                    showAlert("업데이트 성공", "회원 정보가 성공적으로 업데이트되었습니다.");
                    
                    // 유저 정보 갱신된 화면으로 돌아가기
                    getChildren().clear();
                    selectUserInfo();
                } else if (!isUpdated[0] && !hasErrors[0]) {
                    showAlert("변경 사항 없음", "변경된 정보가 없습니다.");
                    
                    // 유저 정보 화면으로 돌아가기
                    getChildren().clear();
                    selectUserInfo();
                }
            });
        }

        // 취소 버튼 이벤트
        cancelButton.setOnAction(e -> {
            // 유저 정보 화면으로 돌아가기
            getChildren().clear();
            selectUserInfo();
        });

        buttonBox.getChildren().addAll(saveButton, cancelButton);
        getChildren().addAll(titleLabel, formGrid, buttonBox);
    }
    
    // 로그인 상태 설정 메서드
    public static void setLogIn(boolean logIn) {
        isLogIn = logIn;
    }
}