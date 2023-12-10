
//댓글,답글 등록
function addComment(form, event) {
    event.preventDefault();

    let data = form.content.value;
    let postId = form.postId.value;
    let contextPath = form.contextPath.value;
    let parentId;
    let comment;

    if(!data || data.trim() === "") {
        alert("공백 또는 입력하지 않은 부분이 있습니다");
        return false;
    }

    if(form.parentId === undefined) {
        comment = {content:data};
    } else {
        parentId = form.parentId.value;
        comment = {content:data, parentId:parentId};
    }


    $.ajax({
        type: 'post',
        url: '/api/post/' + postId + '/comment/add',
//        dataType: 'JSON',
        contentType: 'application/json; charset=utf-8',
        data: JSON.stringify(comment)
    }).done(function(result) {
        $("#commentTextarea").val('');
        contextPath = contextPath + "?commentId=" + result + "#comment_" + result;

        $(".comment-list").load(contextPath + " .comment-list", function() {
            //댓글 위치
            const myElement = document.getElementById('comment_'+result);
            const verticalScrollPosition = setElementScrollPosition(myElement);

            // 수직 스크롤 위치 설정하기
            window.scrollTo({
              top: verticalScrollPosition,
              behavior: 'smooth' // 부드러운 스크롤(선택 사항)
            });
        });


    }).fail(function(error) {
        const result = error.responseJSON;
//        for(var key in result) {
//            alert("댓글을 등록할 수 없습니다. \n[에러메세지:" + result[key] + "]");
//        }

        alert("댓글을 등록할 수 없습니다.");
        window.location.reload();

    });

}


//댓글 수정/삭제 폼
function editComment(commentId) {
    const form = document.getElementById('commentUpdateForm' + commentId);
    const comment = document.getElementById('commentContent' + commentId);
    const isModified = document.getElementById('commentIsModified' + commentId);


    // 숨기기 (display: none)
    if(form.style.display !== 'none') {
        form.style.display = 'none';
        comment.style.display = 'block';
        isModified != null ? isModified.style.display = 'block':'';
    }else {
        // 보이기 (display: block)
        form.style.display = 'block';
        comment.style.display = 'none';
        isModified != null ? isModified.style.display = 'none':'';
    }


}


//댓글 수정
function editCommentSave(form) {

    const commentId = form.commentId.value;
    const postId = form.postId.value;
    const newContent = form.commentContent.value;

    const oldContent = document.getElementById('commentContent' + commentId).innerText;
//    const oldContent = document.getElementById('commentContent').textContent;
    let contextPath = form.contextPath.value;

    if(!newContent || newContent.trim() === "") {
        alert("공백 또는 입력하지 않은 부분이 있습니다");
        return false;
    }

    if(newContent !== oldContent) {

        $.ajax({
            type: 'PUT',
            url: '/api/post/' + postId + '/comment/' + commentId + '/edit',
            //dataType: 'JSON',
            contentType: 'application/json; charset=utf-8',
            data: JSON.stringify({content: newContent})
        }).done(function(result) {
            contextPath = contextPath + "?commentId=" + result + "#comment_" + result;

            $(".comment-list").load(contextPath + " .comment-list", function() {
                //댓글 위치
                const myElement = document.getElementById('comment_'+result);
                const verticalScrollPosition = setElementScrollPosition(myElement);
                // 수직 스크롤 위치 설정하기
                window.scrollTo({
                  top: verticalScrollPosition,
                  behavior: 'smooth' // 부드러운 스크롤(선택 사항)
                });
            });

        }).fail(function(error) {
            const result = error.responseJSON;
//            for(var key in result) {
//                alert("댓글을 수정할 수 없습니다. \n[에러메세지:" + result[key] + "]");
//            }
            alert("댓글을 수정할 수 없습니다.");
            window.location.reload();
        });

    }else {
        return false;
    }
}


//댓글 삭제
function deleteComment(commentId, postId) {

    let origin = window.location.origin;
    let pathname = window.location.pathname;
    let contextPath = origin + pathname;

    const check = confirm("댓글을 삭제하시겠습니까?");
    if(check) {
        $.ajax({
            type: 'delete',
            url: '/api/post/' + postId + '/comment/' + commentId + '/delete',
            dataType: 'JSON'
        }).done(function(result) {
            $(".comment-list").load(contextPath + " .comment-list");
        }).fail(function(error) {
            const result = error.responseJSON;
//            for(var key in result) {
//                alert("댓글을 삭제할 수 없습니다. \n[에러메세지:" + result[key] + "]");
//            }
            alert("댓글을 삭제할 수 없습니다.");
            window.location.reload();
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


//댓글 페이지 이동
function moveToPage(page) {

    let origin = window.location.origin;
    let pathname = window.location.pathname;
    let contextPath = origin + pathname + "?page=" + page;

    //가져온 댓글로 교체
    $(".comment-list").load(contextPath + " .comment-list");

}

//element 스크롤 위치
function setElementScrollPosition(element) {
  const elementPosition = element.getBoundingClientRect().top;
  const currentWindowScrollY = window.scrollY;
  const scrollDifference = currentWindowScrollY + elementPosition;
  return element.scrollTop + scrollDifference;
}


//부모 댓글로 이동
function moveToParent(commentId) {

    let origin = window.location.origin;
    let pathname = window.location.pathname;
    let contextPath = origin + pathname + "?commentId=" + commentId + "#comment_" + commentId;

    $(".comment-list").load(contextPath + " .comment-list", function() {

        //부모 댓글 위치
        const myElement = document.getElementById('comment_'+commentId);
        const verticalScrollPosition = setElementScrollPosition(myElement);

        //수직 스크롤 위치 설정하기
        window.scrollTo({
          top: verticalScrollPosition,
          behavior: 'smooth' // 부드러운 스크롤(선택 사항)
        });
    });
}