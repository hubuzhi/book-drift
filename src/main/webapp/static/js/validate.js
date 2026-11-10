/**
 * 注册表单校验（骨架）
 * 【待填写】校验用户名、密码（不少于6位）、昵称非空，通过后表单才提交
 */
function validateRegisterForm() {
    // 获取输入框的值
    var username = document.getElementById("username").value;
    var password = document.getElementById("password").value;
    var nickname = document.getElementById("nickname").value;

    // 校验用户名非空
    if (username.trim() === "") {
        alert("用户名不能为空！");
        return false; // 阻止表单提交
    }

    // 校验密码长度不少于6位
    if (password.length < 6) {
        alert("密码长度不能少于6位！");
        return false;
    }

    // 校验昵称非空
    if (nickname.trim() === "") {
        alert("昵称不能为空！");
        return false;
    }

    // 校验全部通过，允许提交
    return true;
}