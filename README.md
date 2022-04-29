# Camptact 

> 가천대학교 교내 사용자를 위한 안드로이드 랜덤채팅 어플리케이션입니다. 가천대학교 재학생임이 증명된 사용자 중 랜덤으로 매칭하여 채팅을 나눌 수 있습니다. 뿐만 아니라 게시판 기능이 탑재돼있어 정보글을 공유 할 수도 있습니다.

<br>

## Table of Contents
- **사용자 인증**
	- 가천대학교 이메일을 통해 가천대학교 재학생임을 인증 후 사용가능합니다.

- **게시판**
	- DB에 저장된 회원정보를 기반으로 ID/PW 찾기 가능

- **광고**
	- 구글 애드센스를 이용한 수익창출

- **채팅**
	- 다른 사용자중 랜덤으로 매칭하여 1대1 채팅 가능

- **사진전송 & 신고기능**
	- 채팅 중 사진전송이 가능하며 부적절한 채팅시 신고 가능

- **회원탈퇴**
	- 어플 불필요시 회원탈퇴 가능

<br>

## 사용 예시
**[ 초기 사용안내 화면 ]**
<p align="left">
<img src="https://github.com/idkim97/idkim97.github.io/blob/master/img/camtact2.png?raw=true">
</p>
<br>

**[ 사용자 인증 화면 ]**
<p align="left">
<img src="https://github.com/idkim97/idkim97.github.io/blob/master/img/camtact1.png?raw=true">
</p>
<br>

**[ 이미 가입된 계정일 시 ]**
<p align="left">
<img src="https://github.com/idkim97/idkim97.github.io/blob/master/img/camtact3.png?raw=true">
</p>
<br>

**[ 메인화면 ]**
<p align="left">
<img src="https://github.com/idkim97/idkim97.github.io/blob/master/img/camtact4.png?raw=true">
</p>
<br>


**[ 랜덤채팅 매칭 ]**
<p align="left">
<img src="https://github.com/idkim97/idkim97.github.io/blob/master/img/camtact5.png?raw=true">
</p>
<br>

**[ 사진 전송 & 신고하기 ]**
<p align="left">
<img src="https://github.com/idkim97/idkim97.github.io/blob/master/img/camtact6.png?raw=true">
</p>
<br>

**[ 채팅목록 ]**
<p align="left">
<img src="https://github.com/idkim97/idkim97.github.io/blob/master/img/camtact7.png?raw=true">
</p>
<br>

**[ 게시판 글쓰기 ]**
<p align="left">
<img src="https://github.com/idkim97/idkim97.github.io/blob/master/img/camtact8.png?raw=true">
</p>
<br>

**[ 설정 ]**
<p align="left">
<img src="https://github.com/idkim97/idkim97.github.io/blob/master/img/camtact9.png?raw=true">
</p>
<br>


<br>

## 설치 방법
**개발환경**

> Android Studio  
> JAVA 버전 : JDK 1.8.0_332  
> Server & DB : Firebase Realtime Database  
> 테스트 환경 : Samsung GalaxyS10  


<br>

## 설명
- **사용자 인증 :** 가천대학교 이메일을 가진 사람만이 회원가입이 가능하며 이메일 입력시 해당 메일주소로 본인인증 메일을 전송해 가입을 할 수 있습니다.  
- **랜덤채팅** :  어플 실시간 사용자 중 랜덤으로 매칭을 해 채팅할 수 있습니다. 
- **사진전송 & 신고** : 채팅을 나누며 사진전송이 가능하고 부적절 유저를 신고할 수 있습니다.  
- **DB** : Firebase와 연동하여 유저의 정보를 저장합니다. 추가적으로 신고된 유저를 DB를 통해 확인하여 관리자가 이를 확인 할 수 있습니다.  
- **게시판** : 자유롭게 게시글을 남겨 정보를 공유할 수 있습니다.  

<br>


## 사용자 인증
<p align="left">
<img src="https://github.com/idkim97/idkim97.github.io/blob/master/img/camtact11.png?raw=true">
</p>
<br>

가천대학교 이메일 입력시 해당 메일 주소로 본인인증 메일이 전송됩니다. 
**캠택트 로그인** 텍스트 클릭시 해당 메일 주소가 Firebase로 전달되고 가천대학교 이메일 주소와 일치 할 시 본인인증이 완료됩니다.

아래 JAVA파일에서 해당 코드를 확인할 수 있습니다.
    [main/java/io.camtact.android/presenter/AuthPresenter.java](https://github.com/idkim97/Camptact/blob/main/app/src/main/java/io/camtact/android/presenter/AuthPresenter.java)


<br>


## 랜덤채팅

**[랜덤채팅 함수]**
```java
@Override  
public void searchRandomUser() {  
    // Bind to LocalService  
  if(!isBound) {  
        Intent intent = new Intent(context, MatchService.class);  
  context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);  
  }  
  
    if(!isThreadRunning) {  
        runTimeCheck();  
  }  
    matchModel.deleteOldChatRoom();  
}
```

인증이 완료된 어플 사용자 중 랜덤으로 함수를 돌려 채팅을 매칭시켜줍니다. 

<p align="left">
<img src="https://github.com/idkim97/idkim97.github.io/blob/master/img/camtact10.png?raw=true">
</p>
<br>

"랜덤채팅 시작하기" 버튼을 누르면 매칭이 시작되며 로딩창이 뜨고 매칭 완료시 채팅방으로 자동 이동합니다. 



