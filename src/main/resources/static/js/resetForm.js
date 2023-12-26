
//폼 초기화
function resetForm(form) {

    form.addMemberForm !== undefined && form.addMemberForm.reset();

    form.loginForm !== undefined && form.loginForm.reset();

    form.addPostForm !== undefined && form.addPostForm.reset();

    form.editPostForm !== undefined && form.editPostForm.reset();

    form.editProfileForm !== undefined && form.editProfileForm.reset();

    form.editPwdForm !== undefined && form.editPwdForm.reset();

    form.findIdForm !== undefined && form.findIdForm.reset();

    form.findPasswordForm !== undefined && form.findPasswordForm.reset();

}

