
//이메일 인증코드 전송
function sendMailCode(form) {

    let data = form.email.value;

//    console.log(data);

    $.ajax({
        type: 'post',
        url: '/api/member/verifyMail',
        contentType: 'application/json; charset=utf-8',
        data: JSON.stringify({email:data})
    }).done(function(result) {
        //console.log(result);
        $("#memberEmail").replaceWith(result);
    }).fail(function(error) {
        console.log(error);
    });

}

//인증코드 확인
function verifyCode(form) {

    let data = form.emailVerify.value;

    if(!data || data.trim() === "") {
        alert("인증코드를 입력해주세요");
        return false;
    }

    console.log(data);

    $.ajax({
        type: 'post',
        url: '/api/member/verifyCode',
        contentType: 'application/json; charset=utf-8',
        data: JSON.stringify({code:data})
    }).done(function(result) {
        //console.log(result);
        // $("#validationEmailFeedback").replaceWith(result);
        $("#validationEmail").replaceWith(result);
    }).fail(function(error) {
        console.log(error);
    });

}


function addMember(form, event) {

    event.preventDefault();

    if(form.result != undefined) {
        let result = form.result.value;

        console.log("result=" + result);
    }




}