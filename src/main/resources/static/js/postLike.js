

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
                //$(".like-container").load(contextPath + " .like-container");

                console.log(result);
                let like = result.isLike;
                let likeCount = result.likeCount;
                let postId = result.postId;
                let newContainer = `<div class="like-container">
                                        <div class="d-flex justify-content-center mt-5">
                                            <div class="likeButtonGroup">
                                                <input type="hidden" id="like_check" value="${like}">
                                                <button type="button" class="btn btn-outline-primary" onclick="likePost(${postId});">
                                                    추천
                                                    <i class="bi bi-hand-thumbs-up"></i>
                                                </button>
                                            </div>
                                        </div>

                                        <div class="d-flex justify-content-center mt-3 mb-5">
                                            <span class="like_count"
                                                style="color:#1171cc;font:bold 14px georgia,sans-serif;border:1px solid #ccc;border-radius:3px;white-space:nowrap;padding:3px 7px 0;height:28px">
                                            ${likeCount}</span>
                                        </div>
                                    </div>`;

                $(".like-container").replaceWith(newContainer);
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
                //$(".like-container").load(contextPath + " .like-container");

                console.log(result);
                let like = result.isLike;
                let likeCount = result.likeCount;
                let postId = result.postId;
                let newContainer = `<div class="like-container">
                                        <div class="d-flex justify-content-center mt-5">
                                            <div class="likeButtonGroup">
                                                <input type="hidden" id="like_check" value="${like}">

                                                <button type="button" class="btn btn-primary" onclick="likePost(${postId});">
                                                    추천
                                                    <i class="bi bi-hand-thumbs-up-fill"></i>
                                                </button>
                                            </div>
                                        </div>

                                        <div class="d-flex justify-content-center mt-3 mb-5">
                                            <span class="like_count"
                                                style="color:#1171cc;font:bold 14px georgia,sans-serif;border:1px solid #ccc;border-radius:3px;white-space:nowrap;padding:3px 7px 0;height:28px">
                                            ${likeCount}</span>
                                        </div>
                                    </div>`;

                $(".like-container").replaceWith(newContainer);
            }).fail(function (error) {
                const result = error.responseJSON;
                alert(JSON.stringify(result));
            })
        }
    }

}