
//댓글 등록
function addComment(form, event) {
    event.preventDefault();
    let data = form.content.value;
    let postId = form.postId.value;
    console.log("data: " + data);
    console.log("postId: " + postId);

    if(!data || data.trim() === "") {
        alert("공백 또는 입력하지 않은 부분이 있습니다");
        return false;
    }

    $.ajax({
        type: 'post',
        url: '/api/post/' + postId + '/comment/add',
//        dataType: 'JSON',
        contentType: 'application/json; charset=utf-8',
        data: JSON.stringify({content:data})
    }).done(function(result) {
        console.log(result);
        window.location.reload();
    }).fail(function(error) {
        console.log(error);
    });

}






//댓글 수정/삭제 폼
function editComment(commentId) {
    console.log("commentId:" + commentId);
    const form = document.getElementById('commentUpdateForm' + commentId);
    const comment = document.getElementById('commentContent' + commentId);
    // 숨기기 (display: none)
    if(form.style.display !== 'none') {
        form.style.display = 'none';
        comment.style.display = 'block';
    }
    // 보이기 (display: block)
    else {
        form.style.display = 'block';
        comment.style.display = 'none';
    }


}

//댓글 수정 저장
function editCommentSave(form) {
    const commentId = form.commentId.value;
    const postId = form.postId.value;
    const newContent = form.commentContent.value;

    const oldContent = document.getElementById('commentContent' + commentId).innerText;
//    const oldContent = document.getElementById('commentContent').textContent;

//    console.log("commentId: " + commentId);
//    console.log("postId: " + postId);
//    console.log("oldContent: " + oldContent);
//    console.log("newContent: " + newContent);

    if(!newContent || newContent.trim() === "") {
        alert("공백 또는 입력하지 않은 부분이 있습니다");
        return false;
    }

    if (newContent !== oldContent) {
//        $.post(`/board/${id}/comment/${commentId}/update`, {content: newContent}, function(data) {
//            window.location.href = data;
//        });
        console.log("기존댓글과 다름");

        $.ajax({
            type: 'PUT',
            url: '/api/post/' + postId + '/comment/' + commentId + '/edit',
            dataType: 'JSON',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify({content: newContent})
        }).done(function(result) {
            console.log(result);
            window.location.reload();
        }).fail(function(error) {
            console.log(error);
        });

    }else {
        console.log("기존댓글과 같음");
        return false;
    }
}

//댓글 삭제
function deleteComment(commentId, postId) {

    const check = confirm("댓글을 삭제하시겠습니까?");
    if(check) {
        $.ajax({
            type: 'delete',
            url: '/api/post/' + postId + '/comment/' + commentId + '/delete',
            dataType: 'JSON'
        }).done(function() {
            window.location.reload();
        }).fail(function(error) {
            console.log(error);
        });
    }
}