

//게시물 추천, 추천취소
function likePost(postId) {

    let origin = window.location.origin;
    let pathname = window.location.pathname;
    let contextPath = origin + pathname;

    //현재 로그인한 유저가 해당 게시물을 좋아요 했다면 likeVal = true
    const likeVal = $('#like_check').val();

    if(likeVal === 'true') {
        const con_check = confirm("현재 게시물 추천을 취소하시겠습니까?");
        if (con_check) {
            $.ajax({
                type: 'POST',
                url: '/post/like/' + postId,
                contentType: 'application/json; charset=utf-8'
            }).done(function (result) {
                //클릭 해제된 이미지로 바꾸기 + 추천 숫자 갱신
                $(".like-container").load(contextPath + " .like-container");
            }).fail(function (error) {
                const result = error.responseJSON;
                alert(JSON.stringify(result));
            })
        }

    } else if(likeVal === 'false') {

        const con_check = confirm("현재 게시물을 추천하시겠습니까?");
        if (con_check) {
            $.ajax({
                type: 'POST',
                url: '/post/like/' + postId,
                contentType: 'application/json; charset=utf-8'
            }).done(function (result) {
                //클릭된 이미지로 바꾸기 + 추천 숫자 갱신
                $(".like-container").load(contextPath + " .like-container");
            }).fail(function (error) {
                const result = error.responseJSON;
                alert(JSON.stringify(result));
            })
        }
    }

}