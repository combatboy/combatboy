function ReloadCode(){
    loadCode();
}

function loadCode(){
    var checkcode = document.getElementById("checkcode");
    var dt = new Date().getTime();
    var urlCode = "http://bte.ceair.com/CheckCode.aspx?t=" + dt;
    checkcode.style.backgroundImage = 'url(' + urlCode + ')';
}

var isLogin = 0;
var codeLoaded = false;
var oldname = "";
var errmsg = '';

function ShowCheckcode(bShow){
    if (bShow) {
        if (!codeLoaded) {
            ReloadCode();
            codeLoaded = true;
        }
        $('#divCheckcode')[0].className = 'ub uc-a1';
    }
    else {
        $('#divCheckcode')[0].className = 'uc-a1 uhide';
    }
}

function QueryLogin(username){
    username = $.trim(username);
    if (username != "") {
        $.ajax({
            type: "POST",
            url: 'http://bte.ceair.com/home/QueryLoginCode?t=' + new Date().getTime(),
            data: {
                uid: username
            },
            async: true,
            success: function(data){
                ShowCheckcode((data == "1"));
            },
            error: function(XMLHttpRequest, textStatus, errorThrown){
            }
        });
    }
}

$(document).ready(function(){
    $("#username").bind("blur", function(e){
        var username = $.trim($(this).val());
        if (username != oldname) {
            errmsg = '';
            QueryLogin(username);
            oldname = username;
        }
    });
    
    $("#username,#password,#txtcode").bind("keydown", function(e){
        var v = e.which;
        if (v == 13) {
            e.preventDefault();
            $("#btnSubmit").click();
        }
    });
    $("#btnSubmit").click(save);   	
});
function save(){				
    if (isLogin == 1)        
	showLoading();		
    var uid = '';
	var pwd = '';
	if(getstorage('chanLogin')){
		uid = getstorage('uid_login');
		pwd = getstorage('pwd_login');
		clearstorage('chanLogin');
	} else {
		uid = $("#username").val();
		pwd = $("#password").val();
	}		
	
    if ($.trim(uid) == "") {
        errmsg = '请输入用户名';			
		showerr();	        
        return;
    }
    if ($.trim(pwd) == "") {
        errmsg = '请输入密码';	
		showerr();	
        return;
    }
    var chk = "";
    if ($("#divCheckcode").css("display") != "none") {
        chk = $("#txtcode").val();
        if ($.trim(chk) == "") {
            errmsg = '请输入验证码';	
			showerr();			            
            return;
        }
    }
    $("#errMsg").html("");    
    isLogin = 1;
    $.ajax({
        type: "POST",
        url: 'http://bte.ceair.com/home/LoginVerify?t=' + new Date().getTime(),
        data: {
            uid: uid,
            pwd: pwd,
            code: chk
        },
        async: true,
        success: function(data){
			closeLoading();
            if (data == "1") {                
				setstorage("uid_login",uid); // 存储当前用户	
				// 存储登录用户的用户名和密码
				if(getstorage("uid")){
				  	var arr=JSON.parse(getstorage("uid"));
					var arr_pwd = JSON.parse(getstorage("pwd"));	  	
				  	if(getstorage("uid").indexOf(uid)==-1){			   	
					   	arr.push(uid);
						arr_pwd.push(pwd);
						setstorage('uid',JSON.stringify(arr));
						setstorage('pwd',JSON.stringify(arr_pwd));				
				   }
				 } else {
					 var arr=[];
					 var arr_pwd = [];		 
					 arr.push(uid);
					 arr_pwd.push(pwd);
					 setstorage("uid",JSON.stringify(arr));
					 setstorage("pwd",JSON.stringify(arr_pwd));
				 }	
				// 如果当前是登录用户跳转到联合办公页面，如果为账户切换和删除账户，切换到账户管理页面
				if(getstorage('flag')){
					uexWindow.open('account_mgt',0,'account_mgt.html', '10', '', '', '0');
					clearstorage('flag');	
				} else {
					uexWindow.open('unionoffice',0,'unionoffice.html', '10', '', '', '0');	
				}					
            }
            else 
                if (data == "2") {
                    errmsg = '验证码错误';
					showerr();					
                    ShowCheckcode(true);
                    ReloadCode();                    
                    isLogin = 0;
                }
                else if (data == "0") {						
                        errmsg = '用户名或密码错误';	
						showerr();						
                        $("#password")[0].focus();
                        if (codeLoaded) {
                            QueryLogin(uid);
                            ReloadCode();
                        } else {
                            QueryLogin(uid);
                        }                        
                        isLogin = 0;
                    }
                    else  if (data == "4") {
                            window.location = "http://bte.ceair.com/MobilePwdChecker/ChangePwd";
                        }
                    else  if (data == "5") {
                            window.location = "http://bte.ceair.com/MobilePwdChecker/SmsPwdCheck";
                        } else if (data == "3") {
                            window.location = "http://bte.ceair.com/home/SyncPwd";
                        } else {
                           	errmsg = '系统错误请稍后再试';	
							showerr();							                     
                            isLogin = 0;
                        }
        },
        error: function(XMLHttpRequest, textStatus, errorThrown){
            closeLoading();
			errmsg = '系统错误请稍后再试';	
            showerr();
            isLogin = 0;
        }
    });
}
function ShowTip(){
    var windowWidth = document.documentElement.clientWidth || document.body.clientWidth;
    var popupWidth = $("#divLoginTip").width();
    var top = 40;
    $("#divLoginTip").css({
        "position": "absolute",
        "top": top,
        "left": windowWidth / 2 - popupWidth / 2 - 5
    }).show();
}

function CloseTip(){
	$("#divLoginTip").hide();
}

// 显示错误信息
function showerr(){
	$$('errbox').className = 'ub t-wh ub-ver uc-a c-mlogin-error uts-login-error';
	$$('errmsg').innerHTML = errmsg;
	setTimeout('hideerr()' ,2000)
}
// 隐藏错误信息
function hideerr(){
	$$('errbox').className = 'ub-ver t-wh uc-a c-mlogin-error uts-login-error uhide';
}
// 显示加载信息
function showLoading(){
	$$('divloading').className = 'ub ub-ver ub-ac ub-pc tx-c ub-img3 c-mbla-body';
}
function closeLoading(){
	$$('divloading').className = 'uhide';
}
