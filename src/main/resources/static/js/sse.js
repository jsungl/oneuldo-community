
var sse = null;

function connect() {

    sse = new EventSource("/notifications/subscribe");

    sse.addEventListener('connect', (event) => {
    	const { data: receivedConnectData } = event;
    	console.log('connect event data: ', receivedConnectData);  // "connected!"
    });

    sse.addEventListener('newComment', (event) => {
        const receivedData = JSON.parse(event.data);
        //console.log('receivedData: ', receivedData);
        let sender = receivedData.sender;
        let content = receivedData.content;
        let dto = receivedData.dto;
        let count = receivedData.count;

        content = sender + "님 댓글: \"" + content + "\"";
        addItemToList(dto,count);
        showToast(content);

    });

}

function showToast(content) {
    const toast = $("#liveToast");
    toast.find('.toast-header #notification_title').text('새 알림');
    toast.find('.toast-body').text(content);
    new bootstrap.Toast(toast).show();
}

function addItemToList(dto, count) {
    //const ul = $(".oc_notification");
    var ul = document.getElementById("oc_notification");
    var mobileUlTag = document.getElementById("notify_mobile_toggle");

    let span = document.getElementById("notificationCount");
    let mobileSpanTag = document.getElementById("notify_mobile_count");


    // 새로운 리스트 아이템 생성
    var li = document.createElement('li');
    let a = document.createElement('a');

    //  /post/notify?notificationId=8&postId=20&commentId=22#comment_22
    let url = dto.url;
    a.setAttribute('class', 'dropdown-item');
    a.setAttribute('href', 'javascript:void(0);');
    a.setAttribute('onclick', 'navigateTo("' + url + '");');

    let receiver = dto.receiver;
    let sender = dto.sender;
    let content = dto.content;
    let createdDate = dto.createdDate;

    let span1 = document.createElement('span');
    span1.textContent = sender + " 님 댓글: ";
    let span2 = document.createElement('span');
    // 최대 길이 설정
    let maxLength = 10;
    // 텍스트를 자르고 말줄임표를 추가합니다.
    let truncatedText = content.length > maxLength ? content.substring(0, maxLength) + '...' : content;

    span2.textContent = "\"" + truncatedText + "\" ";
    let span3 = document.createElement('span');
    span3.textContent = "방금";

    a.appendChild(span1);
    a.appendChild(span2);
    a.appendChild(span3);

    li.appendChild(a);

    // 텍스트 노드를 리스트 아이템에 추가
    if(span.textContent === "" && count === 1) {
        let liToRemove = ul.getElementsByTagName("li")[0];
        ul.removeChild(liToRemove);
        ul.appendChild(li);

        //모바일
        let mLiToRemove = mobileUlTag.getElementsByTagName("li")[0];
        mobileUlTag.removeChild(mLiToRemove);
        mobileUlTag.appendChild(li);
    } else {
        let firstChild = ul.firstChild;
        ul.insertBefore(li, firstChild);

        //모바일
        let mFirstChild = mobileUlTag.firstChild;
        mobileUlTag.insertBefore(li, mFirstChild);
    }

    span.textContent = count;
    //모바일
    mobileSpanTag.textContent = count;
}

function getTimeAgo(timeString) {
    var currentTime = new Date();
    var timeToCompare = new Date(timeString);

    var timeDifference = currentTime - timeToCompare;
    var secondsDifference = Math.floor(timeDifference / 1000);
    var minutesDifference = Math.floor(secondsDifference / 60);
    var hoursDifference = Math.floor(minutesDifference / 60);
    var daysDifference = Math.floor(hoursDifference / 24);

    if (secondsDifference < 60) {
        return secondsDifference + "초 전";
    } else if (minutesDifference < 60) {
        return minutesDifference + "분 전";
    } else if (hoursDifference < 24) {
        return hoursDifference + "시간 전";
    } else {
        return daysDifference + "일 전";
    }
}


$(function () {
    connect();
});