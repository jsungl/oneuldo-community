
//댓글 등록
function addComment(form, event) {
    event.preventDefault();
    let data = form.content.value;
    let postId = form.postId.value;

    let pathname = window.location.pathname;
    let origin = window.location.origin;
    let contextPath = origin + pathname + "?postId=" + postId;

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
        //window.location.reload();
        $(".comment-list").load(contextPath + " .comment-list");
        $("#commentTextarea").val('');
    }).fail(function(error) {
        console.log(error);
    });

}


//댓글 수정/삭제 폼
function editComment(commentId) {
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

    let origin = window.location.origin;
    let pathname = window.location.pathname;
    let contextPath = origin + pathname + "?postId=" + postId;

    if(!newContent || newContent.trim() === "") {
        alert("공백 또는 입력하지 않은 부분이 있습니다");
        return false;
    }

    if (newContent !== oldContent) {
//        $.post(`/board/${id}/comment/${commentId}/update`, {content: newContent}, function(data) {
//            window.location.href = data;
//        });

        $.ajax({
            type: 'PUT',
            url: '/api/post/' + postId + '/comment/' + commentId + '/edit',
            //dataType: 'JSON',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify({content: newContent})
        }).done(function(result) {
            console.log("updated commentId = " + result);
            //window.location.reload();
            $(".comment-list").load(contextPath + " .comment-list");
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

    let origin = window.location.origin;
    let pathname = window.location.pathname;
    let contextPath = origin + pathname + "?postId=" + postId;

    const check = confirm("댓글을 삭제하시겠습니까?");
    if(check) {
        $.ajax({
            type: 'delete',
            url: '/api/post/' + postId + '/comment/' + commentId + '/delete',
            dataType: 'JSON'
        }).done(function(result) {
            console.log("deleted commentId = " + result);
            //window.location.reload();
            $(".comment-list").load(contextPath + " .comment-list");
        }).fail(function(error) {
            console.log(error);
        });
    }
}


//답글 폼
function replyComment(commentId) {
    const form = document.getElementById('replyCommentForm' + commentId);

    // 숨기기 (display: none)
    if(form.style.display !== 'none') {
        form.style.display = 'none';
    }
    // 보이기 (display: block)
    else {
        form.style.display = 'block';
    }

}


//답글 저장
function replyCommentSave(form) {

    let parentId = form.parentId.value;
    let postId = form.postId.value;
    let data = form.replyCommentContent.value;
    let depth = form.depth.value;

    let origin = window.location.origin;
    let pathname = window.location.pathname;
    let contextPath = origin + pathname + "?postId=" + postId;

    if(!data || data.trim() === "") {
        alert("공백 또는 입력하지 않은 부분이 있습니다");
        return false;
    }

    $.ajax({
        type: 'post',
        url: '/api/post/' + postId + '/comment/add',
        contentType: 'application/json; charset=utf-8',
        data: JSON.stringify({content:data, parentId:parentId, depth:depth})
        //dataType: 'JSON'
    }).done(function(result) {
        console.log(result);
        //window.location.reload();
        $(".comment-list").load(contextPath + " .comment-list");
    }).fail(function(error) {
        console.log(error);
    });

}