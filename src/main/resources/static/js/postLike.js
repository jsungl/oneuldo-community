

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
                contentType: 'application/json; charset=utf-8',
                dataType:'json'
            }).done(function (result) {
                //클릭 해제된 이미지로 바꾸기 + 추천 숫자 갱신

                let like = result.isLike;
                let likeCount = result.likeCount;
                let postId = result.postId;

                let newLikeButton = `<div class="likeButtonGroup">
                                        <input type="hidden" id="like_check" value="${like}">

                                        <button type="button" class="btn btn-outline-primary" onclick="likePost(${postId});">
                                            추천
                                            <i class="bi bi-hand-thumbs-up"></i>
                                        </button>
                                    </div>`;

                let newLikeCount = `<div class="like_count d-flex justify-content-center mt-3">
                                    <span style="color:#1171cc;font:bold 14px georgia,sans-serif;border:1px solid #ccc;border-radius:3px;white-space:nowrap;padding:3px 7px 0;height:28px">
                                    ${likeCount}</span>
                                    </div>`;

                $(".likeButtonGroup").replaceWith(newLikeButton);
                $(".like_count").replaceWith(newLikeCount);

            }).fail(function (error) {
                const result = error.responseJSON;
                alert(JSON.stringify(result.msg));
                alert("추천을 취소할 수 업습니다.");
            })
        }

    } else if(likeVal === 'false') {

        const con_check = confirm("현재 게시물을 추천하시겠습니까?");
        if (con_check) {
            $.ajax({
                type: 'POST',
                url: '/post/like/' + postId,
                contentType: 'application/json; charset=utf-8',
                dataType:'json' //서버로부터의 응답결과의 형태가 json이라면 이를 javascript object로 변경
            }).done(function (result) {
                //클릭된 이미지로 바꾸기 + 추천 숫자 갱신

                let like = result.isLike;
                let likeCount = result.likeCount;
                let postId = result.postId;

                let newLikeButton = `<div class="likeButtonGroup">
                                        <input type="hidden" id="like_check" value="${like}">

                                        <button type="button" class="btn btn-primary" onclick="likePost(${postId});">
                                            추천
                                            <i class="bi bi-hand-thumbs-up-fill"></i>
                                        </button>
                                    </div>`;

                let newLikeCount = `<div class="like_count d-flex justify-content-center mt-3">
                                    <span style="color:#1171cc;font:bold 14px georgia,sans-serif;border:1px solid #ccc;border-radius:3px;white-space:nowrap;padding:3px 7px 0;height:28px">
                                    ${likeCount}</span>
                                    </div>`;

                $(".likeButtonGroup").replaceWith(newLikeButton);
                $(".like_count").replaceWith(newLikeCount);

            }).fail(function (error) {
                const result = error.responseJSON;
                //{msg: '존재하지 않는 회원입니다.'}
                alert(JSON.stringify(result.msg));
                alert("추천할 수 없습니다.");
            })
        }
    }

}